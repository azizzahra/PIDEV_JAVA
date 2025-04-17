package controller;

import entities.Place;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import services.PlaceService;
import java.sql.SQLException;

public class AddPlaceController {
    @FXML private TextField nameField;
    @FXML private TextField priceField;
    @FXML private TextField capacityField;
    @FXML private TextField imageField;

    private PlaceService placeService;
    private Runnable refreshCallback;

    public void setPlaceService(PlaceService placeService) {
        this.placeService = placeService;
    }

    public void setRefreshCallback(Runnable refreshCallback) {
        this.refreshCallback = refreshCallback;
    }

    @FXML
    private void handleSave() {
        try {
            Place newPlace = new Place(
                    nameField.getText(),
                    Double.parseDouble(priceField.getText()),
                    Integer.parseInt(capacityField.getText()),
                    imageField.getText()
            );
            placeService.ajouter(newPlace);
            refreshCallback.run();
            closeWindow();
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter valid numbers for price and capacity");
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to add place: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        nameField.getScene().getWindow().hide();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}