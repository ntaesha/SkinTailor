package skintailor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SkinTailor extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the FXML file
        Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
        
        // Set the title of the window
        primaryStage.setTitle("SkinTailor - Your Skincare and Makeup Hub");
        
        // Create a scene with the loaded FXML
        Scene scene = new Scene(root);
        
        // Set the stylesheet for the scene
        scene.getStylesheets().add(getClass().getResource("SkinTailorCSS.css").toExternalForm());
        
        // Set the scene to the primary stage
        primaryStage.setScene(scene);
        
        // Show the primary stage
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args); // Launch the JavaFX application
    }
}