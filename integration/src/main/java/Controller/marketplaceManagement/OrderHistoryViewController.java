package Controller.marketplaceManagement;

import Controller.SessionManager;

import model.order;
import model.orderLine;
import model.product;
import model.user;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import services.marketPlace.OrderService;
import services.marketPlace.OrderLineService;
import services.marketPlace.ProductService;
import services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

public class OrderHistoryViewController {

    @FXML private FlowPane ordersContainer;
    @FXML private BorderPane root;
    @FXML private Button cartButton;


    private final OrderService orderService = new OrderService();
    private final OrderLineService orderLineService = new OrderLineService();
    private final ProductService productService = new ProductService();
    private final UserService userService = new UserService();

    // Current user ID from session
    private final int currentUserId = SessionManager.getInstance().getUserFront();

    public void initialize() {
        int currentUserId = getBuyerId();
        // Debug current user ID from session
        System.out.println("Current user ID from session: " + currentUserId);

        // First load orders
        loadOrders(currentUserId);

        // Make sure cartButton is initialized before calling updateCartBadge
        if (cartButton != null) {
            updateCartBadge();
        } else {
            System.err.println("Warning: cartButton is null during initialization");
        }
    }

    private int getBuyerId() {
        // Try multiple approaches to get the current logged-in user

        // First try to get the user from the services.SessionManager
        user currentUser = services.SessionManager.getCurrentUser();
        if (currentUser != null && currentUser.getId() > 0) {
            System.out.println("User ID from services.SessionManager: " + currentUser.getId());
            return currentUser.getId();
        }

        // Then try Controller.SessionManager
        int userId = Controller.SessionManager.getInstance().getUserFront();
        if (userId > 0) {
            System.out.println("User ID from Controller.SessionManager: " + userId);
            return userId;
        }

        // If all fails, try to use the already stored currentUserId field
        if (currentUserId > 0) {
            System.out.println("Using stored currentUserId: " + currentUserId);
            return currentUserId;
        }

        System.err.println("WARNING: Could not find valid user ID from any session source");
        return -1; // Invalid ID
    }

