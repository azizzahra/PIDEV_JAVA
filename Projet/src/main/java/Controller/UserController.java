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

public class UserController {


    @FXML private TextField firstName;
    @FXML private TextField lastName;
    @FXML private TextField email;
    @FXML private TextField phoneNumber;
    @FXML private ComboBox<String> roleU;
    @FXML private DatePicker dateBirthday;
    @FXML private PasswordField password;
    @FXML private PasswordField confirmPassword;
    @FXML private Label checkPassword;
    @FXML private Label checkConfirmPassword;
    @FXML private Label checkEmail;


    private int userId;

    // Méthode pour recevoir les données de l'utilisateur existant
    public void setUserData(int id, String nom, String prenom, String role, String mail, String num_tel, LocalDate birthDate) {
        this.userId = id;
        firstName.setText(prenom);
        lastName.setText(nom);
        email.setText(mail);
        phoneNumber.setText(num_tel);
        roleU.setValue(role);
        dateBirthday.setValue(birthDate);

        // Réinitialiser les messages d'erreur
        checkPassword.setText("");
        checkConfirmPassword.setText("");
        checkEmail.setText("");
    }

    // Méthode déclenchée par le bouton Sauvegarder

    public void saveProfile(javafx.event.ActionEvent actionEvent) {
        // Récupérer les valeurs des champs
        String nom = lastName.getText();
        String prenom = firstName.getText();
        String mail = email.getText();
        String role = roleU.getValue();
        String num_tel = phoneNumber.getText();
        LocalDate birth = dateBirthday.getValue();
        String pwd = password.getText();
        String status = "actif"; // ou autre selon logique

        // Hachage du mot de passe
        String hashedPwd = BCrypt.hashpw(pwd, BCrypt.gensalt());

        // Création de l'objet user
        user updatedUser = new user();
        updatedUser.setNom(nom);
        updatedUser.setPrenom(prenom);
        updatedUser.setMail(mail);
        updatedUser.setRole(role);
        updatedUser.setNum_tel(num_tel);
        updatedUser.setBirth_Date(birth);
        updatedUser.setMotdepasse(hashedPwd);
        updatedUser.setStatus(status);

        // Appel du service
        UserService userService = new UserService();
        userService.updateUser(userId, updatedUser); // userId = ID de l'utilisateur à mettre à jour
    }
}
