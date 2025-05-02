package services;
import Main.DatabaseConnection;
import model.Farm;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
                farm.setUserId(rs.getInt("user_id"));
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


    public List<Farm> getFarmsByLocations(List<String> locations) throws SQLException {
        if (locations.isEmpty()) {
            return getAll();
        }

        List<Farm> filteredFarms = new ArrayList<>();
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM integration5.farm WHERE location IN (");

        for (int i = 0; i < locations.size(); i++) {
            queryBuilder.append("?");
            if (i < locations.size() - 1) {
                queryBuilder.append(",");
            }
        }
        queryBuilder.append(")");

        try (PreparedStatement stm = cnx.prepareStatement(queryBuilder.toString())) {
            for (int i = 0; i < locations.size(); i++) {
                stm.setString(i+1, locations.get(i));
            }

            ResultSet rs = stm.executeQuery();
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
                farm.setUserId(rs.getInt("user_id"));
                filteredFarms.add(farm);
            }
        }

        return filteredFarms;
    }


    public List<Farm> getFarmsBySize(int minSize, int maxSize) throws SQLException {
        String req;
        if (maxSize > 0) {
            req = "SELECT * FROM integration5.farm WHERE size >= ? AND size <= ?";
        } else {
            req = "SELECT * FROM integration5.farm WHERE size >= ?";
        }

        List<Farm> filteredFarms = new ArrayList<>();
        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setInt(1, minSize);
            if (maxSize > 0) {
                stm.setInt(2, maxSize);
            }

            ResultSet rs = stm.executeQuery();
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
                farm.setUserId(rs.getInt("user_id"));
                filteredFarms.add(farm);
            }
        }

        return filteredFarms;
    }


    public Map<String, Integer> getLocationCounts() throws SQLException {
        Map<String, Integer> counts = new HashMap<>();
        String req = "SELECT location, COUNT(*) as count FROM integration5.farm GROUP BY location";

        try (Statement stm = cnx.createStatement()) {
            ResultSet rs = stm.executeQuery(req);
            while (rs.next()) {
                String location = rs.getString("location");
                int count = rs.getInt("count");
                counts.put(location, count);
            }
        }

        return counts;
    }

    public Map<String, Integer> getSizeCounts() throws SQLException {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("small", 0);
        counts.put("medium", 0);
        counts.put("large", 0);

        List<Farm> farms = getAll();
        for (Farm farm : farms) {
            int size = farm.getSize();
            if (size < 1000) {
                counts.put("small", counts.get("small") + 1);
            } else if (size >= 1000 && size <= 2000) {
                counts.put("medium", counts.get("medium") + 1);
            } else {
                counts.put("large", counts.get("large") + 1);
            }
        }

        return counts;
    }

    public List<Farm> getFilteredFarms(int userId, List<String> locations, int minSize, int maxSize, String searchKeyword) throws SQLException {
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM integration5.farm WHERE 1=1");
        List<Object> params = new ArrayList<>();

        // Filter by user ID
        if (userId > 0) {
            queryBuilder.append(" AND user_id = ?");
            params.add(userId);
        }

        // Filter by locations
        if (locations != null && !locations.isEmpty()) {
            queryBuilder.append(" AND location IN (");
            for (int i = 0; i < locations.size(); i++) {
                queryBuilder.append("?");
                params.add(locations.get(i));
                if (i < locations.size() - 1) {
                    queryBuilder.append(",");
                }
            }
            queryBuilder.append(")");
        }

        // Filter by size
        if (minSize > 0) {
            queryBuilder.append(" AND size >= ?");
            params.add(minSize);
        }
        if (maxSize > 0) {
            queryBuilder.append(" AND size <= ?");
            params.add(maxSize);
        }

        // Filter by search keyword
        if (searchKeyword != null && !searchKeyword.isEmpty()) {
            queryBuilder.append(" AND (name LIKE ? OR location LIKE ? OR description LIKE ?)");
            String searchPattern = "%" + searchKeyword + "%";
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
        }

        List<Farm> filteredFarms = new ArrayList<>();
        try (PreparedStatement stm = cnx.prepareStatement(queryBuilder.toString())) {
            // Set parameters
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof String) {
                    stm.setString(i+1, (String) param);
                } else if (param instanceof Integer) {
                    stm.setInt(i+1, (Integer) param);
                }
            }

            ResultSet rs = stm.executeQuery();
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
                farm.setUserId(rs.getInt("user_id"));
                filteredFarms.add(farm);
            }
        }

        return filteredFarms;
    }
}