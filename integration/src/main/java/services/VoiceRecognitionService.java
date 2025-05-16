package services;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.json.JSONObject;
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

public class VoiceRecognitionService extends Service<Void> {

    private static Model sharedModel = null;
    private static boolean modelLoading = false;
    private static boolean modelLoaded = false;

    private final Consumer<String> onResultCallback;
    private volatile boolean running = false;

    public VoiceRecognitionService(Consumer<String> onResultCallback) {
        this.onResultCallback = onResultCallback;

        // Charger le modèle en arrière-plan une seule fois
        if (sharedModel == null && !modelLoading) {
            modelLoading = true;

            new Thread(() -> {
                try {
                    Path modelPath = Paths.get("models/vosk-model-fr-0.22");

                    if (!Files.exists(modelPath)) {
                        System.err.println("Modèle Vosk non trouvé.");
                        Platform.runLater(() -> onResultCallback.accept("Erreur: Modèle non trouvé"));
                        return;
                    }

                    LibVosk.setLogLevel(LogLevel.INFO);
                    sharedModel = new Model(modelPath.toString());
                    modelLoaded = true;
                    System.out.println("Modèle Vosk chargé.");

                } catch (IOException e) {
                    e.printStackTrace();
                    Platform.runLater(() -> onResultCallback.accept("Erreur lors du chargement du modèle: " + e.getMessage()));
                }
            }).start();
        }
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<>() {
            @Override
            protected Void call() {
                if (!modelLoaded || sharedModel == null) {
                    Platform.runLater(() -> onResultCallback.accept("Le modèle n'est pas encore prêt..."));
                    return null;
                }

                try {
                    AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
                    DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

                    if (!AudioSystem.isLineSupported(info)) {
                        Platform.runLater(() -> onResultCallback.accept("Microphone non disponible"));
                        return null;
                    }

                    TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
                    line.open(format);
                    line.start();

                    Recognizer recognizer = new Recognizer(sharedModel, 16000);
                    byte[] buffer = new byte[4096];
                    running = true;

                    long startTime = System.currentTimeMillis();
                    String result = "";

                    while (running && (System.currentTimeMillis() - startTime < 10000)) {
                        int bytesRead = line.read(buffer, 0, buffer.length);

                        if (bytesRead > 0 && recognizer.acceptWaveForm(buffer, bytesRead)) {
                            JSONObject jsonResult = new JSONObject(recognizer.getResult());
                            result = jsonResult.getString("text");
                            if (!result.isEmpty()) break;
                        }
                    }

                    line.stop();
                    line.close();
                    recognizer.close();

                    String finalResult = result;
                    Platform.runLater(() -> onResultCallback.accept(finalResult));

                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> onResultCallback.accept("Erreur de reconnaissance vocale"));
                }

                return null;
            }
        };
    }

    public void stopRecognition() {
        running = false;
    }

    public static boolean isModelReady() {
        return modelLoaded;
    }
}
