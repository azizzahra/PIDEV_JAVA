package Controller;

import Main.test;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.user;
import services.UserService;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterController {

    @FXML private TextField firstName; // prénom
    @FXML private TextField lastName;  // nom
    @FXML private TextField email;
    @FXML private TextField phoneNumber;
    @FXML private ComboBox<String> roleU;
    @FXML private DatePicker dateBirthday;
    @FXML private PasswordField password;
    @FXML private PasswordField ConfirmPassword;
    @FXML private Button registerButton;

    @FXML private Label checkPassword;
    @FXML private Label checkConfirmPassword;
    @FXML private Label checkEmail;

    @FXML
    public void initialize() {
        roleU.getItems().addAll("client", "admin", "agriculteur");
    }

    @FXML
    private void register() {
        clearValidationMessages();

        if (!password.getText().equals(ConfirmPassword.getText())) {
            checkConfirmPassword.setText("Les mots de passe ne correspondent pas.");
            return;
        }

        if (!isValidEmail(email.getText())) {
            checkEmail.setText("L'email n'est pas valide.");
            return;
        }

        if (firstName.getText().isEmpty() || lastName.getText().isEmpty() || email.getText().isEmpty()
                || phoneNumber.getText().isEmpty() || roleU.getValue() == null || dateBirthday.getValue() == null) {
            showValidationError("Tous les champs doivent être remplis.");
            return;
        }

        String nom = lastName.getText();
        String prenom = firstName.getText();
        String hashedPassword = BCrypt.hashpw(password.getText(), BCrypt.gensalt());

        user u = new user(
                nom,
                prenom,
                roleU.getValue(),
                email.getText(),
                hashedPassword,
                "actif",
                phoneNumber.getText(),
                dateBirthday.getValue()
        );

        UserService us = new UserService();
        us.addUser(u);

        // ✉️ Envoi de l'email de confirmation avec try-catch
        try {
            String subject = "Confirmation d'inscription";
            String body = "Bonjour " + prenom + ",<br><br>"
                    + "Votre inscription a été réalisée avec succès.<br>"
                    + "Bienvenue dans notre plateforme !<br><br>"
                    + "Cordialement,<br>L'équipe.";
            EmailSender.sendEmail(email.getText(), subject, body);
            showSuccessAlert("Inscription réussie ! Un email de confirmation a été envoyé.");

            // Redirection vers la page de login après l'inscription réussie
            redirectToLogin();
        } catch (Exception e) {
            e.printStackTrace();
            showValidationError("Inscription réussie, mais l'envoi de l'email a échoué.");

            // On redirige quand même en cas d'échec de l'email
            redirectToLogin();
        }

        clearFields();
    }

    // Nouvelle méthode pour rediriger vers la page de login
    private void redirectToLogin() {
        try {
            Stage mainStage = test.getPrimaryStage();

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Login.fxml"));
            Parent loginRoot = fxmlLoader.load();

            mainStage.getScene().setRoot(loginRoot);
            mainStage.setTitle("Connexion");
        } catch (IOException e) {
            e.printStackTrace();
            showValidationError("Erreur lors de la redirection vers la page de connexion.");
        }
    }
    private void cancel() {
        try {
            Stage mainStage = test.getPrimaryStage();

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Login.fxml"));
            Parent loginRoot = fxmlLoader.load();

            mainStage.getScene().setRoot(loginRoot);
            mainStage.setTitle("Connexion");
        } catch (IOException e) {
            e.printStackTrace();
            showValidationError("Erreur lors de la redirection vers la page de connexion.");
        }
    }
    // Méthode pour afficher une alerte de succès
    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Méthode pour afficher un message d'erreur
    private void showValidationError(String message) {
        checkPassword.setText(message); // Vous pouvez l'utiliser comme message général
    }

    // Méthode pour vérifier la validité de l'email
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    // Méthode pour réinitialiser les messages d'erreur
    private void clearValidationMessages() {
        checkPassword.setText("");
        checkConfirmPassword.setText("");
        checkEmail.setText("");
    }

    // Méthode pour réinitialiser les champs après l'enregistrement
    private void clearFields() {
        firstName.clear();
        lastName.clear();
        email.clear();
        phoneNumber.clear();
        roleU.getSelectionModel().clearSelection();
        dateBirthday.setValue(null);
        password.clear();
        ConfirmPassword.clear();
    }
}