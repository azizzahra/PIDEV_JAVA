package Controller.Farm;

import org.json.simple.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class PlantHarvestPredictor {

    // Plant growth data: maps plant types and names to growing days and recommended planting seasons
    private static final Map<String, Map<String, Map<String, Object>>> PLANT_DATA = initializePlantData();

    // Default growing days for unknown plants
    private static final int DEFAULT_GROWING_DAYS = 90;

    private static Map<String, Map<String, Map<String, Object>>> initializePlantData() {
        Map<String, Map<String, Map<String, Object>>> data = new HashMap<>();

        // Vegetables
        Map<String, Map<String, Object>> vegetables = new HashMap<>();
        vegetables.put("Tomato", createPlantInfo(80, Arrays.asList(3, 4, 5)));
        vegetables.put("Carrot", createPlantInfo(70, Arrays.asList(3, 4, 5, 6, 7)));
        vegetables.put("Lettuce", createPlantInfo(45, Arrays.asList(3, 4, 5, 9)));
        vegetables.put("Potato", createPlantInfo(90, Arrays.asList(3, 4)));
        vegetables.put("Cucumber", createPlantInfo(60, Arrays.asList(4, 5, 6)));
        vegetables.put("Onion", createPlantInfo(100, Arrays.asList(2, 3, 4)));
        vegetables.put("Pepper", createPlantInfo(90, Arrays.asList(4, 5)));
        data.put("Vegetables", vegetables);

        // Fruits
        Map<String, Map<String, Object>> fruits = new HashMap<>();
        fruits.put("Strawberry", createPlantInfo(90, Arrays.asList(3, 4)));
        fruits.put("Watermelon", createPlantInfo(100, Arrays.asList(5, 6)));
        fruits.put("Cantaloupe", createPlantInfo(85, Arrays.asList(5, 6)));
        fruits.put("Raspberry", createPlantInfo(120, Arrays.asList(3, 4)));
        fruits.put("Blueberry", createPlantInfo(150, Arrays.asList(3, 4)));
        data.put("Fruits", fruits);

        // Flowers
        Map<String, Map<String, Object>> flowers = new HashMap<>();
        flowers.put("Rose", createPlantInfo(180, Arrays.asList(3, 4, 5)));
        flowers.put("Tulip", createPlantInfo(150, Arrays.asList(9, 10, 11)));
        flowers.put("Sunflower", createPlantInfo(70, Arrays.asList(4, 5, 6)));
        flowers.put("Daisy", createPlantInfo(60, Arrays.asList(3, 4, 5, 6)));
        flowers.put("Lily", createPlantInfo(100, Arrays.asList(3, 4)));
        data.put("Flowers", flowers);

        return data;
    }

    private static Map<String, Object> createPlantInfo(int growingDays, List<Integer> seasons) {
        Map<String, Object> info = new HashMap<>();
        info.put("growing_days", growingDays);
        info.put("seasons", seasons);
        return info;
    }

    private static Map<String, Object> getGrowingData(String plantType, String plantName) {
        // Try to find exact match first
        if (PLANT_DATA.containsKey(plantType) && PLANT_DATA.get(plantType).containsKey(plantName)) {
            return PLANT_DATA.get(plantType).get(plantName);
        }

        // If not found, look for partial matches
        if (PLANT_DATA.containsKey(plantType)) {
            for (String name : PLANT_DATA.get(plantType).keySet()) {
                if (plantName.toLowerCase().contains(name.toLowerCase()) ||
                        name.toLowerCase().contains(plantName.toLowerCase())) {
                    return PLANT_DATA.get(plantType).get(name);
                }
            }
        }

        // Return default values if no match found
        Map<String, Object> defaultData = new HashMap<>();
        defaultData.put("growing_days", DEFAULT_GROWING_DAYS);

        // Default to all months (1-12)
        List<Integer> allMonths = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            allMonths.add(i);
        }
        defaultData.put("seasons", allMonths);

        return defaultData;
    }

    @SuppressWarnings("unchecked")  // For JSONObject type safety warnings
    public static JSONObject predictHarvestDate(String plantType, String plantName, LocalDate plantationDate) {
        JSONObject result = new JSONObject();

        try {
            // Get growing data
            Map<String, Object> growingData = getGrowingData(plantType, plantName);
            int growingDays = (int) growingData.get("growing_days");
            List<Integer> recommendedSeasons = (List<Integer>) growingData.get("seasons");

            // Calculate harvest date
            LocalDate harvestDate = plantationDate.plusDays(growingDays);

            // Check if plantation date is in recommended season
            boolean isRecommendedSeason = recommendedSeasons.contains(plantationDate.getMonthValue());

            // Find next recommended planting time if current is not recommended
            LocalDate nextRecommendedDate = null;
            String recommendationMessage;

            if (!isRecommendedSeason) {
                int currentYear = plantationDate.getYear();
                Integer nextMonth = null;

                // Find the next recommended month
                for (int month : new TreeSet<>(recommendedSeasons)) {
                    if (month > plantationDate.getMonthValue()) {
                        nextMonth = month;
                        break;
                    }
                }

                // If no next month found in current year, take the first month of next year
                if (nextMonth == null) {
                    nextMonth = Collections.min(recommendedSeasons);
                    currentYear += 1;
                }

                nextRecommendedDate = LocalDate.of(currentYear, nextMonth, 1);

                recommendationMessage = String.format(
                        "Planting in %s is not ideal for %s. Consider planting in %s instead.",
                        plantationDate.getMonth().toString(),
                        plantName,
                        nextRecommendedDate.getMonth().toString()
                );
            } else {
                recommendationMessage = String.format(
                        "Good choice! %s is an ideal time to plant %s.",
                        plantationDate.getMonth().toString(),
                        plantName
                );
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            // Build result JSON
            result.put("predicted_harvest_date", harvestDate.format(formatter));
            result.put("is_recommended_season", isRecommendedSeason);
            result.put("recommendation_message", recommendationMessage);
            result.put("next_recommended_date",
                    nextRecommendedDate != null ? nextRecommendedDate.format(formatter) : null);
            result.put("growing_days", growingDays);

        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("predicted_harvest_date", null);
            result.put("is_recommended_season", false);
            result.put("recommendation_message", "Error processing prediction: " + e.getMessage());
            result.put("next_recommended_date", null);
            result.put("growing_days", 0);
        }

        return result;
    }
}