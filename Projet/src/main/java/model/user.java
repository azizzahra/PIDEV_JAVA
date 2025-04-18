package model;

import java.time.LocalDate;

public class user {
    private int id;
    private String nom;
    private String prenom;
    private String role;
    private String mail;
    private String motdepasse;
    private String status;
    private String num_tel;
    private LocalDate birth_Date;

    public user() {}

    public user(int id, String nom, String prenom, String role, String mail, String motdepasse, String status, String num_tel, LocalDate birth_Date) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.role = role;
        this.mail = mail;
        this.motdepasse = motdepasse;
        this.status = status;
        this.num_tel = num_tel;
        this.birth_Date = birth_Date;
    }

    public user(String nom, String prenom, String role, String mail, String motdepasse, String status, String num_tel, LocalDate birth_Date) {
        this.nom = nom;
        this.prenom = prenom;
        this.role = role;
        this.mail = mail;
        this.motdepasse = motdepasse;
        this.status = status;
        this.num_tel = num_tel;
        this.birth_Date = birth_Date;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }

    public String getMotdepasse() { return motdepasse; }
    public void setMotdepasse(String motdepasse) { this.motdepasse = motdepasse; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNum_tel() { return num_tel; }
    public void setNum_tel(String num_tel) { this.num_tel = num_tel; }

    public LocalDate getBirth_Date() { return birth_Date; }
    public void setBirth_Date(LocalDate birth_Date) { this.birth_Date = birth_Date; }
}
