package test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // 1. Load the FXML file (ensure the path matches your project structure)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/place.fxml"));
        Parent root = loader.load();

        // 2. Optional: Access the controller to initialize data
        // PlaceController controller = loader.getController();
        // controller.initializeData(); // If you need to preload data

        // 3. Set up the stage
        stage.setTitle("Place Management System");  // Customize the window title
        stage.setScene(new Scene(root, 800, 600)); // Set window dimensions
        stage.setMinWidth(600);                    // Prevent resizing too small
        stage.show();
    }

    public static void main(String[] args) {
        // Launch the JavaFX application
        launch(args);
    }
}