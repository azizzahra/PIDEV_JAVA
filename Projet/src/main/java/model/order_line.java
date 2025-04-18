package model;

public class order_line {
    private int id;
    private int productId;
    private int orderQuantity;

    // Constructeurs
    public order_line() {}

    public order_line(int id, int productId, int orderQuantity) {
        this.id = id;
        this.productId = productId;
        this.orderQuantity = orderQuantity;
    }

    // Getters
    public int getId() { return id; }
    public int getProductId() { return productId; }
    public int getOrderQuantity() { return orderQuantity; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setProductId(int productId) { this.productId = productId; }
    public void setOrderQuantity(int orderQuantity) { this.orderQuantity = orderQuantity; }

    @Override
    public String toString() {
        return "OrderLine{" +
                "productId=" + productId +
                ", quantity=" + orderQuantity +
                '}';
    }
}