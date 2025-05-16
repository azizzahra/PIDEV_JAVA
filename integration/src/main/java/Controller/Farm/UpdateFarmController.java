package Controller.Farm;

import Main.test;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Farm;
import netscape.javascript.JSObject;
import services.FarmService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

public class UpdateFarmController {

    @FXML
    private Button btnUpdateFarm;

    @FXML
    private TextField fid;

    @FXML
    private TextArea fdescription;
    @FXML
    private Label fdescriptionError;

    @FXML
    private TextField fimage;
    @FXML
    private Button btnSelectImage;
    @FXML
    private ImageView imagePreview;
    @FXML
    private Label fimageError;

    @FXML
    private TextField flatitude;
    @FXML
    private Label flatitudeError;

    @FXML
    private TextField flocation;
    @FXML
    private Label flocationError;

    @FXML
    private TextField flongitude;
    @FXML
    private Label flongitudeError;

    @FXML
    private TextField fname;
    @FXML
    private Label fnameError;

    @FXML
    private TextField fsize;
    @FXML
    private Label fsizeError;

    @FXML
    private TextField fuser;
    @FXML
    private Label fuserError;

    @FXML
    private WebView mapView;

    private final String UPLOAD_DIR = "C:/xampp/htdocs/uploads/farm_image/";
    private final String RELATIVE_PATH = "uploads/farm_image/";
    private Preferences prefs = Preferences.userNodeForPackage(UpdateFarmController.class);

    private File selectedImageFile;
    private String currentImagePath;
    private int FarmId;

    @FXML
    public void initialize() {
        if (btnUpdateFarm == null) {
            System.out.println("Erreur : btnUpdateFarm est null !");
        } else {
            btnUpdateFarm.setDisable(false);
        }
        // Récupérer l'utilisateur courant depuis la session (services.SessionManager)
        model.user currentUser = services.SessionManager.getCurrentUser();

        if (currentUser != null) {
            // Remplir automatiquement le champ avec l'ID utilisateur
            fuser.setText(String.valueOf(currentUser.getId()));
            // Rendre le champ non modifiable pour empêcher l'édition
            fuser.setEditable(false);
        }

        setupValidationListeners();
        btnSelectImage.setOnAction(event -> selectImage());
        initializeMap();
    }

