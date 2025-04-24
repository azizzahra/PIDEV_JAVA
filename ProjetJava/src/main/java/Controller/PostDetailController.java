package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import services.CommentaireService;
import java.util.List;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.post;
import model.commentaire;
import services.PostService;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PostDetailController {

    @FXML
    private Label postTitleLabel;

    @FXML
    private Label postUsernameLabel;

    @FXML
    private Text postContentText;

    @FXML
    private TextArea commentTextArea;

    @FXML
    private VBox commentsContainer;

    private post post;

    private final CommentaireService commentaireService = new CommentaireService();

    // Appelée depuis le contrôleur précédent après clic sur un post
    public void setPost(post post) {
        this.post = post;
        postTitleLabel.setText(post.getTitle());
        postUsernameLabel.setText("Par: " + post.getUsernameId());
        postContentText.setText(post.getContent());

        afficherCommentaires();
    }

    // Affichage dynamique des commentaires du post
    private void afficherCommentaires() {
        commentsContainer.getChildren().clear();
        List<commentaire> commentaires = commentaireService.getCommentairesByPostId(post.getId());

        for (commentaire c : commentaires) {
            VBox commentBox = new VBox();
            commentBox.getStyleClass().add("comment-box");

            Label userLabel = new Label("Utilisateur ID: " + c.getUser_id());
            userLabel.getStyleClass().add("comment-username");

            Label dateLabel = new Label("Le " + c.getcreated_at());
            dateLabel.getStyleClass().add("comment-date");

            Text contentText = new Text(c.getContent());
            contentText.getStyleClass().add("comment-content");
            contentText.setWrappingWidth(1100);

            commentBox.getChildren().addAll(userLabel, dateLabel, contentText);
            commentsContainer.getChildren().add(commentBox);
        }
    }

    // Traitement lorsqu'on clique sur "Envoyer"
    @FXML
    private void handleCommentSubmit() {
        String commentText = commentTextArea.getText().trim();
        if (!commentText.isEmpty()) {
            commentaire newComment = new commentaire();
            newComment.setUser_id(1); // Remplacer par l’ID de l'utilisateur connecté
            newComment.setPost_id(post.getId());
            newComment.setContent(commentText);
            newComment.setcreated_at(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

            commentaireService.ajouterCommentaire(newComment);

            commentTextArea.clear();
            afficherCommentaires();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Veuillez saisir un commentaire.");
            alert.showAndWait();
        }
    }
}
