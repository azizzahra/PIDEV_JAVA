package Controller;
import Main.mainPrincipal;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.Farm;
import services.FarmService;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.stage.FileChooser;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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


    private final String UPLOAD_DIR = "src/resources/uploads/farms/";
    private File selectedImageFile;

    @FXML
    public void initialize() {
        setupValidationListeners();
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
        return "uploads/farms/" + newFileName;
    }



    @FXML
    void AddFarmAction(ActionEvent event) {
        try {
            // Masquer tous les messages d'erreur existants
            hideAllErrorMessages();

            // Validation des champs
            boolean isValid = validateFields();
            if (!isValid) {
                return;
            }

            // Récupérer les valeurs du formulaire
            String name = fname.getText().trim();
            int size = Integer.parseInt(fsize.getText().trim());
            String location = flocation.getText().trim();
            double longitude = Double.parseDouble(flongitude.getText().trim());
            double latitude = Double.parseDouble(flatitude.getText().trim());
            String image = fimage.getText().trim();
            String description = fdescription.getText().trim();
            Integer userid = Integer.parseInt(fuser.getText().trim());

            // Vérifier si la ferme existe déjà
            FarmService service = new FarmService();
            if (service.farmExiste(name)) {
                // Afficher un message d'erreur si la ferme existe déjà
                fnameError.setText("Cette ferme existe déjà");
                fnameError.setVisible(true);
                fname.requestFocus();
                return;
            }
            // Sauvegarder l'image sélectionnée et récupérer son chemin
            String imagePath = "";
            if (selectedImageFile != null) {
                imagePath = saveImage();
                if (imagePath.isEmpty()) {
                    fimageError.setText("Erreur lors de l'enregistrement de l'image");
                    fimageError.setVisible(true);
                    return;
                }
            }

            // Créer l'objet Evenement
            Farm farm = new Farm(
                    0, // L'ID sera auto-généré par la base de données
                    name,
                    size,
                    location,
                    imagePath, // Utiliser le chemin de l'image sauvegardée
                    description,
                    latitude,
                    longitude,
                    userid
            );

            // Sauvegarder la ferme dans la base de données
            service.add(farm);

            // Afficher un message de succès
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Ferme ajoutée avec succès !");


            // Ne pas fermer le stage, juste charger la vue ListeFarms
            Stage mainStage = mainPrincipal.getPrimaryStage();

            // Rechargez la vue principale
            FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("/ListeFarms.fxml"));
            Parent mainRoot = mainLoader.load();

            // Mettre à jour la scène du stage principal
            mainStage.getScene().setRoot(mainRoot);

            // Assurez-vous que le contrôleur est initialisé et rafraîchi
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

        // Validation de l'image (facultative mais doit être une image valide si sélectionnée)
        if (selectedImageFile == null) {
            fimageError.setText("Une image est requise");
            fimageError.setVisible(true);
            if (isValid) {
                btnSelectImage.requestFocus();
                isValid = false;
            }
        } else {
            // Vérifiez si c'est un format d'image valide (code existant)
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

        // Validation de la longitude (format numérique entre -90 et 90)
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

        // Validation de user requis
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


    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
