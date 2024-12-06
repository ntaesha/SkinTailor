package skintailor;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

public class HomepageController {

    @FXML
    private GridPane productGrid;

    // Database connection details
    private final String DB_URL = "jdbc:mysql://localhost:3306/skintailor"; // Update with your DB URL
    private final String DB_USER = "root"; // Update with your DB username
    private final String DB_PASSWORD = "root"; // Update with your DB password

    private List<Product> products;

    @FXML
    public void initialize() {
        // Load products from the database
        loadProducts();

        // Display products in card form
        displayProducts();
    }

    private void loadProducts() {
        products = new ArrayList<>();
        String query = "SELECT id, name, price, imagePath FROM products"; // Adjust the query as needed

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                double price = resultSet.getDouble("price");
                String imagePath = resultSet.getString("imagePath");

                products.add(new Product(id, name, price, imagePath));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayProducts() {
        int row = 0;
        int col = 0;

        for (Product product : products) {
            VBox productCard = createProductCard(product);
            productGrid.add(productCard, col, row);
            col++;
            if (col > 2) { // Assuming 3 products per row
                col = 0;
                row++;
            }
        }
    }

    private VBox createProductCard(Product product) {
        VBox card = new VBox();
        card.setSpacing(10);
        card.setStyle("-fx-padding: 10; -fx-border-color: lightgray; -fx-border-radius: 5; -fx-background-color: white;");

        // Ensure the image path is a valid URL
        String imagePath = product.getImagePath();
            if (!imagePath.startsWith("file:///")) {
                // Convert to a valid file URL
                imagePath = "file:///" + imagePath.replace("\\", "/"); // Replace backslashes with forward slashes
            }

        ImageView productImage = new ImageView(new Image(imagePath));
        productImage.setFitHeight(150);
        productImage.setFitWidth(150);
        productImage.setPreserveRatio(true);

        Text productName = new Text(product.getName());
        Text productPrice = new Text("$" + product.getPrice());

        Button addToCartButton = new Button("Add to Cart");
        addToCartButton.setOnAction(e -> handleAddToCart(product));

        card.getChildren().addAll(productImage, productName, productPrice, addToCartButton);
        return card;
    }

    private void handleAddToCart(Product product) {
        // Logic to add the product to the cart
        Cart cart = Cart.getInstance(); // Get the singleton instance of the cart
        cart.addProduct(product); // Add the product to the cart

        System.out.println(product.getName() + " added to cart.");

        // Optionally, you can show a confirmation message to the user
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Product Added");
        alert.setHeaderText(null);
        alert.setContentText(product.getName() + " has been added to your cart.");
        alert.showAndWait();
    }

    @FXML
    private void handleLogout() {
        try {
            Parent loginPage = FXMLLoader.load(getClass().getResource("Login.fxml")); // Load login page
            Stage stage = (Stage) productGrid.getScene().getWindow(); // Get current stage using productGrid
            stage.setScene(new Scene(loginPage)); // Set new scene
            stage.show(); // Show the new scene
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void handleCart() {
        try {
            // Load the cart view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/skintailor/Cart.fxml"));
            Parent cartView = loader.load();
            
            // Get the current stage
            Stage stage = (Stage) productGrid.getScene().getWindow(); // Replace logo with productGrid
            stage.setScene(new Scene(cartView));
            stage.show(); // Show the new scene
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load the cart view: " + e.getMessage());
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleProducts() {
        try {
            Parent productsPage = FXMLLoader.load(getClass().getResource("Homepage.fxml")); // Reload Homepage
            Stage stage = (Stage) productGrid.getScene().getWindow(); // Get current stage using productGrid
            stage.setScene(new Scene(productsPage)); // Set new scene
            stage.show(); // Show the new scene
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}