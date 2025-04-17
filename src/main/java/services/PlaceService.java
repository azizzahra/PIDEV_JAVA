package services;

import entities.Place;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlaceService implements Service<Place> {

    private Connection cnx;

    public PlaceService() {
        cnx = MyDatabase.getInstance().getCnx();
    }

    @Override
    public void ajouter(Place place) throws SQLException {
        String sql = "INSERT INTO place (name, price, capacity, image) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, place.getName());
        ps.setDouble(2, place.getPrice());
        ps.setInt(3, place.getCapacity());
        ps.setString(4, place.getImage());
        ps.executeUpdate();
    }

    @Override
    public void modifier(Place place) throws SQLException {
        String sql = "UPDATE place SET name = ?, price = ?, capacity = ?, image = ? WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, place.getName());
        ps.setDouble(2, place.getPrice());
        ps.setInt(3, place.getCapacity());
        ps.setString(4, place.getImage());
        ps.setInt(5, place.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(Place place) throws SQLException {
        String sql = "DELETE FROM place WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, place.getId());
        ps.executeUpdate();
    }

    @Override
    public List<Place> recuperer() throws SQLException {
        List<Place> places = new ArrayList<>();
        String sql = "SELECT * FROM place";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            double price = rs.getDouble("price");
            int capacity = rs.getInt("capacity");
            String image = rs.getString("image");
            Place place = new Place(id, name, price, capacity, image);
            places.add(place);
        }

        return places;
    }

    public Place getAllPlaces() {
        return null;
    }
}
