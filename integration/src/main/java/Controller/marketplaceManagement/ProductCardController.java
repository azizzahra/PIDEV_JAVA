package Controller.marketplaceManagement;

import model.order;
import model.orderLine;
import model.product;
import model.user;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import services.marketPlace.OrderLineService;
import services.marketPlace.OrderService;
import services.marketPlace.ProductService;
import services.UserService;
import services.SessionManager;

import java.util.List;

public class ProductCardController {

    @FXML private VBox productCardRoot;
    @FXML private ImageView productImage;
    @FXML private Label productName;
    @FXML private Label productDescription;
    @FXML private Label productPrice;
    @FXML private Label productStock;
    @FXML private Button addToCartButton;
    @FXML private Label categoryLabel;

    private product productData;
    private MarketplaceViewController parentController;

    // Services for database operations
    private final OrderService orderService = new OrderService();
    private final OrderLineService orderLineService = new OrderLineService();
    private final ProductService productService = new ProductService();
    private final UserService userService = new UserService();

    // Get current user ID from the SessionManager
    private int getBuyerId() {
        // First try to get the user from the services.SessionManager
        user currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            return currentUser.getId();
        }

        // Fallback to the Controller.SessionManager if needed
        return Controller.SessionManager.getInstance().getUserFront();
    }

    public void setProductData(product product, MarketplaceViewController controller) {
        this.productData = product;
        this.parentController = controller;

        // Set the product information
        productName.setText(product.getNameProd());
        productDescription.setText(product.getDescriptionProd());
        productPrice.setText(product.getPriceProd() + " TND");

        // Set the stock label
        if (product.getQuantity() <= 10) {
            productStock.setText("✖ Out of Stock");
            productStock.getStyleClass().add("out-of-stock");

            // Apply styling to the entire card to make it appear faded
            productCardRoot.getStyleClass().add("product-out-of-stock");

            // Disable the add to cart button
            addToCartButton.setDisable(true);
            addToCartButton.getStyleClass().add("button-disabled");
        } else {
            productStock.setText("Stock: " + product.getQuantity());
        }

        // Set category label
        setCategoryLabel(product.getCategoryProdId());

        // Set image if available
        if (product.getProdImg() != null && !product.getProdImg().isEmpty()) {
            try {
                Image image = new Image(product.getProdImg());
                productImage.setImage(image);
            } catch (Exception e) {
                // Use default image if loading fails
                setDefaultImage();
            }
        } else {
            setDefaultImage();
        }
    }

    private void setDefaultImage() {
        // Load a default image based on category
        String defaultImagePath = "/com/devmasters/agrosphere/marketplaceManagement/assets/images/product-default.png";
        try {
            Image defaultImage = new Image(defaultImagePath);
            productImage.setImage(defaultImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setCategoryLabel(int categoryId) {
        // Implementation to set category label
        // This would typically get the category name from a service
    }

    @FXML
    private void handleAddToCart() {
        try {
            // Get the current buyer ID
            int buyerId = getBuyerId();

            // Add debug message
            System.out.println("Attempting to add product to cart for user ID: " + buyerId);

            // Check if the buyer ID exists in the user table
            if (!isValidUser(buyerId)) {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid user account. Please log in again.");
                return;
            }

            // 1. Find if there's an existing pending order for this buyer
            order currentOrder = null;
            List<order> allOrders = orderService.getAll();

            for (order o : allOrders) {
                if (o.getBuyerId() == buyerId && o.getStatus().equals("pending")) {
                    currentOrder = o;
                    System.out.println("Found existing pending order with ID: " + o.getId());
                    break;
                }
            }

            // 2. If no pending order exists, create a new one
            if (currentOrder == null) {
                currentOrder = new order();
                currentOrder.setBuyerId(buyerId);
                currentOrder.setStatus("pending");
                currentOrder.setTotalPrice(0.0); // Initialize total price to 0

                int newOrderId = orderService.add(currentOrder);
                currentOrder.setId(newOrderId);
                System.out.println("Created new order with ID: " + newOrderId);
            }

            // 3. Check if the product is already in the cart
            boolean productExists = false;
            List<orderLine> existingLines = orderLineService.getByOrderId(currentOrder.getId());

            for (orderLine line : existingLines) {
                if (line.getProductId() == productData.getId()) {
                    // Product already in cart, increase quantity
                    line.setOrderQuantity(line.getOrderQuantity() + 1);
                    orderLineService.update(line);
                    productExists = true;

                    break;
                }
            }

            // 4. If product not in cart, add it as a new line
            if (!productExists) {
                orderLine newLine = new orderLine();
                newLine.setOrdId(currentOrder.getId());
                newLine.setProductId(productData.getId());
                newLine.setOrderQuantity(1);
                orderLineService.add(newLine);
                System.out.println("Added new product to cart: " + productData.getNameProd());

                // Show success message
                showAlert(Alert.AlertType.INFORMATION, "Success",
                        productName.getText() + " added to your cart.");
            }

            // 5. Update cart badge count if you have one
            if (parentController != null) {
                parentController.updateCartBadge();
            }

        } catch (Exception e) {
            System.err.println("Error adding product to cart");
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to add product to cart: " + e.getMessage());
        }
    }
    private boolean isValidUser(int userId) {
        try {
            // First, try to verify through the current user in SessionManager
            user currentUser = SessionManager.getCurrentUser();
            if (currentUser != null && currentUser.getId() == userId) {
                return true;
            }

            // If that doesn't work, check in the database
            return userService.userExists(userId);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    private order findOrCreatePendingOrder() {
        try {
            int buyerId = getBuyerId();

            // Check if the user is valid first
            if (!isValidUser(buyerId)) {
                return null;
            }

            // First try to find an existing pending order
            List<order> allOrders = orderService.getAll();

            for (order o : allOrders) {
                if (o.getBuyerId() == buyerId && o.getStatus().equals("pending")) {
                    return o;
                }
            }

            // If no pending order exists, create a new one
            order newOrder = new order();
            newOrder.setBuyerId(buyerId);
            newOrder.setStatus("pending");
            newOrder.setTotalPrice(0.0); // Initialize total price

            int orderId = orderService.add(newOrder);

            // Retrieve the newly created order
            for (order o : orderService.getAll()) {
                if (o.getId() == orderId) {
                    return o;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to create order: " + e.getMessage());
        }

        return null;
    }

    private orderLine findExistingOrderLine(int orderId, int productId) {
        try {
            List<orderLine> lines = orderLineService.getByOrderId(orderId);

            for (orderLine line : lines) {
                if (line.getProductId() == productId) {
                    return line;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}