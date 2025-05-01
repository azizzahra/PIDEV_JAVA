package model;

import java.time.LocalDateTime;

public class Note {
    private Integer id;
    private String task;
    private String status;
    private LocalDateTime createdAt;
    private Farm farm;

    // Constructors
    public Note() {
        this.createdAt = LocalDateTime.now(); // Automatically set the creation date
        this.status = "pending"; // Set default status

    }

    public Note(Integer id, String task, String status, LocalDateTime createdAt, Farm farm) {
        this.id = id;
        this.task = task;
        this.status = status;
        this.createdAt = createdAt;
        this.farm = farm;
    }

    public Note(String task, String status, Farm farm) {
        this.task = task;
        this.status = status;
        this.farm = farm;
        this.createdAt = LocalDateTime.now(); // Automatically set the creation date
    }

    // Getters
    public Integer getId() {
        return id;
    }

    public String getTask() {
        return task;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Farm getFarm() {
        return farm;
    }

    // Setters
    public void setId(Integer id) {
        this.id = id;
    }

    public Note setTask(String task) {
        this.task = task;
        return this;
    }

    public Note setStatus(String status) {
        this.status = status;
        return this;
    }

    public Note setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public Note setFarm(Farm farm) {
        this.farm = farm;
        return this;
    }

    @Override
    public String toString() {
        return "Note{" +
                "id=" + id +
                ", task='" + task + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", farm=" + (farm != null ? farm.getId() : null) +
                '}';
    }
}