package Main;

import model.Farm;
import services.FarmService;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        FarmService farmService = new FarmService();

        while (true) {
            System.out.println("\n=== MENU GESTION DES FERMES ===");
            System.out.println("1. Ajouter une ferme");
            System.out.println("2. Afficher toutes les fermes");
            System.out.println("3. Modifier une ferme");
            System.out.println("4. Supprimer une ferme");
            System.out.println("5. Afficher une ferme par ID");
            System.out.println("0. Quitter");
            System.out.print("Choisissez une option : ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // consommer le saut de ligne

            switch (choice) {
                case 1:
                    try {
                        System.out.println("\n--- AJOUT D'UNE FERME ---");
                        System.out.print("Nom : ");
                        String name = scanner.nextLine();
                        System.out.print("Taille : ");
                        int size = scanner.nextInt();
                        scanner.nextLine();
                        System.out.print("Emplacement : ");
                        String location = scanner.nextLine();
                        System.out.print("Image (URL ou nom fichier) : ");
                        String image = scanner.nextLine();
                        System.out.print("Description : ");
                        String description = scanner.nextLine();
                        System.out.print("Latitude : ");
                        double latitude = scanner.nextDouble();
                        System.out.print("Longitude : ");
                        double longitude = scanner.nextDouble();
                        System.out.print("ID utilisateur : ");
                        int userId = scanner.nextInt();

                        Farm f = new Farm(name, size, location, image, description, latitude, longitude, userId);
                        farmService.add(f);
                        System.out.println("✅ Ferme ajoutée avec succès !");
                    } catch (SQLException e) {
                        System.err.println("❌ Erreur lors de l'ajout : " + e.getMessage());
                    }
                    break;

                case 2:
                    try {
                        System.out.println("\n--- LISTE DES FERMES ---");
                        List<Farm> farms = farmService.getAll();
                        for (Farm f : farms) {
                            System.out.println("🔸 ID: " + f.getId() + " | Nom: " + f.getName() + " | Emplacement: " + f.getLocation());
                        }
                    } catch (SQLException e) {
                        System.err.println("❌ Erreur d'affichage : " + e.getMessage());
                    }
                    break;

                case 3:
                    try {
                        System.out.print("ID de la ferme à modifier : ");
                        int idToUpdate = scanner.nextInt();
                        scanner.nextLine();

                        Farm f = farmService.getone(idToUpdate);
                        if (f == null) {
                            System.out.println("❌ Ferme introuvable !");
                            break;
                        }

                        System.out.println("Nom actuel : " + f.getName());
                        System.out.print("Nouveau nom : ");
                        f.setName(scanner.nextLine());

                        System.out.println("Taille actuelle : " + f.getSize());
                        System.out.print("Nouvelle taille : ");
                        f.setSize(scanner.nextInt());
                        scanner.nextLine();

                        System.out.print("Nouvel emplacement : ");
                        f.setLocation(scanner.nextLine());

                        System.out.print("Nouvelle image : ");
                        f.setImage(scanner.nextLine());

                        System.out.print("Nouvelle description : ");
                        f.setDescription(scanner.nextLine());

                        System.out.print("Nouvelle latitude : ");
                        f.setLatitude(scanner.nextDouble());
                        System.out.print("Nouvelle longitude : ");
                        f.setLongitude(scanner.nextDouble());
                        System.out.println("nouveau user : ");
                        f.setUserId(scanner.nextInt());
                        System.out.println("ID à modifier : " + f.getUserId());


                        farmService.update(f);
                        System.out.println("✅ Ferme mise à jour !");
                    } catch (SQLException e) {
                        System.err.println("❌ Erreur de mise à jour : " + e.getMessage());
                    }
                    break;

                case 4:
                    try {
                        System.out.print("ID de la ferme à supprimer : ");
                        int idToDelete = scanner.nextInt();

                        Farm f = farmService.getone(idToDelete);
                        if (f == null) {
                            System.out.println("❌ Ferme introuvable !");
                            break;
                        }

                        farmService.delete(f);
                        System.out.println("🗑️ Ferme supprimée.");
                    } catch (SQLException e) {
                        System.err.println("❌ Erreur de suppression : " + e.getMessage());
                    }
                    break;

                case 5:
                    try {
                        System.out.print("ID de la ferme à afficher : ");
                        int id = scanner.nextInt();
                        Farm f = farmService.getone(id);
                        if (f != null) {
                            System.out.println("📍 Nom: " + f.getName());
                            System.out.println("📐 Taille: " + f.getSize());
                            System.out.println("📌 Emplacement: " + f.getLocation());
                            System.out.println("🖼️ Image: " + f.getImage());
                            System.out.println("📄 Description: " + f.getDescription());
                            System.out.println("🗺️ Latitude: " + f.getLatitude());
                            System.out.println("🗺️ Longitude: " + f.getLongitude());
                        } else {
                            System.out.println("❌ Aucune ferme trouvée.");
                        }
                    } catch (SQLException e) {
                        System.err.println("❌ Erreur : " + e.getMessage());
                    }
                    break;

                case 0:
                    System.out.println("👋 Fin du programme.");
                    return;

                default:
                    System.out.println("⚠️ Choix invalide.");
            }
        }
    }
}
