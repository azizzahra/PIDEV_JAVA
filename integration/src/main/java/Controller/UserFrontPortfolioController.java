package Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import model.Badge;
import model.user;
import services.BadgeService;
import services.SessionManager;
import Main.test;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class UserFrontPortfolioController {
    @FXML private Label userNameLabel;
    @FXML private FlowPane badgesContainer;

    private final BadgeService badgeService = new BadgeService();

    @FXML
    public void initialize() {
        user currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            userNameLabel.setText(currentUser.getNom() + " " + currentUser.getPrenom());
            loadBadges(currentUser.getId());
        }
    }

    private void loadBadges(int userId) {
        List<Badge> badges = badgeService.getBadgesByUserId(userId);
        badgesContainer.getChildren().clear();

        for (Badge badge : badges) {
            VBox badgeCard = createBadgeCard(badge);
            badgesContainer.getChildren().add(badgeCard);
        }
    }

    private VBox createBadgeCard(Badge badge) {
        VBox card = new VBox(15);
        card.setStyle(
                "-fx-background-color: #ffffff;" +
                        "-fx-border-color: #cccccc;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 15;" +
                        "-fx-background-radius: 15;" +
                        "-fx-padding: 30;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 15, 0, 0, 5);"
        );
        card.setPrefWidth(500);
        card.setPrefHeight(350);

        // Titre du certificat
        Label titleLabel = new Label("🏅 Certificat de Réussite");
        titleLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");
        titleLabel.setWrapText(true);
        titleLabel.setAlignment(Pos.CENTER);

        // Nom de l'utilisateur
        Label userName = new Label(SessionManager.getCurrentUser().getPrenom() + " " + SessionManager.getCurrentUser().getNom());
        userName.setFont(Font.font("Serif", FontWeight.BOLD, 22));
        userName.setStyle("-fx-text-fill: #34495e;");
        userName.setWrapText(true);
        userName.setAlignment(Pos.CENTER);

        // Description du badge
        Label descriptionLabel = new Label("A obtenu le badge : \"" + badge.getTitre() + "\"\n" + badge.getDescription());
        descriptionLabel.setFont(Font.font("Arial", 14));
        descriptionLabel.setStyle("-fx-text-fill: #555555;");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(440);
        descriptionLabel.setAlignment(Pos.CENTER);

        // Date d'obtention
        Label dateLabel = new Label("Délivré le : " +
                badge.getDateAttribution().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        dateLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 12));
        dateLabel.setStyle("-fx-text-fill: #777777;");
        dateLabel.setAlignment(Pos.CENTER);

        // Image du tampon
        ImageView stampImageView = new ImageView(new Image(getClass().getResourceAsStream("/stamp.png")));
        stampImageView.setFitWidth(80);
        stampImageView.setPreserveRatio(true);
        stampImageView.setSmooth(true);
        stampImageView.setCache(true);

        // Signature ou tampon textuel
        // Signature ou tampon textuel
        Label stampLabel = new Label("L'Équipe Agrosphere ");
        stampLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 14px;");

        stampLabel.setAlignment(Pos.CENTER_RIGHT);

        HBox footer = new HBox(10, stampLabel, stampImageView);
        footer.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(titleLabel, userName, descriptionLabel, dateLabel, footer);
        return card;
    }

    @FXML
    private void goBack() {
        try {
            Stage mainStage = test.getPrimaryStage();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
            Parent root = loader.load();

            mainStage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
