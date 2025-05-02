package controller.Farm;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import model.Farm;
import model.plante;
import services.FarmService;
import services.PlanteService;
import services.WeatherService;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CropOptimizationController {

    @FXML private Label yieldPredictionLabel;
    @FXML private TextArea recommendationTextArea;
    @FXML private Label errorLabel;
    private static final Logger LOGGER = Logger.getLogger(CropOptimizationController.class.getName());

    private FarmService farmService = new FarmService();
    private PlanteService planteService = new PlanteService();
    private WeatherService weatherService = new WeatherService();
    private int farmId;

    public void setFarmId(int id) {
        this.farmId = id;
        fetchOptimizationData();
    }

    @FXML
    public void initialize() {
        hideError();
    }

    private void fetchOptimizationData() {
        try {
            // Fetch farm data
            Farm farm = farmService.getone(farmId);
            if (farm == null) {
                LOGGER.warning("No farm found for ID: " + farmId);
                showError("Farm not found for ID: " + farmId);
                return;
            }

            // Fetch plant data
            List<plante> plants = planteService.getPlantesByFarmId(farmId);
            if (plants == null || plants.isEmpty()) {
                LOGGER.warning("No plants found for farm ID: " + farmId);
                showError("No plants found for this farm");
                return;
            }

            // Fetch weather data
            Map<String, Object> weatherData = weatherService.getWeatherForLocation(farm.getLocation());
            if (weatherData == null || !weatherData.containsKey("temperature") || !weatherData.containsKey("humidity")) {
                LOGGER.warning("Invalid weather data for location: " + farm.getLocation());
                showError("Failed to retrieve valid weather data");
                return;
            }

            // Run prediction in background
            CompletableFuture.supplyAsync(() -> {
                try {
                    // Prepare data for Python script
                    StringBuilder plantData = new StringBuilder();
                    for (plante p : plants) {
                        plantData.append(p.getName()).append(",")
                                .append(p.getType()).append(",")
                                .append(p.getQuantity()).append(";");
                    }

                    // Get script path - ensure it's using the correct path
                    String scriptPath = new File("crop_optimization.py").getAbsolutePath();
                    String pythonPath = new File("plant_disease_env\\Scripts\\python.exe").getAbsolutePath();

                    // Verify files exist before execution
                    File pythonFile = new File(pythonPath);
                    File scriptFile = new File(scriptPath);

                    if (!pythonFile.exists()) {
                        LOGGER.severe("Python executable not found at: " + pythonFile.getAbsolutePath());
                        return "ERROR:Python executable not found at: " + pythonFile.getAbsolutePath();
                    }

                    if (!scriptFile.exists()) {
                        LOGGER.severe("Python script not found at: " + scriptFile.getAbsolutePath());
                        return "ERROR:Python script not found at: " + scriptFile.getAbsolutePath();
                    }

                    // Log input data for debugging
                    String scriptArgs = String.format("Latitude: %s, Longitude: %s, Temperature: %s, Humidity: %s, Plants: %s",
                            farm.getLatitude(), farm.getLongitude(),
                            weatherData.get("temperature"), weatherData.get("humidity"),
                            plantData);
                    LOGGER.info("Calling Python script with args: " + scriptArgs);

                    // Call Python script for prediction
                    ProcessBuilder pb = new ProcessBuilder(
                            pythonPath,
                            scriptPath,
                            farm.getLatitude() + "," + farm.getLongitude(),
                            weatherData.get("temperature").toString(),
                            weatherData.get("humidity").toString(),
                            plantData.toString()
                    );

                    pb.redirectErrorStream(true);
                    Process process = pb.start();

                    // Read script output
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    StringBuilder output = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }

                    // Read error stream (for debugging)
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    StringBuilder errorOutput = new StringBuilder();
                    while ((line = errorReader.readLine()) != null) {
                        errorOutput.append(line).append("\n");
                    }
                    if (errorOutput.length() > 0) {
                        LOGGER.severe("Python script error output: " + errorOutput);
                    }

                    int exitCode = process.waitFor();
                    if (exitCode != 0) {
                        LOGGER.severe("Python script exited with code: " + exitCode);
                        return "ERROR:Python script failed with exit code " + exitCode + ": " + errorOutput;
                    }

                    return output.toString().trim();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error executing Python script", e);
                    return "ERROR:" + e.getMessage();
                }
            }).thenAccept(result -> {
                Platform.runLater(() -> {
                    if (result.startsWith("ERROR:")) {
                        showError(result.substring(6));
                    } else {
                        try {
                            // Parse result - use better parsing logic
                            String[] parts = result.split(",recommendation:");
                            if (parts.length >= 2) {
                                String yieldStr = parts[0].replace("yield:", "").trim();
                                String recommendation = parts[1].trim();

                                yieldPredictionLabel.setText("Predicted Yield: " + yieldStr + " units");
                                recommendationTextArea.setText(recommendation);
                                hideError();
                            } else {
                                LOGGER.warning("Invalid prediction format: " + result);
                                showError("Invalid prediction format: " + result);
                            }
                        } catch (Exception e) {
                            LOGGER.log(Level.WARNING, "Error parsing prediction result: " + result, e);
                            showError("Error parsing prediction: " + e.getMessage());
                        }
                    }
                });
            });

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error", e);
            showError("Failed to load farm data: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error in fetchOptimizationData", e);
            showError("Unexpected error: " + e.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
    }
}