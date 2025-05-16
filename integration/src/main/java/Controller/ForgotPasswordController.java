package Controller;

import Main.DatabaseConnection;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.apache.commons.codec.digest.DigestUtils;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

public class ForgotPasswordController {

    // Twilio credentials (remplace par tes propres identifiants)

    private static final String TWILIO_NUMBER = "+16208779051";

    @FXML
    private AnchorPane pwdcheck;

    @FXML
    private TextField numField;

    @FXML
    private TextField codefield;

    @FXML
    private TextField newpasswordfield;

    @FXML
    private Button CodeButton1;

    @FXML
    private Button resetbutton;

    @FXML
    private Label numcheck;

    @FXML
    private Label codecheck;

    @FXML
    private Label passcheck;

    private String codeFromSMS;

    @FXML
    void showcode(ActionEvent event) throws SQLException {
        String num = numField.getText().trim();

        if (num.isEmpty()) {
            numcheck.setText("Veuillez entrer votre numéro de téléphone.");
            numcheck.setStyle("-fx-text-fill: red");
            numcheck.setVisible(true);
            return;
        }

        if (isPhoneNumberAvailable(num)) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText("Vérifiez votre téléphone pour le code.");
            alert.showAndWait();

            Random random = new Random();
            int code = random.nextInt(10000);
            codeFromSMS = String.format("%04d", code);

            codefield.setVisible(true);
            codecheck.setVisible(true);
            CodeButton1.setVisible(true);
            numcheck.setVisible(false);

            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
            Message message = Message.creator(
                            new PhoneNumber("+21620597684" ),
                            new PhoneNumber("+16208779051"),
                            "Bonjour, voici votre code de réinitialisation : " + codeFromSMS)
                    .create();
        } else {
            numcheck.setText("Numéro de téléphone invalide.");
            numcheck.setStyle("-fx-text-fill: red");
            numcheck.setVisible(true);
        }
    }

    private boolean isPhoneNumberAvailable(String phoneNumber) throws SQLException {
        String query = "SELECT * FROM user WHERE num_tel = ?";
        PreparedStatement preparedStatement = DatabaseConnection.getCnx().prepareStatement(query);
        preparedStatement.setString(1, phoneNumber);
        ResultSet resultSet = preparedStatement.executeQuery();
        return resultSet.next();
    }

    @FXML
    void setchangepassword(ActionEvent event) {
        String codeEntered = codefield.getText().trim();

        if (codeEntered.equals(codeFromSMS)) {
            codecheck.setText("Code correct.");
            codecheck.setStyle("-fx-text-fill: green");
            codecheck.setVisible(true);
            newpasswordfield.setVisible(true);
            resetbutton.setVisible(true);
        } else {
            codecheck.setText("Code incorrect.");
            codecheck.setStyle("-fx-text-fill: red");
            codecheck.setVisible(true);
        }
    }

    @FXML
    void changepassword(ActionEvent event) throws SQLException {
        String newPassword = newpasswordfield.getText().trim();

        if (newPassword.isEmpty()) {
            passcheck.setText("Veuillez entrer un nouveau mot de passe.");
            passcheck.setStyle("-fx-text-fill: red");
            passcheck.setVisible(true);
            return;
        }

        // Hashage avec BCrypt
        String hashedPwd = BCrypt.hashpw(newPassword, BCrypt.gensalt());

        // Mise à jour du mot de passe dans la base de données
        String query = "UPDATE user SET motdepasse = ? WHERE num_tel = ?";
        PreparedStatement preparedStatement = DatabaseConnection.getCnx().prepareStatement(query);
        preparedStatement.setString(1, hashedPwd);
        preparedStatement.setString(2, numField.getText().trim());
        preparedStatement.executeUpdate();

        // Affichage d'une alerte de succès
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText("Mot de passe mis à jour avec succès.");
        alert.showAndWait();

        // Fermeture de la fenêtre
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
    @FXML
    void backtologin(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
