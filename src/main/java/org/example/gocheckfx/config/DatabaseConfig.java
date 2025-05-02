package org.example.gocheckfx.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase que maneja la configuración y conexión a la base de datos MySQL.
 */
public class DatabaseConfig {
    // Configuración para la conexión a la base de datos
    private static final String DB_URL = "jdbc:mysql://localhost:3306/gocheck_bd";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Cacahuate$12"; // Configura tu contraseña aquí

    // Patrón Singleton para la conexión a la BD
    private static DatabaseConfig instance;
    private Connection connection;

    private DatabaseConfig() {
        try {
            // Cargar el driver de MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Error al cargar el driver de MySQL: " + e.getMessage());
        }
    }

    /**
     * Obtener la instancia única de la configuración de base de datos
     */
    public static DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    /**
     * Obtener una conexión a la base de datos
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        }
        return connection;
    }

    /**
     * Cerrar la conexión a la base de datos
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }
}
