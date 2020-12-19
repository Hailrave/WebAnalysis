import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
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
    private static BufferedWriter writer;
    private static int currentState; // 1 - все ссылки сохранены в txt, 2 - вся информация в БД
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

    //TODO первичное наполнение, наблюдение

    public void primFill() throws IOException {
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        Elements links = document.select("loc");
        for (Element link: links){
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
        currentState = 1;


        if (SET_LINKS.isEmpty()) {
            fillSet();
        }
        getInfoFields();
        setFlagTxt('2'); //база данных наполнена информацией
        currentState = 2;
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
    }

    private void getInfoFields() {
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        for (String str : SET_LINKS) {
            pool.execute(() -> {
                try {
                    WebSite webSite = new WebSite(str);
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

    /*private void fillTxt() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH));
        for (String str: SET_LINKS) {
            writer.write(str);
            writer.write("\n");
        }
        writer.close();
        //stateFill = true;
    }*/

    /*private void inter() throws IOException, InterruptedException {
        while (true) {
            Document document = Jsoup.connect(GEN_URL)
                    .userAgent("Chrome/4.0.249.0")
                    .referrer("http://www.google.com")
                    .get();
            Elements links = document.select("url > loc");
            ArrayList<String> listLink = new ArrayList<>();
            for (Element div : links) {
                if (!SET_LINKS.contains(div.text())) {
                    SET_LINKS.add(div.text());
                    listLink.add(div.text());
                }
            }

            for (String str : listLink) {
                System.out.println(str);
                WebSite webSite = new WebSite(str);
                webSite.printInfo();
            }

            Thread.sleep(sleepTimer);
        }
    }*/

    /*private void updateTxt() throws IOException, InterruptedException {
        if (!SET_LINKS.isEmpty()) {
            inter();
        }
        else {
            BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH));
            String str = reader.readLine();
            while (str != null) {
                SET_LINKS.add(str);
                str = reader.readLine();
            }
            //stateFill = true;
        }
    }*/

    private void formLink(Element link) throws IOException {
        this.document = Jsoup.connect(link.text())
                .userAgent("Chrome/4.0.249.0")
                .referrer("http://www.google.com")
                .get();

        Elements once = document.select("url > loc");
        for (Element onCE : once) {
            String[] arrr = onCE.text().split("/");
            synchronized (this) {
                if (!arrr[3].equals("tag")) {
                    addLinkTxt(onCE.text());
                    SET_LINKS.add(onCE.text());
                }
            }
        }
    }

    public Parser(int time) throws IOException {
        //TODO проверки на заполненность txt
        this.document = Jsoup.connect("https://rb.ru/sitemap.xml")
                .userAgent("Chrome/4.0.249.0")
                .referrer("http://www.google.com")
                .get();
        setSleepTimer(time);
    }

}
