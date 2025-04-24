package com.devmasters.agrosphere.userManagament.entities;


import java.sql.Date;

public class user {
    private int id;
    private String nom;
    private String prenom;
    private String role;
    private String mail;
    private String motDePasse;
    private String status;
    private String numTel;
    private Date birthDate;

    public user() {}

    public user(int id, String nom, String prenom, String role, String mail, String motDePasse,
                String status, String numTel, Date birthDate) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.role = role;
        this.mail = mail;
        this.motDePasse = motDePasse;
        this.status = status;
        this.numTel = numTel;
        this.birthDate = birthDate;
    }

    // Getters and setters for all fields

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

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNumTel() { return numTel; }
    public void setNumTel(String numTel) { this.numTel = numTel; }

    public Date getBirthDate() { return birthDate; }
    public void setBirthDate(Date birthDate) { this.birthDate = birthDate; }
}
