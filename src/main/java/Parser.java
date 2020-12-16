import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Parser {
    private static final Set<String> setLinks = new HashSet<>();
    private static final String generalURL = "https://rb.ru/sitemap-news.xml";
    private static int sleepTimer = 40 * 60000; //in minutes

    public void setSleepTimer(int time) {
        sleepTimer = time * 60000;
    }

    public Parser(String URL) throws IOException, InterruptedException {
        Document document = Jsoup.connect(generalURL)
                .userAgent("Chrome/4.0.249.0")
                .referrer("http://www.google.com")
                .get();


        Elements links = document.select("url > loc");
        for (Element div : links) {
            setLinks.add(div.text());
        }

        for (String str : setLinks) {
            System.out.println(str);
            //WebSite webSite = new WebSite(str);
            //webSite.printInfo();
        }

        Thread.sleep(sleepTimer);

        System.out.println("-------------------------------");
        System.out.println("new: ");
        while (true) {
            document = Jsoup.connect(generalURL)
                    .userAgent("Chrome/4.0.249.0")
                    .referrer("http://www.google.com")
                    .get();
            links = document.select("url > loc");
            ArrayList<String> listLink = new ArrayList<>();
            for (Element div : links) {
                if (!setLinks.contains(div.text())) {
                    setLinks.add(div.text());
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

}
