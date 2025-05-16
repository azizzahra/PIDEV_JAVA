package Controller;

import Controller.PostCardController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import model.post;
import services.ChatbotService;
import services.PostService;
import services.VoiceRecognitionService;

import java.io.IOException;
import java.util.List;

public class PostController {
    @FXML
    private VBox postListView;
    @FXML
    private VBox postCard;
    @FXML
    private ScrollPane scrollPane;

    // Références aux boutons de catégorie
    @FXML
    private Button btnAllPosts;
    @FXML
    private Button btnAgroTech;
    @FXML
    private Button btnMaterielAgricole;
    @FXML
    private Button btnEnergiesVertes;

    // Éléments pour la reconnaissance vocale
    @FXML
    private Button btnVoiceRecognition;
    @FXML
    private Label lblVoiceStatus;

    // Éléments pour le chatbot
    @FXML
    private ScrollPane chatScrollPane;
    @FXML
    private VBox chatMessagesBox;
    @FXML
    private TextField chatInputField;
    @FXML
    private Button sendMessageBtn;
    @FXML
    private Hyperlink suggestionLink1;
    @FXML
    private Hyperlink suggestionLink2;

    private VoiceRecognitionService voiceService;
    private final PostService postService = new PostService();
    private final ChatbotService chatbotService = new ChatbotService();
    private String currentCategory = null;

    public void initialize() {
        setupCategoryButtons();
        setupVoiceRecognition();
        setupChatbot();
        loadPosts(null);

        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }

    private void setupCategoryButtons() {
        btnAllPosts.setOnAction(event -> filterByCategory(null));
        btnAgroTech.setOnAction(event -> filterByCategory("AgroTech"));
        btnMaterielAgricole.setOnAction(event -> filterByCategory("Matériel Agricole"));
        btnEnergiesVertes.setOnAction(event -> filterByCategory("Énergies Vertes"));

        btnAllPosts.getStyleClass().add("active-category");
    }

    private void setupVoiceRecognition() {
        // Créer le service de reconnaissance vocale
        voiceService = new VoiceRecognitionService(this::processVoiceResult);

        // Configurer le bouton
        btnVoiceRecognition.setOnAction(event -> startVoiceRecognition());

        // Styler le bouton
        btnVoiceRecognition.getStyleClass().add("voice-btn");
    }

    private void setupChatbot() {
        // Configuration de l'action du bouton d'envoi
        sendMessageBtn.setOnAction(event -> sendChatMessage());

        // Définir l'action de la touche Entrée dans le champ de texte
        chatInputField.setOnAction(event -> sendChatMessage());

        // Ajouter un message d'accueil
        addBotMessage("Bonjour ! Je suis votre assistant agricole. Comment puis-je vous aider aujourd'hui ?");
    }

    // Dans le PostController.java, modifiez la méthode sendChatMessage() comme suit:

    @FXML
    private void sendChatMessage() {
        String userMessage = chatInputField.getText().trim();
        if (!userMessage.isEmpty()) {
            // Ajouter le message de l'utilisateur à l'interface
            addUserMessage(userMessage);

            // Ajouter un message de chargement
            HBox loadingContainer = new HBox();
            loadingContainer.setAlignment(Pos.CENTER_LEFT);
            loadingContainer.setPadding(new Insets(5, 50, 5, 5));

            Label loadingLabel = new Label("En train de réfléchir...");
            loadingLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #808080;");
            loadingContainer.getChildren().add(loadingLabel);

            chatMessagesBox.getChildren().add(loadingContainer);

            // Vider le champ de texte
            chatInputField.clear();

            // Envoi de la requête à l'API
            chatbotService.sendMessage(userMessage, new ChatbotService.ChatResponseCallback() {
                @Override
                public void onResponse(String response) {
                    // Supprimer le message de chargement
                    chatMessagesBox.getChildren().remove(loadingContainer);
                    // Ajouter la réponse du chatbot
                    addBotMessage(response);
                }

                @Override
                public void onError(String errorMessage) {
                    // Supprimer le message de chargement
                    chatMessagesBox.getChildren().remove(loadingContainer);
                    // Afficher le message d'erreur
                    addBotMessage("Désolé, je rencontre un problème technique. Veuillez réessayer plus tard.");
                    System.err.println(errorMessage);
                }
            });
        }
    }

    // N'oubliez pas d'ajouter cette méthode pour fermer proprement le service
    public void shutdown() {
        chatbotService.shutdown();
    }

    @FXML
    private void useSuggestion(javafx.event.ActionEvent event) {
        Hyperlink source = (Hyperlink) event.getSource();
        chatInputField.setText(source.getText());
        sendChatMessage();
    }

