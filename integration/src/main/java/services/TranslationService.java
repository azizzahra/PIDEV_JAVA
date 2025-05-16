package services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service de traduction utilisant l'API LibreTranslate, une alternative gratuite et open-source
 * qui ne nécessite pas de clé API si vous utilisez le serveur public ou hébergez votre propre instance.
 */
public class TranslationService {

    private static final Logger LOGGER = Logger.getLogger(TranslationService.class.getName());

    // URL du serveur LibreTranslate public (vous pouvez également héberger votre propre instance)
    private static final String LIBRE_TRANSLATE_API_URL = "https://translate.argosopentech.com/translate";

    // URL alternative (certains serveurs LibreTranslate publics peuvent être plus stables que d'autres)
    private static final String LIBRE_TRANSLATE_API_URL_ALT = "https://libretranslate.de/translate";

    // URL de l'API MyMemory (gratuite pour un usage personnel avec des limites)
    private static final String MYMEMORY_API_URL = "https://api.mymemory.translated.net/get";

    // Sélectionnez l'API que vous souhaitez utiliser ici
    private static final String API_TO_USE = "LIBRE"; // Options: "LIBRE", "MYMEMORY"

    // Ajouter un drapeau pour basculer entre les serveurs LibreTranslate en cas d'échec
    private boolean useAlternateServer = false;

    /**
     * Traduit un texte de la langue source vers la langue cible
     * @param text Texte à traduire
     * @param sourceLanguage Langue source (code ISO-639-1, par ex. "en", "fr", "auto" pour détection)
     * @param targetLanguage Langue cible (code ISO-639-1, par ex. "en", "fr")
     * @return Le texte traduit ou null si une erreur survient
     */
    public String translateText(String text, String sourceLanguage, String targetLanguage) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        // Si le texte est trop long, le diviser en parties pour la traduction
        if (text.length() > 5000) {
            return translateLongText(text, sourceLanguage, targetLanguage);
        }

