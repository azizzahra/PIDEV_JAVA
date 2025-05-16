package Controller.marketplaceManagement;

import model.orderLine;
import model.product;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import services.marketPlace.OrderLineService;
import services.marketPlace.ProductService;

public class CartItemController {

    @FXML private ImageView productImage;
    @FXML private Label nameLabel;
    @FXML private Label unitPriceLabel;
    @FXML private Label quantityLabel;
    @FXML private Label lineTotalLabel;
    @FXML private Button btnIncrease;
    @FXML private Button btnDecrease;
    @FXML private Button btnRemove;

    private orderLine line;
    private product prod;
    private Runnable onCartChanged;

    private final OrderLineService orderLineService = new OrderLineService();
    private final ProductService productService = new ProductService();

    public void setData(orderLine ol, Runnable onCartChanged) {
        this.line = ol;
        this.onCartChanged = onCartChanged;

        try {
            prod = productService.getOne(ol.getProductId());

            nameLabel.setText(prod.getNameProd());
            unitPriceLabel.setText("Price/unit: " + String.format("%.2f", prod.getPriceProd()) + " TND");
            quantityLabel.setText(String.valueOf(ol.getOrderQuantity()));
            updateTotal();

            // Load and display product image
            loadProductImage();

            // Set button actions
            btnIncrease.setOnAction(e -> changeQuantity(1));
            btnDecrease.setOnAction(e -> changeQuantity(-1));
            btnRemove.setOnAction(e -> removeLine());

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load product details");
        }
    }

    /**
     * Load and display the product image directly from URL
     */
    private void loadProductImage() {
        try {
            String imageUrl = prod.getProdImg();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                // Load image directly from URL
                Image img = new Image(imageUrl, true);
                productImage.setImage(img);
            } else {
                // Try to load placeholder from classpath resources
                Image placeholder = new Image(getClass().getResourceAsStream("/images/placeholder.png"));
                productImage.setImage(placeholder);
            }

            // Set image properties
            productImage.setPreserveRatio(true);
            productImage.setSmooth(true);

        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
            // Load placeholder image
            try {
                Image placeholder = new Image(getClass().getResourceAsStream("/images/placeholder.png"));
                productImage.setImage(placeholder);
            } catch (Exception ex) {
                System.err.println("Could not load placeholder image either: " + ex.getMessage());
            }
        }
    }

    /**
     * Update the line total display
     */
    private void updateTotal() {
        double total = prod.getPriceProd() * line.getOrderQuantity();
        lineTotalLabel.setText(String.format("%.2f TND", total));
    }

    /**
     * Change the quantity of the item
     */
    private void changeQuantity(int delta) {
        int newQuantity = line.getOrderQuantity() + delta;

        // Don't allow quantities less than 1
        if (newQuantity < 1) {
            return;
        }

        // Don't allow quantities greater than available stock
        if (newQuantity > prod.getQuantity()) {
            showAlert("Cannot Add More", "Sorry, only " + prod.getQuantity() + " items available in stock");
            return;
        }

        try {
            // Update the order line
            line.setOrderQuantity(newQuantity);
            orderLineService.update(line);

            // Update the UI
            quantityLabel.setText(String.valueOf(newQuantity));
            updateTotal();

            // Notify cart view to refresh totals
            if (onCartChanged != null) {
                onCartChanged.run();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to update quantity");
        }
    }

    /**
     * Remove the item from the cart
     */
    private void removeLine() {
        try {
            // Delete the order line
            orderLineService.delete(line.getId());

            // Notify cart view to refresh
            if (onCartChanged != null) {
                onCartChanged.run();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to remove item from cart");
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