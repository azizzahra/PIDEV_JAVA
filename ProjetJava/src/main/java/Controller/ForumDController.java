package controller;


import model.post;
import model.commentaire;
import services.PostService;
import services.CommentaireService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.event.ActionEvent;
import javafx.util.Callback;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

public class ForumDController {

    @FXML
    private VBox postsContainer;

    @FXML
    private ComboBox<String> categoryFilter;

    @FXML
    private TextField searchField;

    private PostService postService;
    private CommentaireService commentaireService;

    public void initialize() {
        postService = new PostService();
        commentaireService = new CommentaireService();

        // Initialize category filter
        ObservableList<String> categories = FXCollections.observableArrayList();
        categories.addAll("Toutes les catégories", "Agriculture", "Élevage", "Irrigation", "Équipement", "Autres");
        categoryFilter.setItems(categories);
        categoryFilter.setValue("Toutes les catégories");
        categoryFilter.setOnAction(event -> loadPosts());

        // Load posts
        loadPosts();
    }

    private void loadPosts() {
        postsContainer.getChildren().clear();
        List<post> posts = postService.afficher();
        String selectedCategory = categoryFilter.getValue();
        String searchTerm = searchField.getText().toLowerCase();

        for (post p : posts) {
            // Apply filters if necessary
            if (!selectedCategory.equals("Toutes les catégories") && !p.getCategory().equals(selectedCategory)) {
                continue;
            }

            if (!searchTerm.isEmpty() && !p.getTitle().toLowerCase().contains(searchTerm) &&
                    !p.getContent().toLowerCase().contains(searchTerm)) {
                continue;
            }

            createPostCard(p);
        }
    }

