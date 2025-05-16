package Controller;

import Main.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.mindrot.jbcrypt.BCrypt;

import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import model.user;
import services.UserService;
import services.SessionManager;

public class UserController {

    @FXML private TextField firstName;
    @FXML private TextField lastName;
    @FXML private TextField email;
    @FXML private TextField phoneNumber;
    @FXML private DatePicker dateBirthday;
    @FXML private PasswordField password;
    @FXML private PasswordField confirmPassword;
    @FXML private Label checkPassword;
    @FXML private Label checkConfirmPassword;
    @FXML private Label checkEmail;

    private int userId;
    private String userRole; // Stocker le rôle de l'utilisateur

    @FXML
    private void initialize() {
        // Check if there's a logged-in user
        user currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            // Auto-populate fields with current user data
            setUserData(currentUser);
        }
    }

    // Méthode pour recevoir les données de l'utilisateur existant
    public void setUserData(user user) {
        if (user != null) {
            firstName.setText(user.getPrenom());
            lastName.setText(user.getNom());
            email.setText(user.getMail());
            phoneNumber.setText(user.getNum_tel());
            dateBirthday.setValue(user.getBirth_Date());
            userId = user.getId();
            userRole = user.getRole(); // Stocker le rôle

            // Réinitialiser les messages d'erreur
            checkPassword.setText("");
            checkConfirmPassword.setText("");
            checkEmail.setText("");
        }
    }

    // Méthode déclenchée par le bouton Sauvegarder
    public void saveProfile(javafx.event.ActionEvent actionEvent) {
        // Récupérer les valeurs des champs
        String nom = lastName.getText();
        String prenom = firstName.getText();
        String mail = email.getText();
        String num_tel = phoneNumber.getText();
        LocalDate birth = dateBirthday.getValue();
        String pwd = password.getText();

        // Vérifier que tous les champs requis sont remplis
        if (nom.isEmpty() || prenom.isEmpty() || mail.isEmpty() || birth == null) {
            checkEmail.setText("Veuillez remplir tous les champs obligatoires.");
            return;
        }

        // Création de l'objet user
        user updatedUser = new user();
        updatedUser.setId(userId);
        updatedUser.setNom(nom);
        updatedUser.setPrenom(prenom);
        updatedUser.setMail(mail);
        updatedUser.setNum_tel(num_tel);
        updatedUser.setBirth_Date(birth);
        updatedUser.setRole(userRole); // Utiliser le rôle stocké

        // Ne mettre à jour le mot de passe que s'il a été modifié
        if (!pwd.isEmpty()) {
            String hashedPwd = BCrypt.hashpw(pwd, BCrypt.gensalt());
            updatedUser.setMotdepasse(hashedPwd);
        } else {
            // Keep the existing password from the current user
            user currentUser = SessionManager.getCurrentUser();
            if (currentUser != null) {
                updatedUser.setMotdepasse(currentUser.getMotdepasse());
            }
        }

        // Appel du service
        UserService userService = new UserService();
        userService.updateUser(userId, updatedUser);

        // Update the session with the new user data
        SessionManager.setCurrentUser(updatedUser);

        // Show confirmation message
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Profil mis à jour");
        alert.setHeaderText(null);
        alert.setContentText("Votre profil a été mis à jour avec succès.");
        alert.showAndWait();
    }
}