import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class Data_Base {
    public static Connection conn;
    public static Statement statmt;
    public static ResultSet resSet;

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
            conn = DriverManager.getConnection("jdbc:sqlite:NewsBase1.s3db");
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
            statmt.execute("CREATE TABLE if not exists 'NewsBase1' ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'Title' text NOT NULL, 'Description' text NOT NULL, 'Author' text NULL, 'Subcategories' text NOT NULL, 'Type' text NULL, 'Date' text NOT NULL, 'Text' text NOT NULL);");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        System.out.println("Таблица создана или уже существует.");
    }



    // --------Заполнение таблицы--------
    public static void WriteDB(WebSite X) throws SQLException //принимать параметр как в Logic
    {
        statmt.executeUpdate("INSERT INTO 'NewsBase1' (Title,Description,Author,Subcategories,Type,Date,Text)values('"
                +X.getTitle()+"','"+X.getSubscribtion()+"','"+X.getAutor()+"','"+X.getTags()+"','"
                +X.getPublType()+"','"+X.getDate()+"','"+X.getContent()+"');");
    }

    // --------Закрытие--------
    public static void CloseDB() throws ClassNotFoundException, SQLException
    {
        conn.close();
        statmt.close();
        resSet.close();

        System.out.println("Соединения закрыты");
    }

}

