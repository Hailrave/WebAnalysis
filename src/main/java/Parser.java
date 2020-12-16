import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Parser {
    private static final Set<String> setLinks = new HashSet<>();
    private static final String generalURL = "https://rb.ru/sitemap-news.xml";

    public Parser(String URL) throws IOException {
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
            WebSite webSite = new WebSite(str);
            webSite.printInfo();
        }

    }
}
