package Controller.marketplaceManagement;

import model.category;
import model.product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.util.StringConverter;
import services.marketPlace.CategoryService;
import services.marketPlace.ProductService;

import java.io.IOException;
import java.util.List;


public class ProductListController {

    @FXML private VBox productContainer;
    @FXML private BorderPane root;
    @FXML private Button btnToggleView;
    //private boolean inFormView = false;
    @FXML private TextField searchField;
    @FXML private ComboBox<category> productFilterCombo;

    private final ProductService productService = new ProductService();
    private int currentCategoryFilter = -1;  // -1 means "All Categories"
    private String currentSearchKeyword = "";

    @FXML
    public void initialize() {
        // Load categories first
        loadCategoriesForFilter();

        // Load all products initially
        loadProducts();

        // Setup search field listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            currentSearchKeyword = newValue;
            performFilteredSearch();
        });

        // Setup category filter change listener
        productFilterCombo.setOnAction(e -> {
            category selected = productFilterCombo.getValue();
            currentCategoryFilter = (selected != null) ? selected.getId() : -1;
            performFilteredSearch();
        });
    }

    public void loadProducts() {
        try {
            productContainer.getChildren().clear();

            List<product> products = productService.getAll();
            for (product p : products) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/marketplaceManagement/product_item.fxml"));
                AnchorPane productItem = loader.load();

                ProductItemController controller = loader.getController();
                controller.setData(p, this);

                productContainer.getChildren().add(productItem);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private StackPane contentArea;

    public void editProduct(product p) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/marketplaceManagement/product_form.fxml"
            ));
            Region formView = loader.load();

            // Inject the product into the form
            ProductController controller = loader.getController();
            controller.setProductToEdit(p);

            // Find the parent StackPane and replace its content
            StackPane contentArea = (StackPane) productContainer.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(formView);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteProduct(int id) {
        try {
            ProductService ps = new ProductService();
            ps.delete(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCategoryNav() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/marketplaceManagement/category_list.fxml"));
            Region view = loader.load();
            root.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddProduct(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/marketplaceManagement/product_form.fxml"));
            Region formView = loader.load();

            StackPane contentArea = (StackPane) productContainer.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(formView);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BorderPane getParentBorderPane() {
        Parent parent = productContainer.getParent();
        while (parent != null) {
            if (parent instanceof BorderPane && ((BorderPane) parent).getId() != null &&
                    ((BorderPane) parent).getId().equals("dashboardRoot")) {
                return (BorderPane) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    /**
     * Combined search and filter functionality
     */
    private void performFilteredSearch() {
        try {
            List<product> results;

            // Using the combined search and filter method from ProductService
            if (currentCategoryFilter == -1 && (currentSearchKeyword == null || currentSearchKeyword.trim().isEmpty())) {
                results = productService.getAll(); // Get all products if no filters applied
            } else {
                results = productService.searchWithCategoryFilter(
                        currentSearchKeyword != null ? currentSearchKeyword : "",
                        currentCategoryFilter
                );
            }

            // Clear and rebuild product list
            updateProductDisplay(results);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateProductDisplay(List<product> products) {
        try {
            productContainer.getChildren().clear();

            if (products.isEmpty()) {
                // Display "No products found" message
                Label noProductsLabel = new Label("No products found matching your criteria");
                noProductsLabel.getStyleClass().add("no-results-label");
                productContainer.getChildren().add(noProductsLabel);
                return;
            }

            for (product p : products) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/marketplaceManagement/product_item.fxml"));
                AnchorPane productItem = loader.load();
                ProductItemController controller = loader.getController();
                controller.setData(p, this);
                productContainer.getChildren().add(productItem);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadCategoriesForFilter() {
        try {
            CategoryService cs = new CategoryService();
            List<category> categories = cs.getAll();

            // Create a special "All Categories" item
            category allCategories = new category();
            allCategories.setId(-1);
            allCategories.setNameCategory("All Categories");

            ObservableList<category> categoryOptions = FXCollections.observableArrayList();
            categoryOptions.add(allCategories); // Add "All Categories" as first option
            categoryOptions.addAll(categories);

            productFilterCombo.setItems(categoryOptions);
            productFilterCombo.getSelectionModel().selectFirst(); // Select "All Categories" by default

            // Set up converter for display
            productFilterCombo.setConverter(new StringConverter<category>() {
                @Override
                public String toString(category c) {
                    return c != null ? c.getNameCategory() : "All Categories";
                }

                @Override
                public category fromString(String s) {
                    return null; // Not needed for ComboBox
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}