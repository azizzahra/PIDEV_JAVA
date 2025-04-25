package controller.Farm;

import Main.mainPrincipal;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Farm;
import services.FarmService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.regex.Pattern;

import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;

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
    private WebView mapWebView;
    private WebEngine webEngine;

    private final String UPLOAD_DIR = "C:/xampp/htdocs/uploads/farm_image/";
    private final String RELATIVE_PATH = "uploads/farm_image/";

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
        setupConsoleMessageHandling();
    }

    @FXML
    private void selectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");

        // Configurer les filtres d'extension pour n'accepter que les images
        FileChooser.ExtensionFilter imageFilter =
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif");
        fileChooser.getExtensionFilters().add(imageFilter);

        // Ouvrir le sélecteur de fichier
        selectedImageFile = fileChooser.showOpenDialog(btnSelectImage.getScene().getWindow());

        if (selectedImageFile != null) {
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
                    flongitudeError.setText("La longitude doit être entre -90 et 90");
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

    private void initializeMap() {
        webEngine = mapWebView.getEngine();

        // Set explicit dimensions to ensure visibility
        mapWebView.setPrefHeight(300);
        mapWebView.setPrefWidth(300);
        mapWebView.setMinHeight(300);
        mapWebView.setMinWidth(300);
        webEngine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/91.0.4472.124");
        System.out.println("WebView dimensions: " + mapWebView.getWidth() + "x" + mapWebView.getHeight());
        System.out.println("WebView parent: " + (mapWebView.getParent() != null ? "OK" : "NULL"));

// Add listener to track rendering
        mapWebView.widthProperty().addListener((obs, oldVal, newVal) ->
                System.out.println("WebView width changed: " + oldVal + " -> " + newVal));
        mapWebView.heightProperty().addListener((obs, oldVal, newVal) ->
                System.out.println("WebView height changed: " + oldVal + " -> " + newVal));
        webEngine.setJavaScriptEnabled(true);

        // Create a more reliable HTML content with better error reporting
        String htmlContent =
                "<!DOCTYPE html>" +
                        "<html>" +
                        "<head>" +
                        "    <meta charset=\"UTF-8\">" +
                        "    <title>Farm Location Map</title>" +
                        "    <style>" +
                        "        html, body { height: 100%; margin: 0; padding: 0; }" +
                        "        #map-container { height: 280px; width: 100%; position: relative; border: 1px solid #ccc; }" +
                        "        #map { height: 100%; width: 100%; }" +
                        "        #status { padding: 5px; background: #f8f8f8; border-top: 1px solid #ccc; font-size: 12px; }" +
                        "        #error-display { color: red; padding: 10px; display: none; }" +
                        "    </style>" +
                        "</head>" +
                        "<body>" +
                        "    <div id=\"error-display\"></div>" +
                        "    <div id=\"map-container\">" +
                        "        <div id=\"map\"></div>" +
                        "    </div>" +
                        "    <div id=\"status\">Click on map to set location</div>" +
                        "    <!-- Load scripts at the end of body -->" +
                        "    <script>" +
                        "        // Error handling function" +
                        "        function showError(message) {" +
                        "            console.error(message);" +
                        "            var errorDiv = document.getElementById('error-display');" +
                        "            errorDiv.textContent = 'Error: ' + message;" +
                        "            errorDiv.style.display = 'block';" +
                        "        }" +
                        "        " +
                        "        // Initialize map only after scripts are loaded" +
                        "        function initMap() {" +
                        "            try {" +
                        "                if (!L) {" +
                        "                    showError('Leaflet library not loaded');" +
                        "                    return;" +
                        "                }" +
                        "                " +
                        "                // Create the map" +
                        "                var map = L.map('map').setView([36.8065, 10.1815], 7);" +
                        "                " +
                        "                // Add the tile layer (use a reliable tile source)" +
                        "                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {" +
                        "                    attribution: '© OpenStreetMap contributors'," +
                        "                    maxZoom: 19" +
                        "                }).addTo(map);" +
                        "                " +
                        "                // Force resize to render properly" +
                        "                setTimeout(function() { map.invalidateSize(); }, 200);" +
                        "                " +
                        "                // Add marker functionality" +
                        "                var marker;" +
                        "                map.on('click', function(e) {" +
                        "                    if (marker) {" +
                        "                        map.removeLayer(marker);" +
                        "                    }" +
                        "                    marker = L.marker(e.latlng).addTo(map);" +
                        "                    document.getElementById('status').textContent = " +
                        "                        'Selected: ' + e.latlng.lat.toFixed(6) + ', ' + e.latlng.lng.toFixed(6);" +
                        "                    " +
                        "                    // Call Java function if available" +
                        "                    if (window.javaConnector) {" +
                        "                        window.javaConnector.updateCoordinates(e.latlng.lat, e.latlng.lng);" +
                        "                    }" +
                        "                });" +
                        "                " +
                        "                // Success message" +
                        "                console.log('Map initialized successfully');" +
                        "            } catch (err) {" +
                        "                showError('Map initialization failed: ' + err.message);" +
                        "            }" +
                        "        }" +
                        "    </script>" +
                        "</body>" +
                        "</html>";

        // Load the HTML content first
        webEngine.loadContent(htmlContent);

        // Set up JavaScript bridge and load Leaflet after page loads
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                try {
                    // Set up the Java bridge
                    JSObject window = (JSObject) webEngine.executeScript("window");
                    window.setMember("javaConnector", new JavaConnector());
                    System.out.println("Java connector set up successfully");

                    // Now dynamically load Leaflet and initialize the map
                    webEngine.executeScript(
                            "// Load Leaflet CSS\n" +
                                    "var leafletCSS = document.createElement('link');\n" +
                                    "leafletCSS.rel = 'stylesheet';\n" +
                                    "leafletCSS.href = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css';\n" +
                                    "document.head.appendChild(leafletCSS);\n" +
                                    "\n" +
                                    "// Load Leaflet JS\n" +
                                    "var leafletScript = document.createElement('script');\n" +
                                    "leafletScript.src = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js';\n" +
                                    "leafletScript.onload = function() {\n" +
                                    "    console.log('Leaflet loaded successfully');\n" +
                                    "    // Initialize map after Leaflet is loaded\n" +
                                    "    initMap();\n" +
                                    "};\n" +
                                    "leafletScript.onerror = function() {\n" +
                                    "    document.getElementById('error-display').textContent = 'Failed to load Leaflet library';\n" +
                                    "    document.getElementById('error-display').style.display = 'block';\n" +
                                    "};\n" +
                                    "document.body.appendChild(leafletScript);\n"
                    );
                } catch (Exception e) {
                    System.err.println("Error setting up map: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        // Improved error handling
        webEngine.setOnAlert(event -> System.out.println("WebView Alert: " + event.getData()));
        webEngine.setOnError(event -> System.err.println("WebView Error: " + event.getMessage()));
        webEngine.getLoadWorker().exceptionProperty().addListener((obs, oldExc, newExc) -> {
            if (newExc != null) {
                System.err.println("WebView Exception: " + newExc.getMessage());
            }
        });
    }

    // Java bridge for JavaScript communication
    public class JavaConnector {
        public void updateCoordinates(final double lat, final double lng) {
            // Use Platform.runLater to update JavaFX UI elements from JavaScript thread
            javafx.application.Platform.runLater(() -> {
                try {
                    flatitude.setText(String.format("%.6f", lat));
                    flongitude.setText(String.format("%.6f", lng));

                    // Make coordinates visible and provide feedback
                    flatitudeError.setVisible(false);
                    flongitudeError.setVisible(false);

                    System.out.println("Coordinates updated: " + lat + ", " + lng);
                } catch (Exception e) {
                    System.err.println("Error updating coordinates: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }
    }
    private void setupConsoleMessageHandling() {
        webEngine.setOnAlert(event -> System.out.println("JS Alert: " + event.getData()));
        webEngine.setOnError(event -> System.err.println("JS Error: " + event.getMessage()));
    }
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
