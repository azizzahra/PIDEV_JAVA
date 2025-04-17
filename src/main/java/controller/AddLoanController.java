package controller;

import entities.Loan;
import entities.Place;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import services.LoanService;

import java.sql.SQLException;

public class AddLoanController {
    @FXML private TextField priceField;
    @FXML private TextField ticketsLeftField;
    @FXML private TextField formationField;
    @FXML private TextField imageField;
    @FXML private ComboBox<Place> placeCombo;  // Add this line

    private LoanService loanService;
    private Runnable refreshCallback;
    private ObservableList<Place> placeData;

    public void setLoanService(LoanService loanService) {
        this.loanService = loanService;
    }

    public void setRefreshCallback(Runnable refreshCallback) {
        this.refreshCallback = refreshCallback;
    }

    // Add this method to initialize place data
    public void setPlaceData(ObservableList<Place> places) {
        this.placeData = places;
        placeCombo.setItems(placeData);

        // Configure how places are displayed in the ComboBox
        placeCombo.setConverter(new StringConverter<Place>() {
            @Override
            public String toString(Place place) {
                return place != null ? place.getName() : "";
            }

            @Override
            public Place fromString(String string) {
                return placeData.stream()
                        .filter(p -> p.getName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });
    }

    @FXML
    private void handleSave() {
        try {
            // Validate place selection
            Place selectedPlace = placeCombo.getValue();
            if (selectedPlace == null) {
                showAlert("Error", "Please select a place");
                return;
            }

            Loan newLoan = new Loan(
                    Double.parseDouble(priceField.getText()),
                    Integer.parseInt(ticketsLeftField.getText()),
                    formationField.getText(),
                    imageField.getText(),
                    selectedPlace  // Use the selected place
            );

            loanService.ajouter(newLoan);
            refreshCallback.run();
            closeWindow();
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter valid numbers for price and tickets left");
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to add loan: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        priceField.getScene().getWindow().hide();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}