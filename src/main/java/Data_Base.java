import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * класс - база данных
 */
public class Data_Base {
    private static Connection conn;
    private static Statement statmt;
    private static ResultSet resSet;

    /**
     * ПОДКЛЮЧЕНИЕ К БАЗЕ ДАННЫХ
     * Создание таблицы
     */
    // --------ПОДКЛЮЧЕНИЕ К БАЗЕ ДАННЫХ--------
    static
    {
        conn = null;
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:NewsBase.db");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        System.out.println("База Подключена!");

        try {   // --------Создание таблицы--------
            statmt = conn.createStatement();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        try {
            statmt.execute("CREATE TABLE if not exists 'NewsBase' ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'Title' text NOT NULL, 'Description' text NOT NULL, 'Author' text NULL, 'Subcategories' text NOT NULL, 'Type' text NULL, 'Date' text NOT NULL, 'Text' longtext NOT NULL);");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        System.out.println("Таблица создана или уже существует.");
    }

    // --------Заполнение таблицы--------

    /**
     * функция - Заполнение таблицы
     * @param X - элемент класса WebSite
     * @throws SQLException
     */
    public static void WriteDB(WebSite X) throws SQLException //принимать параметр как в Logic
    {
        statmt.executeUpdate("INSERT INTO 'NewsBase' (Title,Description,Author,Subcategories,Type,Date,Text)values('"
                +X.getTitle()+"','"+X.getSubscribtion()+"','"+X.getAutor()+"','"+X.getTags()+"','"
                +X.getPublType()+"','"+X.getDate()+"','"+X.getContent()+"');");
    }


    // --------Закрытие--------
    /**
     * функция - Закрытие
     * @throws SQLException
     */
    public static void CloseDB() throws SQLException
    {
        conn.close();
        statmt.close();
        resSet.close();

        System.out.println("Соединения закрыты");
    }

}

