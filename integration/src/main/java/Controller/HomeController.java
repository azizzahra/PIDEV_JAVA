package Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import model.user;
import services.UserService;
import services.SessionManager;

import java.io.IOException;

public class HomeController {
    @FXML private Button loginButton;

    // Cette méthode est appelée après le chargement du FXML
    @FXML
    public void initialize() {
        // Récupérer l'utilisateur depuis le SessionManager
        user currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            // Initialiser l'interface avec les données de l'utilisateur
            initData(currentUser);
        }
    }

    // Méthode pour initialiser les données de l'interface avec l'utilisateur
    public void initData(user user) {
        // Mettre à jour les éléments de l'interface avec les informations de l'utilisateur
        // Par exemple: userNameLabel.setText(user.getPrenom() + " " + user.getNom());
    }

    // Méthodes obsolètes maintenues pour la compatibilité
    @Deprecated
    public static void setLoggedInUser(user user) {
        // Redirection vers le SessionManager
        SessionManager.setCurrentUser(user);
    }

    @Deprecated
    public static user getLoggedInUser() {
        // Redirection vers le SessionManager
        return SessionManager.getCurrentUser();
    }

}