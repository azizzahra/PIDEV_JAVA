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

        System.out.println(posts);
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


}
