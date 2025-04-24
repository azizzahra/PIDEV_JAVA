package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.stage.Stage;
import model.post;
import services.PostService;

import java.io.IOException;
import java.util.List;

public class PostController {

    @FXML
    private VBox postListView; // conteneur qui va contenir les cartes

    @FXML
    private VBox postCard;
    // modèle à dupliquer (sera caché)

    @FXML
    private ScrollPane scrollPane;

    private final PostService postService = new PostService();

    public void initialize() {
        // Récupérer la liste des posts depuis le service
        List<post> posts = postService.afficher();

        // Cacher la carte modèle dans l'interface

        if (postCard != null) {
            postCard.setVisible(false); // Cache la carte modèle
        }
        for (post p : posts) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/PostCard.fxml"));
                Node card = loader.load();

                // Obtenir le contrôleur spécifique de cette carte
                PostCardController cardController = loader.getController();

                // Injecter les données du post dans la carte
                cardController.setPostData(p);

                // Ajouter la carte au conteneur principal
                postListView.getChildren().add(card);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Ajouter un style dynamique pour un défilement fluide
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }

    @FXML
    private void openAjoutPostWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/ajout.fxml"));
            BorderPane ajoutRoot = fxmlLoader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter un nouveau post");
            stage.setScene(new Scene(ajoutRoot));
            stage.showAndWait(); // attend que l'utilisateur ferme la fenêtre

            // Rafraîchir les posts après ajout
            refreshPostList();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void refreshPostList() {
        postListView.getChildren().clear(); // Efface les anciennes cartes

        List<post> posts = postService.afficher();
        for (post p : posts) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/PostCard.fxml"));
                Node card = loader.load();
                PostCardController cardController = loader.getController();
                cardController.setPostData(p);
                postListView.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
