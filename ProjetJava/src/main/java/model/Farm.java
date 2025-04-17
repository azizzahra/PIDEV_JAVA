package model;

public class Farm {
    private int id;
    private String name;
    private int size;
    private String location;
    private String image;
    private String description;
    private double latitude;
    private double longitude;
    private int userId;

    public Farm() {}

    public Farm(int id, String name, int size, String location, String image, String description, double latitude, double longitude, int userId) {
        this.id = id;
        this.name = name;
        this.size = size;
        this.location = location;
        this.image = image;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.userId = userId;
    }
    public Farm(String name, int size, String location, String image, String description, double latitude, double longitude, int userId) {

        this.name = name;
        this.size = size;
        this.location = location;
        this.image = image;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.userId = userId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}