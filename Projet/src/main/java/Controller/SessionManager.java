package Controller;

import model.user;

public class SessionManager {
    private static SessionManager instance;
    private user currentUser;

    private SessionManager() {
        // Constructeur privé pour le singleton
    }

    // Singleton
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // Définir l'utilisateur connecté
    public void setCurrentUser(user newUser) {
        this.currentUser = newUser;
    }

    // Obtenir l'utilisateur connecté
    public user getCurrentUser() {
        return currentUser;
    }

    // Récupérer des infos spécifiques si besoin
    public String getUserNom() {
        return currentUser != null ? currentUser.getNom() : null;
    }

    public String getUserPrenom() {
        return currentUser != null ? currentUser.getPrenom() : null;
    }

    public String getUserRole() {
        return currentUser != null ? currentUser.getRole() : null;
    }

    public String getUserEmail() {
        return currentUser != null ? currentUser.getMail() : null;
    }

    public String getUserPhone() {
        return currentUser != null ? currentUser.getNum_tel() : null;
    }

    public int getUserId() {
        return currentUser != null ? currentUser.getId() : -1;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public void logout() {
        currentUser = null;
    }
}
