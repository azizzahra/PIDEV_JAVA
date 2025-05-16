package services;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration utility for Weather API settings
 */
public class WeatherConfig {

    private static final String CONFIG_FILE = "config/weather.properties";
    private static Properties properties;

    static {
        loadProperties();
    }

    /**
     * Load configuration properties from file
     */
    private static void loadProperties() {
        properties = new Properties();
        try (InputStream input = WeatherConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
            } else {
                System.err.println("Unable to find " + CONFIG_FILE);
                // Set default values
                properties.setProperty("api.key", "0bbc5740643cedddc0837de3c56ca2ac");
                properties.setProperty("api.url", "https://api.openweathermap.org/data/2.5/weather");
                properties.setProperty("api.units", "metric");
            }
        } catch (IOException e) {
            System.err.println("Error loading weather config: " + e.getMessage());
            // Set default values
            properties.setProperty("api.key", "0bbc5740643cedddc0837de3c56ca2ac");
            properties.setProperty("api.url", "https://api.openweathermap.org/data/2.5/weather");
            properties.setProperty("api.units", "metric");
        }
    }

    /**
     * Get API key from configuration
     * @return String API key
     */
    public static String getApiKey() {
        return properties.getProperty("api.key", "0bbc5740643cedddc0837de3c56ca2ac");
    }

    /**
     * Get base URL from configuration
     * @return String base URL
     */
    public static String getBaseUrl() {
        return properties.getProperty("api.url", "https://api.openweathermap.org/data/2.5/weather");
    }

    /**
     * Get units (metric or imperial) from configuration
     * @return String units
     */
    public static String getUnits() {
        return properties.getProperty("api.units", "metric");
    }

    /**
     * Get refresh interval in minutes
     * @return int minutes
     */
    public static int getRefreshInterval() {
        try {
            return Integer.parseInt(properties.getProperty("refresh.interval", "30"));
        } catch (NumberFormatException e) {
            return 30; // Default 30 minutes
        }
    }
}