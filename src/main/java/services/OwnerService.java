package services;

import entities.Loan;
import entities.Owner;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OwnerService {
    private Connection cnx;

    public OwnerService() {
        cnx = MyDatabase.getInstance().getCnx(); // Ensure the database connection is established
    }

    // Fetch all owners from the database and return as ObservableList
    public ObservableList<Owner> getAllOwners() {
        List<Owner> ownerList = new ArrayList<>();
        String sql = "SELECT * FROM Owner";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Owner owner = new Owner();
                owner.setId(rs.getInt("id"));
                owner.setName(rs.getString("name"));
                owner.setEmail(rs.getString("email"));
                owner.setNumber(rs.getString("number"));

                // Fetching Loan based on loan_id in the database
                int loanId = rs.getInt("loan_id");
                Loan loan = new LoanServiceImpl().getLoanById(loanId); // Ensure this method works well
                owner.setLoan(loan);

                ownerList.add(owner);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Convert List<Owner> to ObservableList for JavaFX usage
        return FXCollections.observableArrayList(ownerList);
    }

    // Add a new owner to the database
    public void addOwner(Owner owner) {
        String sql = "INSERT INTO Owner (name, email, number, loan_id) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, owner.getName());
            ps.setString(2, owner.getEmail());
            ps.setString(3, owner.getNumber());
            ps.setInt(4, owner.getLoan().getId()); // Assuming owner has a Loan object

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Update an existing owner in the database
    public void updateOwner(Owner owner) {
        String sql = "UPDATE Owner SET name = ?, email = ?, number = ?, loan_id = ? WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, owner.getName());
            ps.setString(2, owner.getEmail());
            ps.setString(3, owner.getNumber());
            ps.setInt(4, owner.getLoan().getId());
            ps.setInt(5, owner.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Delete an owner from the database
    public void deleteOwner(Owner owner) {
        String sql = "DELETE FROM Owner WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, owner.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
