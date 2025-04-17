package entities;

import javafx.beans.property.*;

public class Owner {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty number = new SimpleStringProperty();
    private final ObjectProperty<Loan> loan = new SimpleObjectProperty<>(); // formation joined

    public Owner() {}

    public Owner(String name, String email, String number, Loan loan) {
        this.name.set(name);
        this.email.set(email);
        this.number.set(number);
        this.loan.set(loan);
    }

    // Property Getters
    public IntegerProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public StringProperty emailProperty() { return email; }
    public StringProperty numberProperty() { return number; }
    public ObjectProperty<Loan> loanProperty() { return loan; }

    // Regular Getters
    public int getId() { return id.get(); }
    public String getName() { return name.get(); }
    public String getEmail() { return email.get(); }
    public String getNumber() { return number.get(); }
    public Loan getLoan() { return loan.get(); }

    // Regular Setters
    public void setId(int id) { this.id.set(id); }
    public void setName(String name) { this.name.set(name); }
    public void setEmail(String email) { this.email.set(email); }
    public void setNumber(String number) { this.number.set(number); }
    public void setLoan(Loan loan) { this.loan.set(loan); }

    public String getFormationName() {
        return loan.get() != null ? loan.get().getFormation() : "";
    }

    @Override
    public String toString() {
        return "Owner{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", number='" + getNumber() + '\'' +
                ", formation='" + getFormationName() + '\'' +
                '}';
    }
}
