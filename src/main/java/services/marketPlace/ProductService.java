package services.marketPlace;

import com.devmasters.agrosphere.marketplaceManagment.entities.product;
import services.Iservices;
import com.devmasters.agrosphere.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductService implements Iservices<product> {

    private final Connection conn;

    public ProductService() {
        conn = DBConnection.getInstance().getConnection();
    }

    @Override
    public List<product> getAll() throws Exception {
        List<product> list = new ArrayList<>();
        String sql = "SELECT * FROM product";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        while (rs.next()) {
            product p = new product();
            p.setId(rs.getInt("id"));
            p.setProdImg(rs.getString("prod_img"));
            p.setNameProd(rs.getString("name_prod"));
            p.setDescriptionProd(rs.getString("description_prod"));
            p.setPriceProd(rs.getDouble("price_prod"));
            p.setQuantity(rs.getInt("quantity"));
            p.setCategoryProdId(rs.getInt("category_prod_id"));
            p.setFarmerId(rs.getInt("farmer_id_id"));
            list.add(p);
        }
        return list;
    }

    @Override
    public void add(product p) throws Exception {
        String sql = "INSERT INTO product (prod_img, name_prod, description_prod, price_prod, quantity, category_prod_id, farmer_id_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, p.getProdImg());
        ps.setString(2, p.getNameProd());
        ps.setString(3, p.getDescriptionProd());
        ps.setDouble(4, p.getPriceProd());
        ps.setInt(5, p.getQuantity());
        ps.setInt(6, p.getCategoryProdId());
        ps.setInt(7, p.getFarmerId());
        ps.executeUpdate();
    }

    @Override
    public void update(product p) throws Exception {
        String sql = "UPDATE product SET prod_img=?, name_prod=?, description_prod=?, price_prod=?, quantity=?, category_prod_id=?, farmer_id_id=? WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, p.getProdImg());
        ps.setString(2, p.getNameProd());
        ps.setString(3, p.getDescriptionProd());
        ps.setDouble(4, p.getPriceProd());
        ps.setInt(5, p.getQuantity());
        ps.setInt(6, p.getCategoryProdId());
        ps.setInt(7, p.getFarmerId());
        ps.setInt(8, p.getId());
        ps.executeUpdate();
    }

    @Override
    public void delete(int id) throws Exception {
        String sql = "DELETE FROM product WHERE id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }
    /*public void delete(int id) throws SQLException {
        String sql = "DELETE FROM product WHERE id = ?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }*/

    @Override
    public product getOne(int id) throws Exception {
        String sql = "SELECT * FROM product WHERE id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            product p = new product();
            p.setId(rs.getInt("id"));
            p.setProdImg(rs.getString("prod_img"));
            p.setNameProd(rs.getString("name_prod"));
            p.setDescriptionProd(rs.getString("description_prod"));
            p.setPriceProd(rs.getDouble("price_prod"));
            p.setQuantity(rs.getInt("quantity"));
            p.setCategoryProdId(rs.getInt("category_prod_id"));
            p.setFarmerId(rs.getInt("farmer_id_id"));
            return p;
        } else {
            return null;
        }
    }
}
