import java.sql.Connection;
import java.sql.Statement;
import java.sql.*;
import java.io.IOException;
import java.sql.DriverManager;
import java.text.ParseException;
public class DataBase{
    public static final String USER_NAME="root";
    public static  final String PASSWORD="Kop27513kop";
    public static final String URL = "jdbc:mysql://127.0.0.1:3306/newsbase"+
            "?verifyServerCertificate=false"+
            "&useSSL=false"+
            "&requireSSL=false"+
            "&useLegacyDatetimeCode=false"+
            "&amp"+
            "&serverTimezone=UTC";
    public static Connection connection;
    public static Statement statement;
    static {
        try{
            connection=DriverManager.getConnection(URL,USER_NAME,PASSWORD);
        }catch (SQLDataException throwables){
            throwables.printStackTrace();
            throw new RuntimeException();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
    static {
        try {
            statement=connection.createStatement();
        }catch (SQLException throwables){
            throwables.printStackTrace();
            throw new RuntimeException();
        }
    }
    public void Logic(WebSite X) throws ClassNotFoundException, SQLException, IOException, ParseException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        statement.executeUpdate("INSERT INTO newsbase.news(Title,Description,Author,Subcategories,Type,Date,Text)values('"+X.getTitle()+"','"+X.getSubscribtion()+"','"+X.getAutor()+"','"+X.getTags()+"','"+X.getPublType()+"','"+X.getDate()+"','"+X.getContent()+"');");
        //  ResultSet resultSet = statement.executeQuery("SELECT* FROM newsbase.news");

    }
}