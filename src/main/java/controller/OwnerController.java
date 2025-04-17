package controller;

import entities.Loan;
import entities.Owner;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import services.LoanService;
import services.LoanServiceImpl;
import services.OwnerService;

import java.sql.SQLException;

public class OwnerController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField numberField;
    @FXML private ComboBox<Loan> loanComboBox;

    @FXML private TableView<Owner> ownerTable;
    @FXML private TableColumn<Owner, String> nameCol;
    @FXML private TableColumn<Owner, String> emailCol;
    @FXML private TableColumn<Owner, String> numberCol;
    @FXML private TableColumn<Owner, String> formationCol;

    private final OwnerService ownerService = new OwnerService();
    private final LoanService loanService = new LoanServiceImpl(); // you should implement this if not already done


    @FXML
    public void initialize() throws SQLException {
        nameCol.setCellValueFactory(data -> data.getValue().nameProperty());
        emailCol.setCellValueFactory(data -> data.getValue().emailProperty());
        numberCol.setCellValueFactory(data -> data.getValue().numberProperty());
        formationCol.setCellValueFactory(data -> data.getValue().loanProperty().get().formationProperty());

        ownerTable.setItems(ownerService.getAllOwners());

        // Populate ComboBox with all available loans
        loanComboBox.setItems(FXCollections.observableArrayList(loanService.getAllLoans()));
    }

    @FXML
    private void handleAdd() {
        String name = nameField.getText();
        String email = emailField.getText();
        String number = numberField.getText();
        Loan selectedLoan = loanComboBox.getValue();

        if (selectedLoan != null) {
            Owner owner = new Owner(name, email, number, selectedLoan);
            ownerService.addOwner(owner);
            ownerTable.setItems(ownerService.getAllOwners()); // refresh list
            clearFields();
        } else {
            showAlert("Please select a formation.");
        }
    }

    @FXML
    private void handleEdit() {
        Owner selected = ownerTable.getSelectionModel().getSelectedItem();
        Loan selectedLoan = loanComboBox.getValue();

        if (selected != null && selectedLoan != null) {
            selected.setName(nameField.getText());
            selected.setEmail(emailField.getText());
            selected.setNumber(numberField.getText());
            selected.setLoan(selectedLoan);
            ownerService.updateOwner(selected);
            ownerTable.refresh();
        } else {
            showAlert("Please select a row and a formation to edit.");
        }
    }

    @FXML
    private void handleDelete() {
        Owner selected = ownerTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            ownerService.deleteOwner(selected);
            ownerTable.setItems(ownerService.getAllOwners());
        } else {
            showAlert("Please select an owner to delete.");
        }
    }

    private void clearFields() {
        nameField.clear();
        emailField.clear();
        numberField.clear();
        loanComboBox.getSelectionModel().clearSelection();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
