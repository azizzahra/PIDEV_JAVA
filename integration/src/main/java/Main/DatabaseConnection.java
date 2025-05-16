package Main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private static Connection cnx;

    public DatabaseConnection() {
        try {
            // Chargement explicite du pilote
            Class.forName("com.mysql.cj.jdbc.Driver");

            String Url = "jdbc:mysql://localhost:3306/integration5"; // Ajout du port
            String Username = "root";
            String Password = "";

            cnx = DriverManager.getConnection(Url, Username, Password);
            System.out.println("Connexion établie");
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Échec de la connexion à la base de données", e);
        }
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection(); // Correction du singleton
        }
        return instance;
    }

    public static Connection getCnx() {
        try {
            // Vérifier si la connexion est valide
            if (cnx == null || cnx.isClosed()) {
                // Recréer la connexion si elle est fermée ou null
                instance = new DatabaseConnection();
            }
        } catch (SQLException e) {
            // En cas d'erreur, recréer la connexion
            instance = new DatabaseConnection();
        }
        return cnx;
    }
}