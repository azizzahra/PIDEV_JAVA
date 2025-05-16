package Controller;

import Main.test;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import model.Badge;
import model.user;
import services.BadgeService;
import services.UserService;

import java.io.IOException;

public class UserBadgesController {
    @FXML private Label userNameLabel;
    @FXML private TableView<Badge> badgesTable;
    @FXML private TableColumn<Badge, String> colTitre;
    @FXML private TableColumn<Badge, String> colDescription;
    @FXML private TableColumn<Badge, String> colType;
    @FXML private TableColumn<Badge, String> colDate;
    @FXML private TableColumn<Badge, Void> colAction;

    private int userId;
    private final BadgeService badgeService = new BadgeService();
    private final UserService userService = new UserService();

    public void setUserId(int userId) {
        this.userId = userId;
        loadUserData();
        loadBadges();
    }

    private void loadUserData() {
        user user = userService.getUserById(userId);
        if (user != null) {
            userNameLabel.setText("Utilisateur : " + user.getNom() + " " + user.getPrenom());
        }
    }

    @FXML
    public void initialize() {
        setupTableColumns();
    }

    private void setupTableColumns() {
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateAttribution"));

        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");
            private final HBox buttonsContainer = new HBox(5, editButton, deleteButton);

            {
                editButton.setOnAction(event -> {
                    Badge badge = getTableView().getItems().get(getIndex());
                    openEditBadgeWindow(badge);
                });

                deleteButton.setOnAction(event -> {
                    Badge badge = getTableView().getItems().get(getIndex());
                    deleteBadge(badge);
                });

                buttonsContainer.setStyle("-fx-alignment: CENTER;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonsContainer);
                }
            }
        });
    }

    private void loadBadges() {
        badgesTable.getItems().setAll(badgeService.getBadgesByUserId(userId));
    }

    @FXML
    private void openAddBadgeWindow() {
        try {
            Stage mainStage = test.getPrimaryStage();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/add_badge.fxml"));
            Parent root = loader.load();

            AddBadgeController controller = loader.getController();
            controller.setUserId(userId);

            mainStage.getScene().setRoot(root);

            // Rafraîchir la liste après l'ajout
            loadBadges();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre d'ajout de badge", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void openEditBadgeWindow(Badge badge) {
        try {
            Stage mainStage = test.getPrimaryStage();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edit_badge.fxml"));
            Parent root = loader.load();

            EditBadgeController controller = loader.getController();
            controller.setBadge(badge);

            mainStage.getScene().setRoot(root);

            // Rafraîchir la liste après la modification
            loadBadges();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre de modification", Alert.AlertType.ERROR);
        }
    }

    private void deleteBadge(Badge badge) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation");
        confirmDialog.setHeaderText(null);
        confirmDialog.setContentText("Êtes-vous sûr de vouloir supprimer ce badge ?");

        if (confirmDialog.showAndWait().get() == ButtonType.OK) {
            badgeService.deleteBadge(badge.getId());
            loadBadges();
            showAlert("Succès", "Badge supprimé avec succès", Alert.AlertType.INFORMATION);
        }
    }
    @FXML
    private void goBack() {
        Stage stage = (Stage) badgesTable.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 