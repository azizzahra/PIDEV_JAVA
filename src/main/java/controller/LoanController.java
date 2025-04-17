package controller;

import entities.Loan;
import entities.Place;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import services.LoanService;
import services.LoanServiceImpl;
import services.PlaceService;

import java.io.IOException;
import java.sql.SQLException;

public class LoanController {
    @FXML private TableView<Loan> loanTable;
    @FXML private TableColumn<Loan, Integer> idCol;
    @FXML private TableColumn<Loan, Double> priceCol;
    @FXML private TableColumn<Loan, Integer> ticketsLeftCol;
    @FXML private TableColumn<Loan, String> formationCol;
    @FXML private TableColumn<Loan, String> placeCol;

    private final LoanService loanService = new LoanServiceImpl();
    private final PlaceService placeService = new PlaceService();
    private final ObservableList<Loan> loans = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadData();
    }

    private void setupTableColumns() {
        priceCol.setCellValueFactory(new PropertyValueFactory<>("ticketPrice"));
        ticketsLeftCol.setCellValueFactory(new PropertyValueFactory<>("ticketsLeft"));
        formationCol.setCellValueFactory(new PropertyValueFactory<>("formation"));
        placeCol.setCellValueFactory(cellData -> {
            Place place = cellData.getValue().getPlace();
            return new SimpleStringProperty(place != null ? place.getName() : "No Place");
        });

        TableColumn<Loan, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(createActionCellFactory());
        loanTable.getColumns().add(actionsCol);

        // Add a small drop shadow effect to table
        loanTable.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
    }

    private Callback<TableColumn<Loan, Void>, TableCell<Loan, Void>> createActionCellFactory() {
        return param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");

            {
                editButton.getStyleClass().add("edit-button");
                deleteButton.getStyleClass().add("delete-button");

                editButton.setOnAction(event -> handleEditAction());
                deleteButton.setOnAction(event -> handleDeleteAction());
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new HBox(10, editButton, deleteButton));
            }

            private void handleEditAction() {
                Loan loan = getTableView().getItems().get(getIndex());
                showUpdateDialog(loan);
            }

            private void handleDeleteAction() {
                Loan loan = getTableView().getItems().get(getIndex());
                deleteLoan(loan);
            }
        };
    }

    @FXML
    private void handleAdd() {
        showAddDialog();
    }

    private void showAddDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/add-loan.fxml"));
            Parent root = loader.load();

            initializeDialogController(loader.getController());

            Stage stage = createDialogStage(root, "Add New Loan");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            stage.setScene(scene);

            stage.showAndWait();
            refreshTable();
        } catch (IOException | SQLException e) {
            showError("Failed to load add form", e);
        }
    }

    private void showUpdateDialog(Loan loan) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/update-loan.fxml"));
            Parent root = loader.load();

            UpdateLoanController controller = loader.getController();
            controller.setLoan(loan);
            initializeDialogController(controller);

            Stage stage = createDialogStage(root, "Update Loan");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            stage.setScene(scene);

            stage.showAndWait();
            refreshTable();
        } catch (IOException | SQLException e) {
            showError("Failed to load update form", e);
        }
    }

    private void initializeDialogController(Object controller) throws SQLException {
        ObservableList<Place> places = FXCollections.observableArrayList(placeService.recuperer());
        if (places.isEmpty()) {
            showAlert("Warning", "No places available. Please add places first.");
            throw new SQLException("No places available");
        }

        if (controller instanceof AddLoanController addController) {
            addController.setLoanService(loanService);
            addController.setPlaceData(places);
            addController.setRefreshCallback(() -> {
                try {
                    refreshTable();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        } else if (controller instanceof UpdateLoanController updateController) {
            updateController.setLoanService(loanService);
            updateController.setPlaceData(places);
            updateController.setRefreshCallback(() -> {
                try {
                    refreshTable();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private Stage createDialogStage(Parent root, String title) {
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.initModality(Modality.APPLICATION_MODAL);
        return stage;
    }

    private void deleteLoan(Loan loan) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Loan");
        confirm.setContentText("Are you sure you want to delete this loan?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    loanService.supprimer(loan);
                    refreshTable();
                } catch (SQLException e) {
                    showError("Failed to delete loan", e);
                }
            }
        });
    }

    private void loadData() {
        try {
            refreshTable();
        } catch (Exception e) {
            showError("Failed to load data", e);
        }
    }

    private void refreshTable() throws SQLException {
        loans.setAll(loanService.recuperer());
        loanTable.setItems(loans);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String context, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(context);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
        e.printStackTrace();
    }

    public void handleGoToUsers(ActionEvent actionEvent) {
        try {
            // Load the loans management layout
            Parent loansRoot = FXMLLoader.load(
                    getClass().getResource("/owner_view.fxml")
            );
            // Get current window (stage) from the event’s source node
            Stage stage = (Stage) ((Node) actionEvent.getSource())
                    .getScene()
                    .getWindow();
            // Replace the scene
            stage.setScene(new Scene(loansRoot));
            stage.setTitle("owner");
        } catch (IOException e) {
            showAlert("Error", "Failed to load loans screen: " + e.getMessage());
        }
    }


    public void handleBackToPlace(ActionEvent actionEvent) {
        {
            try {
                // Load the loans management layout
                Parent loansRoot = FXMLLoader.load(
                        getClass().getResource("/place.fxml")
                );
                // Get current window (stage) from the event’s source node
                Stage stage = (Stage) ((Node) actionEvent.getSource())
                        .getScene()
                        .getWindow();
                // Replace the scene
                stage.setScene(new Scene(loansRoot));
                stage.setTitle("Loan Management");
            } catch (IOException e) {
                showAlert("Error", "Failed to load loans screen: " + e.getMessage());
            }
        }
    }
}