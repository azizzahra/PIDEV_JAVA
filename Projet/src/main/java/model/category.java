package model;

public class category {
    private int id;
    private String nameCategory;
    private String descriptionCategory;

    public category() {}

    public category(int id, String nameCategory, String descriptionCategory) {
        this.id = id;
        this.nameCategory = nameCategory;
        this.descriptionCategory = descriptionCategory;
    }
    public category( String nameCategory, String descriptionCategory) {
        this.nameCategory = nameCategory;
        this.descriptionCategory = descriptionCategory;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNameCategory() { return nameCategory; }
    public void setNameCategory(String nameCategory) { this.nameCategory = nameCategory; }
    public String getDescriptionCategory() { return descriptionCategory; }
    public void setDescriptionCategory(String descriptionCategory) { this.descriptionCategory = descriptionCategory; }
}