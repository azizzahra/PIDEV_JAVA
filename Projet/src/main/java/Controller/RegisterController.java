package Controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.user;
import services.UserService;
import org.mindrot.jbcrypt.BCrypt;

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

    @FXML private Label checkPassword;
    @FXML private Label checkConfirmPassword;
    @FXML private Label checkEmail;

    @FXML
    public void initialize() {
        roleU.getItems().addAll("Client", "Admin", "Autre");
    }

    @FXML
    private void register() {
        // Réinitialiser les messages d'erreur
        clearValidationMessages();

        // Vérification de la correspondance des mots de passe
        if (!password.getText().equals(ConfirmPassword.getText())) {
            checkConfirmPassword.setText("Les mots de passe ne correspondent pas.");
            return;
        }

        // Vérification de la validité de l'email
        if (!isValidEmail(email.getText())) {
            checkEmail.setText("L'email n'est pas valide.");
            return;
        }

        // Vérification des champs vides
        if (firstName.getText().isEmpty() || lastName.getText().isEmpty() || email.getText().isEmpty() || phoneNumber.getText().isEmpty() || roleU.getValue() == null || dateBirthday.getValue() == null) {
            showValidationError("Tous les champs doivent être remplis.");
            return;
        }

        // Création d'un utilisateur avec les informations
        String nom = lastName.getText();
        String prenom = firstName.getText();
        String hashedPassword = BCrypt.hashpw(password.getText(), BCrypt.gensalt());

        user u = new user(
                nom,
                prenom,
                roleU.getValue(),
                email.getText(),
                hashedPassword,
                "active",
                phoneNumber.getText(),
                dateBirthday.getValue()
        );

        // Ajouter l'utilisateur via le service
        UserService us = new UserService();
        us.addUser(u);

        // Réinitialiser les champs après enregistrement
        clearFields();
        showValidationError("Inscription réussie !");


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
