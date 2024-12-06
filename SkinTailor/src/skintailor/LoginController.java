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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    private final String DB_URL = "jdbc:mysql://localhost:3306/skintailor"; // Update with your DB URL
    private final String DB_USER = "root"; // Update with your DB username
    private final String DB_PASSWORD = "root"; // Update with your DB password

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Login Failed", "Please enter both username and password.");
            return;
        }

        // Check credentials against the database
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT password, isAdmin FROM users WHERE userName = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, username);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String storedPassword = resultSet.getString("password");
                boolean isAdmin = resultSet.getBoolean("isAdmin");

                // Check if the stored password is plain text
                if (isPlainTextPassword(storedPassword)) {
                    // If the password is plain text, verify it directly
                    if (storedPassword.equals(password)) {
                        // Password matches, hash it and update the database
                        String hashedPassword = hashPassword(password);
                        updatePasswordInDatabase(username, hashedPassword);
                        showAlert("Login Successful", "Welcome back, " + username + "!");
                        navigateToHome(isAdmin);
                    } else {
                        showAlert("Login Failed", "Invalid username or password.");
                    }
                } else {
                    // Verify the password using hashing
                    if (verifyPassword(password, storedPassword)) {
                        showAlert("Login Successful", "Welcome back, " + username + "!");
                        navigateToHome(isAdmin);
                    } else {
                        showAlert("Login Failed", "Invalid username or password.");
                    }
                }
            } else {
                showAlert("Login Failed", "Invalid username or password.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "An error occurred while connecting to the database: " + e.getMessage());
        }
    }

    private boolean isPlainTextPassword(String storedPassword) {
        // Check if the stored password is plain text (you can implement your own logic here)
        // For example, if the length is less than a certain threshold, consider it plain text
        return storedPassword.length() < 64; // Assuming hashed passwords are longer
    }

    private void updatePasswordInDatabase(String username, String hashedPassword) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String updateQuery = "UPDATE users SET password = ? WHERE userName = ?";
            PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
            updateStatement.setString(1, hashedPassword);
            updateStatement.setString(2, username);
            updateStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "An error occurred while updating the password: " + e.getMessage());
        }
    }

    private boolean verifyPassword(String inputPassword, String storedPassword) {
        String hashedInputPassword = hashPassword(inputPassword);
        System.out.println("Comparing hashed passwords: " + hashedInputPassword + " vs " + storedPassword);
        return hashedInputPassword.equals(storedPassword);
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0'); // Corrected line
            hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private void navigateToHome(boolean isAdmin) {
        try {
            Parent homePage;
            if (isAdmin) {
                homePage = FXMLLoader.load(getClass().getResource("Admin.fxml")); // Load admin home page
            } else {
                homePage = FXMLLoader.load(getClass().getResource("Homepage.fxml")); // Load user home page
            }
            Stage stage = (Stage) usernameField.getScene().getWindow(); // Get current stage
            stage.setScene(new Scene(homePage)); // Set new scene
            stage.show(); // Show the new scene
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Navigation Error", "An error occurred while navigating to the home page.");
        }
    }

    @FXML
    private void navigateToSignUp() {
        try {
            Parent signupPage = FXMLLoader.load(getClass().getResource("Signup.fxml")); // Load signup page
            Stage stage = (Stage) usernameField.getScene().getWindow(); // Get current stage
            stage.setScene(new Scene(signupPage)); // Set new scene
            stage.show(); // Show the new scene
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Navigation Error", "An error occurred while navigating to the sign up page.");
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