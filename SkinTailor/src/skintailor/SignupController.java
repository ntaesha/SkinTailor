package skintailor;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.fxml.FXMLLoader;

public class SignupController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    private final String DB_URL = "jdbc:mysql://localhost:3306/skintailor"; // Update with your DB URL
    private final String DB_USER = "root"; // Update with your DB username
    private final String DB_PASSWORD = "root"; // Update with your DB password
    private final String USER_ID_PREFIX = "SU00";

    @FXML
    private void handleSignUp() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert("Sign Up Failed", "Please fill in all fields.");
            return;
        }

        // Generate the new user ID
        String userId = generateUserId();

        // Insert user into the database
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "INSERT INTO users (userID, userName, email, password) VALUES (?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, userId);
            preparedStatement.setString(2, username);
            preparedStatement.setString(3, email);
            preparedStatement.setString(4, password); // Note: Hash passwords in a real application!

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                showAlert("Sign Up Successful", "Account created successfully!");
                // Navigate to the login page
                navigateToLogin();
            } else {
                showAlert("Sign Up Failed", "An error occurred while creating your account.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "An error occurred while connecting to the database: " + e.getMessage());
        }
    }

    private String generateUserId() {
        String newUserId = USER_ID_PREFIX + "1"; // Default user ID if no users exist
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT userID FROM users ORDER BY userID DESC LIMIT 1";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String lastUserId = resultSet.getString("userID");
                // Extract the numeric part and increment it
                String numericPart = lastUserId.substring(USER_ID_PREFIX.length());
                int newIdNumber = Integer.parseInt(numericPart) + 1;
                newUserId = USER_ID_PREFIX + newIdNumber; // Create new user ID
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "An error occurred while generating user ID: " + e.getMessage());
        }
        return newUserId;
    }

    @FXML
    public void navigateToLogin() {
        System.out.println("Navigating to Login page...");
        try {
            Parent loginPage = FXMLLoader.load(getClass().getResource("Login.fxml")); // Load login page
            Stage stage = (Stage) usernameField.getScene().getWindow(); // Get current stage
            stage.setScene(new Scene(loginPage)); // Set new scene
            stage.show(); // Show the new scene
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Navigation Error", "An error occurred while navigating to the login page.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}