package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.post;
import services.PostService;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;

public class AjoutController {

    @FXML private TextField titleField;
    @FXML private TextField usernameField;
    @FXML private TextArea contentArea;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextField attachmentField;
    @FXML private DatePicker datePicker;
    @FXML private Button choisirFichierBtn;

    private String attachmentFilePath; // Variable pour le chemin du fichier joint

    private final PostService postService = new PostService();

    private File selectedFile;

    @FXML
    public void initialize() {
        categoryCombo.getItems().addAll("AgroTech", "Matériel Agricole", "Énergies Vertes");
    }

    @FXML
    public void choisirFichier() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une pièce jointe");

        // Exemple : uniquement des fichiers image et PDF
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers Image et PDF", "*.jpg", "*.png", "*.pdf"));

        File file = fileChooser.showOpenDialog(choisirFichierBtn.getScene().getWindow());

        if (file != null) {
            selectedFile = file;
            String fileName = file.getName();
            attachmentField.setText(fileName);
            attachmentFilePath = file.toPath().toString(); // Chemin complet du fichier

            // Copier le fichier dans le répertoire "uploads/"
            try {
                Path destination = Paths.get("uploads", fileName);
                Files.createDirectories(destination.getParent());
                Files.copy(file.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur de copie", "Impossible de copier la pièce jointe : " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleAddPost() {
        try {
            String title = titleField.getText();
            String usernameText = usernameField.getText();
            String content = contentArea.getText();
            String category = categoryCombo.getValue();
            String attachment = attachmentField.getText();
            LocalDate date = datePicker.getValue();

            // Vérifier si tous les champs obligatoires sont remplis
            if (title.isEmpty() || content.isEmpty() || category == null || date == null || usernameText.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Champs manquants", "Veuillez remplir tous les champs obligatoires.");
                return;
            }

            // Vérifier si l'ID utilisateur est un entier
            int usernameId;
            try {
                usernameId = Integer.parseInt(usernameText);
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "L'ID utilisateur doit être un entier.");
                return;
            }

            // Créer un nouvel objet Post
            post newPost = new post();
            newPost.setTitle(title);
            newPost.setUsernameId(usernameId);
            newPost.setContent(content);
            newPost.setCategory(category);
            newPost.setAttachment(attachment);
            newPost.setDateC(date.toString());
            newPost.setViews(0);
            newPost.setVote(0);

            // Ajouter le post à la base de données via le service
            postService.add(newPost);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Post ajouté avec succès !");

            // Fermer la fenêtre
            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue : " + e.getMessage());
        }
    }

    // Méthode pour afficher des alertes d'erreur ou de succès
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
