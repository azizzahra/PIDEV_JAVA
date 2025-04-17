package model;

public class commentaire {
    private int id;
    private int usernameId;
    private String title;
    private String content;
    private String category;
    private String attachment;
    private String dateC;
    private int views;
    private int vote;

    // Constructeurs
    public commentaire() {}

    public commentaire(int id, int usernameId, String title, String content, String category, String attachment, String dateC, int views, int vote) {
        this.id = id;
        this.usernameId = usernameId;
        this.title = title;
        this.content = content;
        this.category = category;
        this.attachment = attachment;
        this.dateC = dateC;
        this.views = views;
        this.vote = vote;
    }
    public commentaire(int usernameId, String title, String content, String category, String attachment, String dateC, int views, int vote) {
        this.usernameId = usernameId;
        this.title = title;
        this.content = content;
        this.category = category;
        this.attachment = attachment;
        this.dateC = dateC;
        this.views = views;
        this.vote = vote;
    }

    // Getters
    public int getId() { return id; }
    public int getUsernameId() { return usernameId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getCategory() { return category; }
    public String getAttachment() { return attachment; }
    public String getDateC() { return dateC; }
    public int getViews() { return views; }
    public int getVote() { return vote; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setUsernameId(int usernameId) { this.usernameId = usernameId; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setCategory(String category) { this.category = category; }
    public void setAttachment(String attachment) { this.attachment = attachment; }
    public void setDateC(String dateC) { this.dateC = dateC; }
    public void setViews(int views) { this.views = views; }
    public void setVote(int vote) { this.vote = vote; }

    @Override
    public String toString() {
        return "Commentaire{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}