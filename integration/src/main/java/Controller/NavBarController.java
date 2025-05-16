package Controller;

import Main.test;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.user;
import services.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class NavBarController implements Initializable {

    @FXML
    private HBox navbarRoot;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // S'assurer que la navbar maintient son propre style
        String cssPath = getClass().getResource("/navbar.css").toExternalForm();
        navbarRoot.getStylesheets().add(cssPath);
    }
    // Méthode pour initialiser les données de l'interface avec l'utilisateur
    public void initData(user user) {
        // Mettre à jour les éléments de l'interface avec les informations de l'utilisateur
        // Par exemple: userNameLabel.setText(user.getPrenom() + " " + user.getNom());
    }
    @FXML
    private void showProfile(ActionEvent event) {
        try {
            // Récupérer l'utilisateur connecté depuis le SessionManager
            user currentUser = SessionManager.getCurrentUser();

            if (currentUser == null) {
                showAlert("Erreur", "Aucun utilisateur connecté", Alert.AlertType.ERROR);
                return;
            }

            // Charger la vue appropriée selon le rôle
            String fxmlFile;
            if ("admin".equals(currentUser.getRole())) {
                fxmlFile = "/users_list.fxml";
            } else {
                fxmlFile = "/ProfileUser.fxml";
            }

            // Charger la vue
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            // Initialiser les données selon le type de controller
            Object controller = loader.getController();
            if (controller instanceof UserController) {
                ((UserController) controller).setUserData(currentUser);
            }

            // Get the window from the event source
            Node sourceNode = (Node) event.getSource();
            Stage stage = (Stage) sourceNode.getScene().getWindow();

            // Afficher la nouvelle scène
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement de la page: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (ClassCastException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur de type de controller: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    @Deprecated
    public static void setLoggedInUser(user user) {
        // Redirection vers le SessionManager
        SessionManager.setCurrentUser(user);
    }

    @Deprecated
    public static user getLoggedInUser() {
        // Redirection vers le SessionManager
        return SessionManager.getCurrentUser();
    }
    @FXML
    private void goToPortfolioFront(ActionEvent event) {
        try {
            Stage mainStage = test.getPrimaryStage();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user_front_portfolio.fxml"));
            Parent root = loader.load();

            // Si tu veux passer des infos au controller, tu peux le récupérer ici :
            // PortfolioFrontController controller = loader.getController();
            // controller.initData(...);

            mainStage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'ouverture du portfolio", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void navigateToHome(ActionEvent event) {
        try {
            // Utiliser directement le stage principal
            Stage mainStage = test.getPrimaryStage();

            // Charger la vue Home
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
            Parent root = loader.load();

            // Remplacer le contenu du stage principal
            mainStage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement de la page d'accueil", Alert.AlertType.ERROR);
        }
    }
    @FXML
    public void navigateToMarketPlace(ActionEvent event) {
        try {
            // Utiliser directement le stage principal
            Stage mainStage = test.getPrimaryStage();

            // Charger la vue Home
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/marketplaceManagement/product_marketplace.fxml"));
            Parent root = loader.load();

            // Remplacer le contenu du stage principal
            mainStage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement de la page d'accueil", Alert.AlertType.ERROR);
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
            showAlert("Erreur", "Erreur lors du chargement de la page d'accueil", Alert.AlertType.ERROR);
        }
    }
    @FXML
    public void navigateToEvent(ActionEvent event) {
        try {
            // Utiliser directement le stage principal
            Stage mainStage = test.getPrimaryStage();

            // Charger la vue Home
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Event/place.fxml"));
            Parent root = loader.load();

            // Remplacer le contenu du stage principal
            mainStage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement de la page d'accueil", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void navigateToForum(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Post/post.fxml"));
            VBox root = loader.load();  // Changé de ScrollPane à VBox pour correspondre au nouveau type racine

            // Utiliser directement le stage principal
            Stage mainStage = test.getPrimaryStage();

            // Remplacer le contenu du stage principal
            mainStage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la vue du forum", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void navigateToListFarm() {
        try {
            // Utiliser directement le stage principal
            Stage mainStage = test.getPrimaryStage();

            // Charger la vue de liste des fermes
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Farm/ListeFarms.fxml"));
            Parent root = loader.load();

            // Remplacer le contenu du stage principal
            mainStage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement de la liste des fermes", Alert.AlertType.ERROR);
        }
    }



    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    @FXML
    private void logout(ActionEvent event) {
        try {
            // Effacer la session
            SessionManager.clearSession();

            // Rediriger vers la page de connexion
            Stage mainStage = test.getPrimaryStage();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
            Parent loginRoot = loader.load();

            mainStage.getScene().setRoot(loginRoot);
            mainStage.setTitle("Connexion");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement de la page de connexion: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}