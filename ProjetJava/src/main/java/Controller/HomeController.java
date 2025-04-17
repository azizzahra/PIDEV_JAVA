package Controller; // Remplace par ton vrai package

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Farm;

public class HomeController {

    @FXML
    private TableView<Farm> tableFarms;

    @FXML
    private TableColumn<Farm, Integer> colId;
    @FXML
    private TableColumn<Farm, String> colTitle;
    @FXML
    private TableColumn<Farm, String> colDescription;
    @FXML
    private TableColumn<Farm, String> colStartDate;
    @FXML
    private TableColumn<Farm, String> colEndDate;
    @FXML
    private TableColumn<Farm, Double> colLatitude;
    @FXML
    private TableColumn<Farm, Double> colLongitude;
    @FXML
    private TableColumn<Farm, Integer> colUserId;

    @FXML
    private Button btnAjouterFarm;

    private final ObservableList<Farm> missionList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colStartDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colEndDate.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        colLatitude.setCellValueFactory(new PropertyValueFactory<>("latitude"));
        colLongitude.setCellValueFactory(new PropertyValueFactory<>("longitude"));
        colUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));

        tableFarms.setItems(missionList);

        btnAjouterFarm.setOnAction(event -> ajouterFarmExemple());
    }

    private void ajouterFarmExemple() {
        Farm m = new Farm("azaz", 12, "Exploration zone A", "2024-05-01", "2024-05-15", 36.8065, 10.1815, 123);
        missionList.add(m);
    }
}
