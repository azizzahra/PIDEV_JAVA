package services.marketPlace;

import model.order;
import Main.DatabaseConnection;
import services.Iserviceszeineb;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

public class OrderService extends Observable {
    private final Connection conn;

    public OrderService() {
        conn = DatabaseConnection.getInstance().getCnx();
    }

    public int add(order o) throws SQLException {
        String sql = "INSERT INTO `order` (buyer_id, total_price, status) VALUES (?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, o.getBuyerId());
        ps.setDouble(2, o.getTotalPrice());
        ps.setString(3, o.getStatus());
        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            int newId = rs.getInt(1);

            // Notify observers about the change
            setChanged();
            try {
                notifyObservers(getAll());
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return newId; // return the generated ID
        } else {
            throw new SQLException("Failed to retrieve inserted order ID.");
        }
    }

    public void update(order o) throws SQLException {
        String sql = "UPDATE `order` SET buyer_id=?, total_price=?, status=? WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, o.getBuyerId());
        ps.setDouble(2, o.getTotalPrice());
        ps.setString(3, o.getStatus());
        ps.setInt(4, o.getId());
        int rowsAffected = ps.executeUpdate();

        if (rowsAffected > 0) {
            // Notify observers about the change
            setChanged();
            try {
                notifyObservers(getAll());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM `order` WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        int rowsAffected = ps.executeUpdate();

        if (rowsAffected > 0) {
            // Notify observers about the change
            setChanged();
            try {
                notifyObservers(getAll());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public order getOne(int id) throws SQLException {
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

    public List<order> getAll() throws SQLException {
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

    public boolean updateOrderStatus(int orderId, String newStatus) throws SQLException {
        String sql = "UPDATE `order` SET status = ? WHERE id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, newStatus);
        ps.setInt(2, orderId);

        int rowsAffected = ps.executeUpdate();

        // Notify observers that an order has been updated
        if (rowsAffected > 0) {
            setChanged();
            notifyObservers(getAll());  // Send the updated list of orders
            return true;
        }

        return false;
    }

    public order getOrderById(int id) throws SQLException {
        return getOne(id);
    }

    // Helper method to check if a column exists in the result set
    private boolean hasColumn(ResultSet rs, String columnName) {
        try {
            ResultSetMetaData metaData = rs.getMetaData();
            int columns = metaData.getColumnCount();
            for (int i = 1; i <= columns; i++) {
                if (columnName.equalsIgnoreCase(metaData.getColumnName(i))) {
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            return false;
        }
    }
}