package Controller.Farm;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import services.WeatherService;
import services.WeatherServiceA;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class WeatherWidgetController {

    @FXML private VBox weatherWidget;
    @FXML private ImageView weatherIcon;
    @FXML private ImageView refreshIcon;
    @FXML private Label temperatureLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label humidityLabel;
    @FXML private Label windSpeedLabel;
    @FXML private Label lastUpdatedLabel;
    @FXML private StackPane loadingPane;
    @FXML private Label errorLabel;

    private WeatherServiceA weatherService;
    private String currentLocation;

    public void initialize() {
        weatherService = new WeatherServiceA();

        // Set refresh icon
        try {
            Image refreshImage = new Image(getClass().getResourceAsStream("/images/refresh-icon.png"));
            refreshIcon.setImage(refreshImage);
        } catch (Exception e) {
            System.err.println("Could not load refresh icon: " + e.getMessage());
        }
    }

    public void setLocation(String location) {
        this.currentLocation = location;
        fetchWeatherData();
    }

    @FXML
    private void refreshWeather() {
        fetchWeatherData();
    }

    private void fetchWeatherData() {
        if (currentLocation == null || currentLocation.isEmpty()) {
            setErrorState("No location provided");
            return;
        }

        setLoadingState(true);

        CompletableFuture.supplyAsync(() -> {
            try {
                return weatherService.getWeatherForLocation(currentLocation);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }).thenAccept(weatherData -> {
            Platform.runLater(() -> {
                if (weatherData != null) {
                    updateWeatherUI(weatherData);
                } else {
                    setErrorState("Failed to load weather data");
                }
                setLoadingState(false);
            });
        });
    }

    private void updateWeatherUI(Map<String, Object> weatherData) {
        try {
            // Update labels with weather data
            double temperature = (double) weatherData.get("temperature");
            temperatureLabel.setText(String.format("%.1f°C", temperature));

            String description = (String) weatherData.get("description");
            descriptionLabel.setText(description);

            double humidity = (double) weatherData.get("humidity");
            humidityLabel.setText(String.format("%.0f%%", humidity));

            double windSpeed = (double) weatherData.get("windSpeed");
            windSpeedLabel.setText(String.format("%.1f m/s", windSpeed));

            // Update weather icon
            String iconUrl = (String) weatherData.get("icon");
            weatherIcon.setImage(new Image(iconUrl));

            // Update last updated time
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
            lastUpdatedLabel.setText("Last updated: " + formatter.format(new Date()));

            // Show the weather widget, hide error
            weatherWidget.setVisible(true);
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        } catch (Exception e) {
            e.printStackTrace();
            setErrorState("Error displaying weather data");
        }
    }

    private void setLoadingState(boolean loading) {
        loadingPane.setVisible(loading);
        loadingPane.setManaged(loading);
    }

    private void setErrorState(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        setLoadingState(false);
    }
}