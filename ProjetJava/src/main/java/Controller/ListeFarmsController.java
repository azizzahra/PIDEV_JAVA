package Controller;
import Main.mainPrincipal;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.Farm;
import services.FarmService;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;


import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javafx.application.Platform;

public class ListeFarmsController {

    private ObservableList<Farm> farmList = FXCollections.observableArrayList();
    private FarmService farmService = new FarmService();


    @FXML
    private TilePane farmContainer; // Changement de VBox à TilePane

    @FXML
    private TextField searchField;

    public void initialize() {
        loadFarms();
        setupSearchField();

        // Ajoutez ce listener pour ajuster les cards lors du redimensionnement
        farmContainer.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() > 0) {
                refreshFarmList();
            }
        });
    }

    private void setupSearchField() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterFarms(newValue);
        });
    }
    private void filterFarms(String keyword) {
        try {
            List<Farm> allFarms = farmService.getAll();
            farmContainer.getChildren().clear();

            if (keyword == null || keyword.isEmpty()) {
                // Si aucun mot-clé, afficher toutes les fermes
                for (Farm f : allFarms) {
                    BorderPane card = createFarmCard(f);
                    farmContainer.getChildren().add(card);
                }
            } else {
                // Filtrer par mot-clé (nom ou description)
                keyword = keyword.toLowerCase();
                for (Farm f : allFarms) {
                    if (f.getName().toLowerCase().contains(keyword) ||
                            f.getDescription().toLowerCase().contains(keyword) ||
                            f.getLocation().toLowerCase().contains(keyword)) {
                        BorderPane card = createFarmCard(f);
                        farmContainer.getChildren().add(card);
                    }
                }
            }

            // Afficher un message si aucune ferme trouvée
            if (farmContainer.getChildren().isEmpty()) {
                Label emptyLabel = new Label("Aucune ferme trouvée pour : " + keyword);
                emptyLabel.setStyle("-fx-font-size: 16px;");
                farmContainer.getChildren().add(emptyLabel);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert("Erreur de recherche",
                    "Une erreur s'est produite lors de la recherche de fermes.",
                    Alert.AlertType.ERROR);
        }
    }

    // Ajouter une méthode de rafraîchissement publique
    public void refreshFarmList() {
        Platform.runLater(() -> {
            farmContainer.getChildren().clear();
            loadFarms();
        });
    }
    // Méthode pour charger les fermes
    private void loadFarms() {
        try {
            List<Farm> farms = farmService.getAll();
            farmContainer.getChildren().clear();

            if (farms.isEmpty()) {
                Label emptyLabel = new Label("Aucune ferme trouvée.");
                emptyLabel.setStyle("-fx-font-size: 16px;");
                farmContainer.getChildren().add(emptyLabel);
            } else {
                for (Farm f : farms) {
                    BorderPane card = createFarmCard(f);
                    farmContainer.getChildren().add(card);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert("Erreur de chargement",
                    "Une erreur s'est produite lors du chargement des fermes.",
                    Alert.AlertType.ERROR);
        }
    }

    // Crée la carte de chaque farm avec une présentation stylisée similaire à l'image
    private BorderPane createFarmCard(Farm farm) {
        BorderPane card = new BorderPane();
        card.getStyleClass().add("farm-card");
        card.setPrefSize(400, 380);
        card.setMaxWidth(Double.MAX_VALUE);

        // Création de l'image
        ImageView farmImage = createFarmImageView(farm);

        // Étiquette de superficie (KM²) positionnée sur l'image
        Label sizeTag = new Label(farm.getSize() + " KM²");
        sizeTag.getStyleClass().add("farm-card-tag");

        // Étiquette de localisation avec icône
        Label locationLabel = new Label(" " + farm.getLocation());
        locationLabel.getStyleClass().add("farm-location");

        // Création du conteneur pour le titre et la localisation
        VBox titleBox = new VBox(5);
        Label titleLabel = new Label(farm.getName());
        titleLabel.getStyleClass().add("farm-title");

        titleBox.getChildren().addAll(titleLabel, locationLabel);
        titleBox.setPadding(new Insets(15, 15, 5, 15));

        // Création du conteneur pour la description
        VBox contentBox = new VBox(10);
        Label descriptionLabel = new Label(farm.getDescription());
        descriptionLabel.getStyleClass().add("farm-description");
        descriptionLabel.setWrapText(true);
        contentBox.getChildren().add(descriptionLabel);
        contentBox.setPadding(new Insets(5, 15, 10, 15));

        // Conteneur pour les boutons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(0, 0, 15, 0));

        Button editButton = new Button("Modifier");
        editButton.getStyleClass().addAll("card-button", "edit-button");
        editButton.setOnAction(e -> openUpdateFormWithId(farm.getId()));

        Button deleteButton = new Button("Supprimer");
        deleteButton.getStyleClass().addAll("card-button", "delete-button");
        deleteButton.setOnAction(e -> deleteFarm(farm.getId()));

        buttonBox.getChildren().addAll(editButton, deleteButton);

        // Mise en place des éléments dans la carte
        StackPane imageContainer = new StackPane();
        imageContainer.getChildren().add(farmImage);

        // Positionnement de l'étiquette de taille en haut à droite de l'image
        StackPane.setAlignment(sizeTag, Pos.TOP_RIGHT);
        StackPane.setMargin(sizeTag, new Insets(10, 10, 0, 0));
        imageContainer.getChildren().add(sizeTag);

        card.setTop(imageContainer);
        card.setCenter(new VBox(titleBox, contentBox));
        card.setBottom(buttonBox);

        return card;
    }

    private ImageView createFarmImageView(Farm farm) {
        ImageView farmImage = new ImageView();
        try {
            // Vérifier si le chemin d'image existe
            if (farm.getImage() != null && !farm.getImage().isEmpty()) {
                String imagePath = "file:src/resources/" + farm.getImage();
                Image image = new Image(imagePath, 400, 200, true, true);
                farmImage.setImage(image);
            } else {
                // Image par défaut si aucune image n'est disponible
                Image defaultImage = new Image("file:src/resources/default-farm.jpg", 400, 200, true, true);
                farmImage.setImage(defaultImage);
            }
        } catch (Exception e) {
            System.err.println("Erreur de chargement d'image: " + e.getMessage());
            try {
                Image defaultImage = new Image("file:src/resources/default-farm.jpg", 400, 200, true, true);
                farmImage.setImage(defaultImage);
            } catch (Exception ex) {
                System.err.println("Impossible de charger l'image par défaut: " + ex.getMessage());
            }
        }

        farmImage.setFitWidth(400);
        farmImage.setFitHeight(200);
        farmImage.setPreserveRatio(false);

        return farmImage;
    }

    private void openUpdateFormWithId(int farmId) {
        try {
            // Utiliser directement le stage principal
            Stage mainStage = mainPrincipal.getPrimaryStage();

            // Charger la vue de mise à jour
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UpdateFarm.fxml"));
            Parent root = loader.load();

            // Configurer le contrôleur avec l'ID de la ferme
            UpdateFarmController controller = loader.getController();
            controller.setFarmId(farmId);

            // Remplacer le contenu du stage principal
            mainStage.getScene().setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void OpenAddForm() {
        try {
            // Utiliser directement le stage principal
            Stage mainStage = mainPrincipal.getPrimaryStage();

            // Charger la vue d'ajout
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddFarm.fxml"));
            Parent root = loader.load();

            // Remplacer le contenu du stage principal
            mainStage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'ouverture du formulaire d'ajout", Alert.AlertType.ERROR);
        }
    }

    private void deleteFarm(int farmId) {
        try {
            // Afficher une boîte de dialogue de confirmation
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Confirmation de suppression");
            confirmDialog.setHeaderText("Êtes-vous sûr de vouloir supprimer cette ferme ?");
            confirmDialog.setContentText("Cette action est irréversible.");

            // Si l'utilisateur confirme
            if (confirmDialog.showAndWait().get() == ButtonType.OK) {
                FarmService farmService = new FarmService();

                // Récupérer d'abord l'objet Farm complet par son ID
                Farm farmToDelete = farmService.getone(farmId);

                if (farmToDelete != null) {
                    // Supprimer la ferme
                    farmService.delete(farmToDelete);

                    // Afficher un message de succès
                    showAlert("Succès", "Ferme supprimée avec succès", Alert.AlertType.INFORMATION);

                    // Rafraîchir la liste des fermes
                    refreshFarmList();
                } else {
                    // Afficher un message d'erreur si la ferme n'est pas trouvée
                    showAlert("Erreur", "Impossible de trouver la ferme à supprimer", Alert.AlertType.ERROR);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur de base de données",
                    "Une erreur s'est produite lors de la suppression : " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

}
