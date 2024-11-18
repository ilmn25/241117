package app;

import app.bms.Account;
import app.bms.Banquet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Objects;

public class Admin {
    public static void login() {
        try {
            String user = BMS.input("Enter database username:");
            String pass = BMS.input("Enter database password:");

            if (!Objects.equals(user, "user")) {
                BMS.setConnection(DriverManager.getConnection(BMS.URL, user, pass));
                BMS.outputLine();
                BMS.output("Connected to the database successfully!");
            } else {
                BMS.outputLine();
                BMS.output("credentials conflict with 'user' user, please change username if admin username is 'user'");
                return;
            }

            try {
                BMS.setConnection(DriverManager.getConnection(BMS.URL + "bms", user, pass));
                BMS.output("bms database found!");
            } catch (SQLException e) {
                try {
                    BMS.output("bms database not found, starting initial setup: ");
                    runSQLFile("bms", "bms error!");
                    runSQLFile("user", "'user' user already created");
                    BMS.setConnection(DriverManager.getConnection(BMS.URL + "bms", user, pass));
                } catch (SQLException ee) {
                    BMS.output("Error during setup: " + ee.getMessage());
                    BMS.setConnection(null);
                }
            }

        } catch (SQLException | NumberFormatException e  ) {
            BMS.setConnection(null);
            BMS.outputLine();
            BMS.output("admin login failed: " + e.getMessage());
        }
    }

    public static void inputAdmin(){
        BMS.output("0 = logout\n1 = add a new banquet\n2 = list banquets (filter by date and time)\n" +
                "3 = view and update banquet/meal info\n4 = view and update user account info\n5 = generate reports\n");
        switch (BMS.input("command >")) {
            case "0":
                BMS.setConnection(null);
                break;
            case "1":
                Banquet.banquetInsert();
                break;
            case "2":
                Banquet.banquetView(false);
                break;
            case "3":
                BMS.outputLine();
                Banquet.banquetUpdate();
                BMS.outputLine();
                break;
            case "4":
                Account.accountUpdate(BMS.input("user email: "));
                break;
            case "5":
                Banquet.banquetReport();
                break;
            default:
                BMS.output("input error: command not found, please check capitalization and spelling");
                break;
        }
    }

    private static void runSQLFile(String fileName, String fail) {
        try {
            String currentPath = Paths.get("").toAbsolutePath().toString();
            String filePath = currentPath + BMS.SQL_PATH + fileName + ".sql";

            String sql = new String(Files.readAllBytes(Paths.get(filePath)));
            String[] statements = sql.split(";");

            try (Statement statement = BMS.getConnection().createStatement()) {
                for (String stmt : statements) {
                    if (!stmt.trim().isEmpty()) {
                        statement.execute(stmt.trim());
                    }
                }
            }
            BMS.output(fileName + " loaded!");
        } catch (SQLException | IOException e) {
            BMS.output(fail);
        }
    }
}