package Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import Main.DatabaseConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class LoginController {

    @FXML private TextField mailFieldLogin;
    @FXML private PasswordField passwordFieldLogin;
    @FXML private TextField tempPasswordField;
    @FXML private Label checkMailLogin;
    @FXML private Label CheckPasswordLogin;

    private boolean isPasswordVisible = false;

    @FXML
    private void initialize() {
        tempPasswordField.setVisible(false); // caché par défaut
        tempPasswordField.managedProperty().bind(tempPasswordField.visibleProperty());
        passwordFieldLogin.textProperty().bindBidirectional(tempPasswordField.textProperty());
    }

    @FXML
    private void eyemdp() {
        isPasswordVisible = !isPasswordVisible;

        if (isPasswordVisible) {
            tempPasswordField.setText(passwordFieldLogin.getText()); // Copier le mot de passe
            tempPasswordField.setVisible(true);
            passwordFieldLogin.setVisible(false);
        } else {
            passwordFieldLogin.setText(tempPasswordField.getText()); // Copier retour
            passwordFieldLogin.setVisible(true);
            tempPasswordField.setVisible(false);
        }
    }
    @FXML
    private void login() {
        String email = mailFieldLogin.getText();
        String password = passwordFieldLogin.getText();

        if (email.isEmpty() || password.isEmpty()) {
            checkMailLogin.setText("Veuillez remplir tous les champs.");
            return;
        }

        try (Connection conn = DatabaseConnection.getInstance().getCnx()) {
            String sql = "SELECT * FROM user WHERE mail = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String hashedPassword = rs.getString("motdepasse");
                if (BCrypt.checkpw(password, hashedPassword)) {
                    // Connexion réussie
                    checkMailLogin.setText("Connexion réussie !");

                    // Récupérer les données de l'utilisateur
                    int id = rs.getInt("id");
                    String nom = rs.getString("nom");
                    String prenom = rs.getString("prenom");
                    String role = rs.getString("role");
                    String mail = rs.getString("mail");
                    String num_tel = rs.getString("num_tel");
                    LocalDate birthDate = rs.getDate("birth_Date").toLocalDate();

                    // Passer les données à la page de profil
                    showProfile(id, nom, prenom, role, mail, num_tel, birthDate);
                } else {
                    CheckPasswordLogin.setText("Mot de passe incorrect !");
                }
            } else {
                checkMailLogin.setText("Aucun utilisateur trouvé avec cet email.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            checkMailLogin.setText("Erreur de connexion.");
        }
    }

    private void showProfile(int id, String nom, String prenom, String role, String mail, String num_tel, LocalDate birthDate) {
        try {
            // Charger la page de profil
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ProfileUser.fxml"));
            Parent profileRoot = fxmlLoader.load();

            // Passer les données au contrôleur de la page de profil
            UserController profileController = fxmlLoader.getController();
            profileController.setUserData(id, nom, prenom, role, mail, num_tel, birthDate);

            Stage stage = (Stage) mailFieldLogin.getScene().getWindow();
            Scene scene = new Scene(profileRoot);
            stage.setScene(scene);
            stage.setTitle("Profil Utilisateur");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void Register(MouseEvent mouseEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Register.fxml"));
            Parent registerRoot = fxmlLoader.load();

            Stage stage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
            Scene scene = new Scene(registerRoot);
            stage.setScene(scene);
            stage.setTitle("Inscription");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
