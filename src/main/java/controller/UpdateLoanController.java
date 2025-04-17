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

public class UpdateLoanController {
    @FXML private TextField priceField;
    @FXML private TextField ticketsLeftField;
    @FXML private TextField formationField;
    @FXML private TextField imageField;
    @FXML private ComboBox<Place> placeCombo;

    private Loan loan;
    private LoanService loanService;
    private Runnable refreshCallback;
    private ObservableList<Place> placeData;

    public void setLoan(Loan loan) {
        this.loan = loan;
        populateFields();
    }

    public void setLoanService(LoanService loanService) {
        this.loanService = loanService;
    }

    public void setRefreshCallback(Runnable refreshCallback) {
        this.refreshCallback = refreshCallback;
    }

    public void setPlaceData(ObservableList<Place> places) {
        this.placeData = places;
        initializePlaceComboBox();
    }

    private void initializePlaceComboBox() {
        placeCombo.setItems(placeData);

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

        // Select current place if exists
        if (loan != null && loan.getPlace() != null) {
            placeCombo.getSelectionModel().select(loan.getPlace());
        }
    }

    private void populateFields() {
        priceField.setText(String.valueOf(loan.getTicketPrice()));
        ticketsLeftField.setText(String.valueOf(loan.getTicketsLeft()));
        formationField.setText(loan.getFormation());
        imageField.setText(loan.getImage());

        if (loan.getPlace() != null && placeData != null) {
            placeCombo.getSelectionModel().select(loan.getPlace());
        }
    }

    @FXML
    private void handleUpdate() {
        try {
            // Validate place selection
            Place selectedPlace = placeCombo.getValue();
            if (selectedPlace == null) {
                showAlert("Error", "Please select a place");
                return;
            }

            loan.setTicketPrice(Double.parseDouble(priceField.getText()));
            loan.setTicketsLeft(Integer.parseInt(ticketsLeftField.getText()));
            loan.setFormation(formationField.getText());
            loan.setImage(imageField.getText());
            loan.setPlace(selectedPlace);

            loanService.modifier(loan);
            refreshCallback.run();
            closeWindow();
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter valid numbers for price and tickets left");
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to update loan: " + e.getMessage());
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