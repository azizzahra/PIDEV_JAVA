package model;

public class note {
    private int id;
    private int userId;
    private String content;
    private String createdAt;

    // Constructeurs
    public note() {}

    public note(int id, int userId, String content, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
    }
    public note(int userId, String content, String createdAt) {

        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
    }

    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getContent() { return content; }
    public String getCreatedAt() { return createdAt; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setContent(String content) { this.content = content; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Note{" +
                "id=" + id +
                ", content='" + content + '\'' +
                '}';
    }
}
