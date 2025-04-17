package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDatabase {
    private static MyDatabase instance;
    private Connection cnx;

    // Remplacez ces valeurs par vos propres paramètres de connexion
    private final String URL = "jdbc:mysql://localhost:3306/pi_java";
    private final String USERNAME = "root";
    private final String PASSWORD = "";

    // Constructeur privé pour empêcher l'instanciation directe
    private MyDatabase() {
        try {
            cnx = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Connexion établie avec succès !");
        } catch (SQLException e) {
            System.err.println("Erreur de connexion : " + e.getMessage());
        }
    }

    // Méthode pour obtenir une seule instance de la classe
    public static MyDatabase getInstance() {
        if (instance == null) {
            instance = new MyDatabase();
        }
        return instance;
    }

    public Connection getCnx() {
        return cnx;
    }
}