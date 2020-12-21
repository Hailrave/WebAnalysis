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
    private static final Set<String> SET_TEMP = new HashSet<>();
    private Document document;
    private int sleepTimer; //in minutes
    private static final String FILE_PATH = "listNews.txt";
    private static final String FILE_TMP = "tempL.txt";
    public static final Data_Base dataBase = new Data_Base();
    private static BufferedWriter writer;
    private static BufferedWriter writer_tmp;
    private static int currentState; // -1 - файл пуст, 0 - не все ссылки сохранились,
    // 1 - все ссылки сохранены в txt, 2 - вся информация в БД
    private final int THREADS = 8;

    static { //создание буферного потока ввода символов
        try {
            writer = new BufferedWriter(new FileWriter(FILE_PATH, true));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    static {
        try {
            writer_tmp = new BufferedWriter(new FileWriter(FILE_TMP, true));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setSleepTimer(int time) {
        sleepTimer = time * 60000;
    } //задержка в мс

    private void stateHandler() throws IOException { //есть ли что-то в файле
        BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH)); //буферизация ввода из осн.файла
        String tmp = reader.readLine(); //считываем построчно в tmp
        if (tmp == null) { //файл пуст
            currentState = -1;
        }
        else {
            try {
                currentState = Integer.parseInt(tmp);
            } catch (Exception e) {
                currentState = 0; //какие-то ссылки сохранились, выяснить какие
            }
        }
        reader.close(); //закрыть поток
    }

    public void primFill() throws IOException {  //первичное наполнение
        while (true) {
            stateHandler(); //есть ли что-то в файле
            if (currentState == -1) { //файл пуст, заполнить
                ExecutorService pool = Executors.newFixedThreadPool(THREADS); //созд.потоки
                Elements links = document.select("loc"); //из <loc> берем UML
                for (Element link : links) {
                    pool.execute(() -> {
                        try {
                            formLink(link); //добавляет инфо в осн.файл и HashSet из ссылки link с ссылками
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
            else if (currentState == 0) { //файл не пуст, дополнить
                fillSet();  //заполняем основной HashSet инфо из основного файла
                ExecutorService pool = Executors.newFixedThreadPool(THREADS); //созд.потоки
                Elements links = document.select("loc"); //из <loc> берем UML
                for (Element link : links) {
                    pool.execute(() -> {
                        try {
                            formLink(link); ////добавляет инфо в осн.файл и HashSet из ссылки с ссылками
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
            else if (currentState == 1) { //все ссылки сохранены в txt
                if (SET_LINKS.isEmpty()) {
                    fillSet(); // если основной HashSet пуст, заполняем его инфо из основного файла
                }
                fillTemp(); //заполняем временный HashSet инфо из временного файла
                getInfoFields(); //добавление инфо в бд
                setFlagTxt('2'); // БД наполнена информацией
                break;
            }
            else if (currentState == 2) { //обновляем инфо
                ArrayList<String> arrlist = new ArrayList<>(); //созд.массив строк
                fillSet(); //заполняем основной HashSet инфо из основного файла
                ExecutorService pool = Executors.newFixedThreadPool(THREADS); //созд.потоки
                Elements links = document.select("loc"); //из <loc> берем UML
                for (Element link : links) {
                    if (!SET_LINKS.contains(link.text())) {
                        pool.execute(() -> {
                            try {
                                formLink(link); //добавляет инфо в осн.файл и HashSet из ссылки с ссылками
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
                System.setProperty("https.protocols", "TLSv1.1"); //устанавливаем протокол подключения к сайту
                for (String str : arrlist) { //заполняем бд
                    pool.execute(() -> {
                        try {
                            WebSite webSite = new WebSite(str);
                            synchronized (this) {
                                dataBase.WriteDB(webSite);
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
                setFlagTxt('3');
            }
            if (currentState == 3) break;
        }
    }

    public void observation(int time) throws IOException, InterruptedException { //начало наблюдения
        setSleepTimer(time);
        if (SET_LINKS.isEmpty()) {
            try {
                fillSet(); //заполняем основной HashSet инфо из основного файла
            } catch (IOException e) {
                primFill(); //заполняем бд уже имеющейся инфо
            }
        }

        while (true) {
            ArrayList<String> list = new ArrayList<>();
            this.document = Jsoup.connect("https://rb.ru/sitemap-news.xml") //подкл. к актуальной ссылке
                    .userAgent("Chrome/4.0.249.0")
                    .referrer("http://www.google.com")
                    .get();
            Elements links = document.select("url > loc");
            for (Element link : links) {
                if (!SET_LINKS.contains(link.text())) { //проверка наличия в осн.HashSet
                    addLinkTxt(link.text()); //добавляем в осн.HashSet
                    list.add(link.text());
                }
            }
            ExecutorService pool = Executors.newFixedThreadPool(THREADS); //созд.потоки
            for (String str : list) { //заполняем базу
                pool.execute(() -> {
                    try {
                        WebSite webSite = new WebSite(str);
                        synchronized (this) {
                            dataBase.WriteDB(webSite);
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
            Thread.sleep(sleepTimer);
        }


    }

    synchronized private void addLinkTxt(String link) throws IOException {
        writer.write(link + "\n");
    }

    synchronized private void addLinkTemp(String str) throws IOException { //запись строки во временный файл
        BufferedWriter writer1 = new BufferedWriter(new FileWriter(FILE_TMP));
        writer1.write(str);
        writer1.close();
    }

    private void setFlagTxt(char unit) throws IOException { //в начало файла записывает его состояние
        //BufferedWriter writerF = new BufferedWriter(new FileWriter("listNews.txt"));
        writer_tmp.write(unit + "\n");
        for (String str : SET_LINKS) {
            writer_tmp.write(str + "\n");
        }
        writer_tmp.close();
        currentState = Integer.parseInt(String.valueOf(unit));
    }

    private void fillTemp() throws IOException { //заполняем временный HashSet инфо из временного файла
        BufferedReader reader = new BufferedReader(new FileReader(FILE_TMP)); //буферизация ввода из врем.файла
        String str = reader.readLine(); //считываем построчно в str
        while (str != null) {  //пока строка не пустая, добавляем во временный HashSet
            SET_TEMP.add(str);
            str = reader.readLine();
        }
        reader.close();
    }

    private void getInfoFields() {     //добавление инфо в бд
        System.setProperty("https.protocols", "TLSv1.1"); //устан.системное свойство
        ExecutorService pool = Executors.newFixedThreadPool(THREADS); //созд.потоки
        for (String str : SET_LINKS) {
            pool.execute(() -> {
                try {
                    if (!SET_TEMP.contains(str)) { //если строки нет во временном HashSet
                        WebSite webSite = new WebSite(str); //создаем объект для получения инфо
                        addLinkTemp(str); //запись строки во временный файл
                        synchronized (this) {
                            dataBase.WriteDB(webSite);//добавляем инфо из ссылки в бд
                        }
                    }
                }catch (IOException | ClassNotFoundException | SQLException | ParseException e ) {
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

    private void fillSet() throws IOException { //заполняем основной HashSet инфо из основного файла
        BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH)); //буферизация ввода из осн.файла
        String str = reader.readLine(); //считываем построчно в str
        if (str.equals("1") || str.equals("2")) { //1 - все ссылки сохранены в txt, 2 - вся информация в БД
            str = reader.readLine();
        }
        while (str != null) { //пока строка не пустая, добавляем в основной HashSet
            SET_LINKS.add(str);
            str = reader.readLine();
        }
        reader.close();
    }

    private void formLink(Element link) throws IOException { //добавляет инфо в осн.файл и HashSet из ссылки с ссылками
        this.document = Jsoup.connect(link.text()) //подключаемся к текущей ссылке
                .userAgent("Chrome/4.0.249.0")
                .referrer("http://www.google.com")
                .get();

        Elements once = document.select("url > loc"); //заходим в ссылку с ссылками и берем их
        for (Element onCE : once) {
            String[] arrr = onCE.text().split("/"); //разбиваем строку
            if (!arrr[3].equals("tag")) { //не берем тэг
                synchronized (this) {
                    if (!SET_LINKS.contains(onCE.text())) { //если нет ссылки в основном HashSet
                        addLinkTxt(onCE.text()); //добавляем в основной файл
                        SET_LINKS.add(onCE.text()); //добавляем  в основной HashSet
                    }
                }
            }
        }
    }

    public Parser() throws IOException {
        this.document = Jsoup.connect("https://rb.ru/sitemap.xml")
                .userAgent("Chrome/4.0.249.0")
                .referrer("http://www.google.com")
                .get();
    }

}
