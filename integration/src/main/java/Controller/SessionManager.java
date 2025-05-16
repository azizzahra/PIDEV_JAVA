package Controller;

import java.security.PublicKey;

public class SessionManager {
    private static SessionManager instance;
    private String userId;

    private String nom ;
    private String prenom ;
    private int num_tel ;
    private  String role;

    private int userfront ;

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public int getTel() {
        return num_tel;
    }

    public void setTel(int tel) {
        this.num_tel = tel;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserFront(int userfront) {
        this.userfront = userfront;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public int getUserFront() {
        return userfront;
    }
    public String getRole(){
        return  role;
    }


    public String getUserId()
    {
        return userId;
    }
    public void cleanUserSessionAdmin() {
        userId= " " ;
    }
    public void cleanUserSessionFront() {
        userfront= 0;
    }

}

