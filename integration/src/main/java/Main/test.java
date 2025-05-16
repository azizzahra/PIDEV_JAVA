package Main;

import Controller.HomeController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.user;
import services.SessionManager;

public class test extends Application {
    private static Stage primaryStage; // Ajout de cette variable

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage; // Initialiser la variable

        // Tenter de charger une session existante
        user existingUser = SessionManager.loadSession();

        // Déterminer quelle vue charger
        String fxmlFile = (existingUser != null) ? "/Home.fxml" : "/Login.fxml";

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();

        // Si on a un utilisateur existant et qu'on charge l'accueil, initialiser le contrôleur
        if (existingUser != null && fxmlFile.equals("/Home.fxml")) {
            HomeController homeController = loader.getController();
            if (homeController != null) {
                homeController.initData(existingUser);
            }
        }

        // Configuration du plein écran
        primaryStage.setMaximized(true); // Pour occuper tout l'écran sans être en mode fullscreen
        primaryStage.centerOnScreen();

        primaryStage.setTitle(existingUser != null ? "Accueil" : "Connexion");
        primaryStage.setScene(new Scene(root));
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}