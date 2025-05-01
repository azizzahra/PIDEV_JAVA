package controller.Farm;

import Main.mainPrincipal;
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
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

public class AddFarmController {

    @FXML
    private Button btnAddFarm;

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
    private static JavaFXCallback callbackInstance;


    private final String UPLOAD_DIR = "C:/xampp/htdocs/uploads/farm_image/";
    private final String RELATIVE_PATH = "uploads/farm_image/";

    private Preferences prefs = Preferences.userNodeForPackage(AddFarmController.class);

    private File selectedImageFile;
    private void createUploadDirectory() {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("Répertoire d'upload créé : " + uploadPath.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la création du répertoire d'upload : " + e.getMessage());
        }
    }
    @FXML
    public void initialize() {
        setupValidationListeners();
        createUploadDirectory();
        initializeMap();

    }
    public class JavaFXCallback {
        public void call(double lat, double lng, String cityName) {
            System.out.println("Callback received from OpenStreetMap: Lat=" + lat + ", Lng=" + lng + ", City=" + cityName);

            javafx.application.Platform.runLater(() -> {
                try {
                    flatitude.setText(String.format("%.6f", lat).replace(',', '.')); // Remplacer la virgule par un point
                    flongitude.setText(String.format("%.6f", lng).replace(',', '.')); // Remplacer la virgule par un point

                    if (cityName != null && !cityName.isEmpty()) {
                        flocation.setText(cityName);
                        flocationError.setVisible(false);
                        System.out.println("Location field updated with city name: " + cityName);
                    } else {
                        flocation.setText("");
                        flocation.setPromptText("Ville (saisie manuelle)");
                        System.out.println("Empty city name received, placeholder updated for manual input");
                    }

                    flatitudeError.setVisible(false);
                    flongitudeError.setVisible(false);

                    System.out.println("UI updated with new coordinates and city: " + lat + ", " + lng + ", " + cityName);
                } catch (Exception e) {
                    System.err.println("Error updating UI with coordinates and city: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }

        // Overload for backward compatibility (if needed)
        public void call(double lat, double lng) {
            call(lat, lng, "");
        }
    }
    private void initializeMap() {
        try {
            // Get the WebEngine
            WebEngine engine = mapView.getEngine();

            // Activer explicitement JavaScript
            engine.setJavaScriptEnabled(true);

            // Créer une seule instance de callback qui persistera
            if (callbackInstance == null) {
                callbackInstance = new JavaFXCallback();
            }

            // Charger la page HTML
            URL mapResource = getClass().getResource("/views/Farm/map.html");
            if (mapResource != null) {
                String mapUrl = mapResource.toExternalForm();
                System.out.println("Loading map from: " + mapUrl);
                engine.load(mapUrl);
            } else {
                // Essayer les alternatives comme dans votre code original
                mapResource = getClass().getClassLoader().getResource("views/Farm/map.html");
                if (mapResource != null) {
                    String mapUrl = mapResource.toExternalForm();
                    System.out.println("Loading map from alternate path: " + mapUrl);
                    engine.load(mapUrl);
                } else {
                    // Essayer de charger depuis le système de fichiers
                    String projectPath = System.getProperty("user.dir");
                    File mapFile = new File(projectPath + "/src/main/resources/views/Farm/map.html");
                    if (mapFile.exists()) {
                        System.out.println("Loading map from file system: " + mapFile.toURI().toString());
                        engine.load(mapFile.toURI().toString());
                    } else {
                        System.err.println("Could not find map.html resource!");
                        showAlert(Alert.AlertType.ERROR, "Error", "Could not find map.html resource");
                    }
                }
            }

            // Configurer le pont JavaScript une fois la page chargée
            engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                    setupJavaScriptBridge(engine);
                } else if (newState == javafx.concurrent.Worker.State.FAILED) {
                    System.err.println("Failed to load map: " + engine.getLoadWorker().getException());
                    if (engine.getLoadWorker().getException() != null) {
                        engine.getLoadWorker().getException().printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("Error initializing map: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to initialize map: " + e.getMessage());
        }
    }

    // Nouvelle méthode pour configurer le pont JavaScript - peut être appelée plusieurs fois
    private void setupJavaScriptBridge(WebEngine engine) {
        try {
            // Get the JavaScript window object
            JSObject window = (JSObject) engine.executeScript("window");

            // Debug: Vérifier que window est bien obtenu
            System.out.println("JavaScript window object: " + (window != null ? "obtained" : "null"));

            // Installer le callback existant
            window.setMember("javafxCallback", callbackInstance);

            // Debug: Confirmer que le callback est bien installé
            System.out.println("JavaFX callback installed");

            // Vérifier le bridge avec un script de test
            engine.executeScript(
                    "console.log('Testing JavaFX bridge');" +
                            "if (window.javafxCallback) {" +
                            "  console.log('Bridge available');" +
                            "  window.bridgeAvailable = true;" +
                            "} else {" +
                            "  console.log('Bridge NOT available');" +
                            "  window.bridgeAvailable = false;" +
                            "}"
            );

            // Réparer le bridge dans l'iframe OpenStreetMap
            engine.executeScript(
                    "document.getElementById('osm-iframe').onload = function() {" +
                            "  console.log('iframe reloaded, reestablishing callback');" +
                            "  setTimeout(function() {" +
                            "    console.log('Bridge available in parent: ' + window.bridgeAvailable);" +
                            "  }, 500);" +
                            "};"
            );

            System.out.println("Map initialized successfully with OpenStreetMap");

            // Définir la position initiale
            try {
                String latStr = flatitude.getText().trim();
                String lonStr = flongitude.getText().trim();

                double lat = latStr.isEmpty() ? 36.8065 : Double.parseDouble(latStr);
                double lon = lonStr.isEmpty() ? 10.1815 : Double.parseDouble(lonStr);

                System.out.println("Setting initial marker at: " + lat + ", " + lon);
                engine.executeScript("setMarker(" + lat + ", " + lon + ")");
            } catch (NumberFormatException e) {
                System.out.println("Using default coordinates due to invalid values: " + e.getMessage());
                engine.executeScript("setMarker(36.8065, 10.1815)");
            }
        } catch (Exception e) {
            System.err.println("Error in setting up JavaScript bridge: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void selectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");

        // Get last directory from preferences
        String lastDirectoryPath = prefs.get("lastImageDirectory", null);
        if (lastDirectoryPath != null) {
            File lastDir = new File(lastDirectoryPath);
            if (lastDir.exists()) {
                fileChooser.setInitialDirectory(lastDir);
            }
        }

        // Configurer les filtres d'extension pour n'accepter que les images
        FileChooser.ExtensionFilter imageFilter =
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif");
        fileChooser.getExtensionFilters().add(imageFilter);

        // Ouvrir le sélecteur de fichier
        selectedImageFile = fileChooser.showOpenDialog(btnSelectImage.getScene().getWindow());

        if (selectedImageFile != null) {
            prefs.put("lastImageDirectory", selectedImageFile.getParent());

            // Afficher le chemin du fichier dans le champ texte caché
            fimage.setText(selectedImageFile.getName());

            // Afficher l'aperçu de l'image
            try {
                Image image = new Image(selectedImageFile.toURI().toString());
                imagePreview.setImage(image);
                imagePreview.setVisible(true);

                // Validation ok - masquer message d'erreur
                fimageError.setVisible(false);
            } catch (Exception e) {
                e.printStackTrace(); // Add this to see detailed error
                fimageError.setText("Impossible de charger l'image");
                fimageError.setVisible(true);
            }
        }
    }

    // Méthode pour sauvegarder l'image sélectionnée
    private String saveImage() throws IOException {
        if (selectedImageFile == null) {
            return "";
        }

        String timestamp = String.valueOf(System.currentTimeMillis());
        String originalFileName = selectedImageFile.getName();
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf('.'));
        String newFileName = timestamp + fileExtension;

        Path destPath = Paths.get(UPLOAD_DIR + newFileName);

        Files.copy(selectedImageFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);

        return RELATIVE_PATH + newFileName;
    }



    @FXML
    void AddFarmAction(ActionEvent event) {
        try {
            hideAllErrorMessages();

            boolean isValid = validateFields();
            if (!isValid) {
                return;
            }

            String name = fname.getText().trim();
            int size = Integer.parseInt(fsize.getText().trim());
            String location = flocation.getText().trim();
            double longitude = Double.parseDouble(flongitude.getText().trim());
            double latitude = Double.parseDouble(flatitude.getText().trim());
            String description = fdescription.getText().trim();
            Integer userid = Integer.parseInt(fuser.getText().trim());

            FarmService service = new FarmService();
            if (service.farmExiste(name)) {
                fnameError.setText("Cette ferme existe déjà");
                fnameError.setVisible(true);
                fname.requestFocus();
                return;
            }

            String imagePath = "";
            if (selectedImageFile != null) {
                imagePath = saveImage();
                if (imagePath.isEmpty()) {
                    fimageError.setText("Erreur lors de l'enregistrement de l'image");
                    fimageError.setVisible(true);
                    return;
                }
            }

            Farm farm = new Farm(
                    0,
                    name,
                    size,
                    location,
                    imagePath,
                    description,
                    latitude,
                    longitude,
                    userid
            );

            service.add(farm);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Ferme ajoutée avec succès !");

            Stage mainStage = mainPrincipal.getPrimaryStage();

            FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("/views/Farm/ListeFarms.fxml"));
            Parent mainRoot = mainLoader.load();

            mainStage.getScene().setRoot(mainRoot);

            ListeFarmsController listController = mainLoader.getController();
            listController.refreshFarmList();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur s'est produite : " + e.getMessage());
            e.printStackTrace();
        }
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
                if (lat < -180 || lat > 180) {
                    flatitudeError.setText("La latitude doit être entre -180 et 180");
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

        if (selectedImageFile == null) {
            fimageError.setText("Une image est requise");
            fimageError.setVisible(true);
            if (isValid) {
                btnSelectImage.requestFocus();
                isValid = false;
            }
        } else {
            String filename = selectedImageFile.getName().toLowerCase();
            if (!(filename.endsWith(".jpg") || filename.endsWith(".jpeg") ||
                    filename.endsWith(".png") || filename.endsWith(".gif"))) {
                fimageError.setText("Format d'image non pris en charge");
                fimageError.setVisible(true);
                if (isValid) {
                    btnSelectImage.requestFocus();
                    isValid = false;
                }
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
                if (lat < -180 || lat > 180) {
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
                double lat = Double.parseDouble(longStr);
                if (lat < -180 || lat > 180) {
                    flongitudeError.setText("La longitude doit être entre -180 et 180");
                    flongitudeError.setVisible(true);
                    if (isValid) {
                        flatitude.requestFocus();
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
            fuserError.setText("La user est requise");
            fuserError.setVisible(true);
            if (isValid) {
                fuser.requestFocus();
                isValid = false;
            }
        }


        return isValid;
    }

    @FXML
    private void cancelAdd() {
        try {
            // Utiliser directement le stage principal
            Stage mainStage = mainPrincipal.getPrimaryStage();

            // Recharger la vue principale
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Farm/ListeFarms.fxml"));
            Parent root = loader.load();

            // Remplacer le contenu du stage principal
            mainStage.getScene().setRoot(root);
        } catch (IOException e) {
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
}