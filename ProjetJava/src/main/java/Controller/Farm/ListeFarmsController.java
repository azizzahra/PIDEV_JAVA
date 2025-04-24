package controller.Farm;

import Main.mainPrincipal;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Farm;
import services.FarmService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ListeFarmsController {

    private FarmService farmService = new FarmService();
    private List<Farm> allFarms; // Nouvelle variable pour stocker toutes les fermes


    @FXML
    private TilePane farmContainer;

    @FXML
    private TextField searchField;

    public void initialize() {
        // Chargement initial des fermes
        try {
            allFarms = farmService.getAll(); // Charge les fermes une seule fois
            displayFarms(allFarms); // Affiche toutes les fermes
        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert("Erreur de chargement",
                    "Une erreur s'est produite lors du chargement des fermes.",
                    Alert.AlertType.ERROR);
        }
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
        farmContainer.getChildren().clear();

        if (keyword == null || keyword.isEmpty()) {
            // Si aucun mot-clé, afficher toutes les fermes
            displayFarms(allFarms);
        } else {
            // Filtrer par mot-clé (nom, description ou lieu)
            keyword = keyword.toLowerCase();
            List<Farm> filteredFarms = new ArrayList<>();

            for (Farm f : allFarms) {
                if (f.getName().toLowerCase().contains(keyword) ||
                        f.getDescription().toLowerCase().contains(keyword) ||
                        f.getLocation().toLowerCase().contains(keyword)) {
                    filteredFarms.add(f);
                }
            }

            displayFarms(filteredFarms);
        }
    }
    // Méthode utilitaire pour afficher les fermes dans le conteneur
    private void displayFarms(List<Farm> farms) {
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


        // Mise en place des éléments dans la carte
        StackPane imageContainer = new StackPane();
        imageContainer.getChildren().add(farmImage);

        // Positionnement de l'étiquette de taille en haut à droite de l'image
        StackPane.setAlignment(sizeTag, Pos.TOP_RIGHT);
        StackPane.setMargin(sizeTag, new Insets(10, 10, 0, 0));
        imageContainer.getChildren().add(sizeTag);

        card.setTop(imageContainer);
        card.setCenter(new VBox(titleBox, contentBox));
        card.setOnMouseClicked(event -> openFarmDetails(farm.getId()));


        return card;
    }

    private ImageView createFarmImageView(Farm farm) {
        ImageView farmImage = new ImageView();
        try {
            // Vérifier si le chemin d'image existe
            if (farm.getImage() != null && !farm.getImage().isEmpty()) {
                String imagePath = "file:C:/xampp/htdocs/" + farm.getImage();
                Image image = new Image(imagePath, 400, 200, true, true);
                farmImage.setImage(image);
            } else {
                // Image par défaut si aucune image n'est disponible
                Image defaultImage = new Image("file:C:/xampp/htdocs/uploads/farm_image/default-farm.jpg", 400, 200, true, true);
                farmImage.setImage(defaultImage);
            }
        } catch (Exception e) {
            System.err.println("Erreur de chargement d'image: " + e.getMessage());
            try {
                Image defaultImage = new Image("file:C:/xampp/htdocs/uploads/farm_image/default-farm.jpg", 400, 200, true, true);
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

    @FXML
    private void OpenAddForm() {
        try {
            // Utiliser directement le stage principal
            Stage mainStage = mainPrincipal.getPrimaryStage();

            // Charger la vue d'ajout
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Farm/AddFarm.fxml"));
            Parent root = loader.load();

            // Remplacer le contenu du stage principal
            mainStage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'ouverture du formulaire d'ajout", Alert.AlertType.ERROR);
        }
    }



    // Nouvelle méthode pour ouvrir les détails d'une ferme
    private void openFarmDetails(int farmId) {
        try {
            // Utiliser directement le stage principal
            Stage mainStage = mainPrincipal.getPrimaryStage();

            // Charger la vue des détails
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Farm/FarmDetails.fxml"));
            Parent root = loader.load();

            // Configurer le contrôleur avec l'ID de la ferme
            FarmDetailsController controller = loader.getController();
            controller.setFarmId(farmId);

            // Remplacer le contenu du stage principal
            mainStage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'ouverture des détails de la ferme", Alert.AlertType.ERROR);
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
