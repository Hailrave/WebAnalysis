
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] argv) throws IOException {
        Document document = Jsoup.connect("https://yandex.ru/")
                .userAgent("Chrome/4.0.249.0")
                .referrer("http://www.google.com")
                .get();

        Elements listNews = document.select("#news_panel_news > ol:nth-child(1)");
        System.out.println(listNews);
        for (Element element : listNews.select("a"))
            System.out.println(element.text());

        Elements titleElem = document.select("head > title");
        //System.out.println(titleElem.text());

        Elements divs = document.select("body > div");
        //System.out.println(divs);

        Elements firstDiv = document.select("body > div:nth-child(1)");

        Elements contentElem = document.select("body > div.content");

        Elements idElem = document.select("#123");

        Elements divHeader = document.select("body > div.header.main :not(h1)");
    }
}
