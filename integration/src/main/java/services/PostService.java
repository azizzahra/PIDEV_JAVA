package services;

import Main.DatabaseConnection;
import model.post;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostService implements Iservices<post> {

    private Connection cnx;

    public PostService() {
        cnx = DatabaseConnection.getInstance().getCnx();
    }

    @Override
    public void add(post post) {
        String req = "INSERT INTO integration5.post (title, username_id, content, category, attachment, date_c, views, vote) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement stm = cnx.prepareStatement(req);
            stm.setString(1, post.getTitle());
            stm.setInt(2, post.getUsernameId());
            stm.setString(3, post.getContent());
            stm.setString(4, post.getCategory());
            stm.setString(5, post.getAttachment());
            stm.setString(6, post.getDateC());
            stm.setInt(7, post.getViews());
            stm.setInt(8, post.getVote());
            stm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void modify(post post) {
        // Implementation for modifying a post (if needed)
    }

    // Méthode pour récupérer tous les posts
    @Override
    public List<post> afficher() {
        List<post> posts = new ArrayList<>();
        String req = "SELECT * FROM post ORDER BY date_c DESC"; // Pour trier par date

        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(req);

            while (rs.next()) {
                post post = new post();
                post.setId(rs.getInt("id"));
                post.setTitle(rs.getString("title"));
                post.setUsernameId(rs.getInt("username_id"));
                post.setContent(rs.getString("content"));
                post.setCategory(rs.getString("category"));
                post.setAttachment(rs.getString("attachment"));
                post.setDateC(rs.getString("date_c"));
                post.setViews(rs.getInt("views"));
                post.setVote(rs.getInt("vote"));

                posts.add(post);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return posts;
    }

    // Méthode spécifique pour récupérer tous les posts
    public List<post> getAllPosts() {
        return afficher(); // On réutilise afficher(), car elle récupère déjà tous les posts
    }

    @Override
    public post getone() {
        return null;
    }

    public void modifier(post p) {
        String req = "UPDATE post SET title = ?, content = ?, category = ?, attachment = ? WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, p.getTitle());
            ps.setString(2, p.getContent());
            ps.setString(3, p.getCategory());
            ps.setString(4, p.getAttachment());
            ps.setInt(5, p.getId());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Post modifié avec succès!");
            } else {
                System.out.println("Aucun post modifié. L'ID pourrait être invalide.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification du post: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void supprimer(int id) {
        // D'abord supprimer tous les commentaires associés à ce post
        String deleteComments = "DELETE FROM commentaire WHERE post_id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(deleteComments)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Commentaires associés supprimés.");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression des commentaires: " + e.getMessage());
            throw new RuntimeException(e);
        }

        // Puis supprimer le post
        String deletePost = "DELETE FROM post WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(deletePost)) {
            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Post supprimé avec succès!");
            } else {
                System.out.println("Aucun post supprimé. L'ID pourrait être invalide.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du post: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Incrémente le compteur de vues pour un post spécifique
     * @param postId ID du post
     * @return le nouveau nombre de vues
     */
    public int incrementViewCount(int postId) {
        // 1. D'abord, récupérer le nombre actuel de vues
        String selectQuery = "SELECT views FROM post WHERE id = ?";
        int currentViews = 0;

        try (PreparedStatement ps = cnx.prepareStatement(selectQuery)) {
            ps.setInt(1, postId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                currentViews = rs.getInt("views");
            } else {
                throw new RuntimeException("Post non trouvé avec l'ID: " + postId);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des vues: " + e.getMessage());
            throw new RuntimeException(e);
        }

        // 2. Incrémenter le nombre de vues
        int newViews = currentViews + 1;
        String updateQuery = "UPDATE post SET views = ? WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(updateQuery)) {
            ps.setInt(1, newViews);
            ps.setInt(2, postId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Échec de la mise à jour du compteur de vues");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour des vues: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return newViews;
    }

    /**
     * Récupère un post spécifique par son ID
     * @param postId l'ID du post à récupérer
     * @return l'objet post correspondant, ou null si non trouvé
     */
    public post getPostById(int postId) {
        String query = "SELECT * FROM post WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, postId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                post p = new post();
                p.setId(rs.getInt("id"));
                p.setTitle(rs.getString("title"));
                p.setUsernameId(rs.getInt("username_id"));
                p.setContent(rs.getString("content"));
                p.setCategory(rs.getString("category"));
                p.setAttachment(rs.getString("attachment"));
                p.setDateC(rs.getString("date_c"));
                p.setViews(rs.getInt("views"));
                p.setVote(rs.getInt("vote"));
                return p;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du post: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return null;
    }
    // Ajoutez ces méthodes à votre classe PostService existante

    /**
     * Met à jour le compteur de votes pour un post
     * @param postId ID du post
     * @param voteChange La valeur à ajouter (positif ou négatif)
     * @return le nouveau nombre de votes
     */
    public int updateVoteCount(int postId, int voteChange) {
        // 1. D'abord, récupérer le nombre actuel de votes
        String selectQuery = "SELECT vote FROM post WHERE id = ?";
        int currentVotes = 0;

        try (PreparedStatement ps = cnx.prepareStatement(selectQuery)) {
            ps.setInt(1, postId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                currentVotes = rs.getInt("vote");
            } else {
                throw new RuntimeException("Post non trouvé avec l'ID: " + postId);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des votes: " + e.getMessage());
            throw new RuntimeException(e);
        }

        // 2. Calculer le nouveau nombre de votes
        int newVotes = currentVotes + voteChange;
        String updateQuery = "UPDATE post SET vote = ? WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(updateQuery)) {
            ps.setInt(1, newVotes);
            ps.setInt(2, postId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Échec de la mise à jour du compteur de votes");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour des votes: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return newVotes;
    }
}