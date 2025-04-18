package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import model.post;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class PostCardController {

    @FXML private Label categoryLabel;
    @FXML private Label usernameLabel;
    @FXML private Label postTitleLabel;
    @FXML private Text postPreviewText;
    @FXML private Label postDateLabel;
    @FXML private Label viewsLabel;
    @FXML private Label attachmentLabel;
    @FXML private Button likeButton;
    @FXML private Button commentButton;
    @FXML private Button shareButton;

    private String attachmentFileName; // nom du fichier seulement

    public void setPostData(post item) {
        categoryLabel.setText(item.getCategory());
        usernameLabel.setText("Auteur ID: " + item.getUsernameId());
        postTitleLabel.setText(item.getTitle());
        postPreviewText.setText(item.getContent());
        postDateLabel.setText("📅 " + item.getDateC());
        viewsLabel.setText("👁️ " + item.getViews() + " vues");
        likeButton.setText("👍 " + item.getVote());
        commentButton.setText("💬 45 commentaires");
        shareButton.setText("🔗 Partager");

        // Gestion de la pièce jointe
        if (item.getAttachment() != null && !item.getAttachment().isEmpty()) {
            attachmentFileName = item.getAttachment(); // juste le nom
            attachmentLabel.setText("📎 " + attachmentFileName);
            attachmentLabel.setOnMouseClicked(event -> handleAttachmentClick());
            attachmentLabel.setStyle("-fx-text-fill: #0066cc; -fx-underline: true; -fx-cursor: hand;");
        } else {
            attachmentFileName = null;
            attachmentLabel.setText("📎 Aucun");
            attachmentLabel.setOnMouseClicked(null);
            attachmentLabel.setStyle("-fx-text-fill: grey;");
        }

        // Ajoute navigation sur titre ou bouton commentaire
        postTitleLabel.setOnMouseClicked(event -> openPostDetailPage(item));
        commentButton.setOnAction(event -> openPostDetailPage(item));
    }
    public void handleAttachmentClick() {
        if (attachmentFileName == null || attachmentFileName.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun fichier associé.");
            return;
        }

        // Chemin absolu dans le dossier uploads
        File file = new File("C:\\Java\\Projet\\uploads", attachmentFileName);
        System.out.println("Chemin absolu du fichier : " + file.getAbsolutePath());

        if (file.exists()) {
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le fichier.");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Fichier introuvable", "Le fichier spécifié n'existe pas.");
        }
    }

    private void openPostDetailPage(post item) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/views/PostDetail.fxml"));
            javafx.scene.Parent root = loader.load();

            PostDetailController controller = loader.getController();
            controller.setPost(item); // envoie les données du post

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Détail du post");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la page de détails.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
