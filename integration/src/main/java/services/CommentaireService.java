package services;

import





        model.commentaire;
import Main.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentaireService {

    private Connection cnx;

    public CommentaireService() {
        cnx = DatabaseConnection.getInstance().getCnx();
    }

    // Récupérer les commentaires principaux (sans parent) d'un post donné
    public List<commentaire> getMainCommentsByPostId(int postId) {
        List<commentaire> commentaires = new ArrayList<>();
        String query = "SELECT * FROM commentaire WHERE post_id = ? AND parent_id IS NULL ORDER BY created_at DESC";

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                commentaire c = mapCommentaireFromResultSet(rs);
                commentaires.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return commentaires;
    }

    // Récupérer les réponses à un commentaire donné
    public List<commentaire> getRepliesByCommentId(int commentId) {
        List<commentaire> replies = new ArrayList<>();
        String query = "SELECT * FROM commentaire WHERE parent_id = ? ORDER BY created_at ASC";

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, commentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                commentaire c = mapCommentaireFromResultSet(rs);
                replies.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return replies;
    }

    // Méthode pour mapper un ResultSet à un objet commentaire
    private commentaire mapCommentaireFromResultSet(ResultSet rs) throws SQLException {
        commentaire c = new commentaire();
        c.setId(rs.getInt("id"));
        c.setPost_id(rs.getInt("post_id"));
        c.setUser_id(rs.getInt("user_id"));
        c.setContent(rs.getString("content"));
        c.setcreated_at(rs.getString("created_at"));

        // Gérer le parent_id qui peut être NULL
        Object parentIdObj = rs.getObject("parent_id");
        if (parentIdObj != null) {
            c.setParent_id((Integer) parentIdObj);
        } else {
            c.setParent_id(null);
        }

        return c;
    }

    // Méthode existante modifiée pour inclure parent_id
    public List<commentaire> getCommentairesByPostId(int postId) {
        List<commentaire> commentaires = new ArrayList<>();
        String query = "SELECT * FROM commentaire WHERE post_id = ? ORDER BY created_at DESC";

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                commentaire c = mapCommentaireFromResultSet(rs);
                commentaires.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return commentaires;
    }

    // Ajouter un commentaire (modifié pour inclure parent_id)
    public void ajouterCommentaire(commentaire c) {
        String sql;
        if (c.getParent_id() != null) {
            sql = "INSERT INTO commentaire (post_id, user_id, content, created_at, parent_id) VALUES (?, ?, ?, ?, ?)";
        } else {
            sql = "INSERT INTO commentaire (post_id, user_id, content, created_at, parent_id) VALUES (?, ?, ?, ?, NULL)";
        }

        try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, c.getPost_id());
            stmt.setInt(2, c.getUser_id());
            stmt.setString(3, c.getContent());
            stmt.setString(4, c.getcreated_at());

            if (c.getParent_id() != null) {
                stmt.setInt(5, c.getParent_id());
            }

            stmt.executeUpdate();
            System.out.println("✅ Commentaire ajouté avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Autres méthodes existantes...
    public void modifier(commentaire c) {
        String req = "UPDATE commentaire SET content = ? WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, c.getContent());
            ps.setInt(2, c.getId());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Commentaire modifié avec succès!");
            } else {
                System.out.println("Aucun commentaire modifié. L'ID pourrait être invalide.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification du commentaire: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void supprimer(int id) {
        // D'abord, supprimer les réponses associées à ce commentaire
        String deleteRepliesReq = "DELETE FROM commentaire WHERE parent_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(deleteRepliesReq)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression des réponses: " + e.getMessage());
            e.printStackTrace();
        }

        // Ensuite, supprimer le commentaire lui-même
        String req = "DELETE FROM commentaire WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, id);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Commentaire supprimé avec succès!");
            } else {
                System.out.println("Aucun commentaire supprimé. L'ID pourrait être invalide.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du commentaire: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public int getCommentCountByPostId(int postId) {
        int count = 0;
        String query = "SELECT COUNT(*) FROM commentaire WHERE post_id = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des commentaires: " + e.getMessage());
            e.printStackTrace();
        }

        return count;
    }
}
