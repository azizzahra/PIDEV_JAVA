package services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Platform;
import org.json.JSONObject;

public class ChatbotService {
    // URL de l'API du chatbot (à remplacer par celle de votre choix)
    private final String API_URL = "https://api.cohere.ai/v1/chat";
    // Votre clé API (à remplacer)
    private final String API_KEY = "q4jCoXxACUB77XciEtdrHo9fRxRH6ky9wAPjDyjW";

    // Service d'exécution pour les appels asynchrones
    private final ExecutorService executorService;

    public ChatbotService() {
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public interface ChatResponseCallback {
        void onResponse(String response);
        void onError(String errorMessage);
    }

    public void sendMessage(String message, ChatResponseCallback callback) {
        CompletableFuture.runAsync(() -> {
            try {
                String response = callChatbotAPI(message);
                Platform.runLater(() -> callback.onResponse(response));
            } catch (IOException e) {
                Platform.runLater(() -> callback.onError("Erreur de communication avec le chatbot: " + e.getMessage()));
            }
        }, executorService);
    }

    private String callChatbotAPI(String message) throws IOException {
        URL url = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
        connection.setDoOutput(true);

        // Préparer le corps de la requête pour Cohere
        JSONObject requestBody = new JSONObject();

        // Pour l'API chat
        if (API_URL.contains("/chat")) {
            requestBody.put("message", "Je suis un utilisateur intéressé par l'agriculture intelligente. " + message);
            requestBody.put("model", "command"); // ou "command-light" pour la version plus légère
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 300);
        }
        // Pour l'API generate
        else {
            requestBody.put("prompt", "En tant qu'assistant agricole, réponds à cette question: " + message);
            requestBody.put("model", "command-light");
            requestBody.put("max_tokens", 300);
            requestBody.put("temperature", 0.7);
        }

        // Envoyer la requête
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Lire la réponse
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            // Analyser la réponse JSON de Cohere
            JSONObject jsonResponse = new JSONObject(response.toString());

            // Pour l'API chat
            if (API_URL.contains("/chat")) {
                return jsonResponse.getString("text");
            }
            // Pour l'API generate
            else {
                return jsonResponse.getJSONArray("generations")
                        .getJSONObject(0)
                        .getString("text");
            }
        }
    }

    public void shutdown() {
        executorService.shutdown();
    }
}