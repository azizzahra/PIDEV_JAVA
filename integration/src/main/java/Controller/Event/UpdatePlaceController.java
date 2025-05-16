package Controller.Event;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import model.Place;
import services.PlaceService;

import java.sql.SQLException;

public class UpdatePlaceController {
    @FXML private TextField nameField;
    @FXML private TextField priceField;
    @FXML private TextField capacityField;
    @FXML private TextField imageField;

    private Place place;
    private PlaceService placeService;
    private Runnable refreshCallback;

    public void setPlace(Place place) {
        this.place = place;
        populateFields();
    }

    public void setPlaceService(PlaceService placeService) {
        this.placeService = placeService;
    }

    public void setRefreshCallback(Runnable refreshCallback) {
        this.refreshCallback = refreshCallback;
    }

    private void populateFields() {
        nameField.setText(place.getName());
        priceField.setText(String.valueOf(place.getPrice()));
        capacityField.setText(String.valueOf(place.getCapacity()));
        imageField.setText(place.getImage());
    }

    @FXML
    private void handleUpdate() {
        try {
            place.setName(nameField.getText());
            place.setPrice(Double.parseDouble(priceField.getText()));
            place.setCapacity(Integer.parseInt(capacityField.getText()));
            place.setImage(imageField.getText());

            placeService.modifier(place);
            refreshCallback.run();
            closeWindow();
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter valid numbers for price and capacity");
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to update place: " + e.getMessage());
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