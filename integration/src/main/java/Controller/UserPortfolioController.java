package Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import model.Badge;
import services.BadgeService;
import services.UserService;
import Main.test;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class UserPortfolioController {
    @FXML
    private Label userNameLabel;
    @FXML
    private FlowPane badgesContainer;

    private int userId;
    private final BadgeService badgeService = new BadgeService();
    private final UserService userService = new UserService();

    public void setUserId(int userId) {
        this.userId = userId;
        loadUserInfo();
        loadBadges();
    }

    private void loadUserInfo() {
        var user = userService.getUserById(userId);
        if (user != null) {
            userNameLabel.setText(user.getNom() + " " + user.getPrenom());
        }
    }

    private void loadBadges() {
        List<Badge> badges = badgeService.getBadgesByUserId(userId);
        badgesContainer.getChildren().clear();

        for (Badge badge : badges) {
            VBox badgeCard = createBadgeCard(badge);
            badgesContainer.getChildren().add(badgeCard);
        }
    }

    private VBox createBadgeCard(Badge badge) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        card.setPrefWidth(300);
        card.setPrefHeight(200);

        // Titre du badge
        Label titleLabel = new Label(badge.getTitre());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.setStyle("-fx-text-fill: #2e7d32;");

        // Description
        Label descriptionLabel = new Label(badge.getDescription());
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(280);

        // Type de badge
        Label typeLabel = new Label("Type: " + badge.getType());
        typeLabel.setStyle("-fx-text-fill: #666666;");

        // Date d'obtention
        Label dateLabel = new Label("Obtenu le: " + 
            badge.getDateAttribution().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        dateLabel.setStyle("-fx-text-fill: #666666;");

        card.getChildren().addAll(titleLabel, descriptionLabel, typeLabel, dateLabel);
        return card;
    }

    @FXML
    private void goBack() {
        try {
            Stage mainStage = test.getPrimaryStage();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/users_list.fxml"));
            Parent root = loader.load();

            mainStage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 