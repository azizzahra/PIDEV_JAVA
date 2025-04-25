package controller.Farm;

import Main.mainPrincipal;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Farm;
import model.plante;
import services.FarmService;
import services.PlanteService;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

import javafx.application.Platform;
import javafx.stage.Modality;
import javafx.scene.control.ProgressIndicator;

import java.util.prefs.Preferences;


public class FarmDetailsController {

    @FXML
    private Label farmNameBreadcrumb;

    @FXML
    private ImageView farmImage;

    @FXML
    private Label farmName;

    @FXML
    private Label farmSize;

    @FXML
    private Label farmLocation;

    @FXML
    private Label farmCoordinates;

    @FXML
    private TextArea farmDescription;

    @FXML
    private TextField searchPlantField;
    @FXML
    private Label plantCount;
    @FXML
    private VBox categoriesContainer;
    @FXML
    private Label listOfPlantsLabel;

    @FXML
    private TextField imagePathField;
    @FXML
    private Button chooseFileButton;
    @FXML
    private Button detectButton;
    @FXML
    private VBox diseaseResultContainer;

    @FXML
    private Button prevPageButton;
    @FXML
    private Button nextPageButton;
    @FXML
    private HBox paginationButtons;
    private int totalPages;
    private int currentPage = 1;
    private final int CARDS_PER_PAGE = 6;
    private List<plante> currentFilteredPlants;

    private Preferences prefs = Preferences.userNodeForPackage(AddFarmController.class);

    private int farmId;
    private Farm currentFarm;
    private FarmService farmService = new FarmService();

    @FXML
    private FlowPane plantsContainer;
    private PlanteService planteService = new PlanteService();
    private List<plante> allPlants = new ArrayList<>();


    public void setFarmId(int id) {
        this.farmId = id;
        loadFarmDetails();
    }
    @FXML
    private void initialize() {
        // Configurer le listener pour le champ de recherche
        if (searchPlantField != null) {
            searchPlantField.textProperty().addListener((observable, oldValue, newValue) -> {
                currentPage = 1; // Reset to first page when searching
                filterPlants(newValue);
            });
        }
        if (listOfPlantsLabel != null) {
            listOfPlantsLabel.setOnMouseClicked(event -> {
                displayFilteredPlants(allPlants);  // Afficher toutes les plantes sans filtre
                if (searchPlantField != null) {
                    searchPlantField.clear();  // Vider le champ de recherche
                }
            });

            // Changer le curseur pour indiquer que c'est cliquable
            listOfPlantsLabel.setStyle("-fx-cursor: hand;");
        }
        if (prevPageButton != null) {
            prevPageButton.setOnAction(e -> {
                if (currentPage > 1) {
                    currentPage--;
                    updatePlantsDisplay();
                }
            });
        }
        if (nextPageButton != null) {
            nextPageButton.setOnAction(e -> {
                if (currentPage < totalPages) {
                    currentPage++;
                    updatePlantsDisplay();
                }
            });
        }
        if (chooseFileButton != null) {
            chooseFileButton.setOnAction(e -> chooseImageFile());
        }

        if (detectButton != null) {
            detectButton.setOnAction(e -> detectDisease());
        }
    }

    private void chooseImageFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Plant Image");
        // Get last directory from preferences
        String lastDirectoryPath = prefs.get("lastImageDirectory", null);
        if (lastDirectoryPath != null) {
            File lastDir = new File(lastDirectoryPath);
            if (lastDir.exists()) {
                fileChooser.setInitialDirectory(lastDir);
            }
        }
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(mainPrincipal.getPrimaryStage());

