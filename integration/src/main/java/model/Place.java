package model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Place {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final DoubleProperty price = new SimpleDoubleProperty();
    private final IntegerProperty capacity = new SimpleIntegerProperty();
    private final StringProperty image = new SimpleStringProperty();
    private final ListProperty<Loan> loans = new SimpleListProperty<>(FXCollections.observableArrayList());

    // Constructors
    public Place() {
    }

    public Place(String name, double price, int capacity, String image) {
        this.name.set(name);
        this.price.set(price);
        this.capacity.set(capacity);
        this.image.set(image);
    }

    public Place(int id, String name, double price, int capacity, String image) {
        this.id.set(id);
        this.name.set(name);
        this.price.set(price);
        this.capacity.set(capacity);
        this.image.set(image);
    }

    // Property getters
    public IntegerProperty idProperty() {
        return id;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public DoubleProperty priceProperty() {
        return price;
    }

    public IntegerProperty capacityProperty() {
        return capacity;
    }

    public StringProperty imageProperty() {
        return image;
    }

    public ListProperty<Loan> loansProperty() {
        return loans;
    }

    // Regular getters
    public int getId() {
        return id.get();
    }

    public String getName() {
        return name.get();
    }

    public double getPrice() {
        return price.get();
    }

    public int getCapacity() {
        return capacity.get();
    }

    public String getImage() {
        return image.get();
    }

    public ObservableList<Loan> getLoans() {
        return loans.get();
    }

    // Regular setters
    public void setId(int id) {
        this.id.set(id);
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public void setPrice(double price) {
        this.price.set(price);
    }

    public void setCapacity(int capacity) {
        this.capacity.set(capacity);
    }

    public void setImage(String image) {
        this.image.set(image);
    }

    public void setLoans(ObservableList<Loan> loans) {
        this.loans.set(loans);
    }

    // Loan management methods
    public void addLoan(Loan loan) {
        loans.add(loan);
        loan.setPlace(this);
    }

    public void removeLoan(Loan loan) {
        loans.remove(loan);
        loan.setPlace(null);
    }

    @Override
    public String toString() {
        return "Place{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", price=" + getPrice() +
                ", capacity=" + getCapacity() +
                ", image='" + getImage() + '\'' +
                '}';
    }
}