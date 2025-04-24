package services.marketPlace;

import com.devmasters.agrosphere.marketplaceManagment.entities.category;
import services.Iservices;
import com.devmasters.agrosphere.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class CategoryService {
    private Connection conn;

    public CategoryService() {

        conn = DBConnection.getInstance().getConnection();

    }

    public void add(category c) throws Exception{
        String sql = "INSERT INTO category (name_category, description_category) VALUES (?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, c.getNameCategory());
        ps.setString(2, c.getDescriptionCategory());
        ps.executeUpdate();
    }

    public void update(category c) throws Exception {
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

    public List<category> getAll()throws Exception {
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

    public category getOne(int id) throws Exception {
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
}
