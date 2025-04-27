package com.devmasters.agrosphere.marketplaceManagement.Controller;

import com.devmasters.agrosphere.marketplaceManagement.entities.order;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import services.marketPlace.OrderService;

import java.util.List;

public class OrderListController {

    @FXML private VBox orderContainer;
    private final OrderService orderService = new OrderService();

    @FXML
    public void initialize() {
        try {
            List<order> orders = orderService.getAll();
            for (order order : orders) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/devmasters/agrosphere/marketplaceManagement/order_item.fxml"));
                AnchorPane card = loader.load();

                OrderItemController controller = loader.getController();
                controller.setData(order);

                orderContainer.getChildren().add(card);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
