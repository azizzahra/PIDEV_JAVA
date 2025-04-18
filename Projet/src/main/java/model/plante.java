package model;

public class plante {
    private int id;
    private int farmId;
    private String name;
    private String type;
    private String image;
    private String plantationDate;
    private String harvestDate;
    private int quantity;

    public plante() {}

    public plante(int id, int farmId, String name, String type, String image, String plantationDate, String harvestDate, int quantity) {
        this.id = id;
        this.farmId = farmId;
        this.name = name;
        this.type = type;
        this.image = image;
        this.plantationDate = plantationDate;
        this.harvestDate = harvestDate;
        this.quantity = quantity;
    }
    public plante( int farmId, String name, String type, String image, String plantationDate, String harvestDate, int quantity) {
        this.farmId = farmId;
        this.name = name;
        this.type = type;
        this.image = image;
        this.plantationDate = plantationDate;
        this.harvestDate = harvestDate;
        this.quantity = quantity;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getFarmId() { return farmId; }
    public void setFarmId(int farmId) { this.farmId = farmId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getPlantationDate() { return plantationDate; }
    public void setPlantationDate(String plantationDate) { this.plantationDate = plantationDate; }
    public String getHarvestDate() { return harvestDate; }
    public void setHarvestDate(String harvestDate) { this.harvestDate = harvestDate; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
