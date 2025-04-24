package com.devmasters.agrosphere.marketplaceManagement.entities;

import javafx.application.Application;
import javafx.stage.Stage;
import java.time.LocalDate;


public class order {
    private int id;
    private int buyerId;
    private double totalPrice;
    private String status;
    private LocalDate ordCreatedAt;

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBuyerId() { return buyerId; }
    public void setBuyerId(int buyerId) { this.buyerId = buyerId; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getOrdCreatedAt() { return ordCreatedAt; }
    public void setOrdCreatedAt(LocalDate ordCreatedAt) { this.ordCreatedAt = ordCreatedAt; }
}
