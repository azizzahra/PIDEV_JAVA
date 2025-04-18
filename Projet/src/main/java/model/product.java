package model;

public class product {
    private int id;
    private int companyProdId;
    private int farmerId;
    private String prodImg;
    private String nameProd;
    private String descriptionProd;
    private double priceProd;
    private int quantity;

    // Constructeurs
    public product() {}

    public product(int id, int companyProdId, int farmerId, String prodImg, String nameProd, String descriptionProd, double priceProd, int quantity) {
        this.id = id;
        this.companyProdId = companyProdId;
        this.farmerId = farmerId;
        this.prodImg = prodImg;
        this.nameProd = nameProd;
        this.descriptionProd = descriptionProd;
        this.priceProd = priceProd;
        this.quantity = quantity;
    }
    public product( int companyProdId, int farmerId, String prodImg, String nameProd, String descriptionProd, double priceProd, int quantity) {
        this.companyProdId = companyProdId;
        this.farmerId = farmerId;
        this.prodImg = prodImg;
        this.nameProd = nameProd;
        this.descriptionProd = descriptionProd;
        this.priceProd = priceProd;
        this.quantity = quantity;
    }

    // Getters
    public int getId() { return id; }
    public int getCompanyProdId() { return companyProdId; }
    public int getFarmerId() { return farmerId; }
    public String getProdImg() { return prodImg; }
    public String getNameProd() { return nameProd; }
    public String getDescriptionProd() { return descriptionProd; }
    public double getPriceProd() { return priceProd; }
    public int getQuantity() { return quantity; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setCompanyProdId(int companyProdId) { this.companyProdId = companyProdId; }
    public void setFarmerId(int farmerId) { this.farmerId = farmerId; }
    public void setProdImg(String prodImg) { this.prodImg = prodImg; }
    public void setNameProd(String nameProd) { this.nameProd = nameProd; }
    public void setDescriptionProd(String descriptionProd) { this.descriptionProd = descriptionProd; }
    public void setPriceProd(double priceProd) { this.priceProd = priceProd; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", nameProd='" + nameProd + '\'' +
                ", priceProd=" + priceProd +
                '}';
    }
}