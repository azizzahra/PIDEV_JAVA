package services;

import model.Loan;

import java.sql.SQLException;
import java.util.List;

public interface LoanService extends Service<Loan> {
    List<Loan> getAllLoans() throws SQLException;
    // Inherits all methods from Service<T> interface
    // Add any loan-specific methods here if needed
}