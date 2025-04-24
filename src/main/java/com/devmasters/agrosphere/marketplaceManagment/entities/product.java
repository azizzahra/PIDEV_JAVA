package com.devmasters.agrosphere.marketplaceManagment.entities;

public class product {
    private int id;
    private String prodImg;
    private String nameProd;
    private String descriptionProd;
    private double priceProd;
    private int quantity;
    private int categoryProdId;
    private int farmerId;

    // Constructors
    public product() {}

    public product(int id, String prodImg, String nameProd, String descriptionProd, double priceProd,
                   int quantity, int categoryProdId, int farmerId) {
        this.id = id;
        this.prodImg = prodImg;
        this.nameProd = nameProd;
        this.descriptionProd = descriptionProd;
        this.priceProd = priceProd;
        this.quantity = quantity;
        this.categoryProdId = categoryProdId;
        this.farmerId = farmerId;
    }

    // Getters and Setters (use your IDE to generate or ask me)
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getProdImg() {
        return prodImg;
    }
    public void setProdImg(String prodImg) {
        this.prodImg = prodImg;
    }
    public String getNameProd() {
        return nameProd;
    }
    public void setNameProd(String nameProd) {
        this.nameProd = nameProd;
    }
    public String getDescriptionProd() {
        return descriptionProd;
    }
    public void setDescriptionProd(String descriptionProd) {
        this.descriptionProd = descriptionProd;
    }
    public double getPriceProd() {
        return priceProd;
    }
    public void setPriceProd(double priceProd) {
        this.priceProd = priceProd;
    }
    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getCategoryProdId() {
        return categoryProdId;
    }
    public void setCategoryProdId(int categoryProdId) {
        this.categoryProdId = categoryProdId;
    }


    public int getFarmerId() {
        return farmerId;

    }
    public void setFarmerId(int farmerId) {
        this.farmerId = farmerId;
    }


}
