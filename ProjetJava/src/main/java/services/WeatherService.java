package services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class WeatherService {

    // Weather data cache with timestamp for expiration
    private static final Map<String, CachedWeatherData> cache = new HashMap<>();

    /**
     * Gets the weather data for a specific location
     * @param location The name of the location/city
     * @return Map containing weather information
     * @throws Exception If an error occurs during API call
     */
    public Map<String, Object> getWeatherForLocation(String location) throws Exception {
        // Check cache first
        String cacheKey = location.toLowerCase().trim();
        CachedWeatherData cachedData = cache.get(cacheKey);

        // If we have valid cached data, return it
        if (isValidCache(cachedData)) {
            return cachedData.getData();
        }

        // Build the API request URL
        String encodedLocation = URLEncoder.encode(location, StandardCharsets.UTF_8.toString());
        String url = WeatherConfig.getBaseUrl() +
                "?q=" + encodedLocation +
                "&units=" + WeatherConfig.getUnits() +
                "&appid=" + WeatherConfig.getApiKey();

        // Make the API call
        String jsonResponse = makeApiCall(url);

        // Parse the response
        Map<String, Object> weatherData = parseWeatherJson(jsonResponse);

        // Cache the result
        cache.put(cacheKey, new CachedWeatherData(weatherData));

        return weatherData;
    }

    /**
     * Makes HTTP GET request to the API
     * @param urlString The URL to call
     * @return String response from the API
     * @throws Exception If connection fails
     */
    private String makeApiCall(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000); // 5 seconds timeout
        connection.setReadTimeout(5000);

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("API call failed with response code: " + responseCode);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            return response.toString();
        }
    }

    /**
     * Parses the JSON response into a Map
     * @param jsonResponse The JSON string from the API
     * @return Map containing the weather data
     * @throws Exception If parsing fails
     */
    private Map<String, Object> parseWeatherJson(String jsonResponse) throws Exception {
        Map<String, Object> weatherData = new HashMap<>();
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(jsonResponse);

        // Extract main weather data
        JSONObject main = (JSONObject) json.get("main");
        double temperature = ((Number) main.get("temp")).doubleValue();
        double humidity = ((Number) main.get("humidity")).doubleValue();

        // Extract wind data
        JSONObject wind = (JSONObject) json.get("wind");
        double windSpeed = ((Number) wind.get("speed")).doubleValue();

        // Extract weather description and icon
        JSONArray weatherArray = (JSONArray) json.get("weather");
        JSONObject weather = (JSONObject) weatherArray.get(0);
        String description = (String) weather.get("description");
        String iconCode = (String) weather.get("icon");
        String iconUrl = "http://openweathermap.org/img/wn/" + iconCode + "@2x.png";

        // Populate the weather data map
        weatherData.put("temperature", temperature);
        weatherData.put("humidity", humidity);
        weatherData.put("windSpeed", windSpeed);
        weatherData.put("description", description);
        weatherData.put("icon", iconUrl);

        return weatherData;
    }

    /**
     * Checks if cached data is still valid
     * @param cachedData The cached weather data
     * @return boolean indicating if cache is valid
     */
    private boolean isValidCache(CachedWeatherData cachedData) {
        if (cachedData == null) {
            return false;
        }

        // Get the configured refresh interval in minutes
        int refreshInterval = WeatherConfig.getRefreshInterval();

        // Convert to milliseconds
        long refreshIntervalMs = TimeUnit.MINUTES.toMillis(refreshInterval);

        // Check if cache has expired
        return System.currentTimeMillis() - cachedData.getTimestamp() < refreshIntervalMs;
    }

    /**
     * Inner class to represent cached weather data with timestamp
     */
    private static class CachedWeatherData {
        private final Map<String, Object> data;
        private final long timestamp;

        public CachedWeatherData(Map<String, Object> data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }

        public Map<String, Object> getData() {
            return data;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}