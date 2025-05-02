module com.devmasters.agrosphere {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;

    // Required to allow FXML reflection access
    opens com.devmasters.agrosphere.marketplaceManagement.Controller to javafx.fxml;
    opens com.devmasters.agrosphere.marketplaceManagement.entities to javafx.base;
    opens com.devmasters.agrosphere to javafx.fxml;

    exports com.devmasters.agrosphere;
    exports com.devmasters.agrosphere.marketplaceManagement.Controller;
    exports com.devmasters.agrosphere.shared;


}
