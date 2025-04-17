package model;

public class order {
    private int id;
    private int buyerId;
    private double totalPrice;
    private String status;

    // Constructeurs
    public order() {}

    public order(int id, int buyerId, double totalPrice, String status) {
        this.id = id;
        this.buyerId = buyerId;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    // Getters
    public int getId() { return id; }
    public int getBuyerId() { return buyerId; }
    public double getTotalPrice() { return totalPrice; }
    public String getStatus() { return status; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setBuyerId(int buyerId) { this.buyerId = buyerId; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", totalPrice=" + totalPrice +
                ", status='" + status + '\'' +
                '}';
    }
}