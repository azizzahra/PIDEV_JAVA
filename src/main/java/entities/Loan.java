package entities;

import javafx.beans.property.*;

public class Loan {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final DoubleProperty ticketPrice = new SimpleDoubleProperty();
    private final IntegerProperty ticketsLeft = new SimpleIntegerProperty();
    private final StringProperty formation = new SimpleStringProperty();
    private final StringProperty image = new SimpleStringProperty();
    private final ObjectProperty<Place> place = new SimpleObjectProperty<>();

    // Constructors
    public Loan() {} // Default constructor for frameworks

    public Loan(double ticketPrice, int ticketsLeft, String formation, String image, Place place) {
        this.ticketPrice.set(ticketPrice);
        this.ticketsLeft.set(ticketsLeft);
        this.formation.set(formation);
        this.image.set(image);
        this.place.set(place);
    }

    // Property getters
    public IntegerProperty idProperty() {
        return id;
    }

    public DoubleProperty ticketPriceProperty() {
        return ticketPrice;
    }

    public IntegerProperty ticketsLeftProperty() {
        return ticketsLeft;
    }

    public StringProperty formationProperty() {
        return formation;
    }

    public StringProperty imageProperty() {
        return image;
    }

    public ObjectProperty<Place> placeProperty() {
        return place;
    }

    // Regular getters
    public int getId() {
        return id.get();
    }

    public double getTicketPrice() {
        return ticketPrice.get();
    }

    public int getTicketsLeft() {
        return ticketsLeft.get();
    }

    public String getFormation() {
        return formation.get();
    }

    public String getImage() {
        return image.get();
    }

    public Place getPlace() {
        return place.get();
    }

    // Regular setters
    public void setId(int id) {
        this.id.set(id);
    }

    public void setTicketPrice(double ticketPrice) {
        this.ticketPrice.set(ticketPrice);
    }

    public void setTicketsLeft(int ticketsLeft) {
        this.ticketsLeft.set(ticketsLeft);
    }

    public void setFormation(String formation) {
        this.formation.set(formation);
    }

    public void setImage(String image) {
        this.image.set(image);
    }

    public void setPlace(Place place) {
        this.place.set(place);
    }

    // Helper method for table display
    public String getPlaceName() {
        return place.get() != null ? place.get().getName() : "";
    }

    @Override
    public String toString() {
        return "Loan{" +
                "id=" + getId() +
                ", ticketPrice=" + getTicketPrice() +
                ", ticketsLeft=" + getTicketsLeft() +
                ", formation='" + getFormation() + '\'' +
                ", place=" + (getPlace() != null ? getPlace().getName() : "null") +
                '}';
    }
}