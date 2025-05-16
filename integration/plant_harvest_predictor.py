import datetime
from typing import Dict, Tuple, Optional, List
import json
import sys
import os

class PlantHarvestPredictor:
    def __init__(self):
        # Plant growth data: maps plant types and names to growing days and recommended planting seasons
        # Format: {plant_type: {plant_name: {"growing_days": int, "seasons": List[int]}}}
        # Seasons are represented as months (1-12)
        self.plant_data = {
            "Vegetables": {
                "Tomato": {"growing_days": 80, "seasons": [3, 4, 5]},  # Spring planting
                "Carrot": {"growing_days": 70, "seasons": [3, 4, 5, 6, 7]},
                "Lettuce": {"growing_days": 45, "seasons": [3, 4, 5, 9]},
                "Potato": {"growing_days": 90, "seasons": [3, 4]},
                "Cucumber": {"growing_days": 60, "seasons": [4, 5, 6]},
                "Onion": {"growing_days": 100, "seasons": [2, 3, 4]},
                "Pepper": {"growing_days": 90, "seasons": [4, 5]},
                # Add more vegetables as needed
            },
            "Fruits": {
                "Strawberry": {"growing_days": 90, "seasons": [3, 4]},
                "Watermelon": {"growing_days": 100, "seasons": [5, 6]},
                "Cantaloupe": {"growing_days": 85, "seasons": [5, 6]},
                "Raspberry": {"growing_days": 120, "seasons": [3, 4]},
                "Blueberry": {"growing_days": 150, "seasons": [3, 4]},
                # Add more fruits as needed
            },
            "Flowers": {
                "Rose": {"growing_days": 180, "seasons": [3, 4, 5]},
                "Tulip": {"growing_days": 150, "seasons": [9, 10, 11]},
                "Sunflower": {"growing_days": 70, "seasons": [4, 5, 6]},
                "Daisy": {"growing_days": 60, "seasons": [3, 4, 5, 6]},
                "Lily": {"growing_days": 100, "seasons": [3, 4]},
                # Add more flowers as needed
            }
        }

        # Add a default category for unknown plants
        self.default_growing_days = 90

    def get_growing_data(self, plant_type: str, plant_name: str) -> Dict:
        """Get growing data for a specific plant"""
        # Try to find exact match first
        if plant_type in self.plant_data and plant_name in self.plant_data[plant_type]:
            return self.plant_data[plant_type][plant_name]

        # If not found, look for partial matches
        for name in self.plant_data.get(plant_type, {}):
            if plant_name.lower() in name.lower() or name.lower() in plant_name.lower():
                return self.plant_data[plant_type][name]

        # Return default values if no match found
        return {"growing_days": self.default_growing_days, "seasons": list(range(1, 13))}

    def predict_harvest_date(self, plant_type: str, plant_name: str,
                            plantation_date_str: str) -> Dict:
        """
        Predict harvest date based on plant type, name and plantation date

        Args:
            plant_type: Type of plant (Vegetables, Fruits, Flowers)
            plant_name: Name of the plant
            plantation_date_str: Plantation date in format YYYY-MM-DD

        Returns:
            Dictionary with predicted harvest date and planting recommendations
        """
        try:
            # Parse plantation date
            plantation_date = datetime.datetime.strptime(plantation_date_str, "%Y-%m-%d").date()

            # Get growing data
            growing_data = self.get_growing_data(plant_type, plant_name)
            growing_days = growing_data["growing_days"]
            recommended_seasons = growing_data["seasons"]

            # Calculate harvest date
            harvest_date = plantation_date + datetime.timedelta(days=growing_days)

            # Check if plantation date is in recommended season
            is_recommended_season = plantation_date.month in recommended_seasons

            # Find next recommended planting time if current is not recommended
            next_recommended_date = None
            recommendation_message = ""

            if not is_recommended_season:
                current_year = plantation_date.year
                next_month = None

                # Find the next recommended month
                for month in sorted(recommended_seasons):
                    if month > plantation_date.month:
                        next_month = month
                        break

                # If no next month found in current year, take the first month of next year
                if next_month is None:
                    next_month = recommended_seasons[0]
                    current_year += 1

                next_recommended_date = datetime.date(current_year, next_month, 1)
                recommendation_message = (f"Planting in {plantation_date.strftime('%B')} is not ideal for {plant_name}. "
                                        f"Consider planting in {next_recommended_date.strftime('%B')} instead.")
            else:
                recommendation_message = f"Good choice! {plantation_date.strftime('%B')} is an ideal time to plant {plant_name}."

            return {
                "predicted_harvest_date": harvest_date.strftime("%Y-%m-%d"),
                "is_recommended_season": is_recommended_season,
                "recommendation_message": recommendation_message,
                "next_recommended_date": next_recommended_date.strftime("%Y-%m-%d") if next_recommended_date else None,
                "growing_days": growing_days
            }

        except Exception as e:
            return {
                "error": str(e),
                "predicted_harvest_date": None,
                "is_recommended_season": False,
                "recommendation_message": f"Error processing prediction: {str(e)}",
                "next_recommended_date": None,
                "growing_days": 0
            }

def predict_from_cli():
    """Function to handle CLI arguments and return JSON result"""
    if len(sys.argv) < 4:
        result = {"error": "Missing arguments. Required: plant_type plant_name plantation_date"}
        print(json.dumps(result))
        return

    plant_type = sys.argv[1]
    plant_name = sys.argv[2]
    plantation_date = sys.argv[3]

    predictor = PlantHarvestPredictor()
    result = predictor.predict_harvest_date(plant_type, plant_name, plantation_date)

    # Print JSON result to stdout for Java to capture
    print(json.dumps(result))

if __name__ == "__main__":
    predict_from_cli()