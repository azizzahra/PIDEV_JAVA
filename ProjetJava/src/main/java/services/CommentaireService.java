package services;

import model.commentaire;
import Main.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentaireService {

    private Connection cnx;

    public CommentaireService() {
        cnx = DatabaseConnection.getInstance().getCnx();
    }

    // Récupérer les commentaires d'un post donné
    public List<commentaire> getCommentairesByPostId(int postId) {
        List<commentaire> commentaires = new ArrayList<>();
        String query = "SELECT * FROM commentaire WHERE post_id = ? ORDER BY created_at DESC";

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                commentaire c = new commentaire();
                c.setId(rs.getInt("id"));
                c.setPost_id(rs.getInt("post_id"));
                c.setUser_id(rs.getInt("user_id"));
                c.setContent(rs.getString("content"));
                c.setcreated_at(rs.getString("created_at"));
                commentaires.add(c);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return commentaires;
    }

    // Ajouter un commentaire
    public void ajouterCommentaire(commentaire c) {
        String sql = "INSERT INTO commentaire (post_id, user_id, content, created_at) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, c.getPost_id());
            stmt.setInt(2, c.getUser_id());
            stmt.setString(3, c.getContent());
            stmt.setString(4, c.getcreated_at());

            stmt.executeUpdate();
            System.out.println("✅ Commentaire ajouté avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
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
}