        switch (API_TO_USE) {
            case "MYMEMORY":
                return translateWithMyMemory(text, sourceLanguage, targetLanguage);
            case "LIBRE":
            default:
                try {
                    // Essayer d'abord avec le serveur principal
                    if (!useAlternateServer) {
                        String result = translateWithLibre(text, sourceLanguage, targetLanguage, LIBRE_TRANSLATE_API_URL);
                        if (result != null && !result.equals(text)) {
                            return result;
                        }
                        // Si échec, on bascule sur le serveur alternatif
                        useAlternateServer = true;
                    }

                    // Utiliser le serveur alternatif
                    return translateWithLibre(text, sourceLanguage, targetLanguage, LIBRE_TRANSLATE_API_URL_ALT);

                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Erreur avec les deux serveurs LibreTranslate, passage à MyMemory", e);
                    // En cas d'échec avec les deux serveurs, essayer avec MyMemory
                    return translateWithMyMemory(text, sourceLanguage, targetLanguage);
                }
        }
    }

    /**
     * Traduit un texte long en le divisant en parties plus petites
     * @param text Texte à traduire
     * @param sourceLanguage Langue source
     * @param targetLanguage Langue cible
     * @return Le texte traduit complet
     */
    /**
     * Traduit un texte long en le divisant en parties plus petites
     * @param text Texte à traduire
     * @param sourceLanguage Langue source
     * @param targetLanguage Langue cible
     * @return Le texte traduit complet
     */
    private String translateLongText(String text, String sourceLanguage, String targetLanguage) {
        // Réduire la taille des segments pour éviter les timeouts des APIs
        int chunkSize = 3000; // Réduit de 4000 à 3000 pour plus de fiabilité
        StringBuilder result = new StringBuilder();
        int totalChunks = (int) Math.ceil((double) text.length() / chunkSize);
        int successfulChunks = 0;
        int failedChunks = 0;

        // Pour garder une trace des délais entre les requêtes (augmentation progressive)
        int baseDelay = 500; // 500ms de délai initial
        int maxDelay = 3000; // 3000ms de délai maximum

        for (int i = 0; i < text.length(); i += chunkSize) {
            int end = Math.min(i + chunkSize, text.length());
            String chunk = text.substring(i, end);
            int chunkNumber = (i / chunkSize) + 1;

            // Message d'avancement pour l'interface utilisateur
            LOGGER.log(Level.INFO, "Traduction du segment {0}/{1}", new Object[]{chunkNumber, totalChunks});

            // Essayer jusqu'à 3 fois pour chaque segment
            String translatedChunk = null;
            for (int attempt = 1; attempt <= 3 && translatedChunk == null; attempt++) {
                try {
                    if (attempt > 1) {
                        LOGGER.log(Level.INFO, "Tentative {0} pour le segment {1}", new Object[]{attempt, chunkNumber});
                    }

                    // Traduire le segment selon l'API configurée
                    switch (API_TO_USE) {
                        case "MYMEMORY":
                            translatedChunk = translateWithMyMemory(chunk, sourceLanguage, targetLanguage);
                            break;
                        case "LIBRE":
                        default:
                            if (!useAlternateServer) {
                                translatedChunk = translateWithLibre(chunk, sourceLanguage, targetLanguage, LIBRE_TRANSLATE_API_URL);
                                if (translatedChunk == null || translatedChunk.equals(chunk)) {
                                    useAlternateServer = true;
                                    translatedChunk = translateWithLibre(chunk, sourceLanguage, targetLanguage, LIBRE_TRANSLATE_API_URL_ALT);
                                }
                            } else {
                                translatedChunk = translateWithLibre(chunk, sourceLanguage, targetLanguage, LIBRE_TRANSLATE_API_URL_ALT);
                            }

                            // Si les serveurs LibreTranslate échouent, essayer MyMemory comme dernier recours
                            if (translatedChunk == null || translatedChunk.equals(chunk)) {
                                translatedChunk = translateWithMyMemory(chunk, sourceLanguage, targetLanguage);
                            }
                            break;
                    }

                    // Comptabiliser les succès/échecs pour ajuster les délais si nécessaire
                    if (translatedChunk != null && !translatedChunk.isEmpty() && !translatedChunk.equals(chunk)) {
                        successfulChunks++;
                    } else {
                        failedChunks++;
                        translatedChunk = null; // Forcer une nouvelle tentative
                        throw new RuntimeException("Traduction non réussie pour le segment " + chunkNumber);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Échec de la tentative {0} pour le segment {1}: {2}",
                            new Object[]{attempt, chunkNumber, e.getMessage()});

                    // Augmenter le délai avant la prochaine tentative
                    try {
                        int retryDelay = Math.min(baseDelay * attempt, maxDelay);
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            // Si après toutes les tentatives, nous n'avons pas de traduction, utiliser le texte original
            if (translatedChunk == null || translatedChunk.isEmpty()) {
                LOGGER.log(Level.WARNING, "Échec de toutes les tentatives pour le segment {0}, utilisation du texte original", chunkNumber);
                translatedChunk = chunk;
            }

            result.append(translatedChunk).append(" ");

            // Calculer le délai adaptatif en fonction du taux de succès
            int adaptiveDelay = successfulChunks > 0
                    ? baseDelay * (1 + (failedChunks / successfulChunks))
                    : maxDelay;
            adaptiveDelay = Math.min(adaptiveDelay, maxDelay);

            // Attendre entre les segments pour ne pas surcharger les APIs
            try {
                LOGGER.log(Level.INFO, "Pause de {0}ms avant le prochain segment", adaptiveDelay);
                Thread.sleep(adaptiveDelay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return result.toString().trim();
    }

    /**
     * Traduit avec l'API LibreTranslate (gratuite et open-source)
     */
    private String translateWithLibre(String text, String sourceLanguage, String targetLanguage, String apiUrl) {
        if (sourceLanguage.equals("auto")) {
            sourceLanguage = "auto";  // LibreTranslate supporte la détection automatique
        }

        HttpURLConnection connection = null;
        try {
            URL url = new URL(apiUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setConnectTimeout(10000); // 10 secondes de timeout
            connection.setReadTimeout(10000);    // 10 secondes de timeout de lecture
            connection.setDoOutput(true);

            // Création du corps de la requête
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("q", text);
            requestBody.addProperty("source", sourceLanguage);
            requestBody.addProperty("target", targetLanguage);
            requestBody.addProperty("format", "text");

            // Log de débogage
            LOGGER.log(Level.INFO, "Requête de traduction: {0}", requestBody.toString());

            // Envoi de la requête
            try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8)) {
                writer.write(requestBody.toString());
                writer.flush();
            }

            // Lecture de la réponse
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    // Log de débogage
                    LOGGER.log(Level.INFO, "Réponse de l'API: {0}", response.toString());

                    // Parsing de la réponse JSON
                    JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
                    if (jsonResponse.has("translatedText")) {
                        return jsonResponse.get("translatedText").getAsString();
                    } else {
                        LOGGER.log(Level.WARNING, "Format de réponse inattendu: {0}", response.toString());
                        return text; // Retourner le texte original
                    }
                }
            } else {
                LOGGER.log(Level.WARNING, "Erreur lors de la traduction: code {0}", connection.getResponseCode());
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    LOGGER.log(Level.WARNING, "Message d'erreur: {0}", response.toString());
                }
                throw new RuntimeException("Erreur HTTP: " + connection.getResponseCode());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception lors de la traduction avec LibreTranslate", e);
            // En cas d'exception, on ne retourne pas le texte original mais null
            // pour signaler qu'il faut essayer avec une autre méthode
            throw new RuntimeException("Erreur de traduction avec LibreTranslate: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Traduit avec l'API MyMemory (gratuite pour un usage limité)
     */
    private String translateWithMyMemory(String text, String sourceLanguage, String targetLanguage) {
        HttpURLConnection connection = null;
        try {
            // Si sourceLanguage est "auto", MyMemory ne supporte pas vraiment "auto"
            // On peut utiliser la détection de langue ou par défaut utiliser l'anglais
            if (sourceLanguage.equals("auto")) {
                sourceLanguage = detectLanguage(text);
                if (sourceLanguage == null || sourceLanguage.isEmpty()) {
                    sourceLanguage = "en";
                }
            }

            // Préparation de l'URL avec les paramètres
            StringBuilder urlBuilder = new StringBuilder(MYMEMORY_API_URL);
            urlBuilder.append("?q=").append(URLEncoder.encode(text, StandardCharsets.UTF_8));
            urlBuilder.append("&langpair=").append(sourceLanguage).append("|").append(targetLanguage);

            // Vous pouvez ajouter votre email pour augmenter les limites de l'API
            // urlBuilder.append("&de=").append(URLEncoder.encode("votre@email.com", StandardCharsets.UTF_8));

            URL url = new URL(urlBuilder.toString());
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000); // 10 secondes de timeout
            connection.setReadTimeout(10000);    // 10 secondes de timeout de lecture

            // Lecture de la réponse
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    // Log de débogage
                    LOGGER.log(Level.INFO, "Réponse de MyMemory: {0}", response.toString());

                    // Parsing de la réponse JSON
                    JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
                    if (jsonResponse.has("responseData") &&
                            jsonResponse.getAsJsonObject("responseData").has("translatedText")) {
                        JsonObject responseData = jsonResponse.getAsJsonObject("responseData");
                        return responseData.get("translatedText").getAsString();
                    } else {
                        LOGGER.log(Level.WARNING, "Format de réponse MyMemory inattendu: {0}", response.toString());
                    }
                }
            } else {
                LOGGER.log(Level.WARNING, "Erreur lors de la traduction MyMemory: code {0}", connection.getResponseCode());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception lors de la traduction avec MyMemory", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        // En cas d'échec, retourner le texte original
        return text;
    }

    /**
     * Récupère la liste des langues supportées par l'API choisie
     * @return Map contenant les codes de langue (ISO-639-1) comme clés et les noms de langue comme valeurs
     */
    public Map<String, String> getSupportedLanguages() {
        Map<String, String> languages = new HashMap<>();

        // Langues les plus courantes (supportées par toutes les API)
        languages.put("fr", "Français");
        languages.put("en", "Anglais");
        languages.put("es", "Espagnol");
        languages.put("de", "Allemand");
        languages.put("it", "Italien");
        languages.put("pt", "Portugais");
        languages.put("ru", "Russe");
        languages.put("zh", "Chinois");
        languages.put("ja", "Japonais");
        languages.put("ar", "Arabe");
        languages.put("ko", "Coréen");
        languages.put("nl", "Néerlandais");
        languages.put("sv", "Suédois");
        languages.put("pl", "Polonais");
        languages.put("tr", "Turc");
        languages.put("he", "Hébreu");
        languages.put("hi", "Hindi");

        return languages;
    }

    /**
     * Méthode pour détecter la langue d'un texte (utile pour l'option "auto")
     * Fonctionne avec l'API LibreTranslate
     * @param text Texte à analyser
     * @return Code de langue détecté ou "en" par défaut
     */
    public String detectLanguage(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "en"; // Par défaut
        }

        HttpURLConnection connection = null;
        try {
            URL url = new URL("https://translate.argosopentech.com/detect");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setConnectTimeout(10000); // 10 secondes de timeout
            connection.setReadTimeout(10000);    // 10 secondes de timeout de lecture
            connection.setDoOutput(true);

            // Création du corps de la requête
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("q", text);

            // Envoi de la requête
            try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8)) {
                writer.write(requestBody.toString());
                writer.flush();
            }

            // Lecture de la réponse
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    // Parsing de la réponse JSON
                    JsonArray jsonResponse = JsonParser.parseString(response.toString()).getAsJsonArray();
                    if (jsonResponse.size() > 0) {
                        JsonObject detection = jsonResponse.get(0).getAsJsonObject();
                        return detection.get("language").getAsString();
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception lors de la détection de langue", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return "en"; // Par défaut
    }

}