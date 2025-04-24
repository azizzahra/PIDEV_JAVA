// Main.java
package com.devmasters.agrosphere;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/devmasters/agrosphere/marketplaceManagment/DashboardProduct.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1500, 750);
        scene.getStylesheets().add(getClass().getResource("/com/devmasters/agrosphere/marketplaceManagment/assets/css/modern.css").toExternalForm());

        primaryStage.setTitle("AgroSphere - Dashboard");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
