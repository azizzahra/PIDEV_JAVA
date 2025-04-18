package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class HomeController {


    @FXML
    public void navigateToForum(ActionEvent event) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/post.fxml"));
            ScrollPane root = loader.load();
            Scene currentScene = ((Node) event.getSource()).getScene();

            // Remplacez le contenu de la scène par le nouveau contenu
            currentScene.setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
            // Afficher une alerte en cas d'erreur
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur de chargement");
            alert.setContentText("Impossible de charger la vue du forum: " + e.getMessage());
            alert.showAndWait();
        }
    }
    // Helper method to load FXML files
    private ScrollPane loadFXML(String fxmlFileName) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
        return loader.load();  // Now returning ScrollPane instead of AnchorPane
    }
}
