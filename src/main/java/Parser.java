import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Parser {
    private static final Set<String> SET_LINKS = new HashSet<>();
    private static final Set<String> SET_TEMP = new HashSet<>();
    private Document document;
    private int sleepTimer; //in minutes
    private static final String FILE_PATH = "listNews.txt";
    private static final String FILE_TMP = "tempL.txt";
    private static final DataBase dataBase = new DataBase();
    private static BufferedWriter writer;
    private static int currentState; // -1 - файл пуст, 0 - не все ссылки сохранились,
    // 1 - все ссылки сохранены в txt, 2 - вся информация в БД
    private final int THREADS = 8;

    static {
        try {
            writer = new BufferedWriter(new FileWriter(FILE_PATH, true));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setSleepTimer(int time) {
        sleepTimer = time * 60000;
    }

    //TODO наблюдение

    private void stateHandler() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH));
        String tmp = reader.readLine();
        if (tmp == null) {
            currentState = -1;
        }
        else {
            try {
                currentState = Integer.parseInt(tmp);
            } catch (Exception e) {
                currentState = 0; //какие-то ссылки сохранились, выяснить какие
            }
        }
        reader.close();
    }

    public void primFill() throws IOException {
        while (true) {
            stateHandler();
            if (currentState == -1) { //заполнить
                ExecutorService pool = Executors.newFixedThreadPool(THREADS);
                Elements links = document.select("loc");
                for (Element link : links) {
                    pool.execute(() -> {
                        try {
                            formLink(link);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
                pool.shutdown();
                try {
                    pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                setFlagTxt('1'); //все необходимые ссылки получены в файл
            }
            else if (currentState == 0) { //дополнить
                fillSet();
                ExecutorService pool = Executors.newFixedThreadPool(THREADS);
                Elements links = document.select("loc");
                for (Element link : links) {
                    pool.execute(() -> {
                        try {
                            formLink(link);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
                pool.shutdown();
                try {
                    pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                setFlagTxt('1');
            }
            else if (currentState == 1) {
                if (SET_LINKS.isEmpty()) {
                    fillSet();
                }
                fillTemp();
                getInfoFields();
                setFlagTxt('2'); // БД наполнена информацией
                break;
            }
            else if (currentState == 2) {
                ArrayList<String> arrlist = new ArrayList<>();
                fillSet();
                ExecutorService pool = Executors.newFixedThreadPool(THREADS);
                Elements links = document.select("loc");
                for (Element link : links) {
                    if (!SET_LINKS.contains(link.text())) {
                        pool.execute(() -> {
                            try {
                                formLink(link);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                        arrlist.add(link.text());
                    }
                }
                pool.shutdown();
                try {
                    pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.setProperty("https.protocols", "TLSv1.1");
                for (String str : arrlist) {
                    pool.execute(() -> {
                        try {
                            WebSite webSite = new WebSite(str);
                            synchronized (this) {
                                dataBase.Logic(webSite);
                            }
                        } catch (IOException | ClassNotFoundException | SQLException | ParseException e) {
                            e.printStackTrace();
                        }
                    });
                }
                pool.shutdown();
                try {
                    pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                    return;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                setFlagTxt('3');
            }
            if (currentState == 3) break;
        }
    }

    public void observation(int time) throws IOException, InterruptedException {
        setSleepTimer(time);
        if (SET_LINKS.isEmpty()) {
            try {
                fillSet();
            } catch (IOException e) {
                primFill();
            }
        }

        while (true) {
            ArrayList<String> list = new ArrayList<>();
            this.document = Jsoup.connect("https://rb.ru/sitemap-news.xml")
                    .userAgent("Chrome/4.0.249.0")
                    .referrer("http://www.google.com")
                    .get();
            Elements links = document.select("url > loc");
            for (Element link : links) {
                if (!SET_LINKS.contains(link.text())) {
                    addLinkTxt(link.text());
                    list.add(link.text());
                }
            }
            ExecutorService pool = Executors.newFixedThreadPool(THREADS);
            for (String str : list) {
                pool.execute(() -> {
                    try {
                        WebSite webSite = new WebSite(str);
                        synchronized (this) {
                            //dataBase.Logic(webSite);
                        }
                    } catch (IOException | ClassNotFoundException | SQLException | ParseException e) {
                        e.printStackTrace();
                    }
                });
            }
            pool.shutdown();
            try {
                pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                return;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Thread.sleep(sleepTimer);
        }


    }

    synchronized private void addLinkTxt(String link) throws IOException {
        writer.write(link + "\n");
    }

    synchronized private void addLinkTemp(String str) throws IOException {
        BufferedWriter writer1 = new BufferedWriter(new FileWriter(FILE_TMP));
        writer1.write(str);
        writer1.close();
    }

    private void setFlagTxt(char unit) throws IOException {
        BufferedWriter writerF = new BufferedWriter(new FileWriter("listNews.txt"));
        writerF.write(unit + "\n");
        for (String str : SET_LINKS) {
            writerF.write(str + "\n");
        }
        writerF.close();
        currentState = Integer.parseInt(String.valueOf(unit));
    }

    private void fillTemp() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(FILE_TMP));
        String str = reader.readLine();
        if (str.equals("1") || str.equals("2")) {
            str = reader.readLine();
        }
        while (str != null) {
            SET_TEMP.add(str);
            str = reader.readLine();
        }
        reader.close();
    }

    private void getInfoFields() {
        System.setProperty("https.protocols", "TLSv1.1");
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        for (String str : SET_LINKS) {
            pool.execute(() -> {
                try {
                    if (!SET_TEMP.contains(str)) {
                        WebSite webSite = new WebSite(str);
                        addLinkTemp(str);
                        synchronized (this) {
                            dataBase.Logic(webSite);
                        }
                    }
                }catch (IOException | ClassNotFoundException | SQLException | ParseException e ) {
                    e.printStackTrace();
                }
            });
        }
        pool.shutdown();
        try {
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            return;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void fillSet() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH));
        String str = reader.readLine();
        if (str.equals("1") || str.equals("2")) {
            str = reader.readLine();
        }
        while (str != null) {
            SET_LINKS.add(str);
            str = reader.readLine();
        }
        reader.close();
    }

    private void formLink(Element link) throws IOException {
        this.document = Jsoup.connect(link.text())
                .userAgent("Chrome/4.0.249.0")
                .referrer("http://www.google.com")
                .get();

        Elements once = document.select("url > loc");
        for (Element onCE : once) {
                String[] arrr = onCE.text().split("/");
                if (!arrr[3].equals("tag")) {
                    synchronized (this) {
                        if (!SET_LINKS.contains(onCE.text())) {
                            addLinkTxt(onCE.text());
                            SET_LINKS.add(onCE.text());
                        }
                    }
                }
        }
    }

    public Parser() throws IOException {
        this.document = Jsoup.connect("https://rb.ru/sitemap.xml")
                .userAgent("Chrome/4.0.249.0")
                .referrer("http://www.google.com")
                .get();
    }

}
