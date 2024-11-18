package app.bms;

import app.BMS;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Meal {
    public static void mealsInsert(int bin) {
        String[] mealTypes = {"fish", "chicken", "beef", "vegetarian"};
        try (
                PreparedStatement preparedStatement = BMS.getConnection().prepareStatement(
                        "INSERT INTO Meal (BIN, MIN, Name, Type, Price, Cuisine) VALUES (?, ?, ?, ?, ?, ?)")
        ) {
            for (int i = 0; i < mealTypes.length; i++) {
                boolean success = false;
                while (!success) {
                    try {
                        BMS.outputLine();
                        String type = mealTypes[i];
                        BMS.output(type);
                        preparedStatement.setInt(1, bin);
                        preparedStatement.setInt(2, i + 1);
                        preparedStatement.setString(3, BMS.input("Enter dish name: "));
                        preparedStatement.setString(4, type);
                        preparedStatement.setBigDecimal(5, new java.math.BigDecimal(BMS.input("Enter price: $")));
                        preparedStatement.setString(6, BMS.input("Enter cuisine info: "));
                        preparedStatement.executeUpdate();
                        success = true;
                    } catch (SQLException | IllegalArgumentException e) {
                        BMS.output("Error inserting meal: " + e.getMessage() + ". Please re-enter the details.");
                    }
                }
            }
            BMS.output("Inserted meals for the banquet.");
        } catch (SQLException e) {
            BMS.output("Error preparing statement: " + e.getMessage());
        }
    }

    public static void mealUpdate(int bin, int min) {
        try {
            boolean updating = true;
            while (updating) {
                BMS.outputLine();
                mealsPrint(bin, min);
                BMS.outputLine();
                BMS.output("0 = back\n1 = update name\n2 = update type\n3 = update price\n4 = update cuisine");
                String choice = BMS.input("command >");

                String query = "UPDATE Meal SET ";
                switch (choice) {
                    case "0":
                        updating = false;
                        break;
                    case "1":
                        query += "Name = ? WHERE BIN = ? AND MIN = ?";
                        mealUpdateExecute(bin, min, query, BMS.input("Enter new dish name:"));
                        break;
                    case "2":
                        query += "Type = ? WHERE BIN = ? AND MIN = ?";
                        mealUpdateExecute(bin, min, query, BMS.input("Enter new type:"));
                        break;
                    case "3":
                        query += "Price = ? WHERE BIN = ? AND MIN = ?";
                        mealUpdateExecute(bin, min, query, new java.math.BigDecimal(BMS.input("Enter new price: $")));
                        break;
                    case "4":
                        query += "Cuisine = ? WHERE BIN = ? AND MIN = ?";
                        mealUpdateExecute(bin, min, query, BMS.input("Enter new cuisine info:"));
                        break;
                    default:
                        BMS.output("Invalid choice. Please try again.");
                }
            }
        } catch (SQLException | IllegalArgumentException e) {
            BMS.output("Error updating meal: " + e.getMessage());
        }
    }

    public static void mealUpdateExecute(int bin, int min, String query, Object newValue) throws SQLException {
        try (PreparedStatement preparedStatement = BMS.getConnection().prepareStatement(query)) {
            if (newValue instanceof String) {
                preparedStatement.setString(1, (String) newValue);
            } else if (newValue instanceof java.math.BigDecimal) {
                preparedStatement.setBigDecimal(1, (java.math.BigDecimal) newValue);
            }
            preparedStatement.setInt(2, bin);
            preparedStatement.setInt(3, min);

            preparedStatement.executeUpdate();
            BMS.output("Updated meal");
        }
    }


    public static void mealsPrint(int bin, int min) {
        String mealQuery = "SELECT * FROM Meal WHERE BIN = ? AND MIN = ?";
        try (PreparedStatement preparedStatement = BMS.getConnection().prepareStatement(mealQuery)) {
            preparedStatement.setInt(1, bin);
            preparedStatement.setInt(2, min);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    BMS.output("  Meal ID " + resultSet.getInt("MIN") + " (" + resultSet.getString("Type") + ")");
                    BMS.output("  Name: " + resultSet.getString("Name"));
                    BMS.output("  Price: $" + resultSet.getBigDecimal("Price"));
                    BMS.output("  Cuisine: " + resultSet.getString("Cuisine"));
                } else {
                    BMS.output("No meal found for BIN " + bin + " and MIN " + min);
                }
            }
        } catch (SQLException e) {
            BMS.output("Error retrieving meals: " + e.getMessage());
        }
    }
}
