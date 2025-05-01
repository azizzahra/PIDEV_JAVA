package controller;

import entities.Place;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import services.PlaceService;

import java.io.IOException;
import java.sql.SQLException;

public class PlaceController {

    @FXML private TableView<Place> placeTable;
    @FXML private TableColumn<Place, Integer> idCol;
    @FXML private TableColumn<Place, String> nameCol;
    @FXML private TableColumn<Place, Double> priceCol;
    @FXML private TableColumn<Place, Integer> capacityCol;
    @FXML private TableColumn<Place, String> imageCol; // Image column

    @FXML private TextField searchField;
    @FXML private TextField minPriceField;
    @FXML private TextField maxPriceField;

    private final PlaceService placeService = new PlaceService();
    private final ObservableList<Place> places = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configure table columns
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        capacityCol.setCellValueFactory(new PropertyValueFactory<>("capacity"));

        // Configure the image column to display an image
        imageCol.setCellValueFactory(new PropertyValueFactory<>("imagePath")); // Assuming imagePath is a String

        // Set the cell factory for the image column to use ImageView
        imageCol.setCellFactory(param -> new TableCell<Place, String>() {
            private final ImageView imageView = new ImageView();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Image image = new Image("file:" + item); // Load image from the path
                    imageView.setImage(image);
                    imageView.setFitHeight(50);  // Set size of the image
                    imageView.setFitWidth(50);
                    setGraphic(imageView); // Set the image in the cell
                }
            }
        });

        // Add action buttons column
        TableColumn<Place, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(createActionCellFactory());
        placeTable.getColumns().add(actionsCol);

        // Load data
        refreshTable();
    }

    private Callback<TableColumn<Place, Void>, TableCell<Place, Void>> createActionCellFactory() {
        return param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");

            {
                editButton.setOnAction(event -> {
                    Place place = getTableView().getItems().get(getIndex());
                    showUpdateDialog(place);
                });

                deleteButton.setOnAction(event -> {
                    Place place = getTableView().getItems().get(getIndex());
                    deletePlace(place);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(new HBox(5, editButton, deleteButton));
                }
            }
        };
    }

    @FXML
    private void handleAdd() {
        showAddDialog();
    }

    private void showAddDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/add-place.fxml"));
            Parent root = loader.load();

            AddPlaceController controller = loader.getController();
            controller.setPlaceService(placeService);
            controller.setRefreshCallback(this::refreshTable);

            Stage stage = new Stage();
            stage.setTitle("Add New Place");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to load add form: " + e.getMessage());
        }
    }

    private void showUpdateDialog(Place place) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/update-place.fxml"));
            Parent root = loader.load();

            UpdatePlaceController controller = loader.getController();
            controller.setPlace(place);
            controller.setPlaceService(placeService);
            controller.setRefreshCallback(this::refreshTable);

            Stage stage = new Stage();
            stage.setTitle("Update Place");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to load update form: " + e.getMessage());
        }
    }

    private void deletePlace(Place place) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Place");
        confirm.setContentText("Are you sure you want to delete " + place.getName() + "?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    placeService.supprimer(place);
                    refreshTable();
                } catch (SQLException e) {
                    showAlert("Error", "Failed to delete place: " + e.getMessage());
                }
            }
        });
    }

    private void refreshTable() {
        try {
            places.setAll(placeService.recuperer());
            placeTable.setItems(places);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load places: " + e.getMessage());
        }
    }

    @FXML
    private void handleFilter() {
        // Get the search text and filter values
        String keyword = searchField.getText().toLowerCase().trim();
        String minText = minPriceField.getText().trim();
        String maxText = maxPriceField.getText().trim();

        // Parse the min and max price, or use default values
        double min = minText.isEmpty() ? 0 : Double.parseDouble(minText);
        double max = maxText.isEmpty() ? Double.MAX_VALUE : Double.parseDouble(maxText);

        // Apply filter
        ObservableList<Place> filtered = places.filtered(place -> {
            boolean matchesName = place.getName().toLowerCase().contains(keyword);
            boolean inPriceRange = place.getPrice() >= min && place.getPrice() <= max;

            return matchesName && inPriceRange;
        });

        // Update the table with the filtered data
        placeTable.setItems(filtered);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void handleGoToLoans(ActionEvent actionEvent) {
        try {
            Parent loansRoot = FXMLLoader.load(
                    getClass().getResource("/loan_management.fxml")
            );
            Stage stage = (Stage) ((Node) actionEvent.getSource())
                    .getScene()
                    .getWindow();
            stage.setScene(new Scene(loansRoot));
            stage.setTitle("Loan Management");
        } catch (IOException e) {
            showAlert("Error", "Failed to load loans screen: " + e.getMessage());
        }
    }
}