    private void createPostCard(post p) {
        // Create post container
        VBox postCard = new VBox();
        postCard.setStyle("-fx-background-color: white; -fx-border-color: #c8e6c9; -fx-border-width: 1; " +
                "-fx-border-radius: 5; -fx-background-radius: 5;");
        postCard.setPadding(new Insets(15));
        postCard.setSpacing(10);

        // Post header with title and actions
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(10);

        Label titleLabel = new Label(p.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1e5631;");

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label categoryLabel = new Label("🏷️ " + p.getCategory());
        categoryLabel.setStyle("-fx-background-color: #e8f5e9; -fx-padding: 5; -fx-background-radius: 3;");

        Button editButton = new Button("✏️");
        editButton.setStyle("-fx-background-color: #f0f7ed;");
        editButton.setOnAction(e -> showEditPostDialog(p));

        Button deleteButton = new Button("🗑️");
        deleteButton.setStyle("-fx-background-color: #f0f7ed;");
        deleteButton.setOnAction(e -> confirmDeletePost(p));

        header.getChildren().addAll(titleLabel, spacer, categoryLabel, editButton, deleteButton);

        // Post content
        Label contentLabel = new Label(p.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setPadding(new Insets(10, 0, 10, 0));

        // Post metadata
        HBox metadata = new HBox();
        metadata.setSpacing(15);

        Label dateLabel = new Label("📅 " + p.getDateC());
        Label viewsLabel = new Label("👁️ " + p.getViews() + " vues");
        Label votesLabel = new Label("👍 " + p.getVote() + " votes");

        metadata.getChildren().addAll(dateLabel, viewsLabel, votesLabel);

        // Comments section
        TitledPane commentsPane = new TitledPane();
        commentsPane.setText("Commentaires (" + getCommentCount(p.getId()) + ")");

        VBox commentsContainer = new VBox();
        commentsContainer.setSpacing(10);
        commentsContainer.setPadding(new Insets(10, 0, 0, 0));

        // Add new comment section
        HBox addCommentBox = new HBox();
        addCommentBox.setSpacing(10);
        addCommentBox.setAlignment(Pos.CENTER_LEFT);

        TextField commentField = new TextField();
        commentField.setPromptText("Ajouter un commentaire...");
        commentField.setPrefWidth(500);
        HBox.setHgrow(commentField, Priority.ALWAYS);

        Button addCommentBtn = new Button("Envoyer");
        addCommentBtn.setStyle("-fx-background-color: #3e8914; -fx-text-fill: white;");
        addCommentBtn.setOnAction(e -> {
            String commentContent = commentField.getText().trim();
            if (!commentContent.isEmpty()) {
                addComment(p.getId(), commentContent);
                commentField.clear();
                loadComments(p.getId(), commentsContainer);
                commentsPane.setText("Commentaires (" + getCommentCount(p.getId()) + ")");
            }
        });

        addCommentBox.getChildren().addAll(commentField, addCommentBtn);

        // Load existing comments
        loadComments(p.getId(), commentsContainer);

        VBox commentsPaneContent = new VBox();
        commentsPaneContent.setSpacing(10);
        commentsPaneContent.getChildren().addAll(addCommentBox, commentsContainer);

        commentsPane.setContent(commentsPaneContent);
        commentsPane.setExpanded(false);

        // Add everything to post card
        postCard.getChildren().addAll(header, contentLabel, metadata, commentsPane);

        // Add post card to main container
        postsContainer.getChildren().add(postCard);
    }

    private void loadComments(int postId, VBox commentsContainer) {
        commentsContainer.getChildren().clear();
        List<commentaire> comments = commentaireService.getCommentairesByPostId(postId);

        if (comments.isEmpty()) {
            Label noCommentsLabel = new Label("Pas encore de commentaires.");
            noCommentsLabel.setStyle("-fx-text-fill: #757575; -fx-font-style: italic;");
            commentsContainer.getChildren().add(noCommentsLabel);
        } else {
            for (commentaire c : comments) {
                createCommentItem(c, commentsContainer);
            }
        }
    }

    private void createCommentItem(commentaire c, VBox container) {
        HBox commentBox = new HBox();
        commentBox.setSpacing(10);
        commentBox.setPadding(new Insets(8));
        commentBox.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 5;");

        VBox commentContent = new VBox();
        commentContent.setSpacing(5);
        HBox.setHgrow(commentContent, Priority.ALWAYS);

        // Comment content
        Label contentLabel = new Label(c.getContent());
        contentLabel.setWrapText(true);

        // Comment metadata
        HBox metadata = new HBox();
        metadata.setSpacing(10);

        Label dateLabel = new Label("📅 " + c.getcreated_at());
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #757575;");

        metadata.getChildren().add(dateLabel);

        commentContent.getChildren().addAll(contentLabel, metadata);

        // Comment actions
        HBox actions = new HBox();
        actions.setSpacing(5);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button editButton = new Button("✏️");
        editButton.setStyle("-fx-background-color: transparent;");
        editButton.setOnAction(e -> showEditCommentDialog(c, container));

        Button deleteButton = new Button("🗑️");
        deleteButton.setStyle("-fx-background-color: transparent;");
        deleteButton.setOnAction(e -> confirmDeleteComment(c, container));

        actions.getChildren().addAll(editButton, deleteButton);

        commentBox.getChildren().addAll(commentContent, actions);
        container.getChildren().add(commentBox);
    }

    private int getCommentCount(int postId) {
        return commentaireService.getCommentairesByPostId(postId).size();
    }

    @FXML
    private void handleSearch() {
        loadPosts();
    }

    @FXML
    private void handleNewPost() {
        // Open dialog to create new post
        Dialog<post> dialog = new Dialog<>();
        dialog.setTitle("Nouveau Post");
        dialog.setHeaderText("Créer un nouveau post");

        ButtonType saveButtonType = new ButtonType("Publier", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField();
        titleField.setPromptText("Titre");

        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll("Agrotech", "Materiel Agricole", "Energie ");
        categoryCombo.setPromptText("Catégorie");

        TextArea contentArea = new TextArea();
        contentArea.setPromptText("Contenu");
        contentArea.setPrefHeight(200);

        grid.add(new Label("Titre:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Catégorie:"), 0, 1);
        grid.add(categoryCombo, 1, 1);
        grid.add(new Label("Contenu:"), 0, 2);
        grid.add(contentArea, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (titleField.getText().trim().isEmpty() ||
                        categoryCombo.getValue() == null ||
                        contentArea.getText().trim().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setHeaderText("Champs incomplets");
                    alert.setContentText("Veuillez remplir tous les champs.");
                    alert.showAndWait();
                    return null;
                }

                post newPost = new post();
                newPost.setTitle(titleField.getText());
                newPost.setCategory(categoryCombo.getValue());
                newPost.setContent(contentArea.getText());
                newPost.setUsernameId(1); // Assuming current user id
                return newPost;
            }
            return null;
        });

        Optional<post> result = dialog.showAndWait();

        result.ifPresent(newPost -> {
            // Call service to add new post
            postService.add(newPost);
            loadPosts();
        });
    }

    private void showEditPostDialog(post p) {
        Dialog<post> dialog = new Dialog<>();
        dialog.setTitle("Modifier le Post");
        dialog.setHeaderText("Modifier les détails du post");

        ButtonType saveButtonType = new ButtonType("Sauvegarder", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField(p.getTitle());

        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll("Agriculture", "Élevage", "Irrigation", "Équipement", "Autres");
        categoryCombo.setValue(p.getCategory());

        TextArea contentArea = new TextArea(p.getContent());
        contentArea.setPrefHeight(200);

        grid.add(new Label("Titre:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Catégorie:"), 0, 1);
        grid.add(categoryCombo, 1, 1);
        grid.add(new Label("Contenu:"), 0, 2);
        grid.add(contentArea, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (titleField.getText().trim().isEmpty() ||
                        categoryCombo.getValue() == null ||
                        contentArea.getText().trim().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setHeaderText("Champs incomplets");
                    alert.setContentText("Veuillez remplir tous les champs.");
                    alert.showAndWait();
                    return null;
                }

                p.setTitle(titleField.getText());
                p.setCategory(categoryCombo.getValue());
                p.setContent(contentArea.getText());
                return p;
            }
            return null;
        });

        Optional<post> result = dialog.showAndWait();

        result.ifPresent(updatedPost -> {
            // Call service to update post
            postService.modifier(updatedPost);
            loadPosts();
        });
    }

