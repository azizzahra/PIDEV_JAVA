package com.devmasters.agrosphere.marketplaceManagement.entities;


public class orderLine {
    private int id;
    private int ordId;
    private int productId;
    private int orderQuantity;



    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrdId() { return ordId; }
    public void setOrdId(int ordId) { this.ordId = ordId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getOrderQuantity() { return orderQuantity; }
    public void setOrderQuantity(int orderQuantity) { this.orderQuantity = orderQuantity; }
}
