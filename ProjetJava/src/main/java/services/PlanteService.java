package services;

import Main.DatabaseConnection;
import model.plante;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlanteService implements Iservices<plante>
{
    Connection cnx;

    public PlanteService() {
        cnx = DatabaseConnection.getInstance().getCnx();
    }

    @Override
    public void add(plante plante) throws SQLException {
        String req = "INSERT INTO integration5.plante (farm_id, name, type, image, plantation_date, harvest_date, quantity) VALUES (?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement stm = cnx.prepareStatement(req);
        stm.setInt(1, plante.getFarmId());
        stm.setString(2, plante.getName());
        stm.setString(3, plante.getType());
        stm.setString(4, plante.getImage());
        stm.setString(5, plante.getPlantationDate());
        stm.setString(6, plante.getHarvestDate());
        stm.setInt(7, plante.getQuantity());
        stm.executeUpdate();
    }

    @Override
    public int update(plante plante) throws SQLException {
        String req = "UPDATE integration5.plante SET farm_id=?, name=?, type=?, image=?, plantation_date=?, harvest_date=?, quantity=? WHERE id=?";

        PreparedStatement stm = cnx.prepareStatement(req);
        stm.setInt(1, plante.getFarmId());
        stm.setString(2, plante.getName());
        stm.setString(3, plante.getType());
        stm.setString(4, plante.getImage());
        stm.setString(5, plante.getPlantationDate());
        stm.setString(6, plante.getHarvestDate());
        stm.setInt(7, plante.getQuantity());
        stm.setInt(8, plante.getId());

        return stm.executeUpdate();
    }

    @Override
    public List<plante> getAll() throws SQLException {
        List<plante> plantes = new ArrayList<>();
        String req = "SELECT * FROM integration5.plante";

        Statement stm = cnx.createStatement();
        ResultSet rs = stm.executeQuery(req);

        while (rs.next()) {
            plante p = new plante();
            p.setId(rs.getInt("id"));
            p.setFarmId(rs.getInt("farm_id"));
            p.setName(rs.getString("name"));
            p.setType(rs.getString("type"));
            p.setImage(rs.getString("image"));
            p.setPlantationDate(rs.getString("plantation_date"));
            p.setHarvestDate(rs.getString("harvest_date"));
            p.setQuantity(rs.getInt("quantity"));

            plantes.add(p);
        }
        System.out.println(plantes);
        return plantes;
    }

    @Override
    public void delete(plante plante) throws SQLException {
        String req = "DELETE FROM integration5.plante WHERE id=?";

        PreparedStatement stm = cnx.prepareStatement(req);
        stm.setInt(1, plante.getId());
        stm.executeUpdate();
    }

    @Override
    public plante getone(int id) throws SQLException {
        String req = "SELECT * FROM integration5.plante WHERE id = ?";

        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setInt(1, id);
            ResultSet rs = stm.executeQuery();
            if (rs.next()) {
                plante p = new plante();
                p.setId(rs.getInt("id"));
                p.setFarmId(rs.getInt("farm_id"));
                p.setName(rs.getString("name"));
                p.setType(rs.getString("type"));
                p.setImage(rs.getString("image"));
                p.setPlantationDate(rs.getString("plantation_date"));
                p.setHarvestDate(rs.getString("harvest_date"));
                p.setQuantity(rs.getInt("quantity"));
                return p;
            }
        }
        return null;
    }

    public boolean planteExiste(String name, int farmId) throws SQLException {
        String req = "SELECT COUNT(*) FROM integration5.plante WHERE name = ? AND farm_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, name);
            ps.setInt(2, farmId);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true; // La plante existe déjà dans cette ferme
            }
        }
        return false; // La plante n'existe pas dans cette ferme
    }

    public List<plante> getPlantesByFarmId(int farmId) throws SQLException {
        List<plante> plantes = new ArrayList<>();
        String req = "SELECT * FROM integration5.plante WHERE farm_id = ?";

        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setInt(1, farmId);
            ResultSet rs = stm.executeQuery();

            while (rs.next()) {
                plante p = new plante();
                p.setId(rs.getInt("id"));
                p.setFarmId(rs.getInt("farm_id"));
                p.setName(rs.getString("name"));
                p.setType(rs.getString("type"));
                p.setImage(rs.getString("image"));
                p.setPlantationDate(rs.getString("plantation_date"));
                p.setHarvestDate(rs.getString("harvest_date"));
                p.setQuantity(rs.getInt("quantity"));

                plantes.add(p);
            }
        }
        return plantes;
    }
}