    // Modified loadOrders method with better logging and debugging
    private void loadOrders(int currentUserId) {
        try {
            ordersContainer.getChildren().clear();
            List<order> orders = orderService.getAll();
            System.out.println("Found " + orders.size() + " total orders");
            int userOrderCount = 0;

            // Debug: Print all order IDs and their buyer IDs
            System.out.println("Debugging all orders:");
            for (order o : orders) {
                System.out.println("Order #" + o.getId() + " - Buyer ID: " + o.getBuyerId() + " - Status: " + o.getStatus());
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            // Fallback for testing if currentUserId is still 0 or negative
            if (currentUserId <= 0) {
                // For testing purposes, show all orders or use a default user ID
                System.out.println("Warning: No valid user ID found, showing all orders for testing");
                // currentUserId = 3; // Uncomment this line to test with a specific user ID
            }

            for (order o : orders) {
                // We'll show all orders regardless of status to match the screenshot
                if (currentUserId <= 0 || o.getBuyerId() == currentUserId) {
                    userOrderCount++;
                    System.out.println("Processing order #" + o.getId() + " with status: " + o.getStatus());

                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/marketplaceManagement/order_card.fxml"));
                        VBox orderCard = loader.load();

                        // Set the order details on the card
                        Label orderNumberLabel = (Label) orderCard.lookup("#orderNumberLabel");
                        Label statusBadge = (Label) orderCard.lookup("#statusBadge");
                        Label totalPriceLabel = (Label) orderCard.lookup("#totalPriceLabel");
                        Label itemCountLabel = (Label) orderCard.lookup("#itemCountLabel");
                        Button viewDetailsButton = (Button) orderCard.lookup("#viewDetailsButton");
                        Button pdfButton = (Button) orderCard.lookup("#pdfButton");

                        // Debug information
                        System.out.println("Found UI elements:");
                        System.out.println("- orderNumberLabel: " + (orderNumberLabel != null));
                        System.out.println("- statusBadge: " + (statusBadge != null));
                        System.out.println("- totalPriceLabel: " + (totalPriceLabel != null));
                        System.out.println("- itemCountLabel: " + (itemCountLabel != null));
                        System.out.println("- viewDetailsButton: " + (viewDetailsButton != null));
                        System.out.println("- pdfButton: " + (pdfButton != null));

                        // Ensure we have the buttons before continuing
                        if (viewDetailsButton == null || pdfButton == null) {
                            System.err.println("ERROR: Required buttons not found in order_card.fxml");
                            continue; // Skip this card
                        }

                        // Update values with more error checking
                        if (orderNumberLabel != null) {
                            orderNumberLabel.setText("Order #" + o.getId());
                            orderNumberLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;-fx-text-fill: black");
                        }

                        if (statusBadge != null) {
                            statusBadge.setText(o.getStatus());

                            // Set appropriate background color based on status
                            if ("completed".equalsIgnoreCase(o.getStatus())) {
                                statusBadge.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 20;");
                            } else if ("confirmed".equalsIgnoreCase(o.getStatus())) {
                                statusBadge.setStyle("-fx-background-color: #FFC107; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 20;");
                            } else if ("delivered".equalsIgnoreCase(o.getStatus())) {
                                statusBadge.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 20;");
                            } else {
                                statusBadge.setStyle("-fx-background-color: #FFD54F; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 20;");
                            }
                        }

                        if (totalPriceLabel != null) {
                            totalPriceLabel.setText(String.format("%.2f TND", o.getTotalPrice()));
                            totalPriceLabel.setStyle("-fx-font-weight: normal; -fx-text-fill: black");
                        }

                        // Get order line items and populate item count
                        try {
                            List<orderLine> lines = orderLineService.getByOrderId(o.getId());
                            int totalItems = 0;
                            for (orderLine line : lines) {
                                totalItems += line.getOrderQuantity();
                            }

                            if (itemCountLabel != null) {
                                itemCountLabel.setText(totalItems + " items");
                                itemCountLabel.setStyle("-fx-font-weight: normal; -fx-text-fill: black");
                            }

                            System.out.println("Order #" + o.getId() + " has " + totalItems + " items and total price: " + o.getTotalPrice());
                        } catch (Exception e) {
                            if (itemCountLabel != null) {
                                itemCountLabel.setText("N/A");
                            }
                            System.err.println("Failed to get order lines for order #" + o.getId() + ": " + e.getMessage());
                        }

                        // Set action for the View Details button
                        final int orderId = o.getId();
                        viewDetailsButton.setOnAction(event -> viewInvoice(orderId));
                        viewDetailsButton.setText("View Details");
                        viewDetailsButton.setStyle("-fx-background-color: #2D2D2D; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-cursor: hand;");

                        // Set action for the PDF button
                        pdfButton.setOnAction(event -> exportOrderToPdf(orderId));
                        pdfButton.setText("Export PDF");
                        pdfButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-cursor: hand;");

                        // Make sure the order card has proper styling
                        orderCard.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
                        orderCard.setMaxWidth(300);
                        orderCard.setMaxHeight(240);

                        // Add the card to the container
                        ordersContainer.getChildren().add(orderCard);
                        System.out.println("Added order card for order #" + o.getId() + " to container");

                    } catch (IOException e) {
                        showAlert(Alert.AlertType.ERROR, "UI Error", "Failed to create order card: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Skipping order #" + o.getId() + " - buyer ID " + o.getBuyerId() +
                            " doesn't match current user ID " + currentUserId);
                }
            }

            System.out.println("Total orders displayed: " + userOrderCount);

            // If no orders displayed, show a message
            if (userOrderCount == 0) {
                Label noOrdersLabel = new Label("No orders found for your account.");
                noOrdersLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #757575; -fx-padding: 20px;");
                ordersContainer.getChildren().add(noOrdersLabel);
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load orders: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void viewInvoice(int orderId) {
        try {
            // Load the invoice view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/marketplaceManagement/invoice_view.fxml"));
            Parent invoiceView = loader.load();

            // Get controller and set data
            InvoiceViewController controller = loader.getController();

            try {
                // Get order details
                order orderDetails = orderService.getOne(orderId);
                if (orderDetails == null) {
                    throw new SQLException("Order not found with ID: " + orderId);
                }

                // Get order lines
                List<orderLine> orderLines = orderLineService.getByOrderId(orderId);

                // Get the current buyer ID using our improved method
                int buyerId = getBuyerId();

                // Default buyer information in case user fetch fails
                String buyerFirstName = "Unknown";
                String buyerLastName = "User";
                String buyerPhone = "N/A";
                String buyerEmail = "N/A";

                try {
                    // Try to get buyer information
                    user buyer = userService.getOne(buyerId);
                    if (buyer != null) {
                        buyerFirstName = buyer.getPrenom();
                        buyerLastName = buyer.getNom();
                        buyerPhone = buyer.getNum_tel();
                        buyerEmail = buyer.getMail();
                        System.out.println("Found buyer information: " + buyerFirstName + " " + buyerLastName);
                    } else {
                        System.err.println("User not found with ID: " + buyerId);
                    }
                } catch (Exception e) {
                    // Log the error but continue with default values
                    System.err.println("Failed to load user information: " + e.getMessage());
                    e.printStackTrace();
                }

                // Set the data in the invoice view with the buyer ID
                controller.setOrderData(
                        orderDetails,
                        orderLines,
                        buyerFirstName,
                        buyerLastName,
                        buyerPhone,
                        buyerEmail,
                        buyerId
                );

                // Create and show the invoice window
                Stage invoiceStage = new Stage();
                invoiceStage.setTitle("Invoice #" + orderId);
                invoiceStage.setScene(new Scene(invoiceView));
                invoiceStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                invoiceStage.show();

            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to retrieve order data: " + e.getMessage());
                e.printStackTrace();
            }

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "UI Error", "Failed to open invoice: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void goToMarketplace() {
        try {
            // Load the marketplace view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/marketplaceManagement/product_marketplace.fxml"));
            Parent marketplaceView = loader.load();

            // Get the current stage and set the new scene
            Scene currentScene = root.getScene();
            currentScene.setRoot(marketplaceView);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to navigate to marketplace: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Update the cart button badge with current item count
     */
    public void updateCartBadge() {
        try {
            // Check if cartButton is initialized
            if (cartButton == null) {
                System.err.println("Warning: cartButton is null in updateCartBadge");
                return;
            }

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
                if (o.getBuyerId() == currentUserId && o.getStatus().equals("pending")) {
                    return o;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // Placeholder navigation methods - implement these according to your app structure
    @FXML
    public void navigateToHome() {
        showAlert(Alert.AlertType.INFORMATION, "Navigation", "Navigating to Home");
    }

    @FXML
    public void navigateToEvent() {
        showAlert(Alert.AlertType.INFORMATION, "Navigation", "Navigating to Events");
    }

    @FXML
    public void navigateToForum() {
        showAlert(Alert.AlertType.INFORMATION, "Navigation", "Navigating to Forum");
    }

    @FXML
    public void navigateToFarm() {
        showAlert(Alert.AlertType.INFORMATION, "Navigation", "Navigating to Farm");
    }

    @FXML
    public void navigateToAbout() {
        showAlert(Alert.AlertType.INFORMATION, "Navigation", "Navigating to About Us");
    }

    @FXML
    public void navigateToContact() {
        showAlert(Alert.AlertType.INFORMATION, "Navigation", "Navigating to Contact");
    }

    @FXML
    public void viewOrderHistory() {
        // Already on the order history page, do nothing
    }

    @FXML
    public void viewCart() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/marketplaceManagement/cart_view.fxml"));
            BorderPane cartView = loader.load();

            CartViewController controller = loader.getController();
            controller.setOrderHistory(this);

            // Set the cart view in the center of the root BorderPane
            root.setCenter(cartView);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void exportOrderToPdf(int orderId) {
        try {
            // Get order details
            order orderDetails = orderService.getOne(orderId);
            if (orderDetails == null) {
                throw new SQLException("Order not found with ID: " + orderId);
            }

            // Get the current buyer ID using our improved method
            int buyerId = getBuyerId();

            // Get buyer information
            user buyer = userService.getOne(buyerId);
            if (buyer == null) {
                throw new SQLException("Buyer not found with ID: " + buyerId);
            }

            // Get order lines for this order
            List<orderLine> orderLines = orderLineService.getByOrderId(orderId);
            if (orderLines == null || orderLines.isEmpty()) {
                throw new SQLException("No order lines found for this order");
            }

            // Create a file chooser dialog
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Invoice as PDF");
            fileChooser.setInitialFileName("Order_" + orderId + "_Invoice.pdf");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );

            // Show save dialog
            File file = fileChooser.showSaveDialog(root.getScene().getWindow());

            if (file != null) {
                // Generate the actual PDF file
                generateInvoicePdf(file, orderDetails, orderLines, buyer);

                // Show success message
                showAlert(Alert.AlertType.INFORMATION, "PDF Generated",
                        "Invoice for Order #" + orderId + " has been saved to " + file.getAbsolutePath());
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "PDF Export Error", "Failed to export invoice: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void generateInvoicePdf(File file, order orderDetails, List<orderLine> orderLines, user buyer) throws IOException {
        // Constants for PDF layout
        final double TVA_RATE = 0.18; // 18% TVA
        final double DELIVERY_FEE = 5.0; // Default delivery fee

        // Create a new PDF document
        PDDocument document = new PDDocument();

        try {
            // Create a new page
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            // Set up fonts
            PDFont titleFont = PDType1Font.HELVETICA_BOLD;
            PDFont regularFont = PDType1Font.HELVETICA;
            PDFont boldFont = PDType1Font.HELVETICA_BOLD;
            PDFont italicFont = PDType1Font.HELVETICA_OBLIQUE;

            // Set up dimensions
            float margin = 50;
            float yStart = page.getMediaBox().getHeight() - margin;
            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();
            float contentWidth = pageWidth - 2 * margin;
            float yPosition = yStart;

            // Create a content stream for adding content to the page
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // Add header section with Invoice title and number
            contentStream.setNonStrokingColor(241/255f, 248/255f, 233/255f); // #f1f8e9
            contentStream.addRect(margin, yPosition - 45, contentWidth, 45);
            contentStream.fill();

            contentStream.setNonStrokingColor(0, 0, 0); // Reset to black

            contentStream.beginText();
            contentStream.setFont(titleFont, 24);
            contentStream.newLineAtOffset(margin + 15, yPosition - 30);
            contentStream.showText("INVOICE");
            contentStream.endText();

            yPosition -= 20;
            contentStream.beginText();
            contentStream.setFont(regularFont, 11);
            contentStream.newLineAtOffset(margin + 15, yPosition - 25);
            contentStream.showText("Invoice Number: " + orderDetails.getId());
            contentStream.endText();

            yPosition -= 15;


            yPosition -= 70; // Move down after the header section

            // Create the "From" and "To" sections side by side
            float columnWidth = contentWidth / 2 - 25; // Adjusted for spacing

            // "From" section - Left column
            contentStream.beginText();
            contentStream.setFont(boldFont, 11);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("From: AgroSphere");
            contentStream.endText();

            // Gather farmer information (using the first product's farmer for simplicity)
            Map<Integer, FarmerData> farmersInfo = new HashMap<>();
            for (orderLine line : orderLines) {
                try {
                    product prod = productService.getOne(line.getProductId());
                    FarmerData farmerInfo = getFarmerData(prod.getFarmerId());
                    if (farmerInfo != null && !farmersInfo.containsKey(prod.getFarmerId())) {
                        farmersInfo.put(prod.getFarmerId(), farmerInfo);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            String farmName = "Farm: AgroSphere Farms";
            String farmerName = "Farmer: -";
            String farmerPhone = "Phone: -";

            if (!farmersInfo.isEmpty()) {
                FarmerData firstFarmer = farmersInfo.values().iterator().next();
                farmName = "Farm: " + firstFarmer.farmName;
                farmerName = "Farmer: " + firstFarmer.farmerName;
                farmerPhone = "Phone: " + firstFarmer.phoneNumber;

                // If there are multiple farmers, indicate it
                if (farmersInfo.size() > 1) {
                    farmName += " (+" + (farmersInfo.size() - 1) + " more)";
                }
            }

            yPosition -= 15;
            contentStream.beginText();
            contentStream.setFont(regularFont, 11);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText(farmName);
            contentStream.endText();

            yPosition -= 15;
            contentStream.beginText();
            contentStream.setFont(regularFont, 11);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText(farmerName);
            contentStream.endText();

            yPosition -= 15;
            contentStream.beginText();
            contentStream.setFont(regularFont, 11);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText(farmerPhone);
            contentStream.endText();
            // Reset Y position for the "To" section
            yPosition += 45;
            // "To" section - Right column
            contentStream.beginText();
            contentStream.setFont(boldFont, 11);
            contentStream.newLineAtOffset(margin + columnWidth + 50, yPosition);
            contentStream.showText("To:");
            contentStream.endText();

            yPosition -= 15;
            contentStream.beginText();
            contentStream.setFont(regularFont, 11);
            contentStream.newLineAtOffset(margin + columnWidth + 50, yPosition);
            contentStream.showText("Name: " + buyer.getPrenom() + " " + buyer.getNom());
            contentStream.endText();

            yPosition -= 15;
            contentStream.beginText();
            contentStream.setFont(regularFont, 11);
            contentStream.newLineAtOffset(margin + columnWidth + 50, yPosition);
            contentStream.showText("Phone: " + buyer.getNum_tel());
            contentStream.endText();

            yPosition -= 15;
            contentStream.beginText();
            contentStream.setFont(regularFont, 11);
            contentStream.newLineAtOffset(margin + columnWidth + 50, yPosition);
            contentStream.showText("Email: " + buyer.getMail());
            contentStream.endText();
            // Move down after the From/To sections
            yPosition -= 30;
            // Delivery Details section - dark background
            contentStream.setNonStrokingColor(51/255f, 51/255f, 51/255f); // #333333
            contentStream.addRect(margin, yPosition - 30, contentWidth, 30);
            contentStream.fill();

            // White text for delivery section
            contentStream.setNonStrokingColor(1f, 1f, 1f); // White text

            contentStream.beginText();
            contentStream.setFont(boldFont, 10);
            contentStream.newLineAtOffset(margin + 10, yPosition - 12);
            contentStream.showText("Delivery Details");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(italicFont, 9);
            contentStream.newLineAtOffset(margin + 10, yPosition - 24);
            contentStream.showText("Delivery will be between 2 to 5 days");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(italicFont, 9);
            contentStream.newLineAtOffset(margin + contentWidth - 120, yPosition - 24);
            contentStream.showText("Delivery Fee: " + String.format("%.2f", DELIVERY_FEE) + " TND");
            contentStream.endText();
            contentStream.setNonStrokingColor(0, 0, 0);

            yPosition -= 40; // Move down after delivery details

            // Add table header
            float[] columnWidths = {
                    contentWidth * 0.30f, // Name
                    contentWidth * 0.15f, // Price/unit
                    contentWidth * 0.15f, // Quantity
                    contentWidth * 0.15f, // Total
                    contentWidth * 0.25f  // From
            };

            // Draw table header background
            contentStream.setNonStrokingColor(220/255f, 220/255f, 220/255f); // Light gray
            contentStream.addRect(margin, yPosition - 20, contentWidth, 20);
            contentStream.fill();
            contentStream.setNonStrokingColor(0, 0, 0); // Reset to black

            // Add table headers
            float xPosition = margin + 5;
            contentStream.beginText();
            contentStream.setFont(boldFont, 11);
            contentStream.newLineAtOffset(xPosition, yPosition - 15);
            contentStream.showText("Name");
            xPosition += columnWidths[0];
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(boldFont, 11);
            contentStream.newLineAtOffset(xPosition, yPosition - 15);
            contentStream.showText("Price/unit");
            xPosition += columnWidths[1];
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(boldFont, 11);
            contentStream.newLineAtOffset(xPosition, yPosition - 15);
            contentStream.showText("Quantity");
            xPosition += columnWidths[2];
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(boldFont, 11);
            contentStream.newLineAtOffset(xPosition, yPosition - 15);
            contentStream.showText("Total");
            xPosition += columnWidths[3];
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(boldFont, 11);
            contentStream.newLineAtOffset(xPosition, yPosition - 15);
            contentStream.showText("From");
            contentStream.endText();

            yPosition -= 20;

            // Add table rows
            double subtotal = 0;

            for (orderLine line : orderLines) {
                try {
                    if (yPosition < 100) {
                        // Add a new page if we're running out of space
                        contentStream.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        yPosition = pageHeight - margin - 20;
                    }

                    product prod = productService.getOne(line.getProductId());
                    double rate = prod.getPriceProd();
                    int quantity = line.getOrderQuantity();
                    double lineTotal = rate * quantity;
                    subtotal += lineTotal;

                    // Get farm name
                    String farmNameStr = "Unknown Farm";
                    try {
                        FarmerData farmerInfo = getFarmerData(prod.getFarmerId());
                        if (farmerInfo != null) {
                            farmNameStr = farmerInfo.farmName;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Draw row
                    xPosition = margin + 5;

                    contentStream.beginText();
                    contentStream.setFont(regularFont, 10);
                    contentStream.newLineAtOffset(xPosition, yPosition - 15);
                    contentStream.showText(prod.getNameProd());
                    xPosition += columnWidths[0];
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.setFont(regularFont, 10);
                    contentStream.newLineAtOffset(xPosition, yPosition - 15);
                    contentStream.showText(String.format("%.2f", rate) + " TND");
                    xPosition += columnWidths[1];
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.setFont(regularFont, 10);
                    contentStream.newLineAtOffset(xPosition, yPosition - 15);
                    contentStream.showText(String.valueOf(quantity));
                    xPosition += columnWidths[2];
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.setFont(regularFont, 10);
                    contentStream.newLineAtOffset(xPosition, yPosition - 15);
                    contentStream.showText(String.format("%.2f", lineTotal) + " TND");
                    xPosition += columnWidths[3];
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.setFont(regularFont, 10);
                    contentStream.newLineAtOffset(xPosition, yPosition - 15);
                    contentStream.showText(farmNameStr);
                    contentStream.endText();

                    yPosition -= 20;

                    // Add a line separator after each row
                    contentStream.setStrokingColor(220/255f, 220/255f, 220/255f);
                    contentStream.setLineWidth(0.5f);
                    contentStream.moveTo(margin, yPosition);
                    contentStream.lineTo(margin + contentWidth, yPosition);
                    contentStream.stroke();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            yPosition -= 20;

            // Calculate totals
            double tva = subtotal * TVA_RATE;
            double total = subtotal + DELIVERY_FEE + tva;

            // Add subtotal, delivery fee, TVA and total
            contentStream.beginText();
            contentStream.setFont(regularFont, 10);
            contentStream.newLineAtOffset(margin + contentWidth - 180, yPosition);
            contentStream.showText("Subtotal: " + String.format("%.2f", subtotal) + " TND");
            contentStream.endText();

            yPosition -= 15;
            contentStream.beginText();
            contentStream.setFont(regularFont, 10);
            contentStream.newLineAtOffset(margin + contentWidth - 180, yPosition);
            contentStream.showText("Delivery Fee: " + String.format("%.2f", DELIVERY_FEE) + " TND");
            contentStream.endText();

            yPosition -= 15;
            contentStream.beginText();
            contentStream.setFont(regularFont, 10);
            contentStream.newLineAtOffset(margin + contentWidth - 180, yPosition);
            contentStream.showText("TVA (" + (int)(TVA_RATE * 100) + "%): " + String.format("%.2f", tva) + " TND");
            contentStream.endText();

            yPosition -= 20;
            contentStream.beginText();
            contentStream.setFont(boldFont, 11);
            contentStream.newLineAtOffset(margin + contentWidth - 180, yPosition);
            contentStream.showText("Total: " + String.format("%.2f", total) + " TND");
            contentStream.endText();

            // Add a line at the bottom of the page
            yPosition -= 30;
            contentStream.setStrokingColor(0, 0, 0);
            contentStream.setLineWidth(0.5f);
            contentStream.moveTo(margin, yPosition);
            contentStream.lineTo(margin + contentWidth, yPosition);
            contentStream.stroke();

            // Add footer text
            yPosition -= 15;
            contentStream.beginText();
            contentStream.setFont(italicFont, 9);
            contentStream.newLineAtOffset(pageWidth / 2 - 70, yPosition);
            contentStream.showText("Thank you for your business!");
            contentStream.endText();

            yPosition -= 10;
            contentStream.beginText();
            contentStream.setFont(italicFont, 8);
            contentStream.newLineAtOffset(pageWidth / 2 - 120, yPosition);
            contentStream.showText("Registered Office Address: Farm Office, 123 Agriculture Ave, TN");
            contentStream.endText();

            // Close the content stream
            contentStream.close();

            // Save the document
            document.save(file);
        } finally {
            // Close the document
            if (document != null) {
                document.close();
            }
        }
    }


    private static class FarmerData {
        String farmName;
        String farmerName;
        String phoneNumber;

        public FarmerData(String farmName, String farmerName, String phoneNumber) {
            this.farmName = farmName;
            this.farmerName = farmerName;
            this.phoneNumber = phoneNumber;
        }
    }

    private FarmerData getFarmerData(int farmerId) {
        try {
            // Get farmer's user record
            user farmer = userService.getOne(farmerId);
            if (farmer != null) {
                // For now, we'll construct farm name from the farmer's name since we don't have farm table
                String farmName = farmer.getPrenom() + "'s Farm";
                String farmerName = farmer.getPrenom() + " " + farmer.getNom();
                String phoneNumber = farmer.getNum_tel();

                return new FarmerData(farmName, farmerName, phoneNumber);
            }
        } catch (Exception e) {
            System.err.println("Error fetching farmer info: " + e.getMessage());
        }
        return null;
    }
}