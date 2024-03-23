package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class database {
	private static final String DB_URL = "jdbc:mysql://localhost:3306/room_chat_app";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    
    public static Connection connect() {
        Connection connection = null;
        try {
    		Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            System.out.println("MySQL veritabanına bağlantı başarılı!");
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("MySQL veritabanına bağlantı hatası: " + e.getMessage());
        }
        return connection;
    }
}
