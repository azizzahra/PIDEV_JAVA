package controller.Farm;

import Main.mainPrincipal;
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
import model.plante;
import services.FarmService;
import services.PlanteService;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;


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




    private int farmId;
    private Farm currentFarm;
    private FarmService farmService = new FarmService();

    @FXML
    private FlowPane plantsContainer;
    private PlanteService planteService = new PlanteService();



    public void setFarmId(int id) {
        this.farmId = id;
        loadFarmDetails();
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
            List<plante> plants = planteService.getPlantesByFarmId(farmId);

            // Effacer le conteneur
            plantsContainer.getChildren().clear();

            if (plants.isEmpty()) {
                Label emptyLabel = new Label("No plants found for this farm.");
                emptyLabel.setStyle("-fx-font-size: 16px;");
                plantsContainer.getChildren().add(emptyLabel);
            } else {
                // Ajouter chaque plante comme une carte
                for (plante p : plants) {
                    BorderPane card = createPlantCard(p);
                    plantsContainer.getChildren().add(card);
                }
            }

            // Ajouter le bouton "Ajouter plante" à la fin
            addAddPlantButton();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load plants for this farm: " + e.getMessage(), Alert.AlertType.ERROR);
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