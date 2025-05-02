package services.marketPlace;
import com.devmasters.agrosphere.marketplaceManagement.entities.product;
import com.devmasters.agrosphere.userManagement.entities.user;
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

    public String getProductName(int productId) {
        String name = "Unknown Product";
        try {
            // Fixed: Use the correct table name "product" and column "name_prod"
            String sql = "SELECT name_prod FROM product WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                name = rs.getString("name_prod");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching product name: " + e.getMessage());
            // Return default name in case of error
        }

        return name;
    }
    public List<product> searchByName(String keyword) throws Exception {
        List<product> list = new ArrayList<>();
        String sql = "SELECT * FROM product WHERE name_prod LIKE ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, "%" + keyword + "%");
        ResultSet rs = ps.executeQuery();

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
    public List<product> searchWithCategoryFilter(String keyword, int categoryId) throws Exception {
        List<product> list = new ArrayList<>();

        String sql = "SELECT p.* FROM product p JOIN category c ON p.category_prod_id = c.id WHERE " +
                "(p.name_prod LIKE ? OR p.description_prod LIKE ? OR c.name_category LIKE ?)";
        if (categoryId != -1) {
            sql += " AND c.id = ?";
        }

        PreparedStatement ps = conn.prepareStatement(sql);
        String like = "%" + keyword + "%";
        ps.setString(1, like);
        ps.setString(2, like);
        ps.setString(3, like);
        if (categoryId != -1) ps.setInt(4, categoryId);

        ResultSet rs = ps.executeQuery();
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

    public List<product> getByCategory(int categoryId) throws Exception {
        List<product> list = new ArrayList<>();
        String sql = "SELECT * FROM product WHERE category_prod_id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, categoryId);
        ResultSet rs = ps.executeQuery();

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



}