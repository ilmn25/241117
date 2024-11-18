package app.bms;

import app.BMS;
import app.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

public class Banquet {


    public static void availabilityUpdateAll() {
        try {
            String query = "SELECT BIN FROM Banquet";
            PreparedStatement preparedStatement = BMS.getConnection().prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                availabilityUpdate(resultSet.getInt("BIN"));
            }
        } catch (SQLException e) {
            System.out.println("Error updating all banquets' availability: " + e.getMessage());
        }
    }

    public static boolean availabilityUpdate(int bin) {
        try {
            String query = "SELECT Date, Quota FROM Banquet WHERE BIN = ?";
            PreparedStatement preparedStatement = BMS.getConnection().prepareStatement(query);
            preparedStatement.setInt(1, bin);
            ResultSet resultSet = preparedStatement.executeQuery();

            resultSet.next();
            LocalDate banquetDate = resultSet.getDate("Date").toLocalDate();

            boolean isAvailable = !LocalDate.now().isAfter(banquetDate) && !Admission.admissionQuotaFull(bin);

            String updateQuery = "UPDATE Banquet SET Available = ? WHERE BIN = ?";
            PreparedStatement updateStatement = BMS.getConnection().prepareStatement(updateQuery);
            updateStatement.setBoolean(1, isAvailable);
            updateStatement.setInt(2, bin);
            updateStatement.executeUpdate();

            return isAvailable;
        } catch (SQLException e) {
            System.out.println("Error updating availability: " + e.getMessage());
        }
        return false;
    }

    public static void banquetReport() {
        int bin;
        try {
            bin = Integer.parseInt(BMS.input("Banquet ID: "));
            BMS.outputLine();
        } catch (NumberFormatException e) {
            BMS.output("Invalid input: Banquet ID must be a number.");
            return;
        }

        if (!banquetExists(bin)) {
            BMS.output("No admission found with the given Banquet ID");
            return;
        }
        availabilityUpdate(bin);

        try (

            PreparedStatement statement = BMS.getConnection().prepareStatement(
            "SELECT AttendeeType, COUNT(*) AS NumberOfRegistrations FROM Account " +
                "JOIN Admission ON Account.Email = Admission.Email " +
                "WHERE Admission.BIN = ? GROUP BY AttendeeType")) {
            statement.setInt(1, bin);
            ResultSet rs = statement.executeQuery();
            BMS.output("Attendees info");

            boolean hasAttendees = false;
            while (rs.next()) {
                hasAttendees = true;
                BMS.output(rs.getString("AttendeeType") + ": " + rs.getInt("NumberOfRegistrations"));
            }
            if (!hasAttendees) {
                BMS.output("No attendees");
            }

            PreparedStatement attendeeStatement = BMS.getConnection().prepareStatement(
                    "SELECT Account.Email, Account.FirstName, Account.LastName, Account.AttendeeType, COUNT(Admission.BIN) AS NumberOfAdmissions " +
                            "FROM Account LEFT JOIN Admission ON Account.Email = Admission.Email " +
                            "WHERE Admission.BIN = ? GROUP BY Account.Email");

            attendeeStatement.setInt(1, bin);
            rs = attendeeStatement.executeQuery();
            while (rs.next()) {
                BMS.output(rs.getString("Email") + " | " + rs.getString("FirstName") + " " + rs.getString("LastName") + " (" + rs.getString("AttendeeType") + ")");
            }

            PreparedStatement mealStatement = BMS.getConnection().prepareStatement(
                    "SELECT Meal.Name, Meal.Type, COUNT(Admission.MIN) AS NumberOfOrders FROM Meal " +
                            "LEFT JOIN Admission ON Meal.BIN = Admission.BIN AND Meal.MIN = Admission.MIN " +
                            "WHERE Meal.BIN = ? GROUP BY Meal.Name, Meal.Type ORDER BY NumberOfOrders DESC");

            mealStatement.setInt(1, bin);
            rs = mealStatement.executeQuery();
            BMS.output("-----------------------------------------------");
            BMS.output("Popular Meals");
            while (rs.next()) {
                BMS.output(rs.getString("Name") + " (" + rs.getString("Type") + "): " + rs.getInt("NumberOfOrders") + " orders");
            }

            PreparedStatement banquetStatement = BMS.getConnection().prepareStatement(
                    "SELECT Banquet.Name, Banquet.Date, Banquet.Time, Banquet.Location, Banquet.Available, " +
                            "Banquet.Quota, Banquet.Quota - COUNT(Admission.BIN) AS RemainingQuota " +
                            "FROM Banquet LEFT JOIN Admission ON Banquet.BIN = Admission.BIN " +
                            "WHERE Banquet.BIN = ? GROUP BY Banquet.BIN");

            banquetStatement.setInt(1, bin);
            rs = banquetStatement.executeQuery();
            BMS.output("-----------------------------------------------");
            BMS.output("Availability");
            if (rs.next()) {
                int totalQuota = rs.getInt("Quota");
                int remainingQuota = rs.getInt("RemainingQuota");
                BMS.output(rs.getString("Name") + " on " + rs.getDate("Date") + " at " + rs.getTime("Time") + " in " + rs.getString("Location") + ": " +
                        (rs.getBoolean("Available") ? "Available" : "Not Available") +
                        ", Remaining Quota: " + remainingQuota + "/" + totalQuota);
            }

        } catch (SQLException e) {
            BMS.output("Error generating reports: " + e.getMessage());
        }
    }

    public static void banquetInsert() {
        try (
            PreparedStatement preparedStatement = BMS.getConnection().prepareStatement(
                "INSERT INTO Banquet (Name, Date, Time, Location, Address, ContactFirstName, ContactLastName, Available, Quota) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)
        ) {
            preparedStatement.setString(1, BMS.input("Enter Name:"));
            preparedStatement.setDate(2, java.sql.Date.valueOf(BMS.input("Enter Date (YYYY-MM-DD):")));
            preparedStatement.setTime(3, java.sql.Time.valueOf(BMS.input("Enter Time (HH:MM:SS):")));
            preparedStatement.setString(4, BMS.input("Enter Location:"));
            preparedStatement.setString(5, BMS.input("Enter Address:"));
            preparedStatement.setString(6, BMS.input("Enter Contact First Name:"));
            preparedStatement.setString(7, BMS.input("Enter Contact Last Name:"));
            preparedStatement.setBoolean(8, true);
            preparedStatement.setInt(9, Integer.parseInt(BMS.input("Enter Quota:")));

            preparedStatement.executeUpdate();
            BMS.output("New banquet added");

            try (var generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int bin = generatedKeys.getInt(1);
                    availabilityUpdate(bin);
                    Meal.mealsInsert(bin);
                }
            }
        } catch (SQLException | IllegalArgumentException e) {
            BMS.output("Error inserting data: " + e.getMessage());
        }
    }

    public static void banquetUpdate() {
        try {
            int bin = Integer.parseInt(BMS.input("Banquet ID: "));
            if (!banquetExists(bin)) {
                BMS.output("Banquet not found!");
                return;
            }

            boolean updating = true;
            while (updating) {
                BMS.outputLine();

//                print banquet with bin
                String statement = "SELECT * FROM Banquet WHERE BIN = ?";
                try (PreparedStatement preparedStatement = BMS.getConnection().prepareStatement(statement)) {
                    preparedStatement.setInt(1, bin);
                    banquetPrint(preparedStatement.executeQuery());
                }

                BMS.outputLine();
                BMS.output("0 = back\n1 = update name\n2 = update date\n3 = update time\n4 = update location\n" +
                        "5 = update address\n6 = update contact first name\n7 = update contact last name\n" +
                        "8 = update availability\n9 = update quota\n10 = update meal 1\n11 = update meal 2\n" +
                        "12 = update meal 3\n13 = update meal 4");
                String choice = BMS.input("command >");
                statement = "UPDATE Banquet SET ";

                switch (choice) {
                    case "0":
                        updating = false;
                        break;
                    case "1":
                        statement += "Name = ? WHERE BIN = ?";
                        banquetUpdateExecute(bin, statement, BMS.input("Enter new name:"));
                        break;
                    case "2":
                        statement += "Date = ? WHERE BIN = ?";
                        banquetUpdateExecute(bin, statement, java.sql.Date.valueOf(BMS.input("Enter new date (YYYY-MM-DD):")));
                        break;
                    case "3":
                        statement += "Time = ? WHERE BIN = ?";
                        banquetUpdateExecute(bin, statement, java.sql.Time.valueOf(BMS.input("Enter new time (HH:MM:SS):")));
                        break;
                    case "4":
                        statement += "Location = ? WHERE BIN = ?";
                        banquetUpdateExecute(bin, statement, BMS.input("Enter new location:"));
                        break;
                    case "5":
                        statement += "Address = ? WHERE BIN = ?";
                        banquetUpdateExecute(bin, statement, BMS.input("Enter new address:"));
                        break;
                    case "6":
                        statement += "ContactFirstName = ? WHERE BIN = ?";
                        banquetUpdateExecute(bin, statement, BMS.input("Enter new contact first name:"));
                        break;
                    case "7":
                        statement += "ContactLastName = ? WHERE BIN = ?";
                        banquetUpdateExecute(bin, statement, BMS.input("Enter new contact last name:"));
                        break;
                    case "8":
                        statement += "Available = ? WHERE BIN = ?";
                        banquetUpdateExecute(bin, statement, Boolean.parseBoolean(BMS.input("Is available (true/false):")));
                        break;
                    case "9":
                        statement += "Quota = ? WHERE BIN = ?";
                        banquetUpdateExecute(bin, statement, Integer.parseInt(BMS.input("Enter new quota:")));
                        break;
                    case "10":
                        Meal.mealUpdate(bin, 1);
                        break;
                    case "11":
                        Meal.mealUpdate(bin, 2);
                        break;
                    case "12":
                        Meal.mealUpdate(bin, 3);
                        break;
                    case "13":
                        Meal.mealUpdate(bin, 4);
                        break;
                    default:
                        BMS.output("Invalid choice. Please try again.");
                }
                availabilityUpdate(bin);
            }
        } catch (SQLException | IllegalArgumentException e) {
            BMS.output("Error during banquet update: " + e.getMessage());
        }
    }

    public static void banquetUpdateExecute(int banquetId, String statement, Object newValue) throws SQLException {
        try (PreparedStatement preparedStatement = BMS.getConnection().prepareStatement(statement)) {
            if (newValue instanceof Integer) {
                preparedStatement.setInt(1, (Integer) newValue);
            } else if (newValue instanceof Boolean) {
                preparedStatement.setBoolean(1, (Boolean) newValue);
            } else if (newValue instanceof java.sql.Date) {
                preparedStatement.setDate(1, (java.sql.Date) newValue);
            } else if (newValue instanceof java.sql.Time) {
                preparedStatement.setTime(1, (java.sql.Time) newValue);
            } else {
                preparedStatement.setString(1, (String) newValue);
            }
            preparedStatement.setInt(2, banquetId);
            preparedStatement.executeUpdate();

            BMS.output("Field updated successfully!");
        }
    }

    public static boolean banquetExists(int banquetId) {
        try {
            String query = "SELECT * FROM Banquet WHERE BIN = ?";
            PreparedStatement preparedStatement = BMS.getConnection().prepareStatement(query);
            preparedStatement.setInt(1, banquetId);
            ResultSet resultSet = preparedStatement.executeQuery();

            return resultSet.next();
        } catch (SQLException e) {
            BMS.output("Error during banquetExists check: " + e.getMessage());
            return false;
        }
    }

    public static void banquetView(boolean isUser) {
        availabilityUpdateAll();
        String statement = "SELECT * FROM Banquet WHERE 1=1";

        boolean regFilter = false;
        if (isUser) {
            BMS.output("0 = don't show registered only\n1 = show registered only");
            regFilter = BMS.input("command >").equals("1");
            if (regFilter) {
                statement += " AND BIN IN (SELECT BIN FROM Admission WHERE Email = ?)";
            }
        }

        BMS.output("0 = don't add date filter\nYYYY-MM-DD = add date filter");
        String dateFilter = BMS.input("command >");
        if (!dateFilter.equals("0")) {
            statement += " AND Date = ?";
        }

        BMS.output("0 = don't add name keyword filter\nkeyword = add name keyword filter");
        String nameFilter = BMS.input("command >");
        if (!nameFilter.equals("0")) {
            statement += " AND Name LIKE ?";
        }

        BMS.output("0 = show available only\n1 = show available and unavailable");
        String availabilityFilter = BMS.input("command >");
        if (availabilityFilter.equals("0")) {
            statement += " AND Available = true";
        }

        try (PreparedStatement preparedStatement = BMS.getConnection().prepareStatement(statement)) {
            int paramIndex = 1;

            if (regFilter) {
                preparedStatement.setString(paramIndex++, User._email);
            }

            if (!dateFilter.equals("0")) {
                preparedStatement.setDate(paramIndex++, java.sql.Date.valueOf(dateFilter));
            }

            if (!nameFilter.equals("0")) {
                preparedStatement.setString(paramIndex, "%" + nameFilter + "%");
            }

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                banquetPrint(resultSet);
            }
        } catch (SQLException | IllegalArgumentException e) {
            BMS.output("error: " + e.getMessage());
        }
    }


    public static void banquetPrint(ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            int bin = resultSet.getInt("BIN");
            BMS.outputLine();
            BMS.output("Banquet ID: " + bin);
            BMS.output("Name: " + resultSet.getString("Name"));
            BMS.output("Date: " + resultSet.getDate("Date"));
            BMS.output("Time: " + resultSet.getTime("Time"));
            BMS.output("Location: " + resultSet.getString("Location"));
            BMS.output("Address: " + resultSet.getString("Address"));
            BMS.output("Contact Name: " + resultSet.getString("ContactFirstName") + " " + resultSet.getString("ContactLastName"));
            BMS.output("Available: " + resultSet.getBoolean("Available"));
            BMS.output("Quota: " + resultSet.getInt("Quota"));
            for (int min = 1; min <= 4; min++) {
                BMS.output("  ---------------------------");
                Meal.mealsPrint(bin, min);
            }
        }
    }
}
