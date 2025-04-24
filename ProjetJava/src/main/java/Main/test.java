/*package Main;

import controller.HomeController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class test extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // Charger le fichier FXML et assigner le contrôleur
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Home.fxml"));
            BorderPane root=loader.load();
             //Charger le FXML dans une VBox
            //VBox vbox = loader.load();

            // Récupérer le contrôleur depuis le loader (s'il y en a un dans le FXML)
            //HomeController controller = loader.getController();

            // Configurer la scène
            Scene scene = new Scene(root);

            // Configuration de la fenêtre principale
            primaryStage.setTitle("Home");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}*/
