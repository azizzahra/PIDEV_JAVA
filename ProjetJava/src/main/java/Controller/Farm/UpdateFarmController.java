package controller.Farm;

import Main.mainPrincipal;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

    private final String UPLOAD_DIR = "C:/xampp/htdocs/uploads/farm_image/";
    private final String RELATIVE_PATH = "uploads/farm_image/";
    private Preferences prefs = Preferences.userNodeForPackage(AddFarmController.class);

    private File selectedImageFile;
    private String currentImagePath;

    @FXML
    public void initialize() {
        // Vérifier si les composants FXML sont bien chargés
        if (btnUpdateFarm == null) {
            System.out.println("Erreur : modifierButton est null !");
        } else {
            btnUpdateFarm.setDisable(false);
        }

        setupValidationListeners();
        btnSelectImage.setOnAction(event -> selectImage());
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

            // Afficher le chemin du fichier dans le champ texte
            fimage.setText(selectedImageFile.getName());

            // Afficher l'aperçu de l'image
            try {
                Image image = new Image(selectedImageFile.toURI().toString());
                imagePreview.setImage(image);
                imagePreview.setFitWidth(200);
                imagePreview.setFitHeight(150);
                imagePreview.setPreserveRatio(true);
                imagePreview.setVisible(true);

                // Validation ok - masquer message d'erreur
                fimageError.setVisible(false);
            } catch (Exception e) {
                fimageError.setText("Impossible de charger l'image");
                fimageError.setVisible(true);
            }
        }
    }

    // Méthode pour sauvegarder l'image sélectionnée
    private String saveImage() throws IOException {
        if (selectedImageFile == null) {
            return currentImagePath; // Retourner le chemin actuel si aucune nouvelle image n'est sélectionnée
        }

        // Générer un nom de fichier unique (timestamp + nom original)
        String timestamp = String.valueOf(System.currentTimeMillis());
        String originalFileName = selectedImageFile.getName();
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf('.'));
        String newFileName = timestamp + fileExtension;

        // Chemin de destination
        Path destPath = Paths.get(UPLOAD_DIR + newFileName);

        // Copier le fichier vers le répertoire d'upload
        Files.copy(selectedImageFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);

        // Retourner le chemin relatif pour l'accès via l'application
        return RELATIVE_PATH + newFileName;
    }


    private int FarmId;
    public void setFarmId(int id) {
        this.FarmId = id;
        loadFarmData(); // Charger les données quand l'ID est défini
    }

    private void loadFarmData() {
        try {
            FarmService service = new FarmService();
            Farm farm = service.getone(FarmId);

            if (farm != null) {
                this.FarmId = farm.getId(); // stocke en interne
                fname.setText(farm.getName());
                fsize.setText(String.valueOf(farm.getSize()));
                flocation.setText(farm.getLocation());
                fimage.setText(farm.getImage());
                currentImagePath = farm.getImage(); // Stocke le chemin de l'image actuelle
                fdescription.setText(farm.getDescription());
                flatitude.setText(String.valueOf(farm.getLatitude()));
                flongitude.setText(String.valueOf(farm.getLongitude()));
                fuser.setText(String.valueOf(farm.getUserId()));

                // Charger l'aperçu de l'image existante si disponible
                if (currentImagePath != null && !currentImagePath.isEmpty()) {
                    try {
                        // Construire le chemin complet vers l'image
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
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void UpdateFarmAction(ActionEvent event) {
        try {
            // Masquer tous les messages d'erreur existants
            hideAllErrorMessages();

            // Validation des champs
            boolean isValid = validateFields();
            if (!isValid) {
                return;
            }

            // Vérifier l'ID de l'événement
            Integer idfarm = this.FarmId;
            if (idfarm==null) {
                showWarningAlert("Veuillez entrer l'ID de l'événement.");
                return;
            }


            // Récupérer les autres champs
            String name = fname.getText().trim();
            String description = fdescription.getText().trim();
            String location = flocation.getText().trim();
            int size = Integer.parseInt(fsize.getText().trim());
            String image = fimage.getText().trim();
            Double latitude = Double.parseDouble(flatitude.getText().trim());
            Double longitude = Double.parseDouble(flongitude.getText().trim());
            Integer userid = Integer.parseInt(fuser.getText().trim());

            // Vérifier que les champs obligatoires sont remplis
            if (name.isEmpty() || description.isEmpty() || location.isEmpty() || size < 1 || image.isEmpty() || latitude.isNaN() || longitude.isNaN()) {
                showWarningAlert("Veuillez remplir tous les champs obligatoires.");
                return;
            }
            // Sauvegarder l'image sélectionnée et récupérer son chemin
            String imagePath;
            if (selectedImageFile != null) {
                imagePath = saveImage();
                if (imagePath.isEmpty()) {
                    fimageError.setText("Erreur lors de l'enregistrement de l'image");
                    fimageError.setVisible(true);
                    return;
                }
            } else {
                // Conserver l'image existante
                imagePath = currentImagePath;
            }


            // Créer l'événement mis à jour
            Farm farm = new Farm(
                    idfarm, name, size, location, imagePath, description, latitude, longitude, userid
            );

            // Mettre à jour l'événement
            FarmService service = new FarmService();
            int rowsAffected = service.update(farm);

            if (rowsAffected > 0) {
                showInfoAlert("La ferme a été mise à jour avec succès !");

                // Ne pas fermer le stage, juste charger la vue ListeFarms
                Stage mainStage = mainPrincipal.getPrimaryStage();

                /// Charger la vue de détails de la ferme
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Farm/FarmDetails.fxml"));
                Parent root = loader.load();

                // Configurer le contrôleur avec l'ID de la ferme
                FarmDetailsController controller = loader.getController();
                controller.setFarmId(idfarm);

                // Remplacer le contenu du stage principal
                mainStage.getScene().setRoot(root);
            } else {
                showErrorAlert("Aucun ferme trouvé avec cet ID.");
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
        // Validation en temps réel pour le nom
        fname.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty() || newValue.trim().length() < 3) {
                fnameError.setText("Le nom doit contenir au moins 3 caractères");
                fnameError.setVisible(true);
            } else {
                fnameError.setVisible(false);
            }
        });

        // Validation en temps réel pour la taille
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

        // Validation en temps réel pour l'emplacement
        flocation.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                flocationError.setText("L'emplacement est requis");
                flocationError.setVisible(true);
            } else {
                flocationError.setVisible(false);
            }
        });

        // Validation en temps réel pour l'URL de l'image
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

        // Validation en temps réel pour la description
        fdescription.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty() || newValue.trim().length() < 10) {
                fdescriptionError.setText("La description doit contenir au moins 10 caractères");
                fdescriptionError.setVisible(true);
            } else {
                fdescriptionError.setVisible(false);
            }
        });

        // Validation en temps réel pour la latitude
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

        // Validation en temps réel pour la longitude
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

        // Validation en temps réel pour l'ID utilisateur
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

        // Validation du nom (obligatoire, min 3 caractères)
        String name = fname.getText().trim();
        if (name.isEmpty() || name.length() < 3) {
            fnameError.setText("Le nom doit contenir au moins 3 caractères");
            fnameError.setVisible(true);
            fname.requestFocus();
            isValid = false;
        }

        // Validation de la taille (obligatoire, doit être un nombre positif)
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

        // Validation de l'emplacement (obligatoire)
        String location = flocation.getText().trim();
        if (location.isEmpty()) {
            flocationError.setText("L'emplacement est requis");
            flocationError.setVisible(true);
            if (isValid) {
                flocation.requestFocus();
                isValid = false;
            }
        }

        // Validation de l'URL de l'image (format URL)
        if (selectedImageFile == null && (currentImagePath == null || currentImagePath.isEmpty())) {
            fimageError.setText("Une image est requise");
            fimageError.setVisible(true);
            if (isValid) {
                btnSelectImage.requestFocus();
                isValid = false;
            }
        }

        // Validation de la description (obligatoire, min 10 caractères)
        String description = fdescription.getText().trim();
        if (description.isEmpty() || description.length() < 10) {
            fdescriptionError.setText("La description doit contenir au moins 10 caractères");
            fdescriptionError.setVisible(true);
            if (isValid) {
                fdescription.requestFocus();
                isValid = false;
            }
        }

        // Validation de la latitude (format numérique entre -90 et 90)
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

        // Validation de la longitude (format numérique entre -180 et 180)
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

        // Validation de l'ID utilisateur (obligatoire, nombre positif)
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