import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Parser {
    private static final Set<String> SET_LINKS = new HashSet<>();
    private static final String GEN_URL = "https://rb.ru/sitemap-news.xml";
    private static int sleepTimer = 40 * 60000; //in minutes
    private static final String FILE_PATH = "listNews.txt";
    private static boolean stateFill = false;

    public void setSleepTimer(int time) {
        sleepTimer = time * 60000;
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

    private void updateTxt() throws IOException, InterruptedException {
        if (!stateFill) fillTxt();
        if (!SET_LINKS.isEmpty()) {
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

                for (String str: listLink) {
                    System.out.println(str);
                    WebSite webSite = new WebSite(str);
                    webSite.printInfo();
                }

                Thread.sleep(sleepTimer);
            }
        }
        else {
            BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH));
            String str = reader.readLine();
            while (str != null) {
                SET_LINKS.add(str);
                str = reader.readLine();
            }
        }

    }

    public Parser() throws IOException, InterruptedException {
        Document document = Jsoup.connect(GEN_URL)
                .userAgent("Chrome/4.0.249.0")
                .referrer("http://www.google.com")
                .get();


        Elements links = document.select("url > loc");
        for (Element div : links) {
            SET_LINKS.add(div.text());
        }

        for (String str : SET_LINKS) {
            System.out.println(str);
            WebSite webSite = new WebSite(str);
            //webSite.printInfo();
        }

        fillTxt();


    }

}
