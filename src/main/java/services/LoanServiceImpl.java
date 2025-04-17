package services;

import entities.Loan;
import entities.Place;
import utils.MyDatabase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LoanServiceImpl implements LoanService {
    private Connection cnx;

    public LoanServiceImpl() {
        cnx = MyDatabase.getInstance().getCnx();
    }

    @Override
    public void ajouter(Loan loan) throws SQLException {
        String sql = "INSERT INTO loans (ticket_price, tickets_left, formation, image, place_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDouble(1, loan.getTicketPrice());
            ps.setInt(2, loan.getTicketsLeft());
            ps.setString(3, loan.getFormation());
            ps.setString(4, loan.getImage());
            ps.setInt(5, loan.getPlace().getId());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    loan.setId(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public void modifier(Loan loan) throws SQLException {
        String sql = "UPDATE loans SET ticket_price = ?, tickets_left = ?, formation = ?, image = ?, place_id = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDouble(1, loan.getTicketPrice());
            ps.setInt(2, loan.getTicketsLeft());
            ps.setString(3, loan.getFormation());
            ps.setString(4, loan.getImage());
            ps.setInt(5, loan.getPlace().getId());
            ps.setInt(6, loan.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(Loan loan) throws SQLException {
        String sql = "DELETE FROM loans WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, loan.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public List<Loan> recuperer() throws SQLException {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT l.*, p.name as place_name FROM loans l JOIN place p ON l.place_id = p.id";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Loan loan = new Loan();
                loan.setId(rs.getInt("id"));
                loan.setTicketPrice(rs.getDouble("ticket_price"));
                loan.setTicketsLeft(rs.getInt("tickets_left"));
                loan.setFormation(rs.getString("formation"));
                loan.setImage(rs.getString("image"));

                Place place = new Place();
                place.setId(rs.getInt("place_id"));
                place.setName(rs.getString("place_name"));

                loan.setPlace(place);
                loans.add(loan);
            }
        }
        return loans;
    }

    @Override
    public List<Loan> getAllLoans() throws SQLException {
        return recuperer(); // Directly return the list from recuperer method
    }

    // Add getLoanById method to fetch a single loan by its ID
    public Loan getLoanById(int loanId) throws SQLException {
        Loan loan = null;
        String sql = "SELECT l.*, p.name as place_name FROM loans l JOIN place p ON l.place_id = p.id WHERE l.id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, loanId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    loan = new Loan();
                    loan.setId(rs.getInt("id"));
                    loan.setTicketPrice(rs.getDouble("ticket_price"));
                    loan.setTicketsLeft(rs.getInt("tickets_left"));
                    loan.setFormation(rs.getString("formation"));
                    loan.setImage(rs.getString("image"));

                    Place place = new Place();
                    place.setId(rs.getInt("place_id"));
                    place.setName(rs.getString("place_name"));

                    loan.setPlace(place);
                }
            }
        }
        return loan;
    }
}
