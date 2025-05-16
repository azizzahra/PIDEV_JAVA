package model;

public class commentaire {
    private int id;
    private int user_id;
    private int post_id;
    private String title;
    private String content;
    private String category;
    private String attachment;
    private String created_at;
    private int views;
    private int vote;
    private Integer parent_id; // Ajouté pour les réponses aux commentaires (null pour commentaires principaux)

    // Constructeurs
    public commentaire() {}

    public commentaire(int id, int user_id, int post_id, String title, String content, String category,
                       String attachment, String created_at, int views, int vote, Integer parent_id) {
        this.id = id;
        this.user_id = user_id;
        this.post_id = post_id;
        this.title = title;
        this.content = content;
        this.category = category;
        this.attachment = attachment;
        this.created_at = created_at;
        this.views = views;
        this.vote = vote;
        this.parent_id = parent_id;
    }

    public commentaire(int user_id, int post_id, String title, String content, String category,
                       String attachment, String created_at, int views, int vote, Integer parent_id) {
        this.user_id = user_id;
        this.post_id = post_id;
        this.title = title;
        this.content = content;
        this.category = category;
        this.attachment = attachment;
        this.created_at = created_at;
        this.views = views;
        this.vote = vote;
        this.parent_id = parent_id;
    }

    // Ancien constructeur maintenu pour compatibilité
    public commentaire(int id, int user_id, int post_id, String title, String content, String category,
                       String attachment, String created_at, int views, int vote) {
        this(id, user_id, post_id, title, content, category, attachment, created_at, views, vote, null);
    }

    // Ancien constructeur maintenu pour compatibilité
    public commentaire(int user_id, int post_id, String title, String content, String category,
                       String attachment, String created_at, int views, int vote) {
        this(user_id, post_id, title, content, category, attachment, created_at, views, vote, null);
    }

    // Getters et Setters existants...
    public int getId() { return id; }
    public int getUser_id() { return user_id; }
    public int getPost_id() { return post_id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getCategory() { return category; }
    public String getAttachment() { return attachment; }
    public String getcreated_at() { return created_at; }
    public int getViews() { return views; }
    public int getVote() { return vote; }

    // Nouveau getter et setter pour parent_id
    public Integer getParent_id() { return parent_id; }
    public void setParent_id(Integer parent_id) { this.parent_id = parent_id; }

    // Setters existants
    public void setId(int id) { this.id = id; }
    public void setUser_id(int user_id) { this.user_id = user_id; }
    public void setPost_id(int post_id) { this.post_id = post_id; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setCategory(String category) { this.category = category; }
    public void setAttachment(String attachment) { this.attachment = attachment; }
    public void setcreated_at(String created_at) { this.created_at = created_at; }
    public void setViews(int views) { this.views = views; }
    public void setVote(int vote) { this.vote = vote; }

    @Override
    public String toString() {
        return "Commentaire{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", parent_id=" + parent_id +
                '}';
    }
}