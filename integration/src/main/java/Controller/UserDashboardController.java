package Controller;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import Main.test;
public class UserDashboardController {
    @FXML
    private Label userNameLabel;
    private int currentUserId;

    public void setUserId(int userId) {
        this.currentUserId = userId;
    }



    @FXML
    private void logout() {
        try {
            Stage mainStage = test.getPrimaryStage();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();

            mainStage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 