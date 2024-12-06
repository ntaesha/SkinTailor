package skintailor;

import java.io.File;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;

import java.sql.*;
import java.util.Optional;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AdminController {

    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, String> userIdColumn;
    @FXML
    private TableColumn<User, String> usernameColumn;
    @FXML
    private TableColumn<User, String> emailColumn;
    @FXML
    private TableColumn<User, Boolean> isAdminColumn;

    @FXML
    private TableView<Product> productTable;
    @FXML
    private TableColumn<Product, Integer> productIdColumn;
    @FXML
    private TableColumn<Product, String> productNameColumn;
    @FXML
    private TableColumn<Product, Double> productPriceColumn;
    @FXML
    private TableColumn<Product, String> productImageColumn; // New column for image path
    
    @FXML
    private Button logoutButton; // Declare the logout button

    private final String DB_URL = "jdbc:mysql://localhost:3306/skintailor"; // Update with your DB URL
    private final String DB_USER = "root"; // Update with your DB username
    private final String DB_PASSWORD = "root"; // Update with your DB password

    private ObservableList<User> userList = FXCollections.observableArrayList();
    private ObservableList<Product> productList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Initialize user table
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email")); // Bind email column

        // Initialize product table
        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        productPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        productImageColumn.setCellValueFactory(new PropertyValueFactory<>("imagePath")); // Set image path column

        loadUsers();
        loadProducts();
    }

    private void loadUsers() {
        userList.clear();
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT * FROM users WHERE userID LIKE 'SU00%'";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                User user = new User(
                        resultSet.getString("userID"),
                        resultSet.getString("userName"),
                        resultSet.getString("email"), 
                        resultSet.getString("password"), // Optional: Do not store this in the User object
                        resultSet.getBoolean("isAdmin")
                );
                userList.add(user);
            }
            userTable.setItems(userList);
        } catch (SQLException e) {
            showAlert("Database Error", "An error occurred while loading users: " + e.getMessage());
        }
    }

    private void loadProducts() {
        productList.clear();
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT * FROM products";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Product product = new Product(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getDouble("price"),
                        resultSet.getString("imagePath") // Load image path
                );
                productList.add(product);
            }
            productTable.setItems(productList);
        } catch (SQLException e) {
            showAlert("Database Error", "An error occurred while loading products: " + e.getMessage());
        }
    }

    @FXML
    private void addUser() {
        // Implement logic to add a new user
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add User");
        dialog.setHeaderText("Enter new user details");
        dialog.setContentText("Username:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(username -> {
            // Prompt for password
            TextInputDialog passwordDialog = new TextInputDialog();
            passwordDialog.setTitle("Add User");
            passwordDialog.setHeaderText("Enter password for new user");
            passwordDialog.setContentText("Password:");

            Optional<String> passwordResult = passwordDialog.showAndWait();
            passwordResult.ifPresent(password -> {

                try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                    String hashedPassword = hashPassword(password); // Hash the password
                    String query = "INSERT INTO users (userName, password) VALUES (?, ?";
                    PreparedStatement preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setString(1, username);
                    preparedStatement.setString(2, hashedPassword);
                    preparedStatement.executeUpdate();
                    loadUsers(); // Refresh the user list
                } catch (SQLException e) {
                    showAlert("Database Error", "An error occurred while adding the user: " + e.getMessage());
                }
            });
        });
    }

    @FXML
    private void updateUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            TextInputDialog dialog = new TextInputDialog(selectedUser.getUsername());
            dialog.setTitle("Update User");
            dialog.setHeaderText("Update user details");
            dialog.setContentText("Username:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(username -> {
                // Prompt for new password
                TextInputDialog passwordDialog = new TextInputDialog();
                passwordDialog.setTitle("Update User");
                passwordDialog.setHeaderText("Enter new password (leave blank to keep current)");
                passwordDialog.setContentText("New Password:");

                Optional<String> passwordResult = passwordDialog.showAndWait();
                String password = passwordResult.orElse(null); // Keep current password if blank
                boolean isAdmin = selectedUser.isAdmin(); // Replace with actual input if needed

                try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                    String query = "UPDATE users SET userName = ?, password = ?, isAdmin = ? WHERE id = ?";
                    PreparedStatement preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setString(1, username);
                    if (password != null && !password.isEmpty()) {
                        preparedStatement.setString(2, hashPassword(password));
                    } else {
                        preparedStatement.setString(2, selectedUser.getPassword());
                    }
                    preparedStatement.setBoolean(3, isAdmin);
                    preparedStatement.setString(4, selectedUser.getUserId());
                    preparedStatement.executeUpdate();
                    loadUsers(); // Refresh the user list
                } catch (SQLException e) {
                    showAlert("Database Error", "An error occurred while updating the user: " + e.getMessage());
                }
            });
        } else {
            showAlert("Selection Error", "Please select a user to update.");
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    private void updateExistingUserPasswords() {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT id, password FROM users";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int userId = resultSet.getInt("id");
                String plainPassword = resultSet.getString("password");

                // Hash the existing password
                String hashedPassword = hashPassword(plainPassword);

                // Update the user with the hashed password
                String updateQuery = "UPDATE users SET password = ? WHERE id = ?";
                PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                updateStatement.setString(1, hashedPassword);
                updateStatement.setInt(2, userId);
                updateStatement.executeUpdate();
            }
            System.out.println("All user passwords updated to hashed format.");
        } catch (SQLException e) {
            showAlert("Database Error", "An error occurred while updating user passwords: " + e.getMessage());
        }
    }

    @FXML
    private void deleteUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            Alert alert = new Alert(AlertType.CONFIRMATION, "Are you sure you want to delete this user?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                    String query = "DELETE FROM users WHERE id = ?";
                    PreparedStatement preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setString(1, selectedUser.getUserId());
                    preparedStatement.executeUpdate();
                    userList.remove(selectedUser);
                } catch (SQLException e) {
                    showAlert("Database Error", "An error occurred while deleting the user: " + e.getMessage());
                }
            }
        } else {
            showAlert("Selection Error", "Please select a user to delete.");
        }
    }

    @FXML
    private void addProduct() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Product");
        dialog.setHeaderText("Enter new product details");
        dialog.setContentText("Product Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(productName -> {
            TextInputDialog priceDialog = new TextInputDialog();
            priceDialog.setTitle("Add Product");
            priceDialog.setHeaderText("Enter product price");
            priceDialog.setContentText("Price:");

            Optional<String> priceResult = priceDialog.showAndWait();
            double productPrice = Double.parseDouble(priceResult.get()); // Get the actual price

            String imagePath = chooseImage(); // Method to choose an image

            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String query = "INSERT INTO products (name, price, imagePath) VALUES (?, ?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, productName);
                preparedStatement.setDouble(2, productPrice);
                preparedStatement.setString(3, imagePath);
                preparedStatement.executeUpdate();
                loadProducts(); // Refresh the product list
            } catch (SQLException e) {
                showAlert("Database Error", "An error occurred while adding the product: " + e.getMessage());
            }
        });
    }

    @FXML
    private void updateProduct() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            TextInputDialog dialog = new TextInputDialog(selectedProduct.getName());
            dialog.setTitle("Update Product");
            dialog.setHeaderText("Update product details");
            dialog.setContentText("Product Name:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(productName -> {
                TextInputDialog priceDialog = new TextInputDialog(String.valueOf(selectedProduct.getPrice()));
                priceDialog.setTitle("Update Product");
                priceDialog.setHeaderText("Enter product price");
                priceDialog.setContentText("Price:");

                Optional<String> priceResult = priceDialog.showAndWait();
                double productPrice = Double.parseDouble(priceResult.get()); // Get the actual price

                String imagePath = chooseImage(); // Method to choose an image

                try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                    String query = "UPDATE products SET name = ?, price = ?, imagePath = ? WHERE id = ?";
                    PreparedStatement preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setString(1, productName);
                    preparedStatement.setDouble(2, productPrice);
                    preparedStatement.setString(3, imagePath);
                    preparedStatement.setInt(4, selectedProduct.getId());
                    preparedStatement.executeUpdate();
                    loadProducts(); // Refresh the product list
                } catch (SQLException e) {
                    showAlert("Database Error", "An error occurred while updating the product: " + e.getMessage());
                }
            });
        } else {
            showAlert("Selection Error", "Please select a product to update.");
        }
    }

    @FXML
    private void deleteProduct() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            Alert alert = new Alert(AlertType.CONFIRMATION, "Are you sure you want to delete this product?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                    String query = "DELETE FROM products WHERE id = ?";
                    PreparedStatement preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setInt(1, selectedProduct.getId());
                    preparedStatement.executeUpdate();
                    productList.remove(selectedProduct);
                } catch (SQLException e) {
                    showAlert("Database Error", "An error occurred while deleting the product: " + e.getMessage());
                }
            }
        } else {
            showAlert("Selection Error", "Please select a product to delete.");
        }
    }

    private String chooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));
        File file = fileChooser.showOpenDialog(null);
        return (file != null) ? file.getAbsolutePath() : null; // Return the image path or null if no file was selected
    }
    
    @FXML
    private void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml")); // Update with your actual login FXML file
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) logoutButton.getScene().getWindow(); // Get the current stage
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Could not load the login screen: " + e.getMessage());
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