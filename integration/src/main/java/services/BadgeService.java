package services;

import Main.DatabaseConnection;
import model.Badge;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BadgeService {
    private final Connection cnx = DatabaseConnection.getInstance().getCnx();

    public void addBadge(Badge badge) {
        String query = "INSERT INTO badge (titre, description, type, dateAttribution, userId) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, badge.getTitre());
            ps.setString(2, badge.getDescription());
            ps.setString(3, badge.getType());
            ps.setDate(4, Date.valueOf(badge.getDateAttribution()));
            ps.setInt(5, badge.getUserId());
            int rowsAffected = ps.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        badge.setId(generatedKeys.getInt(1));
                    }
                }
                System.out.println("Badge ajouté avec succès");
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL: " + e.getMessage());
            System.err.println("Code d'erreur SQL: " + e.getErrorCode());
            System.err.println("État SQL: " + e.getSQLState());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'ajout du badge: " + e.getMessage(), e);
        }
    }

    public List<Badge> getBadgesByUserId(int userId) {
        List<Badge> badges = new ArrayList<>();
        String query = "SELECT * FROM badge WHERE userId = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Badge badge = new Badge();
                badge.setId(rs.getInt("id"));
                badge.setTitre(rs.getString("titre"));
                badge.setDescription(rs.getString("description"));
                badge.setType(rs.getString("type"));
                badge.setDateAttribution(rs.getDate("dateAttribution").toLocalDate());
                badge.setUserId(rs.getInt("userId"));
                badges.add(badge);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return badges;
    }

    public void deleteBadge(int badgeId) {
        String query = "DELETE FROM badge WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, badgeId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateBadge(Badge badge) {
        String query = "UPDATE badge SET titre = ?, description = ?, type = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, badge.getTitre());
            ps.setString(2, badge.getDescription());
            ps.setString(3, badge.getType());
            ps.setInt(4, badge.getId());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Badge modifié avec succès");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la modification du badge: " + e.getMessage(), e);
        }
    }
    public List<Badge> getAllBadges() {
        List<Badge> badges = new ArrayList<>();
        String query = "SELECT * FROM badge";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                Badge badge = new Badge();
                badge.setId(rs.getInt("id"));
                badge.setTitre(rs.getString("titre"));
                badge.setDescription(rs.getString("description"));
                badge.setType(rs.getString("type"));
                badge.setUserId(rs.getInt("userId"));

                // Récupérer la date d'attribution si elle existe
                Date dateAttribution = rs.getDate("dateAttribution");
                if (dateAttribution != null) {
                    badge.setDateAttribution(dateAttribution.toLocalDate());
                }

                badges.add(badge);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de tous les badges: " + e.getMessage());
            e.printStackTrace();
        }
        return badges;
    }

    /**
     * Récupère les badges par type
     * @param type Le type de badge (BRONZE, SILVER, GOLD, etc.)
     * @return Liste des badges du type spécifié
     */
    public List<Badge> getBadgesByType(String type) {
        List<Badge> badges = new ArrayList<>();
        String query = "SELECT * FROM badge WHERE type = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, type);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Badge badge = new Badge();
                badge.setId(rs.getInt("id"));
                badge.setTitre(rs.getString("titre"));
                badge.setDescription(rs.getString("description"));
                badge.setType(rs.getString("type"));
                badge.setUserId(rs.getInt("userId"));

                Date dateAttribution = rs.getDate("dateAttribution");
                if (dateAttribution != null) {
                    badge.setDateAttribution(dateAttribution.toLocalDate());
                }

                badges.add(badge);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des badges par type: " + e.getMessage());
            e.printStackTrace();
        }
        return badges;
    }

    /**
     * Récupère les badges attribués après une date spécifiée
     * @param date La date à partir de laquelle récupérer les badges
     * @return Liste des badges attribués après la date spécifiée
     */
    public List<Badge> getBadgesAfterDate(LocalDate date) {
        List<Badge> badges = new ArrayList<>();
        String query = "SELECT * FROM badge WHERE dateAttribution >= ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setDate(1, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Badge badge = new Badge();
                badge.setId(rs.getInt("id"));
                badge.setTitre(rs.getString("titre"));
                badge.setDescription(rs.getString("description"));
                badge.setType(rs.getString("type"));
                badge.setUserId(rs.getInt("userId"));
                badge.setDateAttribution(rs.getDate("dateAttribution").toLocalDate());

                badges.add(badge);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des badges par date: " + e.getMessage());
            e.printStackTrace();
        }
        return badges;
    }

    /**
     * Compte le nombre de badges par type
     * @param type Le type de badge à compter
     * @return Le nombre de badges du type spécifié
     */
    public int countBadgesByType(String type) {
        String query = "SELECT COUNT(*) FROM badge WHERE type = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, type);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des badges par type: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Récupère les utilisateurs ayant le plus de badges
     * @param limit Nombre maximum d'utilisateurs à récupérer
     * @return Liste de tableaux d'objets contenant l'ID utilisateur et le nombre de badges
     */
    public List<Object[]> getTopUsersWithMostBadges(int limit) {
        List<Object[]> result = new ArrayList<>();
        String query = "SELECT userId, COUNT(*) as badge_count FROM badge GROUP BY userId ORDER BY badge_count DESC LIMIT ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Object[] row = new Object[2];
                row[0] = rs.getInt("userId");
                row[1] = rs.getInt("badge_count");
                result.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des utilisateurs avec le plus de badges: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Compte le nombre total de badges dans la base de données
     * @return Le nombre total de badges
     */
    public int getTotalBadgesCount() {
        String query = "SELECT COUNT(*) FROM badge";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage du nombre total de badges: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
} 