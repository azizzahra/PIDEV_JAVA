package com.devmasters.agrosphere.marketplaceManagement.Controller;

import com.devmasters.agrosphere.marketplaceManagement.entities.category;
import com.devmasters.agrosphere.marketplaceManagement.entities.order;
import com.devmasters.agrosphere.marketplaceManagement.entities.orderLine;
import com.devmasters.agrosphere.marketplaceManagement.entities.product;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.util.StringConverter;
import services.marketPlace.CategoryService;
import services.marketPlace.OrderLineService;
import services.marketPlace.OrderService;
import services.marketPlace.ProductService;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MarketplaceViewController {

    @FXML private BorderPane root;
    @FXML private FlowPane productContainer;
    @FXML private ComboBox<category> productFilterCombo;
    @FXML private ComboBox<String> sortingCombo;
    @FXML private TextField searchField;
    @FXML private Button cartButton;

    private final ProductService productService = new ProductService();
    private final CategoryService categoryService = new CategoryService();
    private final OrderService orderService = new OrderService();
    private final OrderLineService orderLineService = new OrderLineService();

    private List<product> allProducts;

    // Default buyer ID (in a real app, this would come from a logged in user)
    private int buyerId = 3;

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
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/devmasters/agrosphere/marketplaceManagement/product_card.fxml"));
                javafx.scene.layout.VBox productCard = loader.load();

                ProductCardController controller = loader.getController();
                controller.setProductData(p, this);

                productContainer.getChildren().add(productCard);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error displaying products", e.getMessage());
        }
    }

    /**
     * Update the cart button badge with current item count
     */
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
        }
    }

    /**
     * Helper method to find the current pending order
     */
    private order findPendingOrder() {
        try {
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

    /**
     * Handler for the navigation buttons
     */
    @FXML
    public void handleNavigate() {
        // This would be implemented to handle navigation between different sections
        System.out.println("Navigation requested - not implemented yet");
    }

    /**
     * Open the cart view
     */
    @FXML
    public void viewCart() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/devmasters/agrosphere/marketplaceManagement/cart_view.fxml"));
            BorderPane cartView = loader.load();

            // Set the cart view in the center of the root BorderPane
            root.setCenter(cartView);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error loading cart", e.getMessage());
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