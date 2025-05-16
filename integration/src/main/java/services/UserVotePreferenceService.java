package services;

import java.util.prefs.Preferences;

/**
 * Cette classe utilise les préférences utilisateur Java pour stocker les votes
 * Cela permet de conserver l'état des votes sans créer de nouvelles tables
 */
public class UserVotePreferenceService {

    private static final String VOTE_PREFIX = "post_vote_";
    private final Preferences preferences;

    public UserVotePreferenceService() {
        // Utilise les préférences utilisateur pour stocker les votes
        preferences = Preferences.userNodeForPackage(UserVotePreferenceService.class);
    }

    /**
     * Génère une clé unique pour un vote utilisateur-post
     * @param userId ID de l'utilisateur
     * @param postId ID du post
     * @return Clé unique
     */
    private String generateVoteKey(int userId, int postId) {
        return VOTE_PREFIX + userId + "_" + postId;
    }

    /**
     * Récupère le statut de vote d'un utilisateur pour un post
     * @param userId ID de l'utilisateur
     * @param postId ID du post
     * @return 1 pour upvote, -1 pour downvote, 0 si pas de vote
     */
    public int getUserVoteStatus(int userId, int postId) {
        String key = generateVoteKey(userId, postId);
        return preferences.getInt(key, 0);
    }

    /**
     * Enregistre le vote d'un utilisateur pour un post
     * @param userId ID de l'utilisateur
     * @param postId ID du post
     * @param voteType 1 pour upvote, -1 pour downvote, 0 pour annuler le vote
     */
    public void saveUserVote(int userId, int postId, int voteType) {
        String key = generateVoteKey(userId, postId);
        preferences.putInt(key, voteType);
    }

    /**
     * Supprime le vote d'un utilisateur pour un post
     * @param userId ID de l'utilisateur
     * @param postId ID du post
     */
    public void removeUserVote(int userId, int postId) {
        String key = generateVoteKey(userId, postId);
        preferences.remove(key);
    }
}