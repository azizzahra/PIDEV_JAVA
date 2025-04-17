package model;

public class post {
    private int id;
    private String title;
    private int usernameId;
    private String content;
    private String category;
    private String attachment;
    private String dateC;
    private int views;
    private int vote;

    public post() {
    }

    public post(int id, String title, int usernameId, String content, String category, String attachment, String dateC, int views, int vote) {
        this.id = id;
        this.title = title;
        this.usernameId = usernameId;
        this.content = content;
        this.category = category;
        this.attachment = attachment;
        this.dateC = dateC;
        this.views = views;
        this.vote = vote;
    }

    public post(String title, int usernameId, String content, String category, String attachment, String dateC, int views, int vote) {
        this.title = title;
        this.usernameId = usernameId;
        this.content = content;
        this.category = category;
        this.attachment = attachment;
        this.dateC = dateC;
        this.views = views;
        this.vote = vote;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getUsernameId() {
        return usernameId;
    }

    public void setUsernameId(int usernameId) {
        this.usernameId = usernameId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public String getDateC() {
        return dateC;
    }

    public void setDateC(String dateC) {
        this.dateC = dateC;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public int getVote() {
        return vote;
    }

    public void setVote(int vote) {
        this.vote = vote;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", usernameId=" + usernameId +
                ", content='" + content + '\'' +
                ", category='" + category + '\'' +
                ", attachment='" + attachment + '\'' +
                ", dateC='" + dateC + '\'' +
                ", views=" + views +
                ", vote=" + vote +
                '}';
    }
}
