package app;

import app.bms.Account;
import app.bms.Admission;
import app.bms.Banquet;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class User {
    public static String _email;
    public static void login() {
        try {
            BMS.setConnection(DriverManager.getConnection(BMS.URL + "bms", "user", "user"));
            _email = BMS.input("Enter your email:");

            String query = "SELECT COUNT(*) FROM Account WHERE Email = ? AND Password = ?";
            PreparedStatement preparedStatement = BMS.getConnection().prepareStatement(query);
            preparedStatement.setString(1, _email);
            preparedStatement.setString(2, BMS.input("Enter your password:"));
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                BMS.output("Connected to the database successfully!");
                return;
            }

            BMS.output("Invalid email or password!");
            BMS.setConnection(null);
        } catch (SQLException e) {
            BMS.output("Error during login: " + e.getMessage());
            BMS.setConnection(null);
        }
    }

    public static void register() {
        try {
            BMS.setConnection(DriverManager.getConnection(BMS.URL + "bms", "user", "user"));
            _email = BMS.input("Enter your email:");
            if (Account.accountExists(_email)) {
                BMS.setConnection(null);
                _email = "";
                BMS.output("Email already registered!");
                return;
            }

            String query = "INSERT INTO Account (Email, Password, FirstName, LastName, Address, AttendeeType, AffOrg, MobileNumber) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = BMS.getConnection().prepareStatement(query)) {
                preparedStatement.setString(1, _email);
                preparedStatement.setString(2, BMS.input("Enter your password:"));
                preparedStatement.setString(3, BMS.input("Enter your first name:"));
                preparedStatement.setString(4, BMS.input("Enter your last name:"));
                preparedStatement.setString(5, BMS.input("Enter your address:"));
                preparedStatement.setString(6, BMS.input("Enter your attendee type (staff, student, alumni, guest):"));
                preparedStatement.setString(7, BMS.input("Enter your affiliated organization:"));
                preparedStatement.setInt(8, Integer.parseInt(BMS.input("Enter your mobile number:")));
                preparedStatement.executeUpdate();

                BMS.output("Registration successful! Logged in as new user.");
            }
        } catch (SQLException | NumberFormatException e  ) {
            BMS.setConnection(null);
            BMS.output("Registration failed: " + e.getMessage());
        }
    }

    //    ==========================================================================================================
    
    public static void inputUser(){
        BMS.output("0 = logout\n" +
                "1 = view and update account info\n" +
                "2 = view banquets info (filter by admitted, date, and name)\n" +
                "3 = apply for a banquet\n" +
                "4 = view and update admitted banquets info\n");
        switch (BMS.input("command >")) {
            case "0":
                BMS.setConnection(null);
                _email = "";
                break;
            case "1":
                Account.accountUpdate(_email);
                break;
            case "2":
                Banquet.banquetView(true);
                break;
            case "3":
                Admission.admissionInsert();
                break;
            case "4":
                Admission.admissionEdit();
                break;
            default:
                BMS.output("input error: command not found, please check capitalization and spelling");
                break;
        }
    }

}
