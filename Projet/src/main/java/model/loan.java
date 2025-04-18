package model;

public class loan {
    private int id;
    private int placeId;
    private int userId;
    private double ticketPrice;
    private int ticketsLeft;
    private String formation;
    private String image;

    public loan() {}

    public loan(int id, int placeId, int userId, double ticketPrice, int ticketsLeft, String formation, String image) {
        this.id = id;
        this.placeId = placeId;
        this.userId = userId;
        this.ticketPrice = ticketPrice;
        this.ticketsLeft = ticketsLeft;
        this.formation = formation;
        this.image = image;
    }

    public loan(int placeId, int userId, double ticketPrice, int ticketsLeft, String formation, String image) {
        this.placeId = placeId;
        this.userId = userId;
        this.ticketPrice = ticketPrice;
        this.ticketsLeft = ticketsLeft;
        this.formation = formation;
        this.image = image;
    }


    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPlaceId() { return placeId; }
    public void setPlaceId(int placeId) { this.placeId = placeId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public double getTicketPrice() { return ticketPrice; }
    public void setTicketPrice(double ticketPrice) { this.ticketPrice = ticketPrice; }
    public int getTicketsLeft() { return ticketsLeft; }
    public void setTicketsLeft(int ticketsLeft) { this.ticketsLeft = ticketsLeft; }
    public String getFormation() { return formation; }
    public void setFormation(String formation) { this.formation = formation; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
}
