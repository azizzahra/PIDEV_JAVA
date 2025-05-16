package Controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.post;
import services.PostService;
import services.ProfanityFilter;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.Collection;

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
    private final ProfanityFilter profanityFilter = new ProfanityFilter(); // Notre filtre de mots inappropriés

    private File selectedFile;

    @FXML
    public void initialize() {
        // Charger les catégories
        categoryCombo.getItems().addAll("AgroTech", "Matériel Agricole", "Énergies Vertes");

        // Récupérer l'utilisateur courant depuis la session (services.SessionManager)
        model.user currentUser = services.SessionManager.getCurrentUser();

        if (currentUser != null) {
            // Remplir automatiquement le champ avec l'ID utilisateur
            usernameField.setText(String.valueOf(currentUser.getId()));
            // Rendre le champ non modifiable pour empêcher l'édition
            usernameField.setEditable(false);
        }

        // Ajouter un écouteur pour vérifier le contenu du textarea en temps réel
        contentArea.textProperty().addListener((observable, oldValue, newValue) -> {
            // Vérifier si le nouveau texte contient des mots inappropriés
            if (profanityFilter.containsProfanity(newValue)) {
                contentArea.setStyle("-fx-border-color: red;");

                // Afficher un message discret sur le statut
                contentArea.setTooltip(new Tooltip("Le texte contient des mots inappropriés"));
            } else {
                contentArea.setStyle(""); // Remettre le style normal
                contentArea.setTooltip(null);
            }
        });

        // Faire la même chose pour le titre
        titleField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (profanityFilter.containsProfanity(newValue)) {
                titleField.setStyle("-fx-border-color: red;");
                titleField.setTooltip(new Tooltip("Le titre contient des mots inappropriés"));
            } else {
                titleField.setStyle("");
                titleField.setTooltip(null);
            }
        });
    }

    @FXML
    public void choisirFichier() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une pièce jointe");

        // Exemple : uniquement des fichiers image et PDF
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers Image et PDF", ".jpg", ".png", "*.pdf"));

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
        sanitizeContent();
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

            // Vérifier la présence de mots inappropriés dans le titre et le contenu
            if (profanityFilter.containsProfanity(title) || profanityFilter.containsProfanity(content)) {
                // Trouver les mots problématiques pour les afficher à l'utilisateur
                Collection<String> titleProfanities = profanityFilter.findProfanities(title);
                Collection<String> contentProfanities = profanityFilter.findProfanities(content);

                StringBuilder alertMessage = new StringBuilder("Votre message contient des mots inappropriés :\n\n");

                if (!titleProfanities.isEmpty()) {
                    alertMessage.append("Dans le titre : ").append(String.join(", ", titleProfanities)).append("\n");
                }

                if (!contentProfanities.isEmpty()) {
                    alertMessage.append("Dans le contenu : ").append(String.join(", ", contentProfanities)).append("\n");
                }

                alertMessage.append("\nVeuillez modifier votre texte avant de publier.");

                showAlert(Alert.AlertType.WARNING, "Contenu inapproprié", alertMessage.toString());
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

    /**
     * Option pour proposer une correction automatique du texte
     */
    @FXML
    public void sanitizeContent() {
        String title = titleField.getText();
        String content = contentArea.getText();

        if (profanityFilter.containsProfanity(title)) {
            String sanitizedTitle = profanityFilter.censorText(title);
            titleField.setText(sanitizedTitle);
        }

        if (profanityFilter.containsProfanity(content)) {
            String sanitizedContent = profanityFilter.censorText(content);
            contentArea.setText(sanitizedContent);
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