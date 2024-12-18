package bms;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Admission {
    public static void admissionUpdate(String email) {
        try {
            while (true) {
                System.out.println("===================================================================");
                admissionRegisteredView(email);
                BMS.outputLine();
                int bin = Integer.parseInt(BMS.input("Enter Banquet ID, or 0 to return:"));
                if (bin == 0) return;
                if (!admissionExists(bin, email)) {
                    BMS.output("No admission found with the given Banquet ID");
                    return;
                }

                BMS.output("0 = back\n1 = update admission\n2 = delete admission");
                switch (BMS.input("command >")) {
                    case "0":
                        return;
                    case "1":
                        admissionEdit(bin, email);
                        break;
                    case "2":
                        admissionDelete(bin,  email);
                        break;
                    default:
                        BMS.output("Invalid command");
                        break;
                }
            }
        } catch (SQLException | NumberFormatException e) {
            BMS.output("Error during admission update: " + e.getMessage());
        }
    }

    public static boolean admissionExists(int bin, String email) throws SQLException {
        String query = "SELECT COUNT(*) FROM Admission WHERE Email = ? AND BIN = ?";
        try (PreparedStatement preparedStatement = BMS.getConnection().prepareStatement(query)) {
            preparedStatement.setString(1, email);
            preparedStatement.setInt(2, bin);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public static void admissionDelete (int bin, String email) throws SQLException {
        BMS.outputLine();
        String deleteStatement = "DELETE FROM Admission WHERE Email = ? AND BIN = ?";
        try (PreparedStatement preparedStatement = BMS.getConnection().prepareStatement(deleteStatement)) {
            preparedStatement.setString(1, email);
            preparedStatement.setInt(2, bin);
            preparedStatement.executeUpdate();

            BMS.output("Admission successfully deleted!");
        }
    }

    public static void admissionEdit(int bin, String email) throws SQLException {
        boolean updating = true;
        while (updating) {
            BMS.outputLine();
            admissionRegisteredView(email);
            BMS.outputLine();
            BMS.output("0 = back\n1 = update meal\n2 = update drink\n3 = update remark");
            String choice = BMS.input("command >");
            String statement = "UPDATE Admission SET ";

            switch (choice) {
                case "0":
                    updating = false;
                    break;
                case "1":
                    statement += "MIN = ? WHERE Email = ? AND BIN = ?";
                    admissionUpdateExecute(email, statement, bin, Integer.parseInt(BMS.input("Enter new Meal ID: ")));
                    break;
                case "2":
                    statement += "Drink = ? WHERE Email = ? AND BIN = ?";
                    admissionUpdateExecute(email, statement, bin, BMS.input("Enter new Drink: "));
                    break;
                case "3":
                    statement += "Remark = ? WHERE Email = ? AND BIN = ?";
                    admissionUpdateExecute(email, statement, bin, BMS.input("Enter new Remark: "));
                    break;
                default:
                    BMS.output("Invalid choice. Please try again.");
            }
        }
    }

    public static void admissionUpdateExecute(String email, String statement, int bin, Object newValue) throws SQLException {
        try (PreparedStatement preparedStatement = BMS.getConnection().prepareStatement(statement)) {
            if (newValue instanceof Integer) {
                preparedStatement.setInt(1, (Integer) newValue);
            } else {
                preparedStatement.setString(1, (String) newValue);
            }
            preparedStatement.setString(2, email);
            preparedStatement.setInt(3, bin);

            preparedStatement.executeUpdate();
            BMS.output("Field updated successfully!");
        }
    }

    public static void admissionInsert(String email) {
        try {
            int bin = Integer.parseInt(BMS.input("Enter the Banquet ID:"));
            if (admissionExists(bin, email)) {
                BMS.output("An admission already exists for this Banquet ID and Email.");
                return;
            }
            if (!Banquet.availabilityUpdate(bin)) {
                BMS.output("This banquet is no longer avalible (date passed or quota reached)");
                return;
            }

            int min = Integer.parseInt(BMS.input("Enter the Meal ID (MIN):"));
            String drink = BMS.input("Enter your preferred drink:");
            String remark = BMS.input("Enter any remarks (optional):");

            String query = "INSERT INTO Admission (Email, BIN, MIN, Drink, Remark) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = BMS.getConnection().prepareStatement(query)) {
                preparedStatement.setString(1, email);
                preparedStatement.setInt(2, bin);
                preparedStatement.setInt(3, min);
                preparedStatement.setString(4, drink);
                preparedStatement.setString(5, remark);
                preparedStatement.executeUpdate();

                BMS.output("Admission successfully recorded!");
            }
        } catch (SQLException | NumberFormatException e) {
            BMS.output("Error during admission insert: " + e.getMessage());
        }
    }

    public static boolean admissionQuotaFull(int bin) {
        // Query to count the number of admissions for the given banquet
        String query = "SELECT COUNT(*) AS AdmissionCount FROM Admission WHERE BIN = ?";

        try (PreparedStatement admissionStatement = BMS.getConnection().prepareStatement(query)) {
            admissionStatement.setInt(1, bin);
            ResultSet rs = admissionStatement.executeQuery();

            if (rs.next()) {
                int admissionCount = rs.getInt("AdmissionCount");

                query = "SELECT Quota FROM Banquet WHERE BIN = ?";
                try (PreparedStatement banquetStatement = BMS.getConnection().prepareStatement(query)) {
                    banquetStatement.setInt(1, bin);
                    ResultSet quotaRs = banquetStatement.executeQuery();

                    if (quotaRs.next()) {
                        return admissionCount >= quotaRs.getInt("Quota");
                    }
                }
            }
        } catch (SQLException e) {
            BMS.output("Error checking quota availability: " + e.getMessage());
        }

        return false;
    }


    public static void admissionRegisteredView(String email) throws SQLException {
        String statement = "SELECT * FROM Admission WHERE Email = ?";

        try (PreparedStatement preparedStatement = BMS.getConnection().prepareStatement(statement)) {
            preparedStatement.setString(1, email);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    int bin = resultSet.getInt("BIN");
                    int min = resultSet.getInt("MIN");
                    String drink = resultSet.getString("Drink");
                    String remark = resultSet.getString("Remark");

                    BMS.output("Banquet ID: " + bin + ", Meal ID: " + min +
                            ", Drink: " + drink + ", Remark: " + remark);
                }
            }
        } catch (SQLException e) {
            BMS.output("error: " + e.getMessage());
        }
    }
}
