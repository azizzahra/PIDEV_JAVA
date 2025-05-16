package Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import services.CommentaireService;
import services.PostService;
import services.PostViewTracker;
import services.SessionManager;
import services.TwilioService;
import services.UserService;
import model.commentaire;
import model.post;
import model.user;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PostDetailController {

    @FXML private Label postTitleLabel;
    @FXML private Label postUsernameLabel;
    @FXML private Label postDateLabel;
    @FXML private Text postContentText;
    @FXML private TextArea commentTextArea;
    @FXML private VBox commentsContainer;
    @FXML private Label viewsLabel;
    @FXML private Button editPostButton;
    @FXML private VBox editFormContainer;
    @FXML private TextField editTitleField;
    @FXML private TextArea editContentArea;
    @FXML private VBox mainVBox;

    private post post;
    private Integer replyingToCommentId = null;
    private Label replyingToLabel = null;

    private final CommentaireService commentaireService = new CommentaireService();
    private final PostService postService = new PostService();
    private final UserService userService = new UserService();
    private final TwilioService twilioService = new TwilioService();

    public void setPost(post post) {
        this.post = post;
        user author = userService.getUserById(post.getUsernameId());
        if (author != null) {
            postUsernameLabel.setText("Par: " + author.getNom() + " " + author.getPrenom());
        } else {
            postUsernameLabel.setText("Utilisateur #" + post.getUsernameId());
        }
        postTitleLabel.setText(post.getTitle());
        postContentText.setText(post.getContent());
        postDateLabel.setText(post.getDateC());
        if (viewsLabel != null) {
            viewsLabel.setText(post.getViews() + " vues");
        }
        checkIfCurrentUserIsAuthor();
        handleViewTracking();
        afficherCommentaires();
    }

    private void checkIfCurrentUserIsAuthor() {
        user currentUser = SessionManager.getCurrentUser();
        if (currentUser != null && post != null && currentUser.getId() == post.getUsernameId()) {
            editPostButton.setVisible(true);
        } else {
            editPostButton.setVisible(false);
        }
    }

    @FXML
    private void handleEditPost() {
        editTitleField.setText(post.getTitle());
        editContentArea.setText(post.getContent());
        editFormContainer.setVisible(true);
        editFormContainer.setManaged(true);
        postTitleLabel.setVisible(false);
        postContentText.setVisible(false);
    }

    @FXML
    private void handleSaveEdit() {
        String newTitle = editTitleField.getText().trim();
        String newContent = editContentArea.getText().trim();
        if (newTitle.isEmpty() || newContent.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Le titre et le contenu ne peuvent pas être vides.");
            alert.showAndWait();
            return;
        }
        post.setTitle(newTitle);
        post.setContent(newContent);
        postService.modifier(post);
        postTitleLabel.setText(newTitle);
        postContentText.setText(newContent);
        exitEditMode();
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Post modifié avec succès.");
        alert.showAndWait();
    }

    @FXML
    private void handleCancelEdit() {
        exitEditMode();
    }

    private void exitEditMode() {
        editFormContainer.setVisible(false);
        editFormContainer.setManaged(false);
        postTitleLabel.setVisible(true);
        postContentText.setVisible(true);
    }

    private void afficherCommentaires() {
        commentsContainer.getChildren().clear();
        List<commentaire> commentairesPrincipaux = commentaireService.getMainCommentsByPostId(post.getId());
        for (commentaire c : commentairesPrincipaux) {
            VBox commentWithRepliesBox = new VBox();
            commentWithRepliesBox.setSpacing(10);
            commentWithRepliesBox.getStyleClass().add("comment-with-replies-box");
            VBox commentBox = createCommentBox(c);
            commentWithRepliesBox.getChildren().add(commentBox);
            VBox repliesBox = new VBox();
            repliesBox.setSpacing(8);
            repliesBox.setPadding(new Insets(0, 0, 0, 30));
            repliesBox.getStyleClass().add("replies-box");
            afficherReponsesRecursif(c.getId(), repliesBox, 1);
            if (!repliesBox.getChildren().isEmpty()) {
                commentWithRepliesBox.getChildren().add(repliesBox);
            }
            commentsContainer.getChildren().add(commentWithRepliesBox);
        }
    }

    private void afficherReponsesRecursif(int commentId, VBox container, int level) {
        List<commentaire> replies = commentaireService.getRepliesByCommentId(commentId);
        for (commentaire reply : replies) {
            VBox replyBox = createCommentBox(reply);
            replyBox.getStyleClass().add("reply-box");
            replyBox.getStyleClass().add("reply-level-" + Math.min(level, 3));
            container.getChildren().add(replyBox);
            VBox subRepliesBox = new VBox();
            subRepliesBox.setSpacing(8);
            subRepliesBox.setPadding(new Insets(0, 0, 0, 20));
            subRepliesBox.getStyleClass().add("replies-box");
            afficherReponsesRecursif(reply.getId(), subRepliesBox, level + 1);
            if (!subRepliesBox.getChildren().isEmpty()) {
                container.getChildren().add(subRepliesBox);
            }
        }
    }

    private void handleReplyClick(int commentId) {
        commentaire targetComment = findCommentById(commentId);
        replyingToCommentId = commentId;
        String replyLabelText = "Réponse au commentaire";
        if (targetComment != null) {
            String previewContent = targetComment.getContent();
            if (previewContent.length() > 30) {
                previewContent = previewContent.substring(0, 30) + "...";
            }
            replyLabelText += " : \"" + previewContent + "\"";
        } else {
            replyLabelText += " #" + commentId;
        }
        if (replyingToLabel == null) {
            replyingToLabel = new Label(replyLabelText);
            replyingToLabel.getStyleClass().add("replying-to-label");
            int index = mainVBox.getChildren().indexOf(commentTextArea);
            if (index > 0) {
                mainVBox.getChildren().add(index, replyingToLabel);
            }
            Button cancelReplyButton = new Button("Annuler la réponse");
            cancelReplyButton.getStyleClass().add("cancel-reply-button");
            cancelReplyButton.setOnAction(event -> cancelReply());
            HBox replyActionBox = new HBox();
            replyActionBox.setSpacing(10);
            replyActionBox.getChildren().add(cancelReplyButton);
            mainVBox.getChildren().add(index + 1, replyActionBox);
        } else {
            replyingToLabel.setText(replyLabelText);
        }
        commentTextArea.requestFocus();
    }

    private commentaire findCommentById(int commentId) {
        List<commentaire> allComments = commentaireService.getCommentairesByPostId(post.getId());
        for (commentaire c : allComments) {
            if (c.getId() == commentId) {
                return c;
            }
        }
        return null;
    }

    private VBox createCommentBox(commentaire c) {
        VBox commentBox = new VBox();
        commentBox.setSpacing(5);
        commentBox.getStyleClass().add("comment-box");
        user author = userService.getUserById(c.getUser_id());
        String authorName = (author != null) ? author.getNom() + " " + author.getPrenom() : "Utilisateur #" + c.getUser_id();
        HBox headerBox = new HBox();
        headerBox.setSpacing(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        Label userLabel = new Label(authorName);
        userLabel.getStyleClass().add("comment-username");
        Label dateLabel = new Label(c.getcreated_at());
        dateLabel.getStyleClass().add("comment-date");
        headerBox.getChildren().addAll(userLabel, dateLabel);
        Text contentText = new Text(c.getContent());
        contentText.getStyleClass().add("comment-content");
        contentText.setWrappingWidth(1100);
        HBox actionsBox = new HBox();
        actionsBox.setSpacing(10);
        actionsBox.setAlignment(Pos.CENTER_LEFT);
        Button replyButton = new Button("Répondre");
        replyButton.getStyleClass().add("reply-button");
        replyButton.setOnAction(event -> handleReplyClick(c.getId()));
        actionsBox.getChildren().add(replyButton);
        commentBox.getChildren().addAll(headerBox, contentText, actionsBox);
        return commentBox;
    }

    private void cancelReply() {
        replyingToCommentId = null;
        if (replyingToLabel != null) {
            int labelIndex = mainVBox.getChildren().indexOf(replyingToLabel);
            if (labelIndex >= 0) {
                mainVBox.getChildren().remove(labelIndex);
                mainVBox.getChildren().remove(labelIndex);
            }
            replyingToLabel = null;
        }
    }

    @FXML
    private void handleCommentSubmit() {
        String commentText = commentTextArea.getText().trim();
        if (!commentText.isEmpty()) {
            commentaire newComment = new commentaire();
            user currentUser = SessionManager.getCurrentUser();
            if (currentUser != null) {
                newComment.setUser_id(currentUser.getId());
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Vous devez être connecté pour commenter.");
                alert.showAndWait();
                return;
            }
            newComment.setPost_id(post.getId());
            newComment.setContent(commentText);
            newComment.setcreated_at(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            if (replyingToCommentId != null) {
                newComment.setParent_id(replyingToCommentId);
                sendNotificationForReply(replyingToCommentId, currentUser);
                cancelReply();
            } else {
                newComment.setParent_id(null);
            }
            commentaireService.ajouterCommentaire(newComment);
            commentTextArea.clear();
            afficherCommentaires();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Veuillez saisir un commentaire.");
            alert.showAndWait();
        }
    }

    private void sendNotificationForReply(int parentCommentId, user responder) {
        commentaire parentComment = findCommentById(parentCommentId);
        if (parentComment == null) {
            System.out.println("Impossible d'envoyer la notification : commentaire parent non trouvé");
            return;
        }
        user commentOwner = userService.getUserById(parentComment.getUser_id());
        if (commentOwner == null) {
            System.out.println("Impossible d'envoyer la notification : propriétaire du commentaire non trouvé");
            return;
        }
        if (commentOwner.getId() == responder.getId()) {
            System.out.println("Pas de notification : l'utilisateur répond à son propre commentaire");
            return;
        }
        String destinationNumber = commentOwner.getNum_tel();
        if (destinationNumber == null || destinationNumber.isEmpty()) {
            System.out.println("Pas de notification SMS: l'utilisateur " + commentOwner.getId() +
                    " (" + commentOwner.getNom() + " " + commentOwner.getPrenom() + ") " +
                    "n'a pas de numéro de téléphone enregistré");
            return;
        }
        System.out.println("Préparation d'envoi de notification SMS au numéro: " + destinationNumber);
        boolean success = twilioService.sendCommentReplyNotification(
                commentOwner,
                responder,
                post.getTitle()
        );
        if (success) {
            System.out.println("Notification SMS envoyée avec succès à " + commentOwner.getNom() + " " +
                    commentOwner.getPrenom() + " (" + destinationNumber + ") " +
                    "pour une réponse à son commentaire");
        } else {
            System.err.println("Échec de l'envoi de la notification SMS à l'utilisateur " +
                    commentOwner.getId() + " (" + destinationNumber + ")");
        }
    }

    private void handleViewTracking() {
        user currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) return;
        int userId = currentUser.getId();
        int postId = post.getId();
        boolean isNewView = PostViewTracker.recordView(postId, userId);
        if (isNewView) {
            int newViewCount = postService.incrementViewCount(postId);
            post.setViews(newViewCount);
            if (viewsLabel != null) {
                viewsLabel.setText(newViewCount + " vues");
            }
            System.out.println("Vue incrémentée pour le post #" + postId + " par l'utilisateur #" + userId);
        } else {
            System.out.println("L'utilisateur #" + userId + " a déjà vu le post #" + postId);
        }
    }


}