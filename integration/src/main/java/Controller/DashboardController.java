package Controller;

import Main.test;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import services.WeatherService;
import model.user;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class DashboardController implements Initializable {

    @FXML
    private VBox content;

    @FXML
    private ListView<String> tasksList;

    @FXML
    private TextArea notesArea;

    @FXML
    private Label weatherTemperatureLabel;

    @FXML
    private Label weatherForecastLabel;

    @FXML
    private Label weatherHumidityLabel;

    @FXML
    private Label weatherUpdateTimeLabel;

    @FXML
    private Label welcomeLabel; // New label for welcome message

    private WeatherService weatherService;
    private String userLocation = "Ferme principale"; // Nom personnalisé de la localisation

    // Coordonnées de la ferme (à personnaliser)
    private double farmLatitude = 36.8065; // Exemple: latitude de Tunis
    private double farmLongitude = 10.1815; // Exemple: longitude de Tunis

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialiser la liste des tâches
        initTasksList();

        // Initialiser le service météo
        weatherService = new WeatherService();

        // Charger les données météo au démarrage
        updateWeatherData();

        // Configurer une mise à jour périodique des données météo (toutes les 30 minutes)
        setupWeatherUpdates();
    }

    // Method to initialize user data passed from LoginController
    public void initData(user user) {
        if (welcomeLabel != null) {
            welcomeLabel.setText("Bienvenue, " + user.getNom() + " " + user.getPrenom() + " (Admin)");
        }
    }

    private void initTasksList() {
        tasksList.getItems().addAll(
                "Vérifier l'irrigation des champs nord",
                "Commander des semences pour la prochaine saison",
                "Maintenance du tracteur"
        );
    }

    private void updateWeatherData() {
        // Exécuter dans un thread séparé pour ne pas bloquer l'interface utilisateur
        new Thread(() -> {
            try {
                // Récupérer les données météo avec les coordonnées de la ferme
                Map<String, Object> weatherData = weatherService.getWeatherData(farmLatitude, farmLongitude);

                if (weatherData != null) {
                    // Mettre à jour l'interface sur le thread JavaFX
                    Platform.runLater(() -> {
                        double temperature = (double) weatherData.get("temperature");
                        String description = (String) weatherData.get("description");
                        int humidity = (int) weatherData.get("humidity");
                        String forecastText = (String) weatherData.get("forecastText");

                        // Format de l'heure de mise à jour
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                        String updateTime = sdf.format(new Date());

                        // Mettre à jour les labels
                        weatherTemperatureLabel.setText(description + ", " + Math.round(temperature) + "°C");
                        weatherHumidityLabel.setText("Humidité: " + humidity + "%");
                        weatherForecastLabel.setText(forecastText);
                        weatherUpdateTimeLabel.setText("Dernière mise à jour: " + updateTime);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    weatherTemperatureLabel.setText("Données météo non disponibles");
                    weatherForecastLabel.setText("Vérifiez votre connexion Internet");
                    weatherUpdateTimeLabel.setText("Erreur de mise à jour");
                });
            }
        }).start();
    }

    private void setupWeatherUpdates() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateWeatherData();
            }
        }, 30 * 60 * 1000, 30 * 60 * 1000); // Toutes les 30 minutes
    }

    @FXML
    public void handleForum(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Post/ForumD.fxml"));
            Parent root = loader.load();
            Scene currentScene = ((Node) event.getSource()).getScene();
            currentScene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur de chargement");
            alert.setContentText("Impossible de charger la vue du forum: " + e.getMessage());
            alert.showAndWait();
        }
    }
    @FXML
    public void navigateToDashboard(ActionEvent event) {
        try {
            // Utiliser directement le stage principal
            Stage mainStage = test.getPrimaryStage();

            // Charger la vue Home
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/marketplaceManagement/DashboardProduct.fxml"));
            Parent root = loader.load();

            // Remplacer le contenu du stage principal
            mainStage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement de la page d'accueil");
        }
    }

    @FXML
    public void handleLogout(ActionEvent actionEvent) {
        try {
            Parent loginView = FXMLLoader.load(getClass().getResource("/views/login.fxml"));
            Scene currentScene = ((Node) actionEvent.getSource()).getScene();
            currentScene.setRoot(loginView);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de se déconnecter: " + e.getMessage());
        }
    }

    @FXML
    public void handleCommunity(ActionEvent actionEvent) {
        try {
            Parent communityView = FXMLLoader.load(getClass().getResource("/views/community.fxml"));
            Scene currentScene = ((Node) actionEvent.getSource()).getScene();
            currentScene.setRoot(communityView);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la vue communauté: " + e.getMessage());
        }
    }

    @FXML
    public void refreshWeather(ActionEvent event) {
        weatherTemperatureLabel.setText("Chargement...");
        weatherForecastLabel.setText("Actualisation des données...");
        updateWeatherData();
    }

    @FXML
    public void saveNotes(ActionEvent event) {
        // Logique pour sauvegarder les notes
        showAlert("Information", "Notes sauvegardées avec succès!");
    }

    @FXML
    public void addTask(ActionEvent event) {
        // Simple implementation - in a real app, you'd show a dialog
        String newTask = "Nouvelle tâche " + (tasksList.getItems().size() + 1);
        tasksList.getItems().add(newTask);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void applyFadeTransition(Node pane) {
        FadeTransition fade = new FadeTransition(Duration.millis(400), pane);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }
}