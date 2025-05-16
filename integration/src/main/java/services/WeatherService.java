package services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public class WeatherService {

    private static final String API_KEY = "968127896657d6e8e01741a663f0b88f"; // Remplacez par votre clé API
    private static final String ONE_CALL_API_URL = "https://api.openweathermap.org/data/2.5/forecast";


    // Coordonnées par défaut (à remplacer par les coordonnées de la ferme)
    private static final double DEFAULT_LAT = 36.8065; // Latitude de Tunis
    private static final double DEFAULT_LON = 10.1815; // Longitude de Tunis

    public Map<String, Object> getWeatherData() {
        return getWeatherData(DEFAULT_LAT, DEFAULT_LON);
    }

    public Map<String, Object> getWeatherData(double lat, double lon) {
        try {
            // Construction de l'URL avec les coordonnées
            String urlString = ONE_CALL_API_URL +
                    "?lat=" + lat +
                    "&lon=" + lon +
                    "&exclude=minutely,alerts" + // Exclure les données non nécessaires
                    "&units=metric" + // Utiliser les unités métriques (Celsius)
                    "&lang=fr" + // Langue française
                    "&appid=" + API_KEY;

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONObject jsonResponse = new JSONObject(response.toString());

            // Extraction des données pertinentes
            Map<String, Object> weatherData = new HashMap<>();

            // Données actuelles
            JSONObject current = jsonResponse.getJSONObject("current");
            double temperature = current.getDouble("temp");
            int humidity = current.getInt("humidity");
            String description = current.getJSONArray("weather").getJSONObject(0).getString("description");
            String icon = current.getJSONArray("weather").getJSONObject(0).getString("icon");

            weatherData.put("temperature", temperature);
            weatherData.put("humidity", humidity);
            weatherData.put("description", description);
            weatherData.put("icon", icon);

            // Prévisions pour les prochains jours
            StringBuilder forecastText = new StringBuilder();
            int rainCount = 0;

            // Analyser les prévisions quotidiennes
            JSONArray daily = jsonResponse.getJSONArray("daily");
            for (int i = 0; i < Math.min(daily.length(), 7); i++) { // Limiter à 7 jours
                JSONObject dayForecast = daily.getJSONObject(i);
                String weather = dayForecast.getJSONArray("weather").getJSONObject(0).getString("main");
                if (weather.equalsIgnoreCase("Rain")) {
                    rainCount++;
                }
            }

            if (rainCount > 0) {
                forecastText.append("Prévisions: Pluie prévue dans les prochains jours");
            } else {
                forecastText.append("Prévisions: Pas de pluie cette semaine");
            }

            weatherData.put("forecastText", forecastText.toString());

            // Ajouter plus de détails sur les prévisions
            Map<String, Object> dailyForecasts = new HashMap<>();
            for (int i = 0; i < Math.min(daily.length(), 5); i++) { // Limiter à 5 jours
                JSONObject dayForecast = daily.getJSONObject(i);
                Map<String, Object> dayData = new HashMap<>();

                // Date au format UNIX timestamp
                long timestamp = dayForecast.getLong("dt");
                dayData.put("timestamp", timestamp);

                // Températures
                JSONObject temp = dayForecast.getJSONObject("temp");
                dayData.put("temp_min", temp.getDouble("min"));
                dayData.put("temp_max", temp.getDouble("max"));

                // Conditions météo
                JSONObject weatherObj = dayForecast.getJSONArray("weather").getJSONObject(0);
                dayData.put("weather_main", weatherObj.getString("main"));
                dayData.put("weather_description", weatherObj.getString("description"));
                dayData.put("weather_icon", weatherObj.getString("icon"));

                // Probabilité de précipitation
                dayData.put("pop", dayForecast.optDouble("pop", 0) * 100); // en pourcentage

                dailyForecasts.put("day_" + i, dayData);
            }

            weatherData.put("daily", dailyForecasts);

            return weatherData;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Fonction pour convertir les noms de villes en coordonnées
    // Vous pouvez utiliser cette méthode si vous souhaitez conserver la recherche par nom de ville
    public Map<String, Double> getCoordinatesForCity(String cityName) {
        try {
            String geocodingUrl = "https://api.openweathermap.org/geo/1.0/direct?q=" +
                    cityName + "&limit=1&appid=" + API_KEY;

            URL url = new URL(geocodingUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONArray jsonResponse = new JSONArray(response.toString());

            if (jsonResponse.length() > 0) {
                JSONObject location = jsonResponse.getJSONObject(0);
                Map<String, Double> coordinates = new HashMap<>();
                coordinates.put("lat", location.getDouble("lat"));
                coordinates.put("lon", location.getDouble("lon"));
                return coordinates;
            } else {
                return null; // Ville non trouvée
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}