package Controller.marketplaceManagement;

import model.order;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import services.marketPlace.OrderService;

import java.text.DecimalFormat;
import java.util.*;

public class OrderListController implements Observer {

    @FXML private VBox orderContainer;
    @FXML private PieChart statusChart;
    @FXML private Label totalOrdersLabel;
    @FXML private VBox statsContainer;
    @FXML private ComboBox<String> orderSortCombo;
    @FXML private TextField searchField;
    @FXML private Label pendingOrdersCount;
    @FXML private Label deliveredOrdersCount;
    @FXML private Label processingOrdersCount;
    @FXML private Label cancelledOrdersCount;
    @FXML private Label totalRevenueLabel;

    private final OrderService orderService = new OrderService();
    private List<order> currentOrders;
    private String currentSearchTerm = "";
    private String currentSortOption = "";

    @FXML
    public void initialize() {
        try {
            // Register this controller as an observer of the OrderService
            orderService.addObserver(this);

            // Initialize UI components if needed
            if (orderSortCombo != null) {
                orderSortCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        currentSortOption = newVal;
                        sortOrders(newVal);
                    }
                });
            }

            if (searchField != null) {
                searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                    currentSearchTerm = newVal;
                    searchOrders(newVal);
                });
            }



            // Load all orders
            currentOrders = orderService.getAll();

            // Load orders into the container
            loadOrderItems(currentOrders);

            // Update statistics
            if (statsContainer != null) {
                updateOrderStatistics(currentOrders);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error initializing OrderListController: " + e.getMessage());
        }
    }

    // Observer pattern: This method is called whenever the OrderService notifies its observers
    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof List) {
            // Update the current orders list
            @SuppressWarnings("unchecked")
            List<order> updatedOrders = (List<order>) arg;
            currentOrders = updatedOrders;

            // Apply any active filters or sorting
            List<order> filteredList = applyFiltersAndSort(currentOrders);

            // Update UI on JavaFX thread
            javafx.application.Platform.runLater(() -> {
                // Reload the list with updated orders
                loadOrderItems(filteredList);

                // Update statistics
                updateOrderStatistics(currentOrders); // Always update stats with all orders
            });
        }
    }

    private List<order> applyFiltersAndSort(List<order> orders) {
        // First apply search filter if active
        List<order> filtered = new ArrayList<>(orders);
        if (currentSearchTerm != null && !currentSearchTerm.trim().isEmpty()) {
            List<order> searchFiltered = new ArrayList<>();
            for (order o : filtered) {
                if (String.valueOf(o.getId()).contains(currentSearchTerm) ||
                        o.getStatus().toLowerCase().contains(currentSearchTerm.toLowerCase()) ||
                        String.valueOf(o.getTotalPrice()).contains(currentSearchTerm)) {
                    searchFiltered.add(o);
                }
            }
            filtered = searchFiltered;
        }

        // Then apply sort if active
        if (currentSortOption != null && !currentSortOption.isEmpty()) {
            switch (currentSortOption) {
                case "Total (High-Low)":
                    filtered.sort((o1, o2) -> Double.compare(o2.getTotalPrice(), o1.getTotalPrice()));
                    break;
                case "Total (Low-High)":
                    filtered.sort((o1, o2) -> Double.compare(o1.getTotalPrice(), o2.getTotalPrice()));
                    break;
                case "Status":
                    filtered.sort((o1, o2) -> o1.getStatus().compareTo(o2.getStatus()));
                    break;
            }
        }

        return filtered;
    }

    private void sortOrders(String sortCriteria) {
        try {
            // Apply sorting to current filtered list
            List<order> sortedOrders = applyFiltersAndSort(currentOrders);

            // Reload the list with sorted orders
            loadOrderItems(sortedOrders);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void searchOrders(String keyword) {
        try {
            // Apply search filter and any active sorting
            List<order> filteredOrders = applyFiltersAndSort(currentOrders);

            // Update the UI with filtered orders
            loadOrderItems(filteredOrders);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadOrderItems(List<order> orders) {
        // Clear existing items to prevent duplicates
        if (orderContainer != null) {
            orderContainer.getChildren().clear();

            if (orders.isEmpty()) {
                Label noOrdersLabel = new Label("No orders found");
                noOrdersLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #64748b;");
                orderContainer.getChildren().add(noOrdersLabel);
                return;
            }

            for (order order : orders) {
                try {
                    // Use order_item.fxml
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(
                            "/marketplaceManagement/order_item.fxml"));
                    AnchorPane card = loader.load();

                    // Get the controller for the item
                    OrderItemController controller = loader.getController();
                    // Pass the OrderService to the item controller
                    controller.setData(order, orderService);

                    orderContainer.getChildren().add(card);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Failed to load order item: " + e.getMessage());
                }
            }
        }
    }

    public void updateOrderStatistics(List<order> orders) {
        // Set total orders count
        if (totalOrdersLabel != null) {
            totalOrdersLabel.setText(String.valueOf(orders.size()));
        }

        // Count orders by status and calculate total revenue
        Map<String, Integer> statusCounts = new HashMap<>();
        double totalRevenue = 0.0;

        for (order order : orders) {
            String status = order.getStatus().toLowerCase();
            statusCounts.put(status, statusCounts.getOrDefault(status, 0) + 1);

            // Only add to revenue if order is delivered
            if (status.equals("delivered") || status.equals("delivred")) {
                totalRevenue += order.getTotalPrice();
            }
        }

        // Update specific status counters if they exist
        if (pendingOrdersCount != null) {
            pendingOrdersCount.setText(String.valueOf(statusCounts.getOrDefault("pending", 0)));
        }

        if (deliveredOrdersCount != null) {
            int delivered = statusCounts.getOrDefault("delivered", 0) + statusCounts.getOrDefault("delivred", 0);
            deliveredOrdersCount.setText(String.valueOf(delivered));
        }

        if (processingOrdersCount != null) {
            int processing = statusCounts.getOrDefault("processing", 0) + statusCounts.getOrDefault("on wait", 0);
            processingOrdersCount.setText(String.valueOf(processing));
        }

        if (cancelledOrdersCount != null) {
            cancelledOrdersCount.setText(String.valueOf(statusCounts.getOrDefault("cancelled", 0)));
        }

        if (totalRevenueLabel != null) {
            DecimalFormat df = new DecimalFormat("#,##0.00");
            totalRevenueLabel.setText(df.format(totalRevenue) + " TND");
        }

        // Update pie chart if it exists
        if (statusChart != null) {
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            for (Map.Entry<String, Integer> entry : statusCounts.entrySet()) {
                pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
            }

            statusChart.setData(pieChartData);
            statusChart.setTitle("Orders by Status");

            // Add custom colors to pie chart segments
            for (PieChart.Data data : pieChartData) {
                String status = data.getName().toLowerCase();

                String color;
                if (status.equals("delivered") || status.equals("delivred")) {
                    color = "#2e7d32"; // Green
                } else if (status.equals("pending")) {
                    color = "#f57c00"; // Orange
                } else if (status.equals("processing") || status.equals("on wait")) {
                    color = "#1976d2"; // Blue
                } else if (status.equals("cancelled")) {
                    color = "#d32f2f"; // Red
                } else {
                    color = "#616161"; // Gray
                }

                data.getNode().setStyle("-fx-pie-color: " + color + ";");
            }
        }

        // Create status labels with colored indicators for the stats container
        if (statsContainer != null) {
            statsContainer.getChildren().clear();

            for (Map.Entry<String, Integer> entry : statusCounts.entrySet()) {
                String status = entry.getKey();
                int count = entry.getValue();
                String statusText = status + ": " + count;

                // Create HBox for each status with a colored circle indicator
                HBox statusBox = new HBox(10);
                statusBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                // Create color indicator circle
                Circle indicator = new Circle(6);

                // Set color based on status
                String statusLower = status.toLowerCase();
                if (statusLower.contains("delivered") || statusLower.contains("delivred")) {
                    indicator.setFill(Color.web("#2e7d32")); // Green
                } else if (statusLower.contains("pending")) {
                    indicator.setFill(Color.web("#f57c00")); // Orange
                } else if (statusLower.contains("processing") || statusLower.contains("on wait")) {
                    indicator.setFill(Color.web("#1976d2")); // Blue
                } else if (statusLower.contains("cancelled")) {
                    indicator.setFill(Color.web("#d32f2f")); // Red
                } else {
                    indicator.setFill(Color.web("#616161")); // Gray
                }

                Label statusLabel = new Label(statusText);
                statusLabel.getStyleClass().add("stats-label");

                statusBox.getChildren().addAll(indicator, statusLabel);
                statsContainer.getChildren().add(statusBox);
            }

            // Add total revenue if we have delivered orders
            if (totalRevenue > 0) {
                Label revenueHeader = new Label("Total Revenue from Delivered Orders:");
                revenueHeader.setStyle("-fx-font-weight: bold; -fx-padding: 10 0 5 0;");

                DecimalFormat df = new DecimalFormat("#,##0.00");
                Label revenueValue = new Label(df.format(totalRevenue) + " TND");
                revenueValue.setStyle("-fx-font-size: 16px; -fx-text-fill: #2e7d32;");

                statsContainer.getChildren().addAll(revenueHeader, revenueValue);
            }
        }
    }


}