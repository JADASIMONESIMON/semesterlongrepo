package dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Person;
import service.MyLogger;

import java.sql.*;

public class DbConnectivityClass {
    private static final String DB_NAME = "csc311jadaserver";
    private static final String SQL_SERVER_URL = "jdbc:mysql://csc311jadaserver.mysql.database.azure.com:3306";
    private static final String DB_URL = SQL_SERVER_URL + "/" + DB_NAME + "?sslMode=REQUIRED";
    private static final String USERNAME = "csc311admin";
    private static final String PASSWORD = "Mid_Night@2024";
    private final ObservableList<Person> data = FXCollections.observableArrayList();
    private final MyLogger lg = new MyLogger();

    public boolean testConnection() {
        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {
            if (conn != null && !conn.isClosed()) {
                lg.makeLog("Database connection successful!");
                return true;
            }
        } catch (SQLException e) {
            lg.makeLog("Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public ObservableList<Person> getData() {
        testConnection(); // Test connectivity before retrieving data
        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {
            String sql = "SELECT * FROM users";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            data.clear(); // Clear existing data
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String department = resultSet.getString("department");
                String major = resultSet.getString("major");
                String email = resultSet.getString("email");
                String imageURL = resultSet.getString("imageURL");
                data.add(new Person(id, firstName, lastName, department, major, email, imageURL));
            }
        } catch (SQLException e) {
            lg.makeLog("Error retrieving data: " + e.getMessage());
            e.printStackTrace();
        }
        return data;
    }

    public boolean connectToDatabase() {
        try (Connection conn = DriverManager.getConnection(SQL_SERVER_URL, USERNAME, PASSWORD)) {
            try (Statement statement = conn.createStatement()) {
                // Ensure database exists
                statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            }
        } catch (SQLException e) {
            lg.makeLog("Error creating database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
             Statement statement = conn.createStatement()) {
            // Ensure users table exists
            String createTableSQL = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INT(10) NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                    "first_name VARCHAR(200) NOT NULL," +
                    "last_name VARCHAR(200) NOT NULL," +
                    "department VARCHAR(200)," +
                    "major VARCHAR(200)," +
                    "email VARCHAR(200) NOT NULL UNIQUE," +
                    "imageURL VARCHAR(200))";
            statement.executeUpdate(createTableSQL);
        } catch (SQLException e) {
            lg.makeLog("Error creating table: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void insertUser(Person p) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {
            String sql = "INSERT INTO users (first_name, last_name, department, major, email, imageURL) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, p.getFirstName());
            ps.setString(2, p.getLastName());
            ps.setString(3, p.getDepartment());
            ps.setString(4, p.getMajor());
            ps.setString(5, p.getEmail());
            ps.setString(6, p.getImageURL());
            ps.executeUpdate();
            lg.makeLog("User inserted successfully.");
        } catch (SQLException e) {
            lg.makeLog("Error inserting user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Integer retrieveId(Person p) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {
            String sql = "SELECT id FROM users WHERE email = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, p.getEmail());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            lg.makeLog("Error retrieving ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // **New Method: deleteUser**
    public void deleteUser(int userId) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {
            String sql = "DELETE FROM users WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                lg.makeLog("User with ID " + userId + " deleted successfully.");
            } else {
                lg.makeLog("No user found with ID " + userId);
            }
        } catch (SQLException e) {
            lg.makeLog("Error deleting user: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
