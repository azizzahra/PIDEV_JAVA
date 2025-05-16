package Controller.marketplaceManagement;

import model.category;
import model.product;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import services.marketPlace.CategoryService;
import services.marketPlace.ProductService;

import java.util.Optional;


public class ProductItemController {

    @FXML private Label nameLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label priceLabel;
    @FXML private Label stockLabel;
    @FXML private Button categoryButton;
    @FXML private ImageView imageView;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;


    private ProductListController listController;
    private product currentProduct;


    public void setData(product p, ProductListController controller) {
        this.currentProduct = p;
        this.listController = controller;

        nameLabel.setText(p.getNameProd());
        descriptionLabel.setText(p.getDescriptionProd());
        priceLabel.setText(p.getPriceProd() + " TND");
        stockLabel.setText("Stock: " + p.getQuantity());

        // Load category name instead of ID
        CategoryService cs = new CategoryService();
        try {
            category c = cs.getOne(p.getCategoryProdId());
            categoryButton.setText(c.getNameCategory());
        } catch (Exception e) {
            categoryButton.setText("Unknown");
        }

        try {
            String imageUrl = p.getProdImg();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                // Load from full URL
                imageView.setImage(new Image(imageUrl, true));
            } else {
                // Set a default placeholder image
                imageView.setImage(new Image(getClass().getResourceAsStream("/images/placeholder.png")));
            }
        } catch (Exception e) {
            System.out.println("Could not load image: " + e.getMessage());
            try {
                // Attempt to set default image if main image fails
                imageView.setImage(new Image(getClass().getResourceAsStream("/images/placeholder.png")));
            } catch (Exception ex) {
                System.out.println("Could not load placeholder image either.");
            }
        }


        // 💡 Fix: Set button actions
        updateButton.setOnAction(event -> controller.editProduct(p));
        deleteButton.setOnAction(event -> {
            controller.deleteProduct(p.getId());
            controller.loadProducts();
        });
    }



    @FXML
    private void onEditClicked() {
        if (listController != null && currentProduct != null) {
            listController.editProduct(currentProduct);
        }
    }

    @FXML
    private void onDeleteClicked() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Product");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("This will permanently delete the product: " + currentProduct.getNameProd());

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                ProductService ps = new ProductService();
                ps.delete(currentProduct.getId());

                // refresh list after delete
                listController.loadProducts();

                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setContentText("✅ Product deleted!");
                success.show();

            } catch (Exception e) {
                e.printStackTrace();
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setContentText("❌ Failed to delete product: " + e.getMessage());
                error.show();
            }
        }
    }


}