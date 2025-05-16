package Controller;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import Main.DatabaseConnection;
import model.user;
import services.UserService;
import services.SessionManager;
import Main.test; // Import de la classe test qui gère le stage principal

import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

import javafx.application.Platform;

import javax.imageio.ImageIO;

public class LoginController {

    @FXML private TextField mailFieldLogin;
    @FXML private PasswordField passwordFieldLogin;
    @FXML private TextField tempPasswordField;
    @FXML private Label checkMailLogin;
    @FXML private Label CheckPasswordLogin;
    @FXML private Button loginButton;
    @FXML
    private TextField captchaField;
    @FXML
    private ImageView captchaImageView;
    @FXML
    private Button refreshCaptchaButton;

    private DefaultKaptcha captchaProducer;
    private String generatedCaptcha;

    private boolean isPasswordVisible = false;

    @FXML
    private void initialize() {
        tempPasswordField.setVisible(false); // caché par défaut
        tempPasswordField.managedProperty().bind(tempPasswordField.visibleProperty());
        passwordFieldLogin.textProperty().bindBidirectional(tempPasswordField.textProperty());
        setupCaptcha();
        generateCaptcha();

        refreshCaptchaButton.setOnAction(event -> generateCaptcha());

        // Utiliser Platform.runLater pour s'assurer que l'interface est complètement chargée
        Platform.runLater(this::checkForExistingSession);
    }

    private void checkForExistingSession() {
        user existingUser = SessionManager.loadSession();
        if (existingUser != null) {
            // User is already logged in, redirect based on role
            navigateBasedOnRole(existingUser);
        }
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
    private void handleLogin() {
        String email = mailFieldLogin.getText();
        String password = passwordFieldLogin.getText();
        String captchaInput = captchaField.getText();

        // Vérifier que tous les champs sont remplis
        if (email.isEmpty() || password.isEmpty() || captchaInput.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs", Alert.AlertType.ERROR);
            return;
        }

        // Vérification du captcha
        if (!captchaInput.equals(generatedCaptcha)) {
            showAlert("Erreur", "Le code captcha est incorrect", Alert.AlertType.ERROR);
            // Générer un nouveau captcha après une tentative échouée
            generateCaptcha();
            captchaField.clear();
            return;
        }

        // Si le captcha est correct, continuer avec l'authentification
        user user = UserService.authenticate(email, password);
        if (user != null) {
            // Vérifier le statut du compte
            if ("inactif".equals(user.getStatus())) {
                showAlert("Compte désactivé", "Votre compte est actuellement désactivé. Veuillez contacter l'administrateur.", Alert.AlertType.WARNING);
                return;
            }

            // Navigate based on role
            navigateBasedOnRole(user);
        } else {
            showAlert("Erreur", "Email ou mot de passe incorrect", Alert.AlertType.ERROR);
            // Générer un nouveau captcha après une tentative échouée
            generateCaptcha();
            captchaField.clear();
        }
    }

    private void navigateBasedOnRole(user user) {
        try {
            Stage mainStage = test.getPrimaryStage();
            String fxmlPath;
            String title;

            // Determine the FXML file based on role
            if ("admin".equalsIgnoreCase(user.getRole())) {
                fxmlPath = "/Post/Dashboard.fxml";
                title = "Tableau de Bord";
            } else if ("agriculteur".equalsIgnoreCase(user.getRole())) {
                fxmlPath = "/Post/Dashboard.fxml";
                title = "Tableau de Bord";

            } else if ("client".equalsIgnoreCase(user.getRole())) {
                fxmlPath = "/Home.fxml";
                title = "Accueil";
            } else {
                showAlert("Erreur", "Rôle non reconnu. Contactez l'administrateur.", Alert.AlertType.ERROR);
                return;
            }

            // Load the appropriate FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Get the controller and pass data to it
            Object controller = loader.getController();
            if (controller instanceof HomeController) {
                ((HomeController) controller).initData(user);
            } else if (controller instanceof DashboardController) {
                ((DashboardController) controller).initData(user); // Assuming DashboardController exists
            }

            // Update the main stage with the new scene
            mainStage.getScene().setRoot(root);
            mainStage.setTitle(title);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void Register(MouseEvent mouseEvent) {
        try {
            Stage mainStage = test.getPrimaryStage();

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Register.fxml"));
            Parent registerRoot = fxmlLoader.load();

            mainStage.getScene().setRoot(registerRoot);
            mainStage.setTitle("Inscription");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement de la page d'inscription", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void gotoforgotpassword(MouseEvent mouseEvent) {
        try {
            Stage mainStage = test.getPrimaryStage();

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ForgotPassword.fxml"));
            Parent registerRoot = fxmlLoader.load();

            mainStage.getScene().setRoot(registerRoot);
            mainStage.setTitle("Mot de passe oublié");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement de la page de réinitialisation du mot de passe", Alert.AlertType.ERROR);
        }
    }

    private void setupCaptcha() {
        Properties properties = new Properties();
        properties.setProperty("kaptcha.textproducer.char.string", "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ123456789");
        properties.setProperty("kaptcha.textproducer.char.length", "6");
        properties.setProperty("kaptcha.textproducer.font.size", "40");
        properties.setProperty("kaptcha.border", "no");
        properties.setProperty("kaptcha.image.width", "150");
        properties.setProperty("kaptcha.image.height", "50");

        Config config = new Config(properties);
        captchaProducer = new DefaultKaptcha();
        captchaProducer.setConfig(config);
    }

    private void generateCaptcha() {
        generatedCaptcha = captchaProducer.createText();
        BufferedImage bufferedImage = captchaProducer.createImage(generatedCaptcha);
        Image captchaImage = convertBufferedImageToFXImage(bufferedImage);
        captchaImageView.setImage(captchaImage);
    }

    private Image convertBufferedImageToFXImage(BufferedImage bufferedImage) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", outputStream);
            return new Image(new ByteArrayInputStream(outputStream.toByteArray()));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void refreshCaptcha(javafx.event.ActionEvent actionEvent) {
        generateCaptcha();
        System.out.println("Captcha refreshed!");  // Debugging
    }
}