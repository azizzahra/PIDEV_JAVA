package Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.user;
import services.UserService;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class UserListController implements Initializable {

    @FXML private TableView<user> userTable;
    @FXML private TableColumn<user, Integer> colId;
    @FXML private TableColumn<user, String> colNom;
    @FXML private TableColumn<user, String> colPrenom;
    @FXML private TableColumn<user, String> colEmail;
    @FXML private TableColumn<user, String> colRole;
    @FXML private TableColumn<user, String> colTel;
    @FXML private TableColumn<user, String> colStatus;
    @FXML private TableColumn<user, LocalDate> colBirth;

    private final UserService userService = new UserService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("mail"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colTel.setCellValueFactory(new PropertyValueFactory<>("num_tel"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colBirth.setCellValueFactory(new PropertyValueFactory<>("birth_Date"));

        loadUsers();
    }

    private void loadUsers() {
        List<user> users = userService.getAllUsers();
        userTable.getItems().setAll(users);
    }
}
