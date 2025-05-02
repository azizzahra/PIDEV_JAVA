package services.user;

import com.devmasters.agrosphere.userManagement.entities.user;
import services.Iservices;
import com.devmasters.agrosphere.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService implements Iservices<user> {

    private final Connection conn;

    public UserService() {
        conn = DBConnection.getInstance().getConnection();
    }

    @Override
    public void add(user u) throws Exception {
        String sql = "INSERT INTO user (nom, prenom, role, mail, motdepasse, status, num_tel, birth_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, u.getNom());
        ps.setString(2, u.getPrenom());
        ps.setString(3, u.getRole());
        ps.setString(4, u.getMail());
        ps.setString(5, u.getMotDePasse());
        ps.setString(6, u.getStatus());
        ps.setString(7, u.getNumTel());
        ps.setDate(8, u.getBirthDate());
        ps.executeUpdate();
    }

    @Override
    public void update(user u) throws Exception {
        String sql = "UPDATE user SET nom=?, prenom=?, role=?, mail=?, motdepasse=?, status=?, num_tel=?, birth_date=? WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, u.getNom());
        ps.setString(2, u.getPrenom());
        ps.setString(3, u.getRole());
        ps.setString(4, u.getMail());
        ps.setString(5, u.getMotDePasse());
        ps.setString(6, u.getStatus());
        ps.setString(7, u.getNumTel());
        ps.setDate(8, u.getBirthDate());
        ps.setInt(9, u.getId());
        ps.executeUpdate();
    }

    @Override
    public void delete(int id) throws Exception {
        String sql = "DELETE FROM user WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<user> getAll() throws Exception {
        List<user> list = new ArrayList<>();
        String sql = "SELECT * FROM user";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        while (rs.next()) {
            user u = new user();
            u.setId(rs.getInt("id"));
            u.setNom(rs.getString("nom"));
            u.setPrenom(rs.getString("prenom"));
            u.setRole(rs.getString("role"));
            u.setMail(rs.getString("mail"));
            u.setMotDePasse(rs.getString("motdepasse"));
            u.setStatus(rs.getString("status"));
            u.setNumTel(rs.getString("num_tel"));
            u.setBirthDate(rs.getDate("birth_date"));
            list.add(u);
        }
        return list;
    }

    @Override
    public user getOne(int id) throws Exception {
        String sql = "SELECT * FROM user WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            user u = new user();
            u.setId(rs.getInt("id"));
            u.setNom(rs.getString("nom"));
            u.setPrenom(rs.getString("prenom"));
            u.setRole(rs.getString("role"));
            u.setMail(rs.getString("mail"));
            u.setMotDePasse(rs.getString("motdepasse"));
            u.setStatus(rs.getString("status"));
            u.setNumTel(rs.getString("num_tel"));
            u.setBirthDate(rs.getDate("birth_date"));
            return u;
        }
        return null;
    }
    public List<user> getAllByRole(String role) throws Exception {
        List<user> list = new ArrayList<>();
        String sql = "SELECT * FROM user WHERE role=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, role);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            user u = new user();
            u.setId(rs.getInt("id"));
            u.setNom(rs.getString("nom"));
            u.setPrenom(rs.getString("prenom"));
            u.setRole(rs.getString("role"));
            u.setMail(rs.getString("mail"));
            u.setMotDePasse(rs.getString("motdepasse"));
            u.setStatus(rs.getString("status"));
            u.setNumTel(rs.getString("num_tel"));
            u.setBirthDate(rs.getDate("birth_date"));
            list.add(u);
        }
        return list;
    }

    public String getUserName(int userId) {
        String name = "Unknown User";

        try (PreparedStatement stmt = conn.prepareStatement(
                     "SELECT firstName, lastName FROM Users WHERE id = ?")) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String firstName = rs.getString("firstName");
                String lastName = rs.getString("lastName");
                name = firstName + " " + lastName;
            }

        } catch (SQLException e) {
            System.err.println("Error fetching user name: " + e.getMessage());
            // Return default name in case of error
        }

        return name;
    }
    private user mapResultSetToUser(ResultSet rs) throws Exception {
        user u = new user();
        u.setId(rs.getInt("id"));
        u.setNom(rs.getString("nom"));
        u.setPrenom(rs.getString("prenom"));
        u.setMail(rs.getString("mail"));
        u.setNumTel(rs.getString("num_tel"));
        u.setRole(rs.getString("role"));
        u.setMotDePasse(rs.getString("motdepasse"));
        u.setStatus(rs.getString("status"));
        u.setBirthDate(rs.getDate("birth_date"));
        return u;

    }
}

