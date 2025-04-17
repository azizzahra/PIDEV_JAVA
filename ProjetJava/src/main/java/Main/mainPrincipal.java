package Main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Screen;

import java.io.IOException;

public class mainPrincipal extends Application {
    private static Stage primaryStage; // Ajouter cette variable
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            primaryStage = stage; // Initialiser la variable
            Parent root = FXMLLoader.load(getClass().getResource("/ListeFarms.fxml"));

            // Configuration de la taille
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            primaryStage.setMaximized(true); // Pour occuper tout l'écran sans être en mode fullscreen


            primaryStage.centerOnScreen();

            primaryStage.setScene(new Scene(root));
            primaryStage.setTitle("Gestion des Fermes");
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
