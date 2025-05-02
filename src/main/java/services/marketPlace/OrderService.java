package services.marketPlace;
import com.devmasters.agrosphere.marketplaceManagement.entities.order;
import com.devmasters.agrosphere.utils.DBConnection;
import services.Iservices;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class OrderService {
    private final Connection conn;
    public OrderService() {
        conn = DBConnection.getInstance().getConnection();
    }


    public int add(order o) throws Exception {
        String sql = "INSERT INTO `order` (buyer_id, total_price, status) VALUES (?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, o.getBuyerId());
        ps.setDouble(2, o.getTotalPrice());
        ps.setString(3, o.getStatus());
        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            return rs.getInt(1); // return the generated ID
        } else {
            throw new Exception("Failed to retrieve inserted order ID.");
        }
    }



    public void update(order o) throws Exception {
        String sql = "UPDATE `order` SET buyer_id=?, total_price=?, status=? WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, o.getBuyerId());
        ps.setDouble(2, o.getTotalPrice());
        ps.setString(3, o.getStatus());
        ps.setInt(4, o.getId());
        ps.executeUpdate();
    }


    public void delete(int id) throws Exception {
        String sql = "DELETE FROM `order` WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }


    public order getOne(int id) throws Exception {
        String sql = "SELECT * FROM `order` WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            order o = new order();
            o.setId(rs.getInt("id"));
            o.setBuyerId(rs.getInt("buyer_id"));
            o.setTotalPrice(rs.getDouble("total_price"));
            o.setStatus(rs.getString("status"));
            return o;
        }
        return null;
    }


    public List<order> getAll() throws Exception {
        List<order> list = new ArrayList<>();
        String sql = "SELECT * FROM `order`";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        while (rs.next()) {
            order o = new order();
            o.setId(rs.getInt("id"));
            o.setBuyerId(rs.getInt("buyer_id"));
            o.setTotalPrice(rs.getDouble("total_price"));
            o.setStatus(rs.getString("status"));
            list.add(o);
        }
        return list;
    }
}