    private void confirmDeletePost(post p) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le post");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce post ? Cette action supprimera également tous les commentaires associés.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Delete the post
            postService.supprimer(p.getId());
            loadPosts();
        }
    }

    private void showEditCommentDialog(commentaire c, VBox container) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Modifier le Commentaire");
        dialog.setHeaderText("Modifier votre commentaire");

        ButtonType saveButtonType = new ButtonType("Sauvegarder", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create form field
        TextArea contentArea = new TextArea(c.getContent());
        contentArea.setPrefWidth(400);
        contentArea.setPrefHeight(100);
        contentArea.setWrapText(true);

        dialog.getDialogPane().setContent(contentArea);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (contentArea.getText().trim().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setHeaderText("Contenu vide");
                    alert.setContentText("Le commentaire ne peut pas être vide.");
                    alert.showAndWait();
                    return null;
                }
                return contentArea.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(newContent -> {
            // Update the comment
            c.setContent(newContent);
            commentaireService.modifier(c);

            // Refresh comments
            loadComments(c.getPost_id(), container);
        });
    }

    private void confirmDeleteComment(commentaire c, VBox container) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le commentaire");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce commentaire ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Delete the comment
            commentaireService.supprimer(c.getId());

            // Refresh comments
            loadComments(c.getPost_id(), container);

            // Update comment count in title
            TitledPane commentsPane = findTitledPaneParent(container);
            if (commentsPane != null) {
                commentsPane.setText("Commentaires (" + getCommentCount(c.getPost_id()) + ")");
            }
        }
    }
    private TitledPane findTitledPaneParent(Node node) {
        while (node != null && !(node instanceof TitledPane)) {
            node = node.getParent();
        }
        return (TitledPane) node;
    }


    private void addComment(int postId, String content) {
        commentaire c = new commentaire();
        c.setPost_id(postId);
        c.setUser_id(1); // Assuming current user id
        c.setContent(content);
        commentaireService. ajouterCommentaire(c);
    }

    @FXML
    private void handleReturnToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/dashboard.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) postsContainer.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur lors du chargement du dashboard", e.getMessage());
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}