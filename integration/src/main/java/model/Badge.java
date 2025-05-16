package model;

import java.time.LocalDate;

public class Badge {
    private int id;
    private String titre;
    private String description;
    private String type;
    private LocalDate dateAttribution;
    private int userId;

    public Badge() {
        this.dateAttribution = LocalDate.now();
    }

    public Badge(String titre, String description, String type, int userId) {
        this.titre = titre;
        this.description = description;
        this.type = type;
        this.userId = userId;
        this.dateAttribution = LocalDate.now();
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDate getDateAttribution() {
        return dateAttribution;
    }

    public void setDateAttribution(LocalDate dateAttribution) {
        this.dateAttribution = dateAttribution;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Badge{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                ", dateAttribution=" + dateAttribution +
                ", userId=" + userId +
                '}';
    }
} 