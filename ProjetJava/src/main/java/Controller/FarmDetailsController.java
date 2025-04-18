package Controller;

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
import javafx.scene.text.TextAlignment;
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
    private FlowPane plantsContainer; // Changé de TilePane à FlowPane
    private PlanteService planteService = new PlanteService();



    public void setFarmId(int id) {
        this.farmId = id;
        loadFarmDetails();
    }


    private void loadFarmDetails() {
        try {
            // Récupérer les données de la ferme depuis la base de données
            currentFarm = farmService.getone(farmId);

            if (currentFarm != null) {
                // Mettre à jour le fil d'Ariane
                farmNameBreadcrumb.setText(currentFarm.getName());

                // Mettre à jour les informations principales
                farmName.setText(currentFarm.getName());
                farmSize.setText(currentFarm.getSize() + " KM²");
                farmLocation.setText(currentFarm.getLocation());
                farmCoordinates.setText("Lat: " + currentFarm.getLatitude() + ", Long: " + currentFarm.getLongitude());
                farmDescription.setText(currentFarm.getDescription());

                // Charger l'image
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
                // Construire le chemin complet vers l'image
                String fullPath = "src/resources/" + imagePath;
                File imageFile = new File(fullPath);

                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    farmImage.setImage(image);
                } else {
                    // Charger une image par défaut
                    Image defaultImage = new Image("file:src/resources/default-farm.jpg");
                    farmImage.setImage(defaultImage);
                }
            } else {
                // Charger une image par défaut
                Image defaultImage = new Image("file:src/resources/default-farm.jpg");
                farmImage.setImage(defaultImage);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image: " + e.getMessage());
            try {
                // Tenter de charger une image par défaut en cas d'erreur
                Image defaultImage = new Image("file:src/resources/default-farm.jpg");
                farmImage.setImage(defaultImage);
            } catch (Exception ex) {
                System.err.println("Impossible de charger l'image par défaut: " + ex.getMessage());
            }
        }
    }




    @FXML
    private void openUpdateForm() {
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
            showAlert("Erreur",
                    "Erreur lors de l'ouverture du formulaire de mise à jour: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }


    @FXML
    private void deleteFarm() {
        try {
            // Afficher une boîte de dialogue de confirmation
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Confirmation de suppression");
            confirmDialog.setHeaderText("Êtes-vous sûr de vouloir supprimer cette ferme ?");
            confirmDialog.setContentText("Cette action est irréversible.");

            // Si l'utilisateur confirme
            if (confirmDialog.showAndWait().get() == ButtonType.OK) {
                // Supprimer la ferme
                farmService.delete(currentFarm);

                // Afficher un message de succès
                showAlert("Succès", "Ferme supprimée avec succès", Alert.AlertType.INFORMATION);

                // Retourner à la liste des fermes
                returnToList();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur de base de données",
                    "Une erreur s'est produite lors de la suppression : " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }
    private void loadPlantsForFarm(int farmId) {
        try {
            List<plante> plants = planteService.getPlantesByFarmId(farmId);

            // Clear container
            plantsContainer.getChildren().clear();

            if (plants.isEmpty()) {
                Label emptyLabel = new Label("No plants found for this farm.");
                emptyLabel.setStyle("-fx-font-size: 16px;");
                plantsContainer.getChildren().add(emptyLabel);
            } else {
                // Add each plant as a card
                for (plante p : plants) {
                    BorderPane card = createPlantCard(p);
                    plantsContainer.getChildren().add(card);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load plants for this farm: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private BorderPane createPlantCard(plante plant) {
        BorderPane card = new BorderPane();
        card.getStyleClass().add("plant-card");
        card.setPrefSize(220, 310);
        card.setMaxSize(220, 310);

        // Create plant image
        ImageView plantImage = createPlantImageView(plant);

        // Create date label with day and month
        Label dateTag = new Label(formatDate(plant.getHarvestDate()));
        dateTag.getStyleClass().add("plant-card-tag");
        dateTag.setAlignment(Pos.CENTER);
        dateTag.setTextAlignment(TextAlignment.CENTER);

        // Create type label with icon
        HBox typeBox = new HBox(5);
        typeBox.setAlignment(Pos.CENTER_LEFT);
        Label typeIcon = new Label();
        if (plant.getType().equals("Vegetables")) {
            typeIcon.setText("🥕"); // Vegetable emoji
        } else if (plant.getType().equals("Fruits")) {
            typeIcon.setText("🍎"); // Fruit emoji
        } else {
            typeIcon.setText("🌸"); // Flower emoji
        }
        Label typeLabel = new Label(plant.getType());
        typeBox.getChildren().addAll(typeIcon, typeLabel);
        typeBox.getStyleClass().add("plant-type");

        // Create name and quantity
        VBox titleBox = new VBox(10);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label(plant.getName());
        nameLabel.getStyleClass().add("plant-title");

        HBox quantityBox = new HBox(5);
        quantityBox.setAlignment(Pos.CENTER_LEFT);
        Label quantityIcon = new Label("📊");
        Label quantityLabel = new Label(plant.getQuantity() + " Units");
        quantityBox.getChildren().addAll(quantityIcon, quantityLabel);
        quantityLabel.getStyleClass().add("plant-quantity");

        titleBox.getChildren().addAll(nameLabel, quantityBox);
        titleBox.setPadding(new Insets(10, 10, 5, 10));

        // Create button container
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);

        Button editButton = new Button("Edit");
        editButton.getStyleClass().addAll("card-button", "buttonn");
        editButton.setOnAction(e -> openUpdatePlantForm(plant.getId()));

        Button deleteButton = new Button("Delete");
        deleteButton.getStyleClass().addAll("card-button", "buttonn");
        deleteButton.setOnAction(e -> deletePlant(plant.getId()));

        buttonsBox.getChildren().addAll(editButton, deleteButton);
        buttonsBox.setPadding(new Insets(5, 10, 10, 10));

        // Stack the image and date tag
        StackPane imageContainer = new StackPane();
        imageContainer.getChildren().add(plantImage);
        StackPane.setAlignment(dateTag, Pos.TOP_RIGHT);
        StackPane.setMargin(dateTag, new Insets(10, 10, 0, 0));
        imageContainer.getChildren().add(dateTag);

        // Add type label to the bottom of the image
        StackPane.setAlignment(typeBox, Pos.BOTTOM_LEFT);
        StackPane.setMargin(typeBox, new Insets(0, 0, 10, 10));
        imageContainer.getChildren().add(typeBox);

        // Assemble the card
        card.setTop(imageContainer);
        card.setCenter(titleBox);
        card.setBottom(buttonsBox);

        return card;
    }

    private ImageView createPlantImageView(plante plant) {
        ImageView plantImage = new ImageView();
        try {
            if (plant.getImage() != null && !plant.getImage().isEmpty()) {
                String imagePath = "file:src/resources/" + plant.getImage();
                Image image = new Image(imagePath, 220, 150, true, true);
                plantImage.setImage(image);
            } else {
                // Default image if none available
                Image defaultImage = new Image("file:src/resources/default-plant.jpg", 220, 150, true, true);
                plantImage.setImage(defaultImage);
            }
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
            try {
                Image defaultImage = new Image("file:src/resources/default-plant.jpg", 220, 150, true, true);
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddPlante.fxml"));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UpdatePlante.fxml"));
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
    private void returnToList() {
        try {
            // Utiliser directement le stage principal
            Stage mainStage = mainPrincipal.getPrimaryStage();

            // Charger la vue de liste des fermes
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListeFarms.fxml"));
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