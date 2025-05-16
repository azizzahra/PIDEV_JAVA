package services.marketPlace;

import model.category;
import Main.DatabaseConnection;
import services.Iserviceszeineb;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class CategoryService implements Iserviceszeineb<category> {
    private Connection conn;

    public CategoryService() {

        conn = DatabaseConnection.getInstance().getInstance().getCnx();

    }

    public void add(category c) throws SQLException{
        String sql = "INSERT INTO category (name_category, description_category) VALUES (?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, c.getNameCategory());
        ps.setString(2, c.getDescriptionCategory());
        ps.executeUpdate();
    }

    public void update(category c) throws SQLException {
        String sql = "UPDATE category SET name_category = ?, description_category = ? WHERE id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, c.getNameCategory());
        ps.setString(2, c.getDescriptionCategory());
        ps.setInt(3, c.getId());
        ps.executeUpdate();
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM category WHERE id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    public List<category> getAll()throws SQLException {
        List<category> list = new ArrayList<>();
        String sql = "SELECT * FROM category";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            category c = new category();
            c.setId(rs.getInt("id"));
            c.setNameCategory(rs.getString("name_category"));
            c.setDescriptionCategory(rs.getString("description_category"));
            list.add(c);
        }
        return list;
    }

    public category getOne(int id) throws SQLException {
        String sql = "SELECT * FROM category WHERE id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            category c = new category();
            c.setId(rs.getInt("id"));
            c.setNameCategory(rs.getString("name_category"));
            c.setDescriptionCategory(rs.getString("description_category"));
            return c;
        }

        return null; // or throw exception if preferred
    }

    public List<category> searchByName(String keyword) throws SQLException {
        List<category> list = new ArrayList<>();
        String sql = "SELECT * FROM category WHERE name_category LIKE ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, "%" + keyword + "%");
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            category c = new category();
            c.setId(rs.getInt("id"));
            c.setNameCategory(rs.getString("name_category"));
            c.setDescriptionCategory(rs.getString("description_category"));
            list.add(c);
        }

        return list;
    }

    public List<category> getAllSortedAsc() throws SQLException {
        List<category> list = new ArrayList<>();
        String sql = "SELECT * FROM category ORDER BY name_category ASC";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            category c = new category();
            c.setId(rs.getInt("id"));
            c.setNameCategory(rs.getString("name_category"));
            c.setDescriptionCategory(rs.getString("description_category"));
            list.add(c);
        }

        return list;
    }

    public List<category> getAllSortedDesc() throws SQLException {
        List<category> list = new ArrayList<>();
        String sql = "SELECT * FROM category ORDER BY name_category DESC";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            category c = new category();
            c.setId(rs.getInt("id"));
            c.setNameCategory(rs.getString("name_category"));
            c.setDescriptionCategory(rs.getString("description_category"));
            list.add(c);
        }

        return list;
    }

}