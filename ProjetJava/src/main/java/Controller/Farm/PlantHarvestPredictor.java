package controller.Farm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class PlantHarvestPredictor {

    private static final String PYTHON_PATH = "plant_disease_env\\\\Scripts\\\\python.exe"; // or "python3" depending on your system
    private static final String SCRIPT_PATH = "plant_harvest_predictor.py"; // Update with actual path

    /**
     * Predicts harvest date using the Python script
     *
     * @param plantType The type of plant (Vegetables, Fruits, Flowers)
     * @param plantName The name of the plant
     * @param plantationDate The plantation date in LocalDate format
     * @return A JSON object containing the prediction results
     * @throws IOException If there's an error executing the Python script
     * @throws ParseException If there's an error parsing the JSON response
     */
    public static JSONObject predictHarvestDate(String plantType, String plantName, LocalDate plantationDate)
            throws IOException, ParseException {

        // Format the plantation date to YYYY-MM-DD
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = plantationDate.format(formatter);

        // Build the command to execute the Python script
        ProcessBuilder pb = new ProcessBuilder(
                PYTHON_PATH,
                SCRIPT_PATH,
                plantType,
                plantName,
                formattedDate);

        // Start the process
        Process process = pb.start();

        // Read the output from the Python script
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
        }

        // Parse the JSON output
        JSONParser parser = new JSONParser();
        JSONObject result = (JSONObject) parser.parse(output.toString());

        return result;
    }
}