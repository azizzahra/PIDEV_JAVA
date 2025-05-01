package model;

public class place {
    private int id;
    private String name;
    private int size;
    private String location;
    private String image;
    private String description;
    private double latitude;
    private double longitude;
    private int userId;

    // Constructeurs
    public place() {}

    public place(int id, String name, int size, String location, String image, String description, double latitude, double longitude, int userId) {
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
    public place( String name, int size, String location, String image, String description, double latitude, double longitude, int userId) {
        this.name = name;
        this.size = size;
        this.location = location;
        this.image = image;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.userId = userId;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public int getSize() { return size; }
    public String getLocation() { return location; }
    public String getImage() { return image; }
    public String getDescription() { return description; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public int getUserId() { return userId; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setSize(int size) { this.size = size; }
    public void setLocation(String location) { this.location = location; }
    public void setImage(String image) { this.image = image; }
    public void setDescription(String description) { this.description = description; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setUserId(int userId) { this.userId = userId; }

    @Override
    public String toString() {
        return "Place{" +
                "name='" + name + '\'' +
                ", location='" + location + '\'' +
                '}';
    }
}