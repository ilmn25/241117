package app;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.*;
import java.util.Scanner;

public class BMS {
    public static final String SQL_PATH = "/src/app/sql/";
    private static final Scanner _scanner = new Scanner(System.in);
    public static String URL = "jdbc:mysql://localhost:3306/";
    private static Connection _connection = null;

    public static Connection getConnection() {
        return _connection;
    }
    public static void setConnection(Connection connection) {
        _connection = connection;
    }



    public static String input(String message) {
        System.out.print(message + " ");
        return _scanner.nextLine();
    }
    public static void output(String message) {
        System.out.println(message);
    }
    public static void outputLine() {
        System.out.println("===================================================================");
    }



    public static void main(String[] args) {
        outputLine();
        loadJDBC();
        output("Welcome to the Banquet Management System (BMS)");
        output("- for initial setup of database, log in as admin");
        boolean isAdmin;
        while (true) {
            outputLine();
            isAdmin = establishConnection();

            while (_connection != null) {
                outputLine();
                if (isAdmin) Admin.inputAdmin();
                else User.inputUser();
            }
        }
    }

    private static boolean establishConnection() {
        output("MYSQL database url: " + URL);
        switch (input("0 = change URL\n1 = admin login \n2 = user login\n3 = user register\n> ")) {
            case "0":
                URL = input("URL: ");
                return true;
            case "1":
                Admin.login();
                return true;
            case "2":
                User.login();
                return false;
            case "3":
                User.register();
                return false;
            default:
                output("Please enter either 1, 2 or 3");
                return false;
        }
    }



    private static void loadJDBC() {
        try {
            // Load the MySQL JDBC driver dynamically
            File file = new File("mysql-connector-j-9.1.0.jar");
            URL url = file.toURI().toURL();
            URLClassLoader classLoader = new URLClassLoader(new URL[]{url});
            Class.forName("com.mysql.cj.jdbc.Driver", true, classLoader);

            output("JDBC Driver loaded successfully (mysql-connector-j-9.1.0.jar)");
        } catch (Exception e) {
            output("error: failed to load JDBC Driver at working directory (mysql-connector-j-9.1.0.jar)");
            e.printStackTrace();
            System.exit(0);
        }
    }
}