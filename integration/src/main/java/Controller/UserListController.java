package Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.user;
import services.UserService;
import Main.test;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class UserListController {

    @FXML private TableView<user> userTable;
    @FXML private TableColumn<user, Integer> colId;
    @FXML private TableColumn<user, String> colNom;
    @FXML private TableColumn<user, String> colPrenom;
    @FXML private TableColumn<user, String> colEmail;
    @FXML private TableColumn<user, String> colRole;
    @FXML private TableColumn<user, String> colTel;
    @FXML private TableColumn<user, String> colStatus;
    @FXML private TableColumn<user, LocalDate> colBirth;
    @FXML private TableColumn<user, Void> colAction;
    @FXML
    private TextField searchField;
    @FXML private Button refreshButton;

    private final UserService userService = new UserService();
    private List<user> allUsers;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupActionColumn();
        loadUsers();

        // Initialiser la liste complète des utilisateurs
        allUsers = userService.getAllUsers();

        // Configurer le tri pour la colonne de date de naissance
        colBirth.setSortType(TableColumn.SortType.DESCENDING);
        colBirth.setSortable(true);

        // Configurer le tri pour la colonne de rôle
        colRole.setSortable(true);

        // Ajouter un écouteur sur le champ de recherche pour la recherche en temps réel
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                userTable.getItems().setAll(allUsers);
            } else {
                searchUsers();
            }
        });

        // Ajouter des boutons de tri
        setupSortButtons();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("mail"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colTel.setCellValueFactory(new PropertyValueFactory<>("num_tel"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colBirth.setCellValueFactory(new PropertyValueFactory<>("birth_Date"));
    }

    private void setupActionColumn() {
        colAction.setCellFactory(param -> new TableCell<user, Void>() {
            private final Button statusButton = new Button();
            private final Button addBadgeButton = new Button("Ajouter Badge");
            private final Button badgesButton = new Button("Voir Badges");
            private final HBox buttonsContainer = new HBox(5);

            {
                buttonsContainer.getChildren().addAll(statusButton, addBadgeButton, badgesButton);
                addBadgeButton.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-background-radius: 5;");
                badgesButton.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-background-radius: 5;");

                addBadgeButton.setOnAction(event -> {
                    user user = getTableView().getItems().get(getIndex());
                    openAddBadgeWindow(user);
                });

                badgesButton.setOnAction(event -> {
                    user user = getTableView().getItems().get(getIndex());
                    showUserBadges(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    user user = getTableView().getItems().get(getIndex());
                    statusButton.setText(user.getStatus().equals("actif") ? "Désactiver" : "Activer");
                    statusButton.setStyle(user.getStatus().equals("actif")
                        ? "-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-background-radius: 5;"
                        : "-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-background-radius: 5;");
                    statusButton.setOnAction(event -> toggleUserStatus(user));
                    setGraphic(buttonsContainer);
                }
            }
        });
    }

    @FXML
    private void searchUsers() {
        String searchText = searchField.getText().toLowerCase();
        if (searchText.isEmpty()) {
            userTable.getItems().setAll(allUsers);
            return;
        }

        List<user> filteredUsers = allUsers.stream()
                .filter(user ->
                        user.getNom().toLowerCase().contains(searchText) ||
                                user.getPrenom().toLowerCase().contains(searchText) ||
                                user.getMail().toLowerCase().contains(searchText) ||
                                user.getRole().toLowerCase().contains(searchText) ||
                                user.getNum_tel().toLowerCase().contains(searchText))
                .collect(Collectors.toList());

        userTable.getItems().setAll(filteredUsers);
    }

    @FXML
    private void resetSearch() {
        searchField.clear();
        userTable.getItems().setAll(allUsers);
    }

    @FXML
    public void refreshUsers() {
        allUsers = userService.getAllUsers();
        if (searchField.getText().isEmpty()) {
            userTable.getItems().setAll(allUsers);
        } else {
            searchUsers();
        }
    }

    @FXML
    private void loadUsers() {
        userTable.getItems().setAll(userService.getAllUsers());
    }

    private void setupSortButtons() {
        // Créer un menu pour les options de tri
        MenuButton sortMenu = new MenuButton("Trier par");
        sortMenu.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-background-radius: 5;");

        // Option de tri par date de naissance
        MenuItem sortByBirthDate = new MenuItem("Date de naissance");
        sortByBirthDate.setOnAction(e -> {
            colBirth.setSortType(TableColumn.SortType.DESCENDING);
            userTable.getSortOrder().setAll(colBirth);
        });

        // Option de tri par rôle
        MenuItem sortByRole = new MenuItem("Rôle");
        sortByRole.setOnAction(e -> {
            colRole.setSortType(TableColumn.SortType.ASCENDING);
            userTable.getSortOrder().setAll(colRole);
        });

        // Ajouter les options au menu
        sortMenu.getItems().addAll(sortByBirthDate, sortByRole);

        // Trouver la barre d'outils existante et ajouter le menu de tri
        HBox toolbar = (HBox) ((VBox) userTable.getParent()).getChildren().get(1);
        toolbar.getChildren().add(1, sortMenu); // Ajouter après le champ de recherche
    }

    private void openAddBadgeWindow(user user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/add_badge.fxml"));
            Parent root = loader.load();

            AddBadgeController controller = loader.getController();
            controller.setUserId(user.getId());

            Stage stage = new Stage();
            stage.setTitle("Ajouter un Badge");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre d'ajout de badge", Alert.AlertType.ERROR);
        }
    }

    private void toggleUserStatus(user user) {
        String newStatus = user.getStatus().equals("actif") ? "inactif" : "actif";
        userService.updateUserStatus(user.getId(), newStatus);
        loadUsers();
    }

    private void showUserBadges(user user) {
        try {
            Stage mainStage = test.getPrimaryStage();

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/user_badges.fxml"));
            Parent root = loader.load();

            UserBadgesController controller = loader.getController();
            if (controller == null) {
                throw new IOException("Le contrôleur n'a pas pu être chargé");
            }

            controller.setUserId(user.getId());

            mainStage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre des badges : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showUserPortfolio(int userId) {
        try {
            Stage mainStage = test.getPrimaryStage();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user_portfolio.fxml"));
            Parent root = loader.load();

            UserPortfolioController controller = loader.getController();
            controller.setUserId(userId);

            mainStage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le portfolio des badges", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
