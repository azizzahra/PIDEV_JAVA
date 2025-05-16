package Controller.marketplaceManagement;

import model.order;
import model.orderLine;
import model.product;
import model.user;
import services.marketPlace.ProductService;
import services.UserService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvoiceViewController {

    @FXML private Label lblInvoiceNumber;
    @FXML private Label lblFarmName;
    @FXML private Label lblFarmerName;
    @FXML private Label lblFarmerPhone;
    @FXML private Label lblBuyerName;
    @FXML private Label lblBuyerPhone;
    @FXML private Label lblBuyerEmail;
    @FXML private Label lblDeliveryFee;
    @FXML private Label lblSubtotal;
    @FXML private Label lblDeliveryFeeAmount;
    @FXML private Label lblTVA;
    @FXML private Label lblTotal;
    @FXML private Label lblRegisteredAddress;

    @FXML private TableView<OrderItemModel> tableOrderItems;
    @FXML private TableColumn<OrderItemModel, String> colDescription;
    @FXML private TableColumn<OrderItemModel, Double> colRate;
    @FXML private TableColumn<OrderItemModel, Integer> colQuantity;
    @FXML private TableColumn<OrderItemModel, Double> colAmount;
    @FXML private TableColumn<OrderItemModel, String> colFarm;

    private final ProductService productService = new ProductService();
    private final UserService userService = new UserService();
    private double deliveryFee = 5.0; // Default delivery fee
    private final double TVA_RATE = 0.18; // 18% TVA

    public void initialize() {
        // Initialize table columns
        colDescription.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        colRate.setCellValueFactory(cellData -> cellData.getValue().rateProperty().asObject());
        colQuantity.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());
        colAmount.setCellValueFactory(cellData -> cellData.getValue().amountProperty().asObject());
        colFarm.setCellValueFactory(cellData -> cellData.getValue().farmProperty());


        // Set delivery fee
        lblDeliveryFee.setText("Delivery Fee: " + String.format("%.2f", deliveryFee) + " TND");
        lblDeliveryFeeAmount.setText(String.format("%.2f", deliveryFee) + " TND");

        // Set registered address
        lblRegisteredAddress.setText("Registered Office Address: Farm Office, 123 Agriculture Ave, TN");
    }

    public void setOrderData(order order, List<orderLine> orderLines, String buyerFirstName,
                             String buyerLastName, String buyerPhone, String buyerEmail, int buyerId) {

        // Set invoice number
        lblInvoiceNumber.setText("Invoice Number: " + order.getId());

        // Set buyer information
        lblBuyerName.setText("Name: " + buyerFirstName + " " + buyerLastName);
        lblBuyerPhone.setText("Phone: " + buyerPhone);
        lblBuyerEmail.setText("Email: " + buyerEmail);

        // Populate table with order lines
        ObservableList<OrderItemModel> items = FXCollections.observableArrayList();
        double subtotal = 0;

        Map<Integer, FarmerInfo> farmersInfo = new HashMap<>();

        for (orderLine line : orderLines) {
            try {
                product prod = productService.getOne(line.getProductId());
                double rate = prod.getPriceProd();
                int quantity = line.getOrderQuantity();
                double lineTotal = rate * quantity;

                // Get farmer information for this product
                FarmerInfo farmerInfo = getFarmerInfo(prod.getFarmerId());
                farmersInfo.put(prod.getFarmerId(), farmerInfo);

                String farmName = "Unknown Farm";
                if (farmerInfo != null) {
                    farmName = farmerInfo.getFarmName();
                }

                items.add(new OrderItemModel(
                        prod.getNameProd(),
                        rate,
                        quantity,
                        lineTotal,
                        farmName
                ));

                subtotal += lineTotal;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        tableOrderItems.setItems(items);

        // If we have at least one farmer, display their information in the invoice header
        if (!farmersInfo.isEmpty()) {
            FarmerInfo firstFarmer = farmersInfo.values().iterator().next();
            lblFarmName.setText("Farm: " + firstFarmer.getFarmName());
            lblFarmerName.setText("Farmer: " + firstFarmer.getFarmerName());
            lblFarmerPhone.setText("Phone: " + firstFarmer.getPhoneNumber());

            // If there are multiple farmers, indicate it
            if (farmersInfo.size() > 1) {
                lblFarmName.setText(lblFarmName.getText() + " (+" + (farmersInfo.size() - 1) + " more)");
            }
        } else {
            // Default values if no farmer information is available
            lblFarmName.setText("Farm: AgroSphere Farms");
            lblFarmerName.setText("Farmer: -");
            lblFarmerPhone.setText("Phone: -");
        }

        // Calculate TVA (Tax)
        double tva = subtotal * TVA_RATE;

        // Update totals
        lblSubtotal.setText(String.format("%.2f", subtotal) + " TND");
        lblTVA.setText(String.format("%.2f", tva) + " TND");
        double total = subtotal + deliveryFee + tva;
        lblTotal.setText(String.format("%.2f", total) + " TND");
    }

    private FarmerInfo getFarmerInfo(int farmerId) {
        try {
            // Get farmer's user record
            user farmer = userService.getOne(farmerId);
            if (farmer != null) {
                // For now, we'll construct farm name from the farmer's name since we don't have farm table
                String farmName = farmer.getPrenom() + "'s Farm";
                String farmerName = farmer.getPrenom() + " " + farmer.getNom();
                String phoneNumber = farmer.getNum_tel();

                return new FarmerInfo(farmName, farmerName, phoneNumber);
            }
        } catch (Exception e) {
            System.err.println("Error fetching farmer info: " + e.getMessage());
        }
        return null;
    }

    // Simple class to store farmer information
    private static class FarmerInfo {
        private final String farmName;
        private final String farmerName;
        private final String phoneNumber;

        public FarmerInfo(String farmName, String farmerName, String phoneNumber) {
            this.farmName = farmName;
            this.farmerName = farmerName;
            this.phoneNumber = phoneNumber;
        }

        public String getFarmName() {
            return farmName;
        }

        public String getFarmerName() {
            return farmerName;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }
    }

    // Model class for table items
    public static class OrderItemModel {
        private final SimpleStringProperty description;
        private final SimpleDoubleProperty rate;
        private final SimpleIntegerProperty quantity;
        private final SimpleDoubleProperty amount;
        private final SimpleStringProperty farm;

        public OrderItemModel(String description, double rate, int quantity, double amount, String farm) {
            this.description = new SimpleStringProperty(description);
            this.rate = new SimpleDoubleProperty(rate);
            this.quantity = new SimpleIntegerProperty(quantity);
            this.amount = new SimpleDoubleProperty(amount);
            this.farm = new SimpleStringProperty(farm);
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

        public SimpleStringProperty farmProperty() {
            return farm;
        }
    }
}