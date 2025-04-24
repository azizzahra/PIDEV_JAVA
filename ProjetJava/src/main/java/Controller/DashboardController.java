package controller;

import Main.mainPrincipal;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;

public class DashboardController {




    /**
     * Toggles the Offers menu with a smooth slide animation.
     */

@FXML
    public void handleForum(ActionEvent event) {
        try {
            // Spécifiez le chemin correct vers forum.fxml
            // Assurez-vous que ce chemin correspond à l'emplacement réel du fichier
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ForumD.fxml"));
            Parent root = loader.load();

            // Obtenez la scène actuelle
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
    /**
     * Applies a fade-in transition effect to the new content.
     */
    private void applyFadeTransition(Object pane) {
        FadeTransition fade = new FadeTransition(Duration.millis(400), (javafx.scene.Node) pane);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    public void handleLogout(ActionEvent actionEvent) {
    }

    public void handleCommunity(ActionEvent actionEvent) {
    }

    @FXML
    private void navigateToListFarm() {
        try {
            // Utiliser directement le stage principal
            Stage mainStage = mainPrincipal.getPrimaryStage();

            // Charger la vue de liste des fermes
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Farm/ListeFarms.fxml"));
            Parent root = loader.load();

            // Remplacer le contenu du stage principal
            mainStage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();

        }
    }
}
