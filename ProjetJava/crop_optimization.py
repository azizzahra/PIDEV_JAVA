import sys
import pandas as pd
from sklearn.ensemble import RandomForestRegressor
import json

# Simulated historical data (replace with actual data from your database)
historical_data = pd.DataFrame({
    'latitude': [36.8, 36.9, 36.7],
    'longitude': [10.1, 10.2, 10.3],
    'temperature': [20.0, 22.0, 18.0],
    'humidity': [60.0, 65.0, 55.0],
    'plant_count': [100, 150, 120],
    'yield': [1500, 1800, 1400]
})

# Train a simple Random Forest model
model = RandomForestRegressor()
model.fit(historical_data[['latitude', 'longitude', 'temperature', 'humidity', 'plant_count']],
          historical_data['yield'])

def recommend_crops(plants, temperature, humidity, season):
    recommendations = []
    for plant in plants:
        if not plant:  # Skip empty plant entries
            continue

        plant_parts = plant.split(',')
        if len(plant_parts) < 3:
            continue

        plant_name, plant_type, quantity = plant_parts
        try:
            quantity = int(quantity)
        except ValueError:
            continue

        if plant_type == 'Vegetables':
            if 15 <= temperature <= 25 and humidity > 50:
                recommendations.append(f"Optimal conditions for {plant_name}. Plant more in {season}.")
            elif temperature > 25:
                recommendations.append(f"High temperature for {plant_name}. Consider shade or irrigation.")
            else:
                recommendations.append(f"Monitor {plant_name} closely as conditions are not ideal.")
        elif plant_type == 'Fruits':
            if 20 <= temperature <= 30 and humidity > 40:
                recommendations.append(f"Good conditions for {plant_name}. Plant more in {season}.")
            elif humidity < 40:
                recommendations.append(f"Low humidity for {plant_name}. Increase irrigation.")
            else:
                recommendations.append(f"Current conditions require attention for {plant_name}.")
        else:
            recommendations.append(f"No specific recommendations for {plant_name} ({plant_type}).")

    return "; ".join(recommendations) if recommendations else "Adjust conditions for better yield."

def main():
    try:
        # Parse input arguments
        if len(sys.argv) != 5:
            raise ValueError(f"Expected 5 arguments, got {len(sys.argv)}")

        lat_long = sys.argv[1].split(',')
        if len(lat_long) != 2:
            raise ValueError("Latitude,Longitude must be in format 'lat,long'")

        latitude = float(lat_long[0])
        longitude = float(lat_long[1])
        temperature = float(sys.argv[2])
        humidity = float(sys.argv[3])
        plants_data = sys.argv[4]

        plants = plants_data.split(';')
        if not plants or plants == ['']:
            raise ValueError("No valid plant data provided")

        # Count total plants for the prediction model
        plant_count = 0
        for plant in plants:
            if plant and len(plant.split(',')) >= 3:
                try:
                    plant_count += int(plant.split(',')[2])
                except (ValueError, IndexError):
                    continue

        # Predict yield
        features = [[latitude, longitude, temperature, humidity, plant_count]]
        predicted_yield = model.predict(features)[0]

        # Generate recommendation
        season = 'spring'  # Simplified; could be derived from current date
        recommendation = recommend_crops(plants, temperature, humidity, season)

        # Output result
        print(f"yield:{int(predicted_yield)},recommendation:{recommendation}")

    except Exception as e:
        print(f"ERROR:{str(e)}")
        sys.exit(1)

if __name__ == "__main__":
    main()