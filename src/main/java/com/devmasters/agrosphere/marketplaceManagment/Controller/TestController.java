package com.devmasters.agrosphere.marketplaceManagment.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class TestController {

        @FXML private Label testLabel;

        @FXML
        public void initialize() {
                testLabel.setText("TestController Loaded!");
        }
}
