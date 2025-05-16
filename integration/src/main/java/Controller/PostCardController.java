package Controller;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import model.post;
import model.user;
import services.PostService;
import services.PostViewTracker;
import services.SessionManager;
import services.UserService;
import services.TranslationService;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;

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
    @FXML private Button upvoteButton;
    @FXML private Button downvoteButton;
    @FXML private Button translateButton;
    @FXML private Label voteCountLabel;
    private post currentPost;
    private String attachmentFileName;
    private final PostService postService = new PostService();
    private final TranslationService translationService = new TranslationService();
    private boolean isLiked = false;
    private boolean isTranslated = false;
    private String originalContent;
    private String translatedContent;
    private Map<Integer, Integer> userVotes = new HashMap<>();

    public void initialize() {
        setupButtonHoverEffects();
    }

    private void setupButtonHoverEffects() {
        setupButtonHover(upvoteButton);
        setupButtonHover(downvoteButton);
        setupButtonHover(commentButton);
        setupButtonHover(shareButton);
        setupButtonHover(translateButton);
    }

    private void setupButtonHover(Button button) {
        if (button == null) return;
        String originalStyle = button.getStyle();
        button.setOnMouseEntered(e ->
                button.setStyle(originalStyle + "-fx-background-color: rgba(76, 175, 80, 0.2);")
        );
        button.setOnMouseExited(e ->
                button.setStyle(originalStyle)
        );
    }

    private String getUserNameById(int userId) {
        try {
            UserService userService = new UserService();
            user utilisateur = userService.getUserById(userId);
            if (utilisateur != null) {
                return utilisateur.getNom() + " " + utilisateur.getPrenom();
            }
            return "Utilisateur #" + userId;
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération du nom d'utilisateur: " + e.getMessage());
            return "Utilisateur #" + userId;
        }
    }

    private void handleVote(post post, int voteValue) {
        user currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.WARNING, "Connexion requise", "Veuillez vous connecter pour voter.");
            return;
        }

        int postId = post.getId();
        int currentVote = userVotes.getOrDefault(postId, 0);
        int voteChange = 0;

        if (currentVote == voteValue) {
            voteChange = -currentVote;
            userVotes.remove(postId);
            SessionManager.removeVoteForPost(postId);
        } else {
            voteChange = voteValue - currentVote;
            userVotes.put(postId, voteValue);
            SessionManager.setVoteForPost(postId, voteValue);
        }

        int newVoteCount = postService.updateVoteCount(postId, voteChange);
        post.setVote(newVoteCount);
        updateVoteDisplay(newVoteCount);
        updateButtonStyles(postId);
    }

    private void handleTranslation() {
        if (isTranslated) {
            postPreviewText.setText(originalContent);
            translateButton.setText("🌐 Traduire");
            isTranslated = false;
        } else {
            if (originalContent.length() > 500) {
                postPreviewText.setText("Traduction d'un texte long en cours...\nCela peut prendre quelques instants.");
            } else {
                postPreviewText.setText("Traduction en cours...");
            }
            translateButton.setDisable(true);
            final String targetLanguage = "en";

            CompletableFuture.supplyAsync(() ->
                    translationService.translateText(originalContent, "fr", targetLanguage)
            ).thenAccept(result -> {
                Platform.runLater(() -> {
                    if (result != null && !result.isEmpty()) {
                        translatedContent = result;
                        postPreviewText.setText(translatedContent);
                        translateButton.setText("🔄 Original");
                        isTranslated = true;
                    } else {
                        postPreviewText.setText(originalContent);
                        showAlert(Alert.AlertType.ERROR, "Erreur de traduction",
                                "Impossible de traduire le contenu. Veuillez réessayer plus tard.");
                    }
                    translateButton.setDisable(false);
                });
            }).exceptionally(ex -> {
                Platform.runLater(() -> {
                    postPreviewText.setText(originalContent);
                    showAlert(Alert.AlertType.ERROR, "Erreur de traduction",
                            "Une erreur est survenue: " + ex.getMessage());
                    translateButton.setDisable(false);
                });
                return null;
            });
        }
    }

    private void showLanguageOptions() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Choisir la langue");
        dialog.setHeaderText("Sélectionnez la langue de traduction");
        dialog.getDialogPane().setStyle("-fx-background-color: #f5f9f5; -fx-border-color: #99cc99; -fx-border-width: 2px;");

        ComboBox<String> languageCombo = new ComboBox<>();
        Map<String, String> languages = new HashMap<>();
        languages.put("Anglais", "en");
        languages.put("Français", "fr");
        languages.put("Espagnol", "es");
        languages.put("Allemand", "de");
        languages.put("Italien", "it");
        languages.put("Arabe", "ar");
        languages.put("Chinois", "zh");
        languageCombo.getItems().addAll(languages.keySet());
        languageCombo.setValue("Anglais");

        VBox content = new VBox(12);
        content.setPadding(new Insets(20));
        content.getChildren().add(languageCombo);
        dialog.getDialogPane().setContent(content);

        ButtonType confirmButtonType = new ButtonType("Confirmer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, cancelButtonType);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                String selectedLanguage = languageCombo.getValue();
                return languages.get(selectedLanguage);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(language -> {
            SessionManager.setUserPreferredLanguage(language);
            if (isTranslated) {
                handleTranslation();
                handleTranslation();
            }
        });
    }

    public void setPostData(post item) {
        this.currentPost = item;
        categoryLabel.setText(item.getCategory());
        int userId = item.getUsernameId();
        String userName = getUserNameById(userId);
        usernameLabel.setText(userName);
        postTitleLabel.setText(item.getTitle());
        originalContent = item.getContent();
        postPreviewText.setText(originalContent);
        isTranslated = false;
        translatedContent = null;
        postTitleLabel.setOnMouseClicked(event -> openPostDetailPage(item));
        postTitleLabel.setOnMouseEntered(e -> {
            postTitleLabel.setStyle(postTitleLabel.getStyle() + "-fx-underline: true;");
        });
        postTitleLabel.setOnMouseExited(e -> {
            postTitleLabel.setStyle(postTitleLabel.getStyle().replace("-fx-underline: true;", ""));
        });
        postDateLabel.setText("📅 " + item.getDateC());
        updateViewsLabel(item.getViews());
        voteCountLabel.setText(String.valueOf(item.getVote()));
        checkExistingVote(item.getId());
        upvoteButton.setOnAction(e -> handleVote(item, 1));
        downvoteButton.setOnAction(e -> handleVote(item, -1));
        int commentCount = item.getCommentCount();
        commentButton.setText("💬 " + commentCount + " commentaires");
        shareButton.setText("🔗 Partager");
        translateButton.setText("🌐 Traduire");
        translateButton.setOnAction(e -> handleTranslation());
        translateButton.setOnContextMenuRequested(e -> showLanguageOptions());
        commentButton.setOnAction(event -> openPostDetailPage(item));
        shareButton.setOnAction(event -> showSharingOptions(item));
        if (item.getAttachment() != null && !item.getAttachment().isEmpty()) {
            attachmentFileName = item.getAttachment();
            attachmentLabel.setText("📎 " + attachmentFileName);
            attachmentLabel.setOnMouseClicked(event -> handleAttachmentClick());
            attachmentLabel.setStyle("-fx-text-fill: #0066cc; -fx-underline: true; -fx-cursor: hand;");
            attachmentLabel.setOnMouseEntered(e -> {
                attachmentLabel.setStyle("-fx-text-fill: #004499; -fx-underline: true; -fx-cursor: hand; -fx-font-weight: bold;");
            });
            attachmentLabel.setOnMouseExited(e -> {
                attachmentLabel.setStyle("-fx-text-fill: #0066cc; -fx-underline: true; -fx-cursor: hand;");
            });
        } else {
            attachmentFileName = null;
            attachmentLabel.setText("📎 Aucun");
            attachmentLabel.setOnMouseClicked(null);
            attachmentLabel.setStyle("-fx-text-fill: grey;");
        }
        updateButtonStyles(item.getId());
    }

    private void checkExistingVote(int postId) {
        Integer existingVote = SessionManager.getVoteForPost(postId);
        if (existingVote != null) {
            userVotes.put(postId, existingVote);
        }
        updateButtonStyles(postId);
    }

    private void updateButtonStyles(int postId) {
        int currentVote = userVotes.getOrDefault(postId, 0);
        upvoteButton.getStyleClass().removeAll("active-upvote", "disabled-vote");
        downvoteButton.getStyleClass().removeAll("active-downvote", "disabled-vote");
        upvoteButton.setDisable(false);
        downvoteButton.setDisable(false);
        if (currentVote == 1) {
            upvoteButton.getStyleClass().add("active-upvote");
        } else if (currentVote == -1) {
            downvoteButton.getStyleClass().add("active-downvote");
        }
    }

    private void showVoteAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information de vote");
        alert.setHeaderText(null);
        alert.setContentText(message);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/styles/vote-alert.css").toExternalForm());
        alert.showAndWait();
    }

    private void updateViewsLabel(int viewCount) {
        viewsLabel.setText("👁️ " + viewCount + " vues");
    }

    public void handleAttachmentClick() {
        if (attachmentFileName == null || attachmentFileName.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun fichier associé.");
            return;
        }
        File file = new File("C:\\Java\\Projet\\Uploads", attachmentFileName);
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

    private void showSharingOptions(post item) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Partager ce post");
        dialog.setHeaderText("Choisissez une plateforme pour partager");
        dialog.getDialogPane().setStyle("-fx-background-color: #f5f9f5; -fx-border-color: #99cc99; -fx-border-width: 2px;");
        VBox content = new VBox(12);
        content.setPadding(new Insets(20));
        Button facebookButton = createSocialButton("Facebook", "facebook", item);
        Button twitterButton = createSocialButton("Twitter", "twitter", item);
        Button whatsappButton = createSocialButton("WhatsApp", "whatsapp", item);
        Button instagramButton = createSocialButton("Instagram", "instagram", item);
        Button linkedinButton = createSocialButton("LinkedIn", "linkedin", item);
        Button emailButton = createSocialButton("Email", "email", item);
        content.getChildren().addAll(
                facebookButton, twitterButton, whatsappButton,
                instagramButton, linkedinButton, emailButton
        );
        dialog.getDialogPane().setContent(content);
        ButtonType closeButtonType = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButtonType);
        dialog.showAndWait();
    }

    private Button createSocialButton(String name, String platform, post item) {
        String color;
        String emoji;
        switch(platform) {
            case "facebook":
                color = "#3b5998";
                emoji = "👥 ";
                break;
            case "twitter":
                color = "#1da1f2";
                emoji = "🐦 ";
                break;
            case "whatsapp":
                color = "#25d366";
                emoji = "📱 ";
                break;
            case "instagram":
                color = "#c13584";
                emoji = "📷 ";
                break;
            case "linkedin":
                color = "#0077b5";
                emoji = "💼 ";
                break;
            case "email":
                color = "#dd4b39";
                emoji = "✉️ ";
                break;
            default:
                color = "#3498db";
                emoji = "🔗 ";
        }
        Button button = new Button(emoji + name);
        button.setPrefWidth(220);
        String baseStyle = String.format(
                "-fx-background-color: %s; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14px; " +
                        "-fx-padding: 10 16; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;", color);
        button.setStyle(baseStyle);
        String hoverStyle = baseStyle + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 2);";
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(baseStyle));
        button.setOnAction(event -> {
            try {
                shareToSocialMedia(platform, item);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de partager sur " + name);
                e.printStackTrace();
            }
        });
        return button;
    }

    private void shareToSocialMedia(String platform, post item) throws IOException, URISyntaxException {
        String postUrl = "Post #" + item.getId() + " - " + item.getTitle() + " (Depuis l'application JavaFX Forum)";
        String encodedTitle = URLEncoder.encode(item.getTitle(), "UTF-8");
        String encodedText = URLEncoder.encode("Découvrez ce post intéressant: " + postUrl, "UTF-8");
        String shareUrl = "";
        switch(platform) {
            case "facebook":
                shareUrl = "https://www.facebook.com/sharer/sharer.php?u=" + encodedText;
                break;
            case "twitter":
                shareUrl = "https://twitter.com/intent/tweet?text=" + encodedText;
                break;
            case "whatsapp":
                shareUrl = "https://api.whatsapp.com/send?text=" + encodedText;
                break;
            case "instagram":
                showAlert(Alert.AlertType.INFORMATION, "Instagram",
                        "Pour partager sur Instagram, copiez ce texte et partagez-le dans votre story:\n" + postUrl);
                return;
            case "linkedin":
                shareUrl = "https://www.linkedin.com/sharing/share-offsite/?url=" + encodedText;
                break;
            case "email":
                shareUrl = "mailto:?subject=" + encodedTitle + "&body=" + encodedText;
                break;
        }
        if (!shareUrl.isEmpty()) {
            Desktop.getDesktop().browse(new URI(shareUrl));
        }
    }

    private void openPostDetailPage(post item) {
        try {
            user currentUser = SessionManager.getCurrentUser();
            if (currentUser == null) {
                showAlert(Alert.AlertType.WARNING, "Attention",
                        "Connectez-vous pour que votre vue soit comptabilisée.");
            } else {
                int userId = currentUser.getId();
                int postId = item.getId();
                boolean isNewView = PostViewTracker.recordView(postId, userId);
                if (isNewView) {
                    int newViewCount = postService.incrementViewCount(postId);
                    item.setViews(newViewCount);
                    updateViewsLabel(newViewCount);
                    System.out.println("Vue incrémentée pour le post #" + postId + " par l'utilisateur #" + userId);
                } else {
                    System.out.println("L'utilisateur #" + userId + " a déjà vu le post #" + postId);
                }
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Post/PostDetail.fxml"));
            Parent root = loader.load();
            Object controller = loader.getController();
            if (controller != null) {
                try {
                    Method setPostMethod = controller.getClass().getMethod("setPost", post.class);
                    setPostMethod.invoke(controller, item);
                } catch (Exception e) {
                    System.err.println("Erreur lors de l'appel de setPost: " + e.getMessage());
                }
            }

            Scene scene = new Scene(root, 800, 600); // Smaller window for banner effect
            Stage stage = new Stage();
            stage.setTitle("Détail du post: " + item.getTitle());
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL); // Make it modal
            stage.setResizable(false); // Fixed size for banner effect

            // Center the window
            stage.centerOnScreen();

            // Slide-in animation
            root.setTranslateY(-600); // Start above the screen
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), root);
            slideIn.setToY(0);
            slideIn.play();

            // Fade-in animation
            root.setOpacity(0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

            stage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la page de détail : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/styles/alertStyle.css").toExternalForm());
        dialogPane.setStyle("-fx-background-color: #f0f8f0; -fx-border-color: #99cc99; -fx-border-width: 2px;");
        alert.showAndWait();
    }

    public void refreshPostData(post updatedPost) {
        if (updatedPost != null) {
            setPostData(updatedPost);
        }
    }

    private void updateVoteDisplay(int voteCount) {
        if (voteCount > 0) {
            voteCountLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold; -fx-font-size: 16px;");
        } else if (voteCount < 0) {
            voteCountLabel.setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold; -fx-font-size: 16px;");
        } else {
            voteCountLabel.setStyle("-fx-text-fill: #1e3e1e; -fx-font-weight: bold; -fx-font-size: 16px;");
        }
        voteCountLabel.setText(String.valueOf(voteCount));
        ScaleTransition st = new ScaleTransition(Duration.millis(100), voteCountLabel);
        st.setFromX(1.0);
        st.setFromY(1.0);
        st.setToX(1.2);
        st.setToY(1.2);
        st.setCycleCount(2);
        st.setAutoReverse(true);
        st.play();
    }
}