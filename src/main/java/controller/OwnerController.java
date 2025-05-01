package controller;

import entities.Loan;
import entities.Owner;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import services.LoanService;
import services.LoanServiceImpl;
import services.OwnerService;
import services.CertificateService;  // Import CertificateService
import services.EmailVerificationService;

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
    private final LoanService loanService = new LoanServiceImpl();
    private final CertificateService certificateService = new CertificateService();
    private final EmailVerificationService emailVerificationService = EmailVerificationService.getInstance();

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

            try {
                // Generate certificate for the owner
                certificateService.generateCertificate(owner);
                showAlert("Certificate generated successfully!");

                // Send email confirmation after certificate generation
                String subject = "Formation Registration Confirmation";
                String content = "Hello " + owner.getName() + ",\n\n" +
                        "You have successfully joined the formation: " + selectedLoan.getFormation() + ".\n" +
                        "A certificate has been generated for your registration.\n\n" +
                        "Best regards,\nYour Training Team";

                // Send the email confirmation
                emailVerificationService.sendEmail(owner.getEmail(), subject, content);

                showAlert("Confirmation email sent to: " + owner.getEmail());

            } catch (Exception e) {
                showAlert("Error generating certificate: " + e.getMessage());
            }
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
