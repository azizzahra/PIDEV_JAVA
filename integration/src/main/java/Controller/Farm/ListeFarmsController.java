package Controller.Farm;

import Main.test;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.Farm;
import services.FarmService;
import services.SessionManager;
import services.WeatherServiceA;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ListeFarmsController {

    private FarmService farmService = new FarmService();
    private List<Farm> allFarms; // Farms for the current user
    private Map<String, Integer> locationCounts = new HashMap<>(); // Number of farms per location
    private Set<String> selectedLocations = new HashSet<>(); // Selected locations
    private String selectedSizeRange = "all"; // Selected size range
    private int currentUserId; // ID of the logged-in user

    private WeatherServiceA weatherService;
    private Map<String, Map<String, Object>> weatherCache = new HashMap<>();

    @FXML
    private TilePane farmContainer;

    @FXML
    private TextField searchField;

    @FXML
    private VBox locationFilters;

    @FXML
    private CheckBox sizeSmall;

    @FXML
    private CheckBox sizeMedium;

    @FXML
    private CheckBox sizeLarge;

    @FXML
    private Button applyFiltersBtn;

    @FXML
    private Button clearFiltersBtn;

    public void initialize() {
        // Get the current user's ID from the session
        model.user currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            showAlert("Erreur de session", "Aucun utilisateur connecté. Veuillez vous reconnecter.", Alert.AlertType.ERROR);
            return;
        }
        currentUserId = currentUser.getId();

        // Load farms for the current user
        try {
            allFarms = farmService.getFilteredFarms(currentUserId, null, 0, 0, null);
            calculateLocationCounts();
            updateLocationFilters();
            updateSizeCounts();
        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert("Erreur de chargement",
                    "Une erreur s'est produite lors du chargement des fermes.",
                    Alert.AlertType.ERROR);
        }

        setupSearchField();
        setupFilterHandlers();
        applyFilters();
        weatherService = new WeatherServiceA();

        // Add listener for container resizing
        farmContainer.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() > 0) {
                refreshFarmList();
            }
        });
    }

    private void calculateLocationCounts() {
        locationCounts.clear();
        for (Farm farm : allFarms) {
            String location = farm.getLocation();
            locationCounts.put(location, locationCounts.getOrDefault(location, 0) + 1);
        }
    }

    private void updateLocationFilters() {
        if (locationFilters != null) {
            locationFilters.getChildren().clear();

            for (Map.Entry<String, Integer> entry : locationCounts.entrySet()) {
                String location = entry.getKey();
                Integer count = entry.getValue();

                CheckBox locationCheck = new CheckBox(location);
                locationCheck.setSelected(selectedLocations.contains(location));
                locationCheck.setUserData(location);

                Label countLabel = new Label("(" + count + ")");
                countLabel.getStyleClass().add("filter-count");
                locationCheck.setGraphic(countLabel);

                locationCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal) {
                        selectedLocations.add(location);
                    } else {
                        selectedLocations.remove(location);
                    }
                });

                locationFilters.getChildren().add(locationCheck);
            }
        }
    }

    private void setupFilterHandlers() {
        if (sizeSmall != null) {
            sizeSmall.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    selectedSizeRange = "small";
                    sizeMedium.setSelected(false);
                    sizeLarge.setSelected(false);
                } else if (!sizeMedium.isSelected() && !sizeLarge.isSelected()) {
                    selectedSizeRange = "all";
                }
            });
        }

        if (sizeMedium != null) {
            sizeMedium.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    selectedSizeRange = "medium";
                    sizeSmall.setSelected(false);
                    sizeLarge.setSelected(false);
                } else if (!sizeSmall.isSelected() && !sizeLarge.isSelected()) {
                    selectedSizeRange = "all";
                }
            });
        }

        if (sizeLarge != null) {
            sizeLarge.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    selectedSizeRange = "large";
                    sizeSmall.setSelected(false);
                    sizeMedium.setSelected(false);
                } else if (!sizeSmall.isSelected() && !sizeMedium.isSelected()) {
                    selectedSizeRange = "all";
                }
            });
        }

        if (applyFiltersBtn != null) {
            applyFiltersBtn.setOnAction(event -> applyFilters());
        }

        if (clearFiltersBtn != null) {
            clearFiltersBtn.setOnAction(event -> clearFilters());
        }
    }

    private void applyFilters() {
        try {
            farmContainer.getChildren().clear();

            if (selectedLocations.isEmpty() && selectedSizeRange.equals("all") &&
                    (searchField.getText() == null || searchField.getText().isEmpty())) {
                displayFarms(allFarms);
                return;
            }

            List<String> locations = selectedLocations.isEmpty() ? null : new ArrayList<>(selectedLocations);
            int minSize = 0;
            int maxSize = 0;

            switch (selectedSizeRange) {
                case "small":
                    minSize = 0;
                    maxSize = 999;
                    break;
                case "medium":
                    minSize = 1000;
                    maxSize = 2000;
                    break;
                case "large":
                    minSize = 2001;
                    maxSize = 0;
                    break;
            }

            String searchKeyword = (searchField.getText() != null && !searchField.getText().isEmpty()) ?
                    searchField.getText() : null;

            List<Farm> filteredFarms = farmService.getFilteredFarms(currentUserId, locations, minSize, maxSize, searchKeyword);
            displayFarms(filteredFarms);

        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert("Erreur de filtrage",
                    "Une erreur s'est produite lors du filtrage des fermes.",
                    Alert.AlertType.ERROR);
        }
    }

    private void clearFilters() {
        selectedLocations.clear();
        selectedSizeRange = "all";

        if (locationFilters != null) {
            locationFilters.getChildren().forEach(node -> {
                if (node instanceof CheckBox) {
                    ((CheckBox) node).setSelected(false);
                }
            });
        }

        if (sizeSmall != null) sizeSmall.setSelected(false);
        if (sizeMedium != null) sizeMedium.setSelected(false);
        if (sizeLarge != null) sizeLarge.setSelected(false);

        searchField.clear();

        displayFarms(allFarms);
    }

    private void displayFilteredFarms(List<Farm> farms) {
        farmContainer.getChildren().clear();
        displayFarms(farms);
    }

    private void setupSearchField() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterFarms(newValue);
        });
    }

    private void filterFarms(String keyword) {
        farmContainer.getChildren().clear();

        if (keyword == null || keyword.isEmpty()) {
            applyFilters();
        } else {
            keyword = keyword.toLowerCase();
            List<Farm> filteredFarms = new ArrayList<>();

            for (Farm f : allFarms) {
                if (f.getName().toLowerCase().contains(keyword) ||
                        f.getLocation().toLowerCase().contains(keyword)) {
                    filteredFarms.add(f);
                }
            }

            if (!selectedLocations.isEmpty()) {
                filteredFarms = filteredFarms.stream()
                        .filter(farm -> selectedLocations.contains(farm.getLocation()))
                        .collect(Collectors.toList());
            }

            if (!selectedSizeRange.equals("all")) {
                filteredFarms = filteredFarms.stream()
                        .filter(farm -> {
                            int size = farm.getSize();
                            switch (selectedSizeRange) {
                                case "small":
                                    return size < 1000;
                                case "medium":
                                    return size >= 1000 && size <= 2000;
                                case "large":
                                    return size > 2000;
                                default:
                                    return true;
                            }
                        })
                        .collect(Collectors.toList());
            }

            displayFarms(filteredFarms);
        }
    }

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

    public void refreshFarmList() {
        Platform.runLater(() -> {
            try {
                allFarms = farmService.getFilteredFarms(currentUserId, null, 0, 0, null);
                calculateLocationCounts();
                updateLocationFilters();
                updateSizeCounts();
                applyFilters();
            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert("Erreur de chargement",
                        "Une erreur s'est produite lors du chargement des fermes.",
                        Alert.AlertType.ERROR);
            }
        });
    }

    private void updateSizeCounts() {
        int smallCount = 0;
        int mediumCount = 0;
        int largeCount = 0;

        for (Farm farm : allFarms) {
            int size = farm.getSize();
            if (size < 1000) {
                smallCount++;
            } else if (size >= 1000 && size <= 2000) {
                mediumCount++;
            } else {
                largeCount++;
            }
        }

        if (sizeSmall != null && sizeSmall.getGraphic() instanceof Label) {
            ((Label) sizeSmall.getGraphic()).setText("(" + smallCount + ")");
        }

        if (sizeMedium != null && sizeMedium.getGraphic() instanceof Label) {
            ((Label) sizeMedium.getGraphic()).setText("(" + mediumCount + ")");
        }

        if (sizeLarge != null && sizeLarge.getGraphic() instanceof Label) {
            ((Label) sizeLarge.getGraphic()).setText("(" + largeCount + ")");
        }
    }

    private BorderPane createFarmCard(Farm farm) {
        BorderPane card = new BorderPane();
        card.getStyleClass().add("farm-card");
        card.setPrefSize(400, 420);
        card.setMaxWidth(Double.MAX_VALUE);

        ImageView farmImage = createFarmImageView(farm);

        Label sizeTag = new Label(farm.getSize() + " KM²");
        sizeTag.getStyleClass().add("farm-card-tag");

        Label locationLabel = new Label(" " + farm.getLocation());
        locationLabel.getStyleClass().add("farm-location");

        VBox titleBox = new VBox(5);
        Label titleLabel = new Label(farm.getName());
        titleLabel.getStyleClass().add("farm-title");

        titleBox.getChildren().addAll(titleLabel, locationLabel);
        titleBox.setPadding(new Insets(15, 15, 5, 15));

        VBox contentBox = new VBox(10);
        Label descriptionLabel = new Label(farm.getDescription());
        descriptionLabel.getStyleClass().add("farm-description");
        descriptionLabel.setWrapText(true);
        contentBox.getChildren().add(descriptionLabel);
        contentBox.setPadding(new Insets(5, 15, 10, 15));

        HBox weatherBox = createWeatherBox(farm.getLocation());
        weatherBox.getStyleClass().add("farm-card-weather");
        weatherBox.setPadding(new Insets(5, 15, 10, 15));

        VBox fullContentBox = new VBox(0);
        fullContentBox.getChildren().addAll(titleBox, contentBox, weatherBox);

        StackPane imageContainer = new StackPane();
        imageContainer.getChildren().add(farmImage);

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

        ImageView weatherIcon = new ImageView();
        weatherIcon.setFitWidth(24);
        weatherIcon.setFitHeight(24);

        Label tempLabel = new Label("--°C");
        tempLabel.getStyleClass().add("weather-temp");

        Label conditionLabel = new Label("Loading...");
        conditionLabel.getStyleClass().add("weather-condition");

        weatherBox.getChildren().addAll(weatherIcon, tempLabel, conditionLabel);

        loadWeatherData(location, weatherIcon, tempLabel, conditionLabel);

        return weatherBox;
    }

    private void loadWeatherData(String location, ImageView iconView, Label tempLabel, Label conditionLabel) {
        String cacheKey = location.toLowerCase().trim();

        if (weatherCache.containsKey(cacheKey)) {
            Map<String, Object> cachedData = weatherCache.get(cacheKey);
            updateWeatherUI(cachedData, iconView, tempLabel, conditionLabel);
        } else {
            CompletableFuture.supplyAsync(() -> {
                try {
                    if (weatherService == null) {
                        weatherService = new WeatherServiceA();
                    }
                    return weatherService.getWeatherForLocation(location);
                } catch (Exception e) {
                    System.err.println("Error fetching weather: " + e.getMessage());
                    return null;
                }
            }).thenAccept(weatherData -> {
                if (weatherData != null) {
                    weatherCache.put(cacheKey, weatherData);
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

    private void updateWeatherUI(Map<String, Object> weatherData, ImageView iconView, Label tempLabel, Label conditionLabel) {
        try {
            double temperature = (double) weatherData.get("temperature");
            tempLabel.setText(String.format("%.1f°C", temperature));

            String description = (String) weatherData.get("description");
            conditionLabel.setText(description);

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
            if (farm.getImage() != null && !farm.getImage().isEmpty()) {
                String imagePath = "file:C:/xampp/htdocs/" + farm.getImage();
                Image image = new Image(imagePath, 400, 200, true, true);
                farmImage.setImage(image);
            } else {
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
            Stage mainStage = test.getPrimaryStage();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Farm/AddFarm.fxml"));
            Parent root = loader.load();

            mainStage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'ouverture du formulaire d'ajout", Alert.AlertType.ERROR);
        }
    }

    private void openFarmDetails(int farmId) {
        try {
            Stage mainStage = test.getPrimaryStage();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Farm/FarmDetails.fxml"));
            Parent root = loader.load();

            FarmDetailsController controller = loader.getController();
            controller.setFarmId(farmId);

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