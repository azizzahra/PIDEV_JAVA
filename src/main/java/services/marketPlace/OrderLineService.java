package services.marketPlace;

import com.devmasters.agrosphere.marketplaceManagement.entities.orderLine;
import services.Iservices;
import com.devmasters.agrosphere.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;



public class OrderLineService{
    private final Connection conn;

    public OrderLineService() {
        conn = DBConnection.getInstance().getConnection();
    }


    public void add(orderLine ol) throws Exception {
        String sql = "INSERT INTO order_line (ord_id, product_id, order_quantity) VALUES (?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, ol.getOrdId());
        ps.setInt(2, ol.getProductId());
        ps.setInt(3, ol.getOrderQuantity());
        ps.executeUpdate();
    }


    public void update(orderLine ol) throws Exception {
        String sql = "UPDATE order_line SET ord_id=?, product_id=?, order_quantity=? WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, ol.getOrdId());
        ps.setInt(2, ol.getProductId());
        ps.setInt(3, ol.getOrderQuantity());
        ps.setInt(4, ol.getId());
        ps.executeUpdate();
    }


    public void delete(int id) throws Exception {
        String sql = "DELETE FROM order_line WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }


    public orderLine getOne(int id) throws Exception {
        String sql = "SELECT * FROM order_line WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            orderLine ol = new orderLine();
            ol.setId(rs.getInt("id"));
            ol.setOrdId(rs.getInt("ord_id"));
            ol.setProductId(rs.getInt("product_id"));
            ol.setOrderQuantity(rs.getInt("order_quantity"));
            return ol;
        }
        return null;
    }


    public List<orderLine> getAll() throws Exception {
        List<orderLine> list = new ArrayList<>();
        String sql = "SELECT * FROM order_line";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        while (rs.next()) {
            orderLine ol = new orderLine();
            ol.setId(rs.getInt("id"));
            ol.setOrdId(rs.getInt("ord_id"));
            ol.setProductId(rs.getInt("product_id"));
            ol.setOrderQuantity(rs.getInt("order_quantity"));
            list.add(ol);
        }
        return list;
    }

    // ➕ Bonus: Get lines for one order
    public List<orderLine> getByOrderId(int orderId) throws Exception {
        List<orderLine> lines = new ArrayList<>();
        String sql = "SELECT * FROM order_line WHERE ord_id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, orderId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            orderLine ol = new orderLine();
            ol.setId(rs.getInt("id"));
            ol.setOrdId(rs.getInt("ord_id"));
            ol.setProductId(rs.getInt("product_id"));
            ol.setOrderQuantity(rs.getInt("order_quantity"));
            lines.add(ol);
        }
        return lines;
    }
}
