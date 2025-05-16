package Controller.marketplaceManagement;

import Main.test;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class DashboardProductController {

    @FXML private StackPane contentArea;
    @FXML private BorderPane dashboardRoot;
    @FXML private BorderPane root;
    @FXML private Button btnProducts;
    @FXML private Button btnCategories;
    @FXML private Button btnOrders;
    @FXML private Button btnArchived;

    @FXML
    public void initialize() {
        // Store controller reference for scene graph access
        if (dashboardRoot != null) {
            dashboardRoot.getProperties().put("controller", this);
        }

        // Load product list by default
        loadView("/marketplaceManagement/product_list.fxml");
    }

    public void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Region view = loader.load();

            // Make sure contentArea is not null
            if (contentArea == null) {
                throw new IllegalStateException("contentArea StackPane is null");
            }

            contentArea.getChildren().setAll(view);

            // If this is loading a category controller, set up the reference between controllers
            Object controller = loader.getController();
            if (controller instanceof CategoryListController) {
                ((CategoryListController) controller).setDashboardController(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading view: " + fxmlPath + " - " + e.getMessage());
        }
    }

    // Method to get the contentArea for other controllers
    public StackPane getContentArea() {
        return contentArea;
    }

    @FXML
    private void handleProductList() {
        setActiveButton(btnProducts);
        loadView("/marketplaceManagement/product_list.fxml");
    }

    @FXML
    private void handleArchivedProducts() {
        setActiveButton(btnArchived);
        // This will be implemented later when you build the archive functionality
        System.out.println("Archived products requested - feature coming soon");
        loadView("/marketplaceManagement/Archived.fxml");
    }

    @FXML
    private void handleLogout() {
        System.out.println("Logging out...");
        // Implement actual logout logic here
    }

    @FXML
    private void handleOrdersList() {
        setActiveButton(btnOrders);
        loadView("/marketplaceManagement/orders_list.fxml");
    }

    @FXML
    private void handleCategoryList() {
        setActiveButton(btnCategories);
        loadView("/marketplaceManagement/category_list.fxml");
    }

    private void setActiveButton(Button activeButton) {
        List<Button> allButtons = List.of(btnProducts, btnCategories, btnOrders, btnArchived);
        for (Button b : allButtons) {
            if (b != null) {
                b.getStyleClass().remove("product-nav-button-active");
            }
        }
        if (activeButton != null) {
            activeButton.getStyleClass().add("product-nav-button-active");
        }
    }

    @FXML
    private void handleMarketplaceNav() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/marketplaceManagement/product_marketplace.fxml"));
            BorderPane marketplaceView = loader.load();

            if (contentArea != null) {
                contentArea.getChildren().setAll(marketplaceView);
            } else {
                StackPane foundContentArea = findContentArea();
                if (foundContentArea != null) {
                    foundContentArea.getChildren().setAll(marketplaceView);
                } else if (root != null && root.getScene() != null) {
                    root.getScene().setRoot(marketplaceView); // fallback
                } else {
                    System.err.println("Could not find contentArea or root scene");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Helper method to find the contentArea in the scene graph
    private StackPane findContentArea() {
        // Try finding through dashboardRoot first
        if (dashboardRoot != null) {
            StackPane result = (StackPane) dashboardRoot.lookup("#contentArea");
            if (result != null) {
                return result;
            }
        }

        // Try finding through root next
        if (root != null) {
            StackPane result = (StackPane) root.lookup("#contentArea");
            if (result != null) {
                return result;
            }
        }

        // If neither direct lookup works, try to search the scene
        if (btnCategories != null && btnCategories.getScene() != null) {
            Parent sceneRoot = btnCategories.getScene().getRoot();
            return (StackPane) sceneRoot.lookup("#contentArea");
        }

        return null;
    }

    @FXML
    public void handleForum(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Post/ForumD.fxml"));
            Parent root = loader.load();
            Scene currentScene = ((Node) event.getSource()).getScene();
            currentScene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur de chargement");
            alert.setContentText("Impossible de charger la vue du forum: " + e.getMessage());
            alert.showAndWait();
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
            showAlert("Erreur", "Erreur lors du chargement de la page d'accueil");
        }
    }

    @FXML
    public void handleLogout(ActionEvent actionEvent) {
        try {
            Parent loginView = FXMLLoader.load(getClass().getResource("/views/login.fxml"));
            Scene currentScene = ((Node) actionEvent.getSource()).getScene();
            currentScene.setRoot(loginView);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de se déconnecter: " + e.getMessage());
        }
    }
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}