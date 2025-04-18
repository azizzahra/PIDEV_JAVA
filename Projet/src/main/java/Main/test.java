package Main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class test extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // Charger le fichier FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));


            // Remplacer ScrollPane par BorderPane
            BorderPane root = loader.load(); // Remplacement de ScrollPane par BorderPane

            // Création de la scène
            Scene scene = new Scene(root);

            // Configuration de la fenêtre principale
            primaryStage.setTitle("Home");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    }

