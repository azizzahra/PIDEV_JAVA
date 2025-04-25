import os
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '3'  # Désactive les logs de TensorFlow (sauf les erreurs)
import numpy as np
import sys
from keras._tf_keras.keras.models import load_model
from keras._tf_keras.keras.preprocessing import image

# Chemin absolu du modèle
model_path = os.path.join(os.path.dirname(__file__), 'plant_disease_model.keras')

if not os.path.exists(model_path):
    raise FileNotFoundError(f"Le fichier {model_path} n'existe pas.")

# Charger le modèle
model = load_model(model_path)

# Fonction pour prédire une image
def predict_image(image_path):
    img = image.load_img(image_path, target_size=(150, 150))
    img_array = image.img_to_array(img)
    img_array = np.expand_dims(img_array, axis=0)
    img_array /= 255.0

    prediction = model.predict(img_array, verbose=0)  # Désactive les logs de prédiction
    predicted_class = 1 if prediction > 0.5 else 0  # Seuil de 0.5
    accuracy = float(prediction[0][0] if predicted_class == 1 else 1 - prediction[0][0])
    return ("healthy" if predicted_class == 1 else "diseased", accuracy * 100)

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python predict.py <image_path>")
    else:
        image_path = sys.argv[1]
        result, accuracy = predict_image(image_path)
        print(f"{result},{accuracy:.2f}")