    private void addUserMessage(String message) {
        HBox messageContainer = new HBox();
        messageContainer.setAlignment(Pos.CENTER_RIGHT);
        messageContainer.setPadding(new Insets(5, 5, 5, 50));

        TextFlow textFlow = new TextFlow();
        textFlow.setStyle("-fx-background-color: #4d804d; -fx-background-radius: 15; -fx-padding: 8;");

        Text text = new Text(message);
        text.setFill(Color.WHITE);
        textFlow.getChildren().add(text);

        messageContainer.getChildren().add(textFlow);
        chatMessagesBox.getChildren().add(messageContainer);

        // Faire défiler automatiquement vers le bas
        chatScrollPane.setVvalue(1.0);
    }

    private void addBotMessage(String message) {
        HBox messageContainer = new HBox();
        messageContainer.setAlignment(Pos.CENTER_LEFT);
        messageContainer.setPadding(new Insets(5, 50, 5, 5));

        TextFlow textFlow = new TextFlow();
        textFlow.setStyle("-fx-background-color: #e6f2e6; -fx-background-radius: 15; -fx-padding: 8;");

        Text text = new Text(message);
        text.setFill(Color.BLACK);
        textFlow.getChildren().add(text);

        messageContainer.getChildren().add(textFlow);
        chatMessagesBox.getChildren().add(messageContainer);

        // Faire défiler automatiquement vers le bas
        chatScrollPane.setVvalue(1.0);
    }

    private void startVoiceRecognition() {
        if (!VoiceRecognitionService.isModelReady()) {
            lblVoiceStatus.setText("Chargement du modèle... Veuillez patienter.");
            return;
        }

        lblVoiceStatus.setText("Écoute en cours...");
        btnVoiceRecognition.setDisable(true);

        if (!voiceService.isRunning()) {
            voiceService.restart();
        }
    }

    private void processVoiceResult(String result) {
        btnVoiceRecognition.setDisable(false);

        result = result.toLowerCase().trim();  // On normalise un peu
        System.out.println("Texte reconnu après nettoyage: " + result);

        String category = null;

        if (result.contains("tous") || result.contains("tout")) {
            category = null;
            lblVoiceStatus.setText("Catégorie: Tous les posts");
        } else if (result.contains("agrotech") || result.contains("agro tech")) {
            category = "AgroTech";
            lblVoiceStatus.setText("Catégorie: AgroTech");
        } else if (result.contains("matériel") || result.contains("materiel")) {
            category = "Matériel Agricole";
            lblVoiceStatus.setText("Catégorie: Matériel Agricole");
        } else if (result.contains("énergie") || result.contains("energie")) {
            category = "Énergies Vertes";
            lblVoiceStatus.setText("Catégorie: Énergies Vertes");
        } else {
            lblVoiceStatus.setText("Catégorie non reconnue: " + result);
            return;
        }

        filterByCategory(category);
    }

    private void filterByCategory(String category) {
        currentCategory = category;
        resetCategoryButtonStyles();

        if (category == null) {
            btnAllPosts.getStyleClass().add("active-category");
        } else if (category.equals("AgroTech")) {
            btnAgroTech.getStyleClass().add("active-category");
        } else if (category.equals("Matériel Agricole")) {
            btnMaterielAgricole.getStyleClass().add("active-category");
        } else if (category.equals("Énergies Vertes")) {
            btnEnergiesVertes.getStyleClass().add("active-category");
        }

        loadPosts(category);
    }

    private void resetCategoryButtonStyles() {
        btnAllPosts.getStyleClass().remove("active-category");
        btnAgroTech.getStyleClass().remove("active-category");
        btnMaterielAgricole.getStyleClass().remove("active-category");
        btnEnergiesVertes.getStyleClass().remove("active-category");
    }

    private void loadPosts(String category) {
        postListView.getChildren().clear();

        List<post> allPosts = postService.afficher();

        List<post> filteredPosts;
        if (category == null) {
            filteredPosts = allPosts;
        } else {
            filteredPosts = allPosts.stream()
                    .filter(p -> category.equals(p.getCategory()))
                    .toList();
        }

        for (post p : filteredPosts) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Post/PostCard.fxml"));
                Node card = loader.load();

                PostCardController cardController = loader.getController();
                cardController.setPostData(p);

                postListView.getChildren().add(card);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void openAjoutPostWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Post/ajout.fxml"));
            BorderPane ajoutRoot = fxmlLoader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter un nouveau post");
            stage.setScene(new Scene(ajoutRoot));
            stage.showAndWait();

            loadPosts(currentCategory);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void refreshPostList() {
        loadPosts(currentCategory);
    }
}