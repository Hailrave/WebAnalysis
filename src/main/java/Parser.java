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
    private Document document;
    private static final String GEN_URL = "https://rb.ru/sitemap-news.xml";
    private int sleepTimer; //in minutes
    private static final String FILE_PATH = "listNews.txt";
    //private static final DataBase dataBase = new DataBase();
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

    //TODO первичное наблюдение

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
        stateHandler();

        while (true) {
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
                    if (!SET_LINKS.contains(link.text())) {
                        pool.execute(() -> {
                            try {
                                formLink(link);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    }
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
                getInfoFields();
                setFlagTxt('2'); // БД наполнена информацией
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
                currentState = 3;
            }
            if (currentState == 3) break;
        }
    }

    /*public void observation() {
        if (SET_LINKS.isEmpty()) {

        }
    }*/

    synchronized private void addLinkTxt(String link) throws IOException {
        writer.write(link + "\n");
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

    private void getInfoFields() {
        System.setProperty("https.protocols", "TLSv1.1");
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        for (String str : SET_LINKS) {
            pool.execute(() -> {
                try {
                    WebSite webSite = new WebSite(str);
                    synchronized (this) {
                        //dataBase.Logic(webSite);
                    }
                } catch (IOException | ClassNotFoundException | SQLException | ParseException e ) {
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
                        addLinkTxt(onCE.text());
                        SET_LINKS.add(onCE.text());
                    }
                }
        }
    }

    public Parser(int time) throws IOException {
        this.document = Jsoup.connect("https://rb.ru/sitemap.xml")
                .userAgent("Chrome/4.0.249.0")
                .referrer("http://www.google.com")
                .get();
        setSleepTimer(time);
        primFill();
    }

}
