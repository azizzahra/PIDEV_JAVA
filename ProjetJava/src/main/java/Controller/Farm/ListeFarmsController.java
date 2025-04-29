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
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.Farm;
import services.FarmService;
import services.WeatherService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ListeFarmsController {

    private FarmService farmService = new FarmService();
    private List<Farm> allFarms; // Nouvelle variable pour stocker toutes les fermes

    private WeatherService weatherService;
    private Map<String, Map<String, Object>> weatherCache = new HashMap<>();


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
        weatherService = new WeatherService();

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
        card.setPrefSize(400, 420); // Increased height for weather info
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

        // Weather information section
        HBox weatherBox = createWeatherBox(farm.getLocation());
        weatherBox.getStyleClass().add("farm-card-weather");
        weatherBox.setPadding(new Insets(5, 15, 10, 15));

        // Container for both content and weather
        VBox fullContentBox = new VBox(0);
        fullContentBox.getChildren().addAll(titleBox, contentBox, weatherBox);

        // Mise en place des éléments dans la carte
        StackPane imageContainer = new StackPane();
        imageContainer.getChildren().add(farmImage);

        // Positionnement de l'étiquette de taille en haut à droite de l'image
        StackPane.setAlignment(sizeTag, Pos.TOP_RIGHT);
        StackPane.setMargin(sizeTag, new Insets(10, 10, 0, 0));
        imageContainer.getChildren().add(sizeTag);

        card.setTop(imageContainer);
        card.setCenter(fullContentBox);
        card.setOnMouseClicked(event -> openFarmDetails(farm.getId()));

        return card;
    }
    private HBox createWeatherBox(String location) {
        HBox weatherBox = new HBox(10);
        weatherBox.setAlignment(Pos.CENTER_LEFT);

        // Weather icon placeholder
        ImageView weatherIcon = new ImageView();
        weatherIcon.setFitWidth(24);
        weatherIcon.setFitHeight(24);

        // Labels for temperature and condition
        Label tempLabel = new Label("--°C");
        tempLabel.getStyleClass().add("weather-temp");

        Label conditionLabel = new Label("Loading...");
        conditionLabel.getStyleClass().add("weather-condition");

        // Add components to box
        weatherBox.getChildren().addAll(weatherIcon, tempLabel, conditionLabel);

        // Load weather data asynchronously
        loadWeatherData(location, weatherIcon, tempLabel, conditionLabel);

        return weatherBox;
    }

    /**
     * Loads weather data asynchronously for a farm card
     */
    private void loadWeatherData(String location, ImageView iconView, Label tempLabel, Label conditionLabel) {
        // Create temporary cache key based on location
        String cacheKey = location.toLowerCase().trim();

        // Check weather cache first
        if (weatherCache.containsKey(cacheKey)) {
            // Use cached data
            Map<String, Object> cachedData = weatherCache.get(cacheKey);
            updateWeatherUI(cachedData, iconView, tempLabel, conditionLabel);
        } else {
            // Load data in background
            CompletableFuture.supplyAsync(() -> {
                try {
                    // Create service instance if not exists
                    if (weatherService == null) {
                        weatherService = new WeatherService();
                    }
                    return weatherService.getWeatherForLocation(location);
                } catch (Exception e) {
                    System.err.println("Error fetching weather: " + e.getMessage());
                    return null;
                }
            }).thenAccept(weatherData -> {
                if (weatherData != null) {
                    // Cache the data
                    weatherCache.put(cacheKey, weatherData);

                    // Update UI on JavaFX thread
                    Platform.runLater(() -> {
                        updateWeatherUI(weatherData, iconView, tempLabel, conditionLabel);
                    });
                } else {
                    Platform.runLater(() -> {
                        tempLabel.setText("--°C");
                        conditionLabel.setText("Weather unavailable");
                    });
                }
            });
        }
    }

    /**
     * Updates weather UI elements with data
     */
    private void updateWeatherUI(Map<String, Object> weatherData, ImageView iconView, Label tempLabel, Label conditionLabel) {
        try {
            // Get temperature and format it
            double temperature = (double) weatherData.get("temperature");
            tempLabel.setText(String.format("%.1f°C", temperature));

            // Get description
            String description = (String) weatherData.get("description");
            conditionLabel.setText(description);

            // Load weather icon
            String iconUrl = (String) weatherData.get("icon");
            iconView.setImage(new Image(iconUrl));
        } catch (Exception e) {
            System.err.println("Error updating weather UI: " + e.getMessage());
            tempLabel.setText("--°C");
            conditionLabel.setText("Weather error");
        }
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
