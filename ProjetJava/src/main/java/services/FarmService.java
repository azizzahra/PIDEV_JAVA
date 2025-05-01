package services;
import Main.DatabaseConnection;
import model.Farm;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FarmService implements IservicesAziz<Farm>
{
    Connection cnx;

    public FarmService() {
        cnx = DatabaseConnection.getInstance().getCnx();
    }

    @Override
    public void add(Farm farm) throws SQLException{
        String req = "INSERT INTO integration5.Farm (name, size, location, image, description, latitude, longitude, user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement stm = cnx.prepareStatement(req);
        stm.setString(1, farm.getName());
        stm.setInt(2, farm.getSize());
        stm.setString(3, farm.getLocation());
        stm.setString(4, farm.getImage());
        stm.setString(5, farm.getDescription());
        stm.setDouble(6, farm.getLatitude());
        stm.setDouble(7, farm.getLongitude());
        stm.setInt(8, farm.getUserId());
        stm.executeUpdate();

    }

    @Override
    public int update(Farm farm)  throws SQLException{
        // Implementation for modifying a farm (if needed)
        String req= "UPDATE integration5.farm SET name=?, size=?,  location=?, image=?, description=?, latitude=?, longitude=?, user_id=? WHERE id=?";

        PreparedStatement stm = cnx.prepareStatement(req);
        stm.setString(1, farm.getName());
        stm.setInt(2, farm.getSize());
        stm.setString(3, farm.getLocation());
        stm.setString(4, farm.getImage());
        stm.setString(5, farm.getDescription());
        stm.setDouble(6, farm.getLatitude());
        stm.setDouble(7, farm.getLongitude());
        stm.setInt(8, farm.getUserId());
        stm.setInt(9, farm.getId()); // Clause WHERE pour l'ID

        return stm.executeUpdate();


    }

    @Override
    public List<Farm> getAll()  throws SQLException {
        List<Farm> farms = new ArrayList<>();
        String req = "SELECT * FROM integration5.farm";


        Statement stm = cnx.createStatement();
        ResultSet rs = stm.executeQuery(req);

        while (rs.next()) {
            Farm farm = new Farm();
            farm.setId(rs.getInt("id"));
            farm.setName(rs.getString("name"));
            farm.setSize(rs.getInt("size"));
            farm.setLocation(rs.getString("location"));
            farm.setImage(rs.getString("image"));
            farm.setDescription(rs.getString("description"));
            farm.setLatitude(rs.getDouble("latitude"));
            farm.setLongitude(rs.getDouble("longitude"));
            farm.setUserId(rs.getInt("user_id")); // Ajoutez cette ligne

            farms.add(farm);
        }
        System.out.println(farms);
        return farms;
    }

    @Override
    public void delete(Farm farm) throws SQLException {
        String req = "DELETE FROM integration5.farm WHERE id=?";

        PreparedStatement stm = cnx.prepareStatement(req);
        stm.setInt(1, farm.getId());
        stm.executeUpdate();
    }

    @Override
    public Farm getone(int id) throws SQLException {
        String req = "SELECT * FROM integration5.farm WHERE id = ?";

        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setInt(1, id);
            ResultSet rs = stm.executeQuery();
            if (rs.next()) {
                Farm farm = new Farm();
                // Ajoutez l'ID et user_id
                farm.setId(rs.getInt("id"));
                farm.setName(rs.getString("name"));
                farm.setSize(rs.getInt("size"));
                farm.setLocation(rs.getString("location"));
                farm.setImage(rs.getString("image"));
                farm.setDescription(rs.getString("description"));
                farm.setLatitude(rs.getDouble("latitude"));
                farm.setLongitude(rs.getDouble("longitude"));
                farm.setUserId(rs.getInt("user_id")); // Ajoutez user_id
                return farm;
            }
        }
        return null;
    }
    public boolean farmExiste(String name) throws SQLException {
        String req = "SELECT COUNT(*) FROM integration5.farm WHERE name = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true; // La ferme existe déjà
            }
        }
        return false; // La ferme n'existe pas
    }
}
