package services;

import model.user;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Service to track and manage post views.
 * Uses a file-based approach to persist view data across application sessions.
 */
public class PostViewTracker {
    private static final String VIEW_TRACKING_FILE = "post_views.dat";
    // Map: postId -> Set of userIds who viewed the post
    private static Map<Integer, Set<Integer>> postViewers = new HashMap<>();

    // Load existing view data when class is initialized
    static {
        loadViewData();
    }

    /**
     * Records a view for a post by a specific user.
     *
     * @param postId ID of the post being viewed
     * @param userId ID of the user viewing the post
     * @return true if this is a new view (count incremented), false if user already viewed this post
     */
    public static boolean recordView(int postId, int userId) {
        // Get or create the set of viewers for this post
        Set<Integer> viewers = postViewers.computeIfAbsent(postId, k -> new HashSet<>());

        // Check if this user has already viewed this post
        if (!viewers.contains(userId)) {
            // This is a new view
            viewers.add(userId);
            saveViewData(); // Persist the view data
            return true; // Indicate that the view count should be incremented
        }

        return false; // User already viewed this post, don't increment
    }

    /**
     * Checks if a user has already viewed a post.
     *
     * @param postId ID of the post
     * @param userId ID of the user
     * @return true if the user has already viewed the post, false otherwise
     */
    public static boolean hasUserViewedPost(int postId, int userId) {
        Set<Integer> viewers = postViewers.get(postId);
        return viewers != null && viewers.contains(userId);
    }

    /**
     * Saves the view tracking data to a file.
     */
    private static void saveViewData() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(VIEW_TRACKING_FILE))) {
            out.writeObject(postViewers);
        } catch (IOException e) {
            System.err.println("Error saving post view data: " + e.getMessage());
        }
    }

    /**
     * Loads the view tracking data from file.
     */
    @SuppressWarnings("unchecked")
    private static void loadViewData() {
        File file = new File(VIEW_TRACKING_FILE);
        if (!file.exists()) {
            postViewers = new HashMap<>();
            return;
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            postViewers = (Map<Integer, Set<Integer>>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading post view data: " + e.getMessage());
            postViewers = new HashMap<>();
        }
    }
}