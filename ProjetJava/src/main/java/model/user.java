package model;
public class user {
    private int id;
    private String personn;
    private String role;
    private String mail;
    private String motiepasse;
    private String status;
    private String numFel;
    private String birthDate;

    // Constructeurs
    public user() {}

    public user(int id, String personn, String role, String mail, String motiepasse, String status, String numFel, String birthDate) {
        this.id = id;
        this.personn = personn;
        this.role = role;
        this.mail = mail;
        this.motiepasse = motiepasse;
        this.status = status;
        this.numFel = numFel;
        this.birthDate = birthDate;
    }

    // Getters
    public int getId() { return id; }
    public String getPersonn() { return personn; }
    public String getRole() { return role; }
    public String getMail() { return mail; }
    public String getMotiepasse() { return motiepasse; }
    public String getStatus() { return status; }
    public String getNumFel() { return numFel; }
    public String getBirthDate() { return birthDate; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setPersonn(String personn) { this.personn = personn; }
    public void setRole(String role) { this.role = role; }
    public void setMail(String mail) { this.mail = mail; }
    public void setMotiepasse(String motiepasse) { this.motiepasse = motiepasse; }
    public void setStatus(String status) { this.status = status; }
    public void setNumFel(String numFel) { this.numFel = numFel; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", mail='" + mail + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}