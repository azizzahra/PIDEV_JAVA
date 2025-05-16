package model;

/**
 * Entity class for categories in the marketplace management system.
 * Note: Renamed from lowercase 'category' to proper capitalized 'Category'
 * to follow Java naming conventions.
 */
public class category {

    private int id;
    private String nameCategory;
    private String descriptionCategory;

    // Default constructor
    public category() {
    }

    // Parameterized constructor
    public category(int id, String nameCategory, String descriptionCategory) {
        this.id = id;
        this.nameCategory = nameCategory;
        this.descriptionCategory = descriptionCategory;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNameCategory() {
        return nameCategory;
    }

    public void setNameCategory(String nameCategory) {
        this.nameCategory = nameCategory;
    }

    public String getDescriptionCategory() {
        return descriptionCategory;
    }

    public void setDescriptionCategory(String descriptionCategory) {
        this.descriptionCategory = descriptionCategory;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", nameCategory='" + nameCategory + '\'' +
                ", descriptionCategory='" + descriptionCategory + '\'' +
                '}';
    }
}