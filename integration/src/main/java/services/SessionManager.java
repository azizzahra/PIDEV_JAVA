package services;

import model.user;
import org.mindrot.jbcrypt.BCrypt;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SessionManager {
    private static final String SESSION_FILE = "session.properties";
    private static user currentUser = null;
    private static Map<Integer, Integer> userVotes = new HashMap<>();
    private static String userPreferredLanguage = "fr";

    // Save user session to properties file
    public static void saveSession(user user) {
        Properties props = new Properties();

        // Only save the email and a session token (not the password)
        props.setProperty("user_id", String.valueOf(user.getId()));
        props.setProperty("email", user.getMail());
        // Create a session token (you might want a more sophisticated approach)
        String sessionToken = BCrypt.hashpw(user.getMail() + System.currentTimeMillis(), BCrypt.gensalt());
        props.setProperty("session_token", sessionToken);

        try (FileOutputStream out = new FileOutputStream(SESSION_FILE)) {
            props.store(out, "User Session");
            currentUser = user; // Also keep in memory
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Check if a session exists and try to load it
    public static user loadSession() {
        File sessionFile = new File(SESSION_FILE);
        if (!sessionFile.exists()) {
            return null;
        }

        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(sessionFile)) {
            props.load(in);

            int userId = Integer.parseInt(props.getProperty("user_id", "-1"));
            String email = props.getProperty("email", "");

            if (userId != -1 && !email.isEmpty()) {
                // Fetch user from database using the ID and email
                UserService userService = new UserService();
                user user = userService.getUserById(userId);

                // Verify this is the right user
                if (user != null && user.getMail().equals(email)) {
                    currentUser = user;
                    return user;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Get the current logged-in user
    public static user getCurrentUser() {
        return currentUser;
    }

    // Set the current user (typically after authentication)
    public static void setCurrentUser(user user) {
        currentUser = user;
        // Also save to persistent storage
        saveSession(user);
    }

    // Clear the session (logout)
    public static void clearSession() {
        currentUser = null;
        File sessionFile = new File(SESSION_FILE);
        if (sessionFile.exists()) {
            sessionFile.delete();
        }
    }
    public static void setVoteForPost(int postId, int vote) {
        userVotes.put(postId, vote);
    }

    public static Integer getVoteForPost(int postId) {
        return userVotes.get(postId);
    }
    public static void removeVoteForPost(int postId) {
        // Vérifie d'abord si l'utilisateur a voté pour ce post
        if (userVotes.containsKey(postId)) {
            // Récupère la valeur du vote avant suppression pour log
            int removedVote = userVotes.get(postId);

            // Supprime le vote de la map
            userVotes.remove(postId);

            // Log de débogage (optionnel)
            System.out.println("Vote supprimé - Post ID: " + postId
                    + ", Valeur: " + removedVote);
        } else {
            System.out.println("Aucun vote à supprimer pour le post ID: " + postId);
        }
    }
    public static void setUserPreferredLanguage(String language) {
        userPreferredLanguage = language;
        System.out.println("Langue préférée définie sur: " + language);
    }

}