        if (selectedFile != null) {
            prefs.put("lastImageDirectory", selectedFile.getParent());
            imagePathField.setText(selectedFile.getAbsolutePath());
        }
    }

    private void detectDisease() {
        String imagePath = imagePathField.getText();

        if (imagePath == null || imagePath.isEmpty()) {
            showAlert("Error", "Please select an image first", Alert.AlertType.ERROR);
            return;
        }

        try {
            // Vérifier que le fichier existe
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                showAlert("Error", "Image file does not exist", Alert.AlertType.ERROR);
                return;
            }

            // Create and show loading dialog
            Stage loadingStage = createLoadingDialog(imageFile);
            loadingStage.show();

            // Run prediction in background thread
            new Thread(() -> {
                try {
                    // Appeler le script Python pour la prédiction
                    ProcessBuilder processBuilder = new ProcessBuilder("plant_disease_env\\\\Scripts\\\\python.exe", "predict.py", imagePath);
                    processBuilder.redirectErrorStream(true);

                    Process process = processBuilder.start();

                    // Lire la sortie du script Python
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    StringBuilder output = new StringBuilder();

                    while ((line = reader.readLine()) != null) {
                        output.append(line);
                    }

                    int exitCode = process.waitFor();

                    final String resultOutput = output.toString();

                    // Update UI on JavaFX thread
                    Platform.runLater(() -> {
                        // Close loading dialog
                        loadingStage.close();

                        if (exitCode != 0) {
                            showAlert("Error", "Error executing prediction script: " + resultOutput, Alert.AlertType.ERROR);
                            return;
                        }

                        // Traiter la sortie
                        String[] result = resultOutput.split(",");
                        if (result.length >= 2) {
                            String status = result[0].trim();
                            double accuracy = Double.parseDouble(result[1].trim());

                            // Show results dialog
                            showResultsDialog(imageFile, status, accuracy);
                        } else {
                            showAlert("Error", "Invalid output format from prediction script", Alert.AlertType.ERROR);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    // Update UI on JavaFX thread
                    Platform.runLater(() -> {
                        loadingStage.close();
                        showAlert("Error", "Error detecting disease: " + e.getMessage(), Alert.AlertType.ERROR);
                    });
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Error detecting disease: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private Stage createLoadingDialog(File imageFile) {
        // Create dialog stage
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Disease Detection");
        dialogStage.setResizable(false);

        // Create layout
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setMinWidth(450);
        root.setMinHeight(350);
        root.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1px;");

        // Add image
        ImageView plantImageView = new ImageView();
        try {
            Image image = new Image(imageFile.toURI().toString());
            plantImageView.setImage(image);
            plantImageView.setFitWidth(300);
            plantImageView.setFitHeight(200);
            plantImageView.setPreserveRatio(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Add loading indicator
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(50, 50);

        // Add title and message
        Label titleLabel = new Label("Analysis in Progress");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label messageLabel = new Label("Please wait while we analyze your plant image...");
        messageLabel.setStyle("-fx-font-size: 14px;");

        // Add all elements to layout
        root.getChildren().addAll(titleLabel, plantImageView, progressIndicator, messageLabel);

        // Set scene
        Scene scene = new Scene(root);
        dialogStage.setScene(scene);

        return dialogStage;
    }

    private void showResultsDialog(File imageFile, String status, double accuracy) {
        // Create dialog stage
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Disease Detection Results");
        dialogStage.setResizable(false);

        // Create layout
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setMinWidth(450);
        root.setMinHeight(400);
        root.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1px;");

        // Add title
        Label titleLabel = new Label("Analysis Result");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        // Add image
        ImageView plantImageView = new ImageView();
        try {
            Image image = new Image(imageFile.toURI().toString());
            plantImageView.setImage(image);
            plantImageView.setFitWidth(300);
            plantImageView.setFitHeight(200);
            plantImageView.setPreserveRatio(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Status box
        HBox statusBox = new HBox(10);
        statusBox.setAlignment(Pos.CENTER);

        Label statusLabel = new Label("Status:");
        statusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Label statusValue = new Label(status);
        if ("healthy".equalsIgnoreCase(status)) {
            statusValue.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold; -fx-font-size: 16px;");
        } else {
            statusValue.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 16px;");
        }

        statusBox.getChildren().addAll(statusLabel, statusValue);

        // Confidence box
        HBox confidenceBox = new HBox(10);
        confidenceBox.setAlignment(Pos.CENTER);

        Label confidenceLabel = new Label("Confidence:");
        confidenceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label confidenceValue = new Label(String.format("%.2f%%", accuracy));
        confidenceValue.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        confidenceBox.getChildren().addAll(confidenceLabel, confidenceValue);

        // Recommendation
        VBox recommendationBox = new VBox(5);
        recommendationBox.setAlignment(Pos.CENTER_LEFT);
        recommendationBox.setPadding(new Insets(10));
        recommendationBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 5px;");

        Label recommendationTitle = new Label("Recommendation:");
        recommendationTitle.setStyle("-fx-font-weight: bold;");

        Label recommendation;
        if ("healthy".equalsIgnoreCase(status)) {
            recommendation = new Label("Your plant appears to be healthy. Continue with your current care routine.");
            recommendation.setStyle("-fx-text-fill: #2ecc71;");
        } else {
            recommendation = new Label("Your plant may have a disease. Consider implementing appropriate treatment measures and monitoring its condition closely.");
            recommendation.setStyle("-fx-text-fill: #e74c3c;");
        }
        recommendation.setWrapText(true);

        recommendationBox.getChildren().addAll(recommendationTitle, recommendation);

        // Close button
        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 20;");
        closeButton.setOnAction(e -> dialogStage.close());

        // Add all elements to layout
        root.getChildren().addAll(titleLabel, plantImageView, statusBox, confidenceBox, recommendationBox, closeButton);

        // Set scene
        Scene scene = new Scene(root);
        dialogStage.setScene(scene);
        dialogStage.show();
    }


    private void updatePaginationControls(List<plante> plantsToDisplay) {
        totalPages = (int) Math.ceil((double) plantsToDisplay.size() / CARDS_PER_PAGE);

        // Ensure current page is valid
        if (currentPage > totalPages && totalPages > 0) {
            currentPage = totalPages;
        } else if (currentPage < 1 || totalPages == 0) {
            currentPage = 1;
        }

        // Update pagination UI
        paginationButtons.getChildren().clear();

        // Add previous button
        prevPageButton.setDisable(currentPage == 1 || totalPages == 0);
        paginationButtons.getChildren().add(prevPageButton);

        // Add page buttons
        int startPage = Math.max(1, currentPage - 2);
        int endPage = Math.min(totalPages, startPage + 4);

        for (int i = startPage; i <= endPage; i++) {
            Button pageButton = new Button(String.valueOf(i));
            pageButton.getStyleClass().add("pagination-button");
            if (i == currentPage) {
                pageButton.getStyleClass().add("active");
            }

            final int pageNum = i;
            pageButton.setOnAction(e -> {
                currentPage = pageNum;
                updatePlantsDisplay();
            });

            paginationButtons.getChildren().add(pageButton);
        }

        // Add next button
        nextPageButton.setDisable(currentPage == totalPages || totalPages == 0);
        paginationButtons.getChildren().add(nextPageButton);
    }
    private void updatePlantsDisplay() {
        if (currentFilteredPlants != null) {
            displayPagedPlants(currentFilteredPlants);
        } else if (allPlants != null) {
            displayPagedPlants(allPlants);
        }
    }

    private void loadFarmDetails() {
        try {
            currentFarm = farmService.getone(farmId);

            if (currentFarm != null) {
                farmNameBreadcrumb.setText(currentFarm.getName());

                farmName.setText(currentFarm.getName());
                farmSize.setText(currentFarm.getSize() + " KM²");
                farmLocation.setText(currentFarm.getLocation());
                farmCoordinates.setText("Lat: " + currentFarm.getLatitude() + ", Long: " + currentFarm.getLongitude());
                farmDescription.setText(currentFarm.getDescription());

                loadFarmImage(currentFarm.getImage());
                loadPlantsForFarm(farmId);

            } else {
                showAlert("Erreur", "Impossible de trouver la ferme avec l'ID: " + farmId, Alert.AlertType.ERROR);
                returnToList();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur de chargement",
                    "Une erreur s'est produite lors du chargement des détails de la ferme: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    private void loadFarmImage(String imagePath) {
        try {
            if (imagePath != null && !imagePath.isEmpty()) {
                String fullPath = "C:/xampp/htdocs/" + imagePath;
                File imageFile = new File(fullPath);

                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    farmImage.setImage(image);
                } else {
                    Image defaultImage = new Image("C:/xampp/htdocs/uploads/farm_image/default-farm.jpg");
                    farmImage.setImage(defaultImage);
                }
            } else {
                Image defaultImage = new Image("C:/xampp/htdocs/uploads/farm_image/default-farm.jpg");
                farmImage.setImage(defaultImage);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image: " + e.getMessage());
            try {
                Image defaultImage = new Image("C:/xampp/htdocs/uploads/farm_image/default-farm.jpg");
                farmImage.setImage(defaultImage);
            } catch (Exception ex) {
                System.err.println("Impossible de charger l'image par défaut: " + ex.getMessage());
            }
        }
    }

    private void loadPlantsForFarm(int farmId) {
        try {
            // Récupérer toutes les plantes et les stocker
            allPlants = planteService.getPlantesByFarmId(farmId);

            updatePlantCount();
            updateCategories();
            currentPage = 1;  // Start at first page
            displayPagedPlants(allPlants);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load plants for this farm: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    private void updateCategories() {
        if (categoriesContainer != null) {
            categoriesContainer.getChildren().clear();

            // Créer un Map pour compter les types de plantes
            Map<String, Integer> categoryCount = new HashMap<>();

            // Compter les plantes par type
            for (plante p : allPlants) {
                String type = p.getType();
                categoryCount.put(type, categoryCount.getOrDefault(type, 0) + 1);
            }

            // Créer les éléments d'interface pour chaque catégorie
            for (Map.Entry<String, Integer> entry : categoryCount.entrySet()) {
                HBox categoryItem = new HBox(5);
                categoryItem.setAlignment(Pos.CENTER_LEFT);
                categoryItem.getStyleClass().add("category-item");

                Label categoryName = new Label(entry.getKey());
                categoryName.getStyleClass().add("category-name");
                HBox.setHgrow(categoryName, Priority.ALWAYS);

                Label categoryCountLabel = new Label("(" + entry.getValue() + ")");
                categoryCountLabel.getStyleClass().add("category-count");

                categoryItem.getChildren().addAll(categoryName, categoryCountLabel);
                categoriesContainer.getChildren().add(categoryItem);

                // Ajouter un gestionnaire d'événements pour filtrer par catégorie lors du clic
                categoryItem.setOnMouseClicked(e -> filterPlantsByCategory(entry.getKey()));
            }
        }
    }
    private void filterPlantsByCategory(String category) {
        currentFilteredPlants = allPlants.stream()
                .filter(p -> p.getType().equals(category))
                .collect(java.util.stream.Collectors.toList());

        currentPage = 1;  // Reset to first page when filtering
        displayPagedPlants(currentFilteredPlants);
    }
    private void updatePlantCount() {
        if (plantCount != null) {
            plantCount.setText(String.valueOf(allPlants.size()));
        }
    }

    private void displayFilteredPlants(List<plante> plants) {
        currentFilteredPlants = plants;
        currentPage = 1;  // Reset to first page when displaying new list
        displayPagedPlants(plants);
    }
    private void displayPagedPlants(List<plante> plants) {
        // Clear the container
        plantsContainer.getChildren().clear();

        if (plants.isEmpty()) {
            Label emptyLabel = new Label("No plants found for this farm.");
            emptyLabel.setStyle("-fx-font-size: 16px;");
            plantsContainer.getChildren().add(emptyLabel);
            updatePaginationControls(plants);  // Update pagination with empty list
        } else {
            // Calculate start and end indices for current page
            int startIndex = (currentPage - 1) * CARDS_PER_PAGE;
            int endIndex = Math.min(startIndex + CARDS_PER_PAGE, plants.size());

            // Get plants for current page
            List<plante> pagedPlants = plants.subList(startIndex, endIndex);

            // Add each plant as a card
            for (plante p : pagedPlants) {
                BorderPane card = createPlantCard(p);
                plantsContainer.getChildren().add(card);
            }

            // Update pagination controls
            updatePaginationControls(plants);
        }

        // Add the "Add Plant" button if we're on the last page or there are few plants
        if (currentPage == totalPages || plants.size() <= CARDS_PER_PAGE) {
            addAddPlantButton();
        }
    }
    private void filterPlants(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            // If search field is empty, display all plants
            currentFilteredPlants = null;  // Reset filtered plants
            displayPagedPlants(allPlants);
        } else {
            // Convert to lowercase for case-insensitive search
            String searchLower = searchText.toLowerCase();

            // Filter plants whose name or type contains the search text
            currentFilteredPlants = allPlants.stream()
                    .filter(p -> p.getName().toLowerCase().contains(searchLower) ||
                            p.getType().toLowerCase().contains(searchLower))
                    .collect(java.util.stream.Collectors.toList());

            // Display filtered plants
            displayPagedPlants(currentFilteredPlants);
        }
    }

    private BorderPane createPlantCard(plante plant) {
        BorderPane card = new BorderPane();
        card.getStyleClass().add("plant-card");
        card.setPrefSize(250, 350);

        // Image de la plante
        ImageView plantImage = createPlantImageView(plant);

        // Zone de date et mois (en haut à droite)
        VBox dateBox = new VBox(2);
        dateBox.getStyleClass().add("plant-date-tag");
        dateBox.setAlignment(Pos.CENTER);

        // Extraire le jour et le mois de la date de récolte
        String[] dateParts = formatDate(plant.getHarvestDate()).split("\n");
        Label dayLabel = new Label(dateParts[0]);
        dayLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label monthLabel = new Label(dateParts.length > 1 ? dateParts[1] : "");
        monthLabel.getStyleClass().add("month-text");

        dateBox.getChildren().addAll(dayLabel, monthLabel);

        // Type de plante avec icône (en bas à gauche de l'image)
        HBox typeBox = new HBox(5);
        typeBox.setAlignment(Pos.CENTER_LEFT);
        Label typeIcon = new Label();
        // Ajouter l'icône en fonction du type
        if (plant.getType().equals("Vegetables")) {
            typeIcon.setText("🥕");
        } else if (plant.getType().equals("Fruits")) {
            typeIcon.setText("🍎");
        } else {
            typeIcon.setText("🌸");
        }

        Label typeLabel = new Label(plant.getType());
        typeBox.getChildren().addAll(typeIcon, typeLabel);
        typeBox.getStyleClass().add("plant-type");


        // Superposer image, date et type
        StackPane imageStack = new StackPane();
        imageStack.getChildren().add(plantImage);
        imageStack.setPrefHeight(180);

        // Positionnement de la date en haut à droite
        StackPane.setAlignment(dateBox, Pos.TOP_RIGHT);
        StackPane.setMargin(dateBox, new Insets(10, 10, 120, 160));
        imageStack.getChildren().add(dateBox);

        // Positionnement du type en bas à gauche
        StackPane.setAlignment(typeBox, Pos.BOTTOM_LEFT);
        StackPane.setMargin(typeBox, new Insets(160, 100, 10, 10));
        imageStack.getChildren().add(typeBox);

        // Zone du titre et quantité
        VBox infoBox = new VBox(5);
        infoBox.setPadding(new Insets(10));

        Label nameLabel = new Label(plant.getName());
        nameLabel.getStyleClass().add("plant-title");

        HBox quantityBox = new HBox(5);
        quantityBox.setAlignment(Pos.CENTER_LEFT);
        FontAwesomeIconView quantityIcon = new FontAwesomeIconView(FontAwesomeIcon.TREE);
        quantityIcon.getStyleClass().add("icon");
        Label quantityLabel = new Label(plant.getQuantity() + " Units");
        quantityLabel.getStyleClass().add("plant-quantity");
        quantityBox.getChildren().addAll(quantityIcon, quantityLabel);

        infoBox.setAlignment(Pos.CENTER);
        infoBox.getChildren().addAll(nameLabel, quantityBox);

        // Zone des boutons
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.setPadding(new Insets(0, 10, 10, 10));

        Button editButton = new Button("Edit");
        editButton.getStyleClass().addAll("card-button");
        FontAwesomeIconView editIcon = new FontAwesomeIconView(FontAwesomeIcon.EDIT);
        editButton.setGraphic(editIcon);
        editButton.setOnAction(e -> openUpdatePlantForm(plant.getId()));

        Button deleteButton = new Button("Delete");
        deleteButton.getStyleClass().addAll("card-button");
        FontAwesomeIconView deleteIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
        deleteButton.setGraphic(deleteIcon);
        deleteButton.setOnAction(e -> deletePlant(plant.getId()));

        buttonsBox.getChildren().addAll(editButton, deleteButton);

        // Assemblage final
        card.setTop(imageStack);
        card.setCenter(infoBox);
        card.setBottom(buttonsBox);
        card.setPadding(new Insets(0, 0, 10, 0));

        return card;
    }
    // Ajouter un bouton "Ajouter Plante" à la fin du conteneur de plantes
    private void addAddPlantButton() {
        VBox addPlantBox = new VBox(10);
        addPlantBox.getStyleClass().add("add-plant-container");
        addPlantBox.setAlignment(Pos.CENTER);
        addPlantBox.setPrefSize(200, 280);

        Label plusIcon = new Label("+");
        plusIcon.getStyleClass().add("add-plant-icon");

        Label addText = new Label("Add Plant");
        addText.getStyleClass().add("add-plant-text");

        addPlantBox.getChildren().addAll(plusIcon, addText);
        addPlantBox.setOnMouseClicked(e -> openAddPlantForm());

        plantsContainer.getChildren().add(addPlantBox);
    }

    private ImageView createPlantImageView(plante plant) {
        ImageView plantImage = new ImageView();
        try {
            if (plant.getImage() != null && !plant.getImage().isEmpty()) {
                String imagePath = "file:C:/xampp/htdocs/uploads/plant_image/" + plant.getImage();
                Image image = new Image(imagePath, 220, 150, true, true);
                plantImage.setImage(image);
            } else {
                // Default image if none available
                Image defaultImage = new Image("C:/xampp/htdocs/uploads/plant_image/default-plant.jpg", 220, 150, true, true);
                plantImage.setImage(defaultImage);
            }
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
            try {
                Image defaultImage = new Image("C:/xampp/htdocs/uploads/plant_image/default-plant.jpg", 220, 150, true, true);
                plantImage.setImage(defaultImage);
            } catch (Exception ex) {
                System.err.println("Unable to load default image: " + ex.getMessage());
            }
        }

        plantImage.setFitWidth(220);
        plantImage.setFitHeight(150);
        plantImage.setPreserveRatio(false);

        return plantImage;
    }

    private String formatDate(String dateString) {
        // Simple implementation - adjust based on your date format
        if (dateString == null || dateString.isEmpty()) {
            return "No date";
        }

        try {
            // Parse the date string
            LocalDate date = LocalDate.parse(dateString);

            // Format day and month
            int day = date.getDayOfMonth();
            String month = date.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

            return day + "\n" + month;
        } catch (Exception e) {
            return dateString;
        }
    }

    @FXML
    private void openAddPlantForm() {
        try {
            // Use the main stage
            Stage mainStage = mainPrincipal.getPrimaryStage();

            // Load the add plant view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Farm/AddPlante.fxml"));
            Parent root = loader.load();

            // Configure the controller with the farm ID
            AddPlanteController controller = loader.getController();
            controller.setFarmId(farmId);

            // Replace the content of the main stage
            mainStage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Error opening add plant form: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void openUpdatePlantForm(int planteId) {
        try {
            // Use the main stage
            Stage mainStage = mainPrincipal.getPrimaryStage();

            // Load the update plant view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Farm/UpdatePlante.fxml"));
            Parent root = loader.load();

            // Configure the controller with the plant ID and farm ID
            UpdatePlanteController controller = loader.getController();
            controller.setPlanteId(planteId);
            controller.setFarmId(farmId);

            // Replace the content of the main stage
            mainStage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Error opening update plant form: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void deletePlant(int planteId) {
        try {
            // Show confirmation dialog
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Confirm Deletion");
            confirmDialog.setHeaderText("Are you sure you want to delete this plant?");
            confirmDialog.setContentText("This action cannot be undone.");

            // If user confirms
            if (confirmDialog.showAndWait().get() == ButtonType.OK) {
                // Get the plant by ID
                plante plantToDelete = planteService.getone(planteId);

                if (plantToDelete != null) {
                    // Delete the plant
                    planteService.delete(plantToDelete);

                    // Show success message
                    showAlert("Success", "Plant deleted successfully", Alert.AlertType.INFORMATION);

                    // Refresh the plant list
                    loadPlantsForFarm(farmId);
                } else {
                    showAlert("Error", "Could not find plant to delete", Alert.AlertType.ERROR);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error",
                    "An error occurred while deleting the plant: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }
    @FXML
    private void openUpdateForm() {
        try {
            Stage mainStage = mainPrincipal.getPrimaryStage();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Farm/UpdateFarm.fxml"));
            Parent root = loader.load();

            UpdateFarmController controller = loader.getController();
            controller.setFarmId(farmId);

            mainStage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur",
                    "Erreur lors de l'ouverture du formulaire de mise à jour: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }


    @FXML
    private void deleteFarm() {
        try {
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Confirmation de suppression");
            confirmDialog.setHeaderText("Êtes-vous sûr de vouloir supprimer cette ferme ?");
            confirmDialog.setContentText("Cette action est irréversible.");

            if (confirmDialog.showAndWait().get() == ButtonType.OK) {
                farmService.delete(currentFarm);

                showAlert("Succès", "Ferme supprimée avec succès", Alert.AlertType.INFORMATION);

                returnToList();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur de base de données",
                    "Une erreur s'est produite lors de la suppression : " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void returnToList() {
        try {
            // Utiliser directement le stage principal
            Stage mainStage = mainPrincipal.getPrimaryStage();

            // Charger la vue de liste des fermes
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Farm/ListeFarms.fxml"));
            Parent root = loader.load();

            // Remplacer le contenu du stage principal
            mainStage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur de navigation",
                    "Une erreur s'est produite lors du retour à la liste des fermes: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    private void showAlert (String title, String content, Alert.AlertType type){
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

}