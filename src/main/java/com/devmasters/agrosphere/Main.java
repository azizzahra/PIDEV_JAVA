package com.devmasters.agrosphere;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

public class Main extends Application {

    // Main.java
    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Updated path with correct spelling
            String fxmlPath = "/com/devmasters/agrosphere/marketplaceManagement/DashboardProduct.fxml";
            URL resourceUrl = getClass().getResource(fxmlPath);

            if (resourceUrl == null) {
                System.err.println("ERROR: Could not find FXML file at: " + fxmlPath);
                throw new IOException("FXML resource not found");
            }

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent root = loader.load();

            Scene scene = new Scene(root, 1500, 750);

            // Updated CSS path
            String cssPath = "/com/devmasters/agrosphere/marketplaceManagement/assets/css/modern.css";
            URL cssUrl = getClass().getResource(cssPath);

            if (cssUrl == null) {
                System.err.println("WARNING: Could not find CSS file at: " + cssPath);
            } else {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            primaryStage.setTitle("AgroSphere - Dashboard");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            System.err.println("Application failed to start due to exception:");
            e.printStackTrace();
            throw e;
        }
    }
}