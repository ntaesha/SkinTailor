package skintailor;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;
import javafx.scene.control.Button; // Import Button
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class CartController {
    
    @FXML
    private ListView<String> cartListView;
    
    @FXML
    private Text totalPriceText;

    @FXML
    private Button homeButton; // Button to navigate to Homepage
    @FXML
    private Button cartButton; // Button to navigate to Cart
    @FXML
    private Button logoutButton; // Button to logout

    @FXML
    public void initialize() {
        displayCartItems();
    }
    
    private void displayCartItems() {
        cartListView.getItems().clear();
        double totalPrice = 0.0;
        
        for (Product product : Cart.getInstance().getProducts()) {
            cartListView.getItems().add(product.getName() + " - RM" + product.getPrice());
            totalPrice += product.getPrice();
        }
        
        totalPriceText.setText("Total Price: RM" + totalPrice);
    }
    
   @FXML
    private void handleCheckout() {
        // Show a confirmation dialog
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Checkout Successful");
        alert.setHeaderText(null);
        alert.setContentText("You have successfully checked out!");

        // Show the alert and wait for the user to close it
        alert.showAndWait();

        // Clear the cart after checkout
        Cart.getInstance().clearCart();

        // Navigate back to the Homepage
        try {
            Parent homepage = FXMLLoader.load(getClass().getResource("Homepage.fxml")); // Load Homepage
            Stage stage = (Stage) cartListView.getScene().getWindow(); // Get current stage
            stage.setScene(new Scene(homepage)); // Set new scene
            stage.show(); // Show the new scene
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void navigateToHomepage() {
        try {
            Parent homepage = FXMLLoader.load(getClass().getResource("Homepage.fxml")); // Load Homepage
            Stage stage = (Stage) cartListView.getScene().getWindow(); // Get current stage
            stage.setScene(new Scene(homepage)); // Set new scene
            stage.show(); // Show the new scene
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void navigateToCart() {
        try {
            Parent cartPage = FXMLLoader.load(getClass().getResource("Cart.fxml")); // Load Cart page
            Stage stage = (Stage) cartListView.getScene().getWindow(); // Get current stage
            stage.setScene(new Scene(cartPage)); // Set new scene
            stage.show(); // Show the new scene
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void logout() {
        try {
            Parent loginScreen = FXMLLoader.load(getClass().getResource("Login.fxml")); // Load Login screen
            Stage stage = (Stage) cartListView.getScene().getWindow(); // Get current stage
            stage.setScene(new Scene(loginScreen)); // Set new scene
            stage.show(); // Show the new scene
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}