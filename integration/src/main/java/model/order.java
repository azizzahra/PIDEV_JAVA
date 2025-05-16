package model;
import model.orderLine;


import javafx.application.Application;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.util.ArrayList;


public class order {
    private int id;
    private int buyerId;
    private double totalPrice;
    private String status;

    public order(){}

    public order(int id, int buyerId, double totalPrice, String status) {
        this.id = id;
        this.buyerId = buyerId;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBuyerId() { return buyerId; }
    public void setBuyerId(int buyerId) { this.buyerId = buyerId; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

}
