import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
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
    private static int sleepTimer = 40 * 60000; //in minutes
    private static final String FILE_PATH = "listNews.txt";
    private static boolean stateFill = false;


    public void setSleepTimer(int time) {
        sleepTimer = time * 60000;
    }

    //TODO первичное наполнение, наблюдение

    public void primFill() throws IOException {
        Elements links = document.select("url > loc");
        for (Element div : links) {
            String str = div.text();
            String[] str1 = str.split("/"); //3 элемент
            if (str1[3].equals("news") || str1[3].equals("story") || str1[3].equals("opinion")
                    || str1[3].equals("interview")) {
                SET_LINKS.add(div.text());
            }
        }

        for (String str : SET_LINKS) {
            System.out.println(str);
            WebSite webSite = new WebSite(str);
            webSite.printInfo();
        }

        fillTxt();
    }

    public void observation() {
        if (SET_LINKS.isEmpty()) {

        }
    }

    private void fillTxt() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH));
        for (String str: SET_LINKS) {
            writer.write(str);
            writer.write("\n");
        }
        writer.close();
        stateFill = true;
    }

    private void inter() throws IOException, InterruptedException {
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
    }

    private void updateTxt() throws IOException, InterruptedException {
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
            stateFill = true;
        }

    }

    private void printLink(Element link) throws IOException {
        this.document = Jsoup.connect(link.text())
                .userAgent("Chrome/4.0.249.0")
                .referrer("http://www.google.com")
                .get();
        Elements once = document.select("url > loc");
        for (Element onCE : once) {
            System.out.println(onCE.text());
            synchronized (this) {
                SET_LINKS.add(onCE.text());
            }
        }
        //fillTxt();
    }

    public Parser() throws IOException {
        this.document = Jsoup.connect("https://rb.ru/sitemap.xml")
                .userAgent("Chrome/4.0.249.0")
                .referrer("http://www.google.com")
                .get();


        final int THREADS = 8;
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        Elements links = document.select("loc");
        for (Element link: links){
            pool.execute(() -> {
                try {
                    printLink(link);
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

        System.out.println("FINALLY");

        fillTxt();

    }

}
