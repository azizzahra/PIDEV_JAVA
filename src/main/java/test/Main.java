package test;

import entities.Place;
import services.PlaceService;
import services.SmsSender;

import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {
        String myNumber = "+216 96628518";
        String message = "Hello from Java!";

        SmsSender.sendSms(myNumber, message);
        System.out.println("Message sent successfully!");
        PlaceService ps = new PlaceService();
        try {
            // Uncomment to modify a place
            // ps.modifier(new Place(1, "Updated Resort", 400.5, 60, "updated_resort.jpg"));

            // Example of adding a new place
            ps.ajouter(new Place("Beach Resort", 300.5, 50, "beach.jpg"));

            // Example of getting and printing all places
            System.out.println(ps.recuperer());

            // Example of deleting a place (assuming you have a place with ID 1)
            // ps.supprimer(new Place(1, "Beach Resort", 300.5, 50, "beach.jpg"));

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

    }
}
