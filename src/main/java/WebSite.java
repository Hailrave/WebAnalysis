import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class WebSite {
    private static Document document;
    private static String title;
    private static ArrayList<String> tags;
    private static java.util.Date date;
    private static String autor;
    private static String content;


    public WebSite(String URL) throws IOException {
        document = Jsoup.connect(URL)
                .userAgent("Chrome/4.0.249.0")
                .referrer("http://www.google.com")
                .get();
        setInfo();
    }

    private void setInfo() {
        setAutor();
        setContent();
        setDate();
        setTags();
        setTitle();
    }

    public static Document getDocument() {
        return document;
    }

    public static String getTitle() {
        return title;
    }

    public static ArrayList<String> getTags() {
        return tags;
    }

    public static Date getDate() {
        return date;
    }

    public static String getAutor() {
        return autor;
    }

    public static String getContent() {
        return content;
    }

    private void setTitle() {
        Elements titleL = document.select("#article-feed > div > section.article-header > " +
                "div > div.article-header__wrap.article-header__wrap--rubric > h1");
        for (Element div : titleL) {
            title = div.text();
        }
    }

    private void setTags() {
        tags = new ArrayList<>();
        Elements tegsL = document.select("#article-feed > div > section.article-header > div " +
                "> div.article-header__wrap.article-header__wrap--tags > ul");
        for (Element div : tegsL) {
            tags.add(div.text());
        }
    }

    private void setDate() {
        Elements dateL = document.select("#article-feed > div > section.article-header > div " +
                "> div.article-header__wrap.article-header__wrap--l2 > " +
                "div.article-header__wrap.article-header__wrap--header.article-header__wrap--header-2 > time > span:nth-child(2)");
        for (Element div : dateL) {
            try {
                date = new SimpleDateFormat("d MMMM yyyy").parse(div.text());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private void setAutor() {
        Elements autorL = document.select("#article-feed > div > section.article-header > div > " +
                "div.article-header__wrap.article-header__wrap--l2 > div.article-header__wrap.article-header__wrap--author > " +
                "div > span > a");
        for (Element div : autorL) {
            autor = div.text();
        }
    }

    private void setContent() {
        StringBuilder sb = new StringBuilder();
        Elements contentL = document.select("#article-feed > div > section.article > " +
                "div.article__container.article__container--main > div > div:nth-child(2)");
        for (Element div : contentL) {
            sb.append(div.text());
        }
        content = sb.toString();
    }

    public void printInfo() {
        System.out.println(title);
        System.out.println(tags);
        System.out.println(date);
        System.out.println(autor);
        System.out.println(content);
    }

}
