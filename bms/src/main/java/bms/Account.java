package bms;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Account {
    public static void accountUpdate(String email) {
        try {
            boolean updating = true;
            while (updating) {
                BMS.outputLine();
                accountView(email);
                BMS.outputLine();
                BMS.output("0 = exit\n1 = update first name\n2 = update last name\n3 = update address\n4 = update attendee type\n5 = update affiliated organization\n6 = update mobile number\n7 = update password");
                String choice = BMS.input("command >");
                String statement = "UPDATE Account SET ";

                switch (choice) {
                    case "0":
                        updating = false;
                        break;
                    case "1":
                        statement += "FirstName = ? WHERE Email = ?";
                        accountUpdateExecute(email, statement, BMS.input("Enter new first name:"));
                        break;
                    case "2":
                        statement += "LastName = ? WHERE Email = ?";
                        accountUpdateExecute(email, statement, BMS.input("Enter new last name:"));
                        break;
                    case "3":
                        statement += "Address = ? WHERE Email = ?";
                        accountUpdateExecute(email, statement, BMS.input("Enter new address:"));
                        break;
                    case "4":
                        statement += "AttendeeType = ? WHERE Email = ?";
                        accountUpdateExecute(email, statement, BMS.input("Enter new attendee type:"));
                        break;
                    case "5":
                        statement += "AffOrg = ? WHERE Email = ?";
                        accountUpdateExecute(email, statement, BMS.input("Enter new affiliated organization:"));
                        break;
                    case "6":
                        statement += "MobileNumber = ? WHERE Email = ?";
                        accountUpdateExecute(email, statement, Integer.parseInt(BMS.input("Enter new mobile number:")));
                        break;
                    case "7":
                        statement += "Password = ? WHERE Email = ?";
                        accountUpdateExecute(email, statement, BMS.input("Enter new password:"));
                        break;
                    default:
                        BMS.output("Invalid choice. Please try again.");
                }
            }
        } catch (SQLException | NumberFormatException e) {
            BMS.output("Error during account update: " + e.getMessage());
        }
    }

    public static void accountUpdateExecute(String email, String statement, Object newValue) throws SQLException {
        try (PreparedStatement preparedStatement = BMS.getConnection().prepareStatement(statement)) {
            if (newValue instanceof Integer) {
                preparedStatement.setInt(1, (Integer) newValue);
            } else {
                preparedStatement.setString(1, (String) newValue);
            }
            preparedStatement.setString(2, email);
            preparedStatement.executeUpdate();

            BMS.output("Field updated successfully!");
        }
    }

    public static boolean accountExists(String email) {
        try {
            String query = "SELECT * FROM Account WHERE Email = ?";
            PreparedStatement preparedStatement = BMS.getConnection().prepareStatement(query);
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return true;
            }
        } catch (SQLException e) {
            BMS.output("Error during accountExists: " + e.getMessage());
        }
        return false;
    }

    public static void accountView(String email) {
        try {
            String query = "SELECT * FROM Account WHERE Email = ?";
            PreparedStatement preparedStatement = BMS.getConnection().prepareStatement(query);
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();

            resultSet.next();
            BMS.output("Email: " + resultSet.getString("Email"));
            BMS.output("Name: " + resultSet.getString("FirstName") + " " + resultSet.getString("LastName"));
            BMS.output("Address: " + resultSet.getString("Address"));
            BMS.output("Attendee Type: " + resultSet.getString("AttendeeType"));
            BMS.output("Affiliated Organization: " + resultSet.getString("AffOrg"));
            BMS.output("Mobile Number: " + resultSet.getInt("MobileNumber"));
            BMS.output("Password: " + resultSet.getString("Password"));
        } catch (SQLException e) {
            BMS.output("Error during account info retrieval: " + e.getMessage());
        }
    }
}
