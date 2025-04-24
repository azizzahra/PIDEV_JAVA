module com.devmasters.agrosphere {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    // Required to allow FXML reflection access
    opens com.devmasters.agrosphere.marketplaceManagment.Controller to javafx.fxml;
    opens com.devmasters.agrosphere.marketplaceManagment.entities to javafx.base;

    exports com.devmasters.agrosphere;
    exports com.devmasters.agrosphere.marketplaceManagment.Controller;
    exports com.devmasters.agrosphere.shared;
}
