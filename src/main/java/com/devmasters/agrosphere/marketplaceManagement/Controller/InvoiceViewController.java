package com.devmasters.agrosphere.marketplaceManagement.Controller;

import com.devmasters.agrosphere.marketplaceManagement.entities.order;
import com.devmasters.agrosphere.marketplaceManagement.entities.orderLine;
import com.devmasters.agrosphere.marketplaceManagement.entities.product;
import services.marketPlace.ProductService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Window;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class InvoiceViewController {

    @FXML private Label lblInvoiceNumber;
    @FXML private Label lblInvoiceDate;
    @FXML private Label lblFarmName;
    @FXML private Label lblFarmerName;
    @FXML private Label lblFarmerPhone;
    @FXML private Label lblBuyerName;
    @FXML private Label lblBuyerPhone;
    @FXML private Label lblBuyerEmail;
    @FXML private Label lblDeliveryFee;
    @FXML private Label lblSubtotal;
    @FXML private Label lblDeliveryFeeAmount;
    @FXML private Label lblTotal;
    @FXML private Label lblRegisteredAddress;

    @FXML private TableView<OrderItemModel> tableOrderItems;
    @FXML private TableColumn<OrderItemModel, String> colDescription;
    @FXML private TableColumn<OrderItemModel, Double> colRate;
    @FXML private TableColumn<OrderItemModel, Integer> colQuantity;
    @FXML private TableColumn<OrderItemModel, Double> colAmount;

    private final ProductService productService = new ProductService();
    private double deliveryFee = 5.0; // Default delivery fee

    public void initialize() {
        // Initialize table columns
        colDescription.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        colRate.setCellValueFactory(cellData -> cellData.getValue().rateProperty().asObject());
        colQuantity.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());
        colAmount.setCellValueFactory(cellData -> cellData.getValue().amountProperty().asObject());

        // Set today's date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        lblInvoiceDate.setText("Tax Date: " + dateFormat.format(new Date()));

        // Set delivery fee
        lblDeliveryFee.setText("Delivery Fee: " + String.format("%.2f", deliveryFee) + " TND");
        lblDeliveryFeeAmount.setText(String.format("%.2f", deliveryFee) + " TND");

        // Set registered address
        lblRegisteredAddress.setText("Registered Office Address: Farm Office, 123 Agriculture Ave, TN");
    }

    public void setOrderData(order order, List<orderLine> orderLines, String buyerFirstName,
                             String buyerLastName, String buyerPhone, String buyerEmail,
                             String farmerName, String farmName, String farmerPhone) {

        // Set invoice number
        lblInvoiceNumber.setText("Invoice Number: " + order.getId());

        // Set buyer information
        lblBuyerName.setText("Name: " + buyerFirstName + " " + buyerLastName);
        lblBuyerPhone.setText("Phone: " + buyerPhone);
        lblBuyerEmail.setText("Email: " + buyerEmail);

        // Set farmer information
        lblFarmName.setText("Farm: " + farmName);
        lblFarmerName.setText("Farmer: " + farmerName);
        lblFarmerPhone.setText("Phone: " + farmerPhone);

        // Populate table with order lines
        ObservableList<OrderItemModel> items = FXCollections.observableArrayList();
        double subtotal = 0;

        for (orderLine line : orderLines) {
            try {
                product prod = productService.getOne(line.getProductId());
                double rate = prod.getPriceProd();
                int quantity = line.getOrderQuantity();
                double lineTotal = rate * quantity;

                items.add(new OrderItemModel(
                        prod.getNameProd(),
                        rate,
                        quantity,
                        lineTotal
                ));

                subtotal += lineTotal;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        tableOrderItems.setItems(items);

        // Update totals
        lblSubtotal.setText(String.format("%.2f", subtotal) + " TND");
        double total = subtotal + deliveryFee;
        lblTotal.setText(String.format("%.2f", total) + " TND");
    }

    @FXML
    private void printInvoice() {
        // Get the window from any control in the scene
        Window window = lblInvoiceNumber.getScene().getWindow();

        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null) {
            // Show printer dialog
            boolean proceed = job.showPrintDialog(window);

            if (proceed) {
                // Get the root BorderPane
                BorderPane root = (BorderPane) lblInvoiceNumber.getScene().getRoot();

                // Print the page
                boolean printed = job.printPage(root);

                if (printed) {
                    job.endJob();
                }
            }
        }
    }

    // Model class for table items
    public static class OrderItemModel {
        private final SimpleStringProperty description;
        private final SimpleDoubleProperty rate;
        private final SimpleIntegerProperty quantity;
        private final SimpleDoubleProperty amount;

        public OrderItemModel(String description, double rate, int quantity, double amount) {
            this.description = new SimpleStringProperty(description);
            this.rate = new SimpleDoubleProperty(rate);
            this.quantity = new SimpleIntegerProperty(quantity);
            this.amount = new SimpleDoubleProperty(amount);
        }

        public SimpleStringProperty descriptionProperty() {
            return description;
        }

        public SimpleDoubleProperty rateProperty() {
            return rate;
        }

        public SimpleIntegerProperty quantityProperty() {
            return quantity;
        }

        public SimpleDoubleProperty amountProperty() {
            return amount;
        }
    }
}