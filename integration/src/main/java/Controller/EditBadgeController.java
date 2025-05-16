package Controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Badge;
import services.BadgeService;

import java.time.LocalDate;

public class EditBadgeController {
    @FXML private TextField titreField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private DatePicker datePicker;

    private Badge badge;
    private final BadgeService badgeService = new BadgeService();

    public void setBadge(Badge badge) {
        this.badge = badge;
        loadBadgeData();
    }

    private void loadBadgeData() {
        titreField.setText(badge.getTitre());
        descriptionField.setText(badge.getDescription());
        typeComboBox.getItems().addAll("BRONZE", "SILVER", "GOLD");
        typeComboBox.setValue(badge.getType());
        datePicker.setValue(badge.getDateAttribution());
        datePicker.setDisable(true);
    }

    @FXML
    private void updateBadge() {
        if (validateFields()) {
            badge.setTitre(titreField.getText());
            badge.setDescription(descriptionField.getText());
            badge.setType(typeComboBox.getValue());

            badgeService.updateBadge(badge);
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