    private void initializeMap() {
        try {
            WebEngine engine = mapView.getEngine();
            engine.setJavaScriptEnabled(true);

            URL mapResource = getClass().getResource("/Farm/map.html");
            if (mapResource != null) {
                String mapUrl = mapResource.toExternalForm();
                System.out.println("Loading map from: " + mapUrl);
                engine.load(mapUrl);
            } else {
                String projectPath = System.getProperty("user.dir");
                File mapFile = new File(projectPath + "/src/main/resources/Farm/map.html");
                if (mapFile.exists()) {
                    System.out.println("Loading map from file system: " + mapFile.toURI().toString());
                    engine.load(mapFile.toURI().toString());
                } else {
                    System.err.println("Could not find map.html resource!");
                    showAlert(Alert.AlertType.ERROR, "Error", "Could not find map.html resource");
                }
            }

            engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                    JSObject window = (JSObject) engine.executeScript("window");
                    window.setMember("javafxCallback", new JavaFXCallback());
                    System.out.println("JavaFX callback installed for UpdateFarm map");
                } else if (newState == javafx.concurrent.Worker.State.FAILED) {
                    System.err.println("Failed to load map: " + engine.getLoadWorker().getException());
                }
            });
        } catch (Exception e) {
            System.err.println("Error initializing map: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to initialize map: " + e.getMessage());
        }
    }

    public class JavaFXCallback {
        public void call(double lat, double lng, String cityName) {
            Platform.runLater(() -> {
                try {
                    flatitude.setText(String.format("%.6f", lat).replace(',', '.')); // Remplacer la virgule par un point
                    flongitude.setText(String.format("%.6f", lng).replace(',', '.')); // Remplacer la virgule par un point

                    if (cityName != null && !cityName.isEmpty()) {
                        flocation.setText(cityName);
                    }
                    System.out.println("Map callback received: Lat=" + lat + ", Lng=" + lng + ", City=" + cityName);
                } catch (Exception e) {
                    System.err.println("Error in map callback: " + e.getMessage());
                }
            });
        }

        public void call(double lat, double lng) {
            call(lat, lng, "");
        }
    }

    @FXML
    private void selectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        String lastDirectoryPath = prefs.get("lastImageDirectory", null);
        if (lastDirectoryPath != null) {
            File lastDir = new File(lastDirectoryPath);
            if (lastDir.exists()) {
                fileChooser.setInitialDirectory(lastDir);
            }
        }

        FileChooser.ExtensionFilter imageFilter =
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif");
        fileChooser.getExtensionFilters().add(imageFilter);

        selectedImageFile = fileChooser.showOpenDialog(btnSelectImage.getScene().getWindow());

        if (selectedImageFile != null) {
            prefs.put("lastImageDirectory", selectedImageFile.getParent());
            fimage.setText(selectedImageFile.getName());

            try {
                Image image = new Image(selectedImageFile.toURI().toString());
                imagePreview.setImage(image);
                imagePreview.setFitWidth(200);
                imagePreview.setFitHeight(150);
                imagePreview.setPreserveRatio(true);
                imagePreview.setVisible(true);
                fimageError.setVisible(false);
            } catch (Exception e) {
                fimageError.setText("Impossible de charger l'image");
                fimageError.setVisible(true);
            }
        }
    }

    private String saveImage() throws IOException {
        if (selectedImageFile == null) {
            return currentImagePath;
        }

        String timestamp = String.valueOf(System.currentTimeMillis());
        String originalFileName = selectedImageFile.getName();
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf('.'));
        String newFileName = timestamp + fileExtension;

        Path destPath = Paths.get(UPLOAD_DIR + newFileName);
        Files.copy(selectedImageFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);

        return RELATIVE_PATH + newFileName;
    }

    public void setFarmId(int id) {
        this.FarmId = id;
        loadFarmData();
    }

    private void loadFarmData() {
        try {
            FarmService service = new FarmService();
            Farm farm = service.getone(FarmId);

            if (farm != null) {
                this.FarmId = farm.getId();
                fname.setText(farm.getName());
                fsize.setText(String.valueOf(farm.getSize()));
                flocation.setText(farm.getLocation());
                fimage.setText(farm.getImage());
                currentImagePath = farm.getImage();
                fdescription.setText(farm.getDescription());
                flatitude.setText(String.valueOf(farm.getLatitude()));
                flongitude.setText(String.valueOf(farm.getLongitude()));
                fuser.setText(String.valueOf(farm.getUserId()));

                // Load image preview
                if (currentImagePath != null && !currentImagePath.isEmpty()) {
                    try {
                        String fullPath = "C:/xampp/htdocs/" + currentImagePath;
                        File imageFile = new File(fullPath);
                        if (imageFile.exists()) {
                            Image image = new Image(imageFile.toURI().toString());
                            imagePreview.setImage(image);
                            imagePreview.setVisible(true);
                        }
                    } catch (Exception e) {
                        System.err.println("Erreur lors du chargement de l'image: " + e.getMessage());
                    }
                }

                // Set initial marker on map
                if (mapView != null && flatitude.getText() != null && !flatitude.getText().isEmpty() &&
                        flongitude.getText() != null && !flongitude.getText().isEmpty()) {
                    try {
                        WebEngine engine = mapView.getEngine();
                        if (engine.getLoadWorker().getState() == javafx.concurrent.Worker.State.SUCCEEDED) {
                            double lat = Double.parseDouble(flatitude.getText());
                            double lng = Double.parseDouble(flongitude.getText());
                            engine.executeScript("setMarker(" + lat + ", " + lng + ")");
                        }
                    } catch (Exception e) {
                        System.err.println("Error setting initial marker in loadFarmData: " + e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load farm data: " + e.getMessage());
        }
    }

    @FXML
    void UpdateFarmAction(ActionEvent event) {
        try {
            hideAllErrorMessages();
            boolean isValid = validateFields();
            if (!isValid) {
                return;
            }

            if (FarmId == 0) {
                showWarningAlert("Veuillez spécifier un ID de ferme valide.");
                return;
            }

            String name = fname.getText().trim();
            String description = fdescription.getText().trim();
            String location = flocation.getText().trim();
            int size = Integer.parseInt(fsize.getText().trim());
            String image = fimage.getText().trim();
            double latitude = Double.parseDouble(flatitude.getText().trim());
            double longitude = Double.parseDouble(flongitude.getText().trim());
            int userid = Integer.parseInt(fuser.getText().trim());

            if (name.isEmpty() || description.isEmpty() || location.isEmpty() || size < 1 || image.isEmpty()) {
                showWarningAlert("Veuillez remplir tous les champs obligatoires.");
                return;
            }

            String imagePath;
            if (selectedImageFile != null) {
                imagePath = saveImage();
                if (imagePath.isEmpty()) {
                    fimageError.setText("Erreur lors de l'enregistrement de l'image");
                    fimageError.setVisible(true);
                    return;
                }
            } else {
                imagePath = currentImagePath;
            }

            Farm farm = new Farm(FarmId, name, size, location, imagePath, description, latitude, longitude, userid);
            FarmService service = new FarmService();
            int rowsAffected = service.update(farm);

            if (rowsAffected > 0) {
                showInfoAlert("La ferme a été mise à jour avec succès !");

                Stage mainStage = test.getPrimaryStage();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Farm/FarmDetails.fxml"));
                Parent root = loader.load();
                FarmDetailsController controller = loader.getController();
                controller.setFarmId(FarmId);
                mainStage.getScene().setRoot(root);
            } else {
                showErrorAlert("Aucune ferme trouvée avec cet ID.");
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Les champs numériques doivent contenir des nombres valides.");
            e.printStackTrace();
        } catch (Exception e) {
            showErrorAlert("Une erreur s'est produite : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupValidationListeners() {
        fname.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty() || newValue.trim().length() < 3) {
                fnameError.setText("Le nom doit contenir au moins 3 caractères");
                fnameError.setVisible(true);
            } else {
                fnameError.setVisible(false);
            }
        });

        fsize.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (newValue.trim().isEmpty()) {
                    fsizeError.setText("La superficie est requise");
                    fsizeError.setVisible(true);
                    return;
                }
                int size = Integer.parseInt(newValue.trim());
                if (size <= 0) {
                    fsizeError.setText("La superficie doit être un nombre positif");
                    fsizeError.setVisible(true);
                } else {
                    fsizeError.setVisible(false);
                }
            } catch (NumberFormatException e) {
                fsizeError.setText("Veuillez saisir un nombre entier valide");
                fsizeError.setVisible(true);
            }
        });

        flocation.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                flocationError.setText("L'emplacement est requis");
                flocationError.setVisible(true);
            } else {
                flocationError.setVisible(false);
            }
        });

        fimage.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.trim().isEmpty()) {
                String urlRegex = "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})[/\\w .-]*/?$";
                if (!Pattern.matches(urlRegex, newValue.trim())) {
                    fimageError.setText("Format d'URL invalide");
                    fimageError.setVisible(true);
                } else {
                    fimageError.setVisible(false);
                }
            } else {
                fimageError.setVisible(false);
            }
        });

        fdescription.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty() || newValue.trim().length() < 10) {
                fdescriptionError.setText("La description doit contenir au moins 10 caractères");
                fdescriptionError.setVisible(true);
            } else {
                fdescriptionError.setVisible(false);
            }
        });

        flatitude.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (newValue.trim().isEmpty()) {
                    flatitudeError.setText("La latitude est requise");
                    flatitudeError.setVisible(true);
                    return;
                }
                double lat = Double.parseDouble(newValue.trim());
                if (lat < -90 || lat > 90) {
                    flatitudeError.setText("La latitude doit être entre -90 et 90");
                    flatitudeError.setVisible(true);
                } else {
                    flatitudeError.setVisible(false);
                }
            } catch (NumberFormatException e) {
                flatitudeError.setText("Format numérique invalide");
                flatitudeError.setVisible(true);
            }
        });

        flongitude.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (newValue.trim().isEmpty()) {
                    flongitudeError.setText("La longitude est requise");
                    flongitudeError.setVisible(true);
                    return;
                }
                double lon = Double.parseDouble(newValue.trim());
                if (lon < -180 || lon > 180) {
                    flongitudeError.setText("La longitude doit être entre -180 et 180");
                    flongitudeError.setVisible(true);
                } else {
                    flongitudeError.setVisible(false);
                }
            } catch (NumberFormatException e) {
                flongitudeError.setText("Format numérique invalide");
                flongitudeError.setVisible(true);
            }
        });

        fuser.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (newValue.trim().isEmpty()) {
                    fuserError.setText("L'ID utilisateur est requis");
                    fuserError.setVisible(true);
                    return;
                }
                int userId = Integer.parseInt(newValue.trim());
                if (userId <= 0) {
                    fuserError.setText("L'ID utilisateur doit être positif");
                    fuserError.setVisible(true);
                } else {
                    fuserError.setVisible(false);
                }
            } catch (NumberFormatException e) {
                fuserError.setText("Format numérique invalide");
                fuserError.setVisible(true);
            }
        });
    }

    private void hideAllErrorMessages() {
        fnameError.setVisible(false);
        fsizeError.setVisible(false);
        flocationError.setVisible(false);
        fimageError.setVisible(false);
        fdescriptionError.setVisible(false);
        flatitudeError.setVisible(false);
        flongitudeError.setVisible(false);
        fuserError.setVisible(false);
    }

    private boolean validateFields() {
        boolean isValid = true;

        String name = fname.getText().trim();
        if (name.isEmpty() || name.length() < 3) {
            fnameError.setText("Le nom doit contenir au moins 3 caractères");
            fnameError.setVisible(true);
            fname.requestFocus();
            isValid = false;
        }

        String sizeStr = fsize.getText().trim();
        if (sizeStr.isEmpty()) {
            fsizeError.setText("La superficie est requise");
            fsizeError.setVisible(true);
            if (isValid) {
                fsize.requestFocus();
                isValid = false;
            }
        } else {
            try {
                int size = Integer.parseInt(sizeStr);
                if (size <= 0) {
                    fsizeError.setText("La superficie doit être un nombre positif");
                    fsizeError.setVisible(true);
                    if (isValid) {
                        fsize.requestFocus();
                        isValid = false;
                    }
                }
            } catch (NumberFormatException e) {
                fsizeError.setText("Veuillez saisir un nombre entier valide");
                fsizeError.setVisible(true);
                if (isValid) {
                    fsize.requestFocus();
                    isValid = false;
                }
            }
        }

        String location = flocation.getText().trim();
        if (location.isEmpty()) {
            flocationError.setText("L'emplacement est requis");
            flocationError.setVisible(true);
            if (isValid) {
                flocation.requestFocus();
                isValid = false;
            }
        }

        if (selectedImageFile == null && (currentImagePath == null || currentImagePath.isEmpty())) {
            fimageError.setText("Une image est requise");
            fimageError.setVisible(true);
            if (isValid) {
                btnSelectImage.requestFocus();
                isValid = false;
            }
        }

        String description = fdescription.getText().trim();
        if (description.isEmpty() || description.length() < 10) {
            fdescriptionError.setText("La description doit contenir au moins 10 caractères");
            fdescriptionError.setVisible(true);
            if (isValid) {
                fdescription.requestFocus();
                isValid = false;
            }
        }

        String latStr = flatitude.getText().trim();
        if (latStr.isEmpty()) {
            flatitudeError.setText("La latitude est requise");
            flatitudeError.setVisible(true);
            if (isValid) {
                flatitude.requestFocus();
                isValid = false;
            }
        } else {
            try {
                double lat = Double.parseDouble(latStr);
                if (lat < -90 || lat > 90) {
                    flatitudeError.setText("La latitude doit être entre -90 et 90");
                    flatitudeError.setVisible(true);
                    if (isValid) {
                        flatitude.requestFocus();
                        isValid = false;
                    }
                }
            } catch (NumberFormatException e) {
                flatitudeError.setText("Format numérique invalide");
                flatitudeError.setVisible(true);
                if (isValid) {
                    flatitude.requestFocus();
                    isValid = false;
                }
            }
        }

        String longStr = flongitude.getText().trim();
        if (longStr.isEmpty()) {
            flongitudeError.setText("La longitude est requise");
            flongitudeError.setVisible(true);
            if (isValid) {
                flongitude.requestFocus();
                isValid = false;
            }
        } else {
            try {
                double lon = Double.parseDouble(longStr);
                if (lon < -180 || lon > 180) {
                    flongitudeError.setText("La longitude doit être entre -180 et 180");
                    flongitudeError.setVisible(true);
                    if (isValid) {
                        flongitude.requestFocus();
                        isValid = false;
                    }
                }
            } catch (NumberFormatException e) {
                flongitudeError.setText("Format numérique invalide");
                flongitudeError.setVisible(true);
                if (isValid) {
                    flongitude.requestFocus();
                    isValid = false;
                }
            }
        }

        String userStr = fuser.getText().trim();
        if (userStr.isEmpty()) {
            fuserError.setText("L'ID utilisateur est requis");
            fuserError.setVisible(true);
            if (isValid) {
                fuser.requestFocus();
                isValid = false;
            }
        } else {
            try {
                int userId = Integer.parseInt(userStr);
                if (userId <= 0) {
                    fuserError.setText("L'ID utilisateur doit être positif");
                    fuserError.setVisible(true);
                    if (isValid) {
                        fuser.requestFocus();
                        isValid = false;
                    }
                }
            } catch (NumberFormatException e) {
                fuserError.setText("Format numérique invalide");
                fuserError.setVisible(true);
                if (isValid) {
                    fuser.requestFocus();
                    isValid = false;
                }
            }
        }

        return isValid;
    }

    @FXML
    private void cancelUpdate() {
        try {
            Stage mainStage = test.getPrimaryStage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Farm/ListeFarms.fxml"));
            Parent root = loader.load();
            mainStage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load farm list: " + e.getMessage());
        }
    }

    private void showInfoAlert(String message) {
        showAlert(Alert.AlertType.INFORMATION, "Succès", message);
    }

    private void showErrorAlert(String message) {
        showAlert(Alert.AlertType.ERROR, "Erreur", message);
    }

    private void showWarningAlert(String message) {
        showAlert(Alert.AlertType.WARNING, "Avertissement", message);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}