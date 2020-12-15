
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] argv) throws IOException {
        Document document = Jsoup.connect("https://rb.ru/sitemap-news.xml")
                .userAgent("Chrome/4.0.249.0")
                .referrer("http://www.google.com")
                .get();



        //Elements listNews = document.select("#news_panel_news > ol:nth-child(1)");
        /*System.out.println(listNews);
        for (Element element : listNews.select("a"))
            System.out.println(element.text());*/

        WebInfo webInfo = new WebInfo();
        webInfo.getInfo("https://rb.ru/news/rossiya-apple-pay/");

        //Elements links = document.select("url > loc");

        //System.out.println(links.text());
    }
}
