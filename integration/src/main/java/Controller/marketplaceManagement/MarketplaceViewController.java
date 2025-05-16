package Controller.marketplaceManagement;

import javafx.scene.layout.VBox;
import model.category;
import model.order;
import model.orderLine;
import model.product;
import model.user;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.util.StringConverter;
import services.marketPlace.CategoryService;
import services.marketPlace.OrderLineService;
import services.marketPlace.OrderService;
import services.marketPlace.ProductService;
import services.SessionManager;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class MarketplaceViewController {

    @FXML private BorderPane root;
    @FXML private FlowPane productContainer;
    @FXML private ComboBox<category> productFilterCombo;
    @FXML private ComboBox<String> sortingCombo;
    @FXML private TextField searchField;
    @FXML private Button cartButton;
    @FXML private Button orderHistoryButton;

    private final ProductService productService = new ProductService();
    private final CategoryService categoryService = new CategoryService();
    private final OrderService orderService = new OrderService();
    private final OrderLineService orderLineService = new OrderLineService();

    private List<product> allProducts;

    // Get buyer ID from the correct SessionManager
    private int getBuyerId() {
        // First try to get the user from the services.SessionManager
        user currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            return currentUser.getId();
        }

        // Fallback to the Controller.SessionManager if needed
        return Controller.SessionManager.getInstance().getUserFront();
    }

    @FXML
    public void initialize() {
        loadProducts();
        setupFilters();
        updateCartBadge();

        // Setup search functionality
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            applyFilters();
        });
    }

    public void loadProducts() {
        try {
            // Clear existing products
            productContainer.getChildren().clear();

            // Get all products
            allProducts = productService.getAll();

            // Apply any active filters
            displayFilteredProducts(allProducts);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error loading products", e.getMessage());
        }
    }

    private void setupFilters() {
        try {
            // Setup category filter
            List<category> categories = categoryService.getAll();
            category allCategory = new category();
            allCategory.setId(0);
            allCategory.setNameCategory("All Categories");
            categories.add(0, allCategory);

            productFilterCombo.getItems().setAll(categories);
            productFilterCombo.setValue(allCategory);
            productFilterCombo.setConverter(new StringConverter<category>() {
                @Override
                public String toString(category category) {
                    return category.getNameCategory();
                }

                @Override
                public category fromString(String string) {
                    return null;
                }
            });

            productFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                applyFilters();
            });

            // Setup sorting options
            sortingCombo.getItems().addAll(
                    "Newest First",
                    "Price: Low to High",
                    "Price: High to Low",
                    "Name: A to Z",
                    "Name: Z to A"
            );
            sortingCombo.setValue("Newest First");

            sortingCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                applyFilters();
            });

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error setting up filters", e.getMessage());
        }
    }

    private void applyFilters() {
        if (allProducts == null) return;

        List<product> filteredProducts = allProducts;

        // Apply category filter
        category selectedCategory = productFilterCombo.getValue();
        if (selectedCategory != null && selectedCategory.getId() != 0) {
            filteredProducts = filteredProducts.stream()
                    .filter(p -> p.getCategoryProdId() == selectedCategory.getId())
                    .collect(Collectors.toList());
        }

        // Apply search filter
        String searchText = searchField.getText().toLowerCase().trim();
        if (!searchText.isEmpty()) {
            filteredProducts = filteredProducts.stream()
                    .filter(p -> p.getNameProd().toLowerCase().contains(searchText) ||
                            (p.getDescriptionProd() != null &&
                                    p.getDescriptionProd().toLowerCase().contains(searchText)))
                    .collect(Collectors.toList());
        }

        // Apply sorting
        String sortOption = sortingCombo.getValue();
        if (sortOption != null) {
            switch (sortOption) {
                case "Price: Low to High":
                    filteredProducts.sort((p1, p2) -> Double.compare(p1.getPriceProd(), p2.getPriceProd()));
                    break;
                case "Price: High to Low":
                    filteredProducts.sort((p1, p2) -> Double.compare(p2.getPriceProd(), p1.getPriceProd()));
                    break;
                case "Name: A to Z":
                    filteredProducts.sort((p1, p2) -> p1.getNameProd().compareToIgnoreCase(p2.getNameProd()));
                    break;
                case "Name: Z to A":
                    filteredProducts.sort((p1, p2) -> p2.getNameProd().compareToIgnoreCase(p1.getNameProd()));
                    break;
                // Newest First is default, based on ID
                default:
                    filteredProducts.sort((p1, p2) -> Integer.compare(p2.getId(), p1.getId()));
                    break;
            }
        }

        // Display the filtered products
        displayFilteredProducts(filteredProducts);
    }

    private void displayFilteredProducts(List<product> products) {
        try {
            // Clear the container
            productContainer.getChildren().clear();

            // Add products to the container
            for (product p : products) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/marketplaceManagement/product_card.fxml"));
                VBox productCard = loader.load();

                ProductCardController controller = loader.getController();
                controller.setProductData(p, this);

                productContainer.getChildren().add(productCard);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error displaying products", e.getMessage());
        }
    }

    public void updateCartBadge() {
        try {
            // Find the current pending order
            order currentOrder = findPendingOrder();

            if (currentOrder != null) {
                // Count items in cart
                List<orderLine> lines = orderLineService.getByOrderId(currentOrder.getId());
                int totalItems = 0;

                for (orderLine line : lines) {
                    totalItems += line.getOrderQuantity();
                }

                // Update cart button text
                if (totalItems > 0) {
                    cartButton.setText("🛒 (" + totalItems + ")");
                } else {
                    cartButton.setText("🛒");
                }
            } else {
                cartButton.setText("🛒");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error updating cart badge: " + e.getMessage());
        }
    }

    private order findPendingOrder() {
        try {
            int buyerId = getBuyerId();
            List<order> allOrders = orderService.getAll();

            for (order o : allOrders) {
                if (o.getBuyerId() == buyerId && o.getStatus().equals("pending")) {
                    return o;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @FXML
    public void viewCart() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/marketplaceManagement/cart_view.fxml"));
            BorderPane cartView = loader.load();

            CartViewController controller = loader.getController();
            controller.setMarketplaceController(this);

            // Set the cart view in the center of the root BorderPane
            root.setCenter(cartView);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error loading cart", e.getMessage());
        }
    }

    @FXML
    public void viewOrderHistory() {
        try {
            // Load the order history view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/marketplaceManagement/order_history_view.fxml"));
            Parent orderHistoryView = loader.load();

            // Update the scene without creating a new stage
            Scene currentScene = root.getScene();
            currentScene.setRoot(orderHistoryView);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Failed to navigate to order history: " + e.getMessage());
        }
    }

    @FXML
    public void handleNavigate() {
        // This would be implemented to handle navigation between different sections
        System.out.println("Navigation requested - not implemented yet");
    }

    public void loadMarketplaceView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/marketplaceManagement/product_marketplace.fxml"));
            VBox marketplaceView = loader.load();

            // Get the controller and update the root reference
            MarketplaceViewController newController = loader.getController();

            // Replace the root content with the new view
            root.getScene().setRoot(marketplaceView);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error loading marketplace", e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}