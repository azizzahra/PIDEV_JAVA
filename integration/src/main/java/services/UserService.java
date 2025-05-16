package services;

import model.user;
import Main.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    private final Connection cnx;

    {
        DatabaseConnection.getInstance();
        cnx = DatabaseConnection.getCnx();
    }

    // Méthode déléguée au SessionManager
    public static user getCurrentUser() {
        return SessionManager.getCurrentUser();
    }

    public static user authenticate(String email, String password) {
        String sql = "SELECT * FROM user WHERE mail = ?";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getCnx().prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("motdepasse");
                if (BCrypt.checkpw(password, hashedPassword)) {
                    user loggedInUser = new user();
                    loggedInUser.setId(rs.getInt("id"));
                    loggedInUser.setNom(rs.getString("nom"));
                    loggedInUser.setPrenom(rs.getString("prenom"));
                    loggedInUser.setRole(rs.getString("role"));
                    loggedInUser.setMail(rs.getString("mail"));
                    loggedInUser.setMotdepasse(hashedPassword); // Nécessaire pour la mise à jour du profil
                    loggedInUser.setNum_tel(rs.getString("num_tel"));
                    loggedInUser.setBirth_Date(rs.getDate("birth_Date").toLocalDate());
                    loggedInUser.setStatus(rs.getString("status"));

                    // Mettre à jour dans le SessionManager au lieu de currentUser
                    SessionManager.setCurrentUser(loggedInUser);

                    return loggedInUser;
                }
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

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

    public void updateUser(int currentUserId, user u) {
        String req = "UPDATE user SET nom = ?, prenom = ?, mail = ?, motdepasse = ?, num_tel = ?, birth_Date = ? WHERE id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getMail());
            ps.setString(4, u.getMotdepasse());
            ps.setString(5, u.getNum_tel());
            ps.setDate(6, java.sql.Date.valueOf(u.getBirth_Date()));
            ps.setInt(7, currentUserId);

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Utilisateur mis à jour avec succès !");

                // Mettre à jour la session si l'utilisateur actuel est modifié
                user currentUser = SessionManager.getCurrentUser();
                if (currentUser != null && currentUser.getId() == currentUserId) {
                    // Mise à jour de la session avec les nouvelles données
                    u.setId(currentUserId);
                    u.setRole(currentUser.getRole()); // Préserver le rôle
                    u.setStatus(currentUser.getStatus()); // Préserver le statut
                    SessionManager.setCurrentUser(u);
                }
            } else {
                System.out.println("Aucune mise à jour effectuée. Vérifiez que l'utilisateur avec l'ID " + currentUserId + " existe.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour : " + e.getMessage());
            e.printStackTrace();
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

    public void updateUserStatus(int userId, String newStatus) {
        String req = "UPDATE user SET status = ? WHERE id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, newStatus);
            ps.setInt(2, userId);

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Statut de l'utilisateur mis à jour avec succès !");

                // Mettre à jour la session si l'utilisateur actuel est modifié
                user currentUser = SessionManager.getCurrentUser();
                if (currentUser != null && currentUser.getId() == userId) {
                    currentUser.setStatus(newStatus);
                    SessionManager.setCurrentUser(currentUser);

                    // Si l'utilisateur est désactivé, déconnectez-le
                    if ("inactif".equals(newStatus)) {
                        SessionManager.clearSession();
                    }
                }
            } else {
                System.out.println("Aucune mise à jour effectuée. Vérifiez l'ID de l'utilisateur.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du statut : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public user getUserById(int userId) {
        String sql = "SELECT * FROM user WHERE id = ?";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getCnx().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                user user = new user();
                user.setId(rs.getInt("id"));
                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                user.setRole(rs.getString("role"));
                user.setMail(rs.getString("mail"));
                user.setMotdepasse(rs.getString("motdepasse")); // Important pour la mise à jour
                user.setNum_tel(rs.getString("num_tel"));
                user.setBirth_Date(rs.getDate("birth_Date").toLocalDate());
                user.setStatus(rs.getString("status"));
                return user;
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    public List<user> getAllByRole(String role) throws Exception {
        List<user> list = new ArrayList<>();
        String sql = "SELECT * FROM user WHERE role=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, role);
        ResultSet rs = ps.executeQuery();

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
            list.add(u);
        }
        return list;
    }
    public user getOne(int id) throws Exception {
        String sql = "SELECT * FROM user WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            user u = new user();
            u.setId(rs.getInt("id"));
            u.setNom(rs.getString("nom"));
            u.setPrenom(rs.getString("prenom"));
            u.setRole(rs.getString("role"));
            u.setMail(rs.getString("mail"));
            u.setMotdepasse(rs.getString("motdepasse"));
            u.setStatus(rs.getString("status"));
            u.setNum_tel(rs.getString("num_tel"));
            u.setBirth_Date(rs.getDate("birth_date").toLocalDate());
            return u;
        }
        return null;
    }
    public boolean userExists(int userId) {
        String sql = "SELECT COUNT(*) FROM `user` WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


}