package services;

import model.user;
import Main.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    private Connection cnx = DatabaseConnection.getInstance().getCnx();

    public void addUser(user u) {
        String req = "INSERT INTO user (nom, prenom, role, mail, motdepasse, status, num_tel, birth_Date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getRole());
            ps.setString(4, u.getMail());
            ps.setString(5, u.getMotdepasse());
            ps.setString(6, u.getStatus());
            ps.setString(7, u.getNum_tel());
            ps.setDate(8, java.sql.Date.valueOf(u.getBirth_Date()));
            ps.executeUpdate();
            System.out.println("Utilisateur ajouté !");
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout : " + e.getMessage());
        }
    }
    public void updateUser(int id, user u) {
        String req = "UPDATE user SET nom = ?, prenom = ?, role = ?, mail = ?, motdepasse = ?, status = ?, num_tel = ?, birth_Date = ? WHERE id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getRole());
            ps.setString(4, u.getMail());
            ps.setString(5, u.getMotdepasse()); // Déjà hashé avant d'arriver ici
            ps.setString(6, u.getStatus());
            ps.setString(7, u.getNum_tel());
            ps.setDate(8, java.sql.Date.valueOf(u.getBirth_Date()));
            ps.setInt(9, id);

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Utilisateur mis à jour !");
            } else {
                System.out.println("Aucune mise à jour effectuée. Vérifiez l'ID.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour : " + e.getMessage());
        }
    }
    public List<user> getAllUsers() {
        List<user> users = new ArrayList<>();
        String sql = "SELECT * FROM user";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getCnx().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                user u = new user();
                u.setId(rs.getInt("id"));
                u.setNom(rs.getString("nom"));
                u.setPrenom(rs.getString("prenom"));
                u.setRole(rs.getString("role"));
                u.setMail(rs.getString("mail"));
                u.setMotdepasse(rs.getString("motdepasse"));
                u.setStatus(rs.getString("status"));
                u.setNum_tel(rs.getString("num_tel"));
                u.setBirth_Date(rs.getDate("birth_Date").toLocalDate());
                users.add(u);
            }
        } catch (SQLException e) {
            System.err.println("Erreur chargement des utilisateurs : " + e.getMessage());
        }
        return users;
    }
}
