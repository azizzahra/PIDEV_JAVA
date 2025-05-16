package Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Badge;
import services.BadgeService;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class AddBadgeController implements Initializable {
    @FXML private TextField titreField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private DatePicker datePicker;

    private int userId;
    private final BadgeService badgeService = new BadgeService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialiser le ComboBox avec les types de badge
        typeComboBox.getItems().addAll("BRONZE", "SILVER", "GOLD");
        
        // Définir la date du jour par défaut
        datePicker.setValue(LocalDate.now());
        datePicker.setDisable(true); // Empêcher la modification de la date
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @FXML
    private void addBadge() {
        if (validateFields()) {
            Badge badge = new Badge(
                titreField.getText(),
                descriptionField.getText(),
                typeComboBox.getValue(),
                userId
            );

            badgeService.addBadge(badge);
            closeWindow();
        }
    }

    @FXML
    private void cancel() {
        closeWindow();
    }

    private boolean validateFields() {
        if (titreField.getText().isEmpty()) {
            showAlert("Erreur", "Le titre est requis", Alert.AlertType.ERROR);
            return false;
        }
        if (descriptionField.getText().isEmpty()) {
            showAlert("Erreur", "La description est requise", Alert.AlertType.ERROR);
            return false;
        }
        if (typeComboBox.getValue() == null) {
            showAlert("Erreur", "Le type de badge est requis", Alert.AlertType.ERROR);
            return false;
        }
        return true;
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) titreField.getScene().getWindow();
        stage.close();
    }
} 