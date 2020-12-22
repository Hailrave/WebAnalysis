import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * класс - вся нужная инфо из каждой ссылки
 * состоит из сеттеров и геттеров
 */
public class WebSite {        //вся нужная инфо из каждой ссылки
    /**
     * в поля записывается инфо из соответствующих тегов
     */
    private static Document document;
    private final String URL;
    private String title;
    private ArrayList<String> tags;
    private java.util.Date date;
    private String autor;
    private String content;
    private String publType;
    private String subscribtion;

    /**
     * конструктор
     * @param URL - адрес ссылки
     * @throws IOException
     * @throws ParseException
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public WebSite(String URL) throws IOException, ParseException, SQLException, ClassNotFoundException {
        /**
         * @see setInfo()
         */
        document = Jsoup.connect(URL)
                .userAgent("Chrome/4.0.249.0")
                .referrer("http://www.google.com")
                .get();
        this.URL = URL;
        setInfo();
    }

    /**
     * функция заполнения полей
     */
    private void setInfo() {
        setPublType();
        setAutor();
        setSubscribtion();
        setContent();
        setDate();
        setTags();
        setTitle();
    }

    public String getPublType() {
        return publType;
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public Date getDate() {
        return date;
    }

    public String getAutor() {
        return autor;
    }

    public String getContent() {
        return content;
    }

    public String getSubscribtion() {
        return subscribtion;
    }

    private void setPublType() {
        String[] str1 = URL.split("/"); //3 элемент
        this.publType = str1[3];
    }

    private void setTitle() {
        Elements title = document.select("#article-feed > div > section.article-header > " +
                "div > div.article-header__wrap.article-header__wrap--rubric > h1");
        for (Element div : title) {
            this.title = div.text();
        }
    }

    private void setTags() {
        tags = new ArrayList<>();
        Elements tegs = document.select("#article-feed > div > section.article-header > div " +
                "> div.article-header__wrap.article-header__wrap--tags > ul");
        for (Element div : tegs) {
            this.tags.add(div.text());
        }
    }

    private void setDate() {
        Elements date = document.select("#article-feed > div > section.article-header > div " +
                "> div.article-header__wrap.article-header__wrap--l2 > " +
                "div.article-header__wrap.article-header__wrap--header.article-header__wrap--header-2 > time > span:nth-child(2)");
        for (Element div : date) {
            try {
                this.date = new SimpleDateFormat("d MMMM yyyy").parse(div.text());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private void setAutor() {
        Elements autor = document.select("#article-feed > div > section.article-header > div > " +
                "div.article-header__wrap.article-header__wrap--l2 > div.article-header__wrap.article-header__wrap--author > " +
                "div > span > a");
        for (Element div : autor) {
            this.autor = div.text();
        }
    }

    private void setSubscribtion() {
        Elements subscr = document.select("#article-feed > div > section.article > " +
                "div.article__container.article__container--main > div > div:nth-child(2) > div.article__introduction");
        for (Element div : subscr) {
            this.subscribtion = div.text();
        }
    }

    private void setContent() {
        StringBuilder sb = new StringBuilder();
        Elements content = document.select("#article-feed > div > section.article > " +
                "div.article__container.article__container--main > div > div:nth-child(2) > div.article__content-block.abv > p");
        for (Element div : content) {
            sb.append(div.text());
        }
        this.content = sb.toString();
    }



}
