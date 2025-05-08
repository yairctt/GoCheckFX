package org.example.gocheckfx.dao;

import org.example.gocheckfx.config.DatabaseConfig;
import org.example.gocheckfx.models.CambioDescanso;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase DAO para acceder a los datos de cambios de descanso
 */
public class CambioDescansoDAO {

    /**
     * Registra un nuevo cambio de día de descanso
     * @param cambio El cambio a registrar
     * @return true si se registró con éxito, false en caso contrario
     */
    public boolean registrarCambio(CambioDescanso cambio) {
        String sql = "INSERT INTO cambios_descanso (id_empleado, fecha_original, fecha_nueva, " +
                "motivo, fecha_registro, id_admin) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, cambio.getIdEmpleado());
            stmt.setDate(2, Date.valueOf(cambio.getFechaOriginal()));
            stmt.setDate(3, Date.valueOf(cambio.getFechaNueva()));
            stmt.setString(4, cambio.getMotivo());
            stmt.setTimestamp(5, Timestamp.valueOf(cambio.getFechaRegistro()));
            stmt.setInt(6, cambio.getIdAdmin());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    cambio.setIdCambio(rs.getInt(1));
                }
                return true;
            }

            return false;

        } catch (SQLException e) {
            System.err.println("Error al registrar cambio de descanso: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene los cambios de descanso de un empleado específico
     * @param idEmpleado ID del empleado
     * @return Lista de cambios de descanso
     */
    public List<CambioDescanso> obtenerCambiosEmpleado(int idEmpleado) {
        List<CambioDescanso> cambios = new ArrayList<>();
        String sql = "SELECT c.*, " +
                "CONCAT(e.nombre, ' ', e.apellido) as nombre_empleado, " +
                "CONCAT(a.nombre, ' ', a.apellido) as nombre_admin " +
                "FROM cambios_descanso c " +
                "JOIN empleados e ON c.id_empleado = e.id_empleado " +
                "JOIN empleados a ON c.id_admin = a.id_empleado " +
                "WHERE c.id_empleado = ? " +
                "ORDER BY c.fecha_registro DESC";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEmpleado);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                cambios.add(extraerCambioDeResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener cambios de descanso: " + e.getMessage());
        }

        return cambios;
    }

    /**
     * Obtiene todos los cambios de descanso registrados
     * @return Lista de cambios de descanso
     */
    public List<CambioDescanso> obtenerTodosCambios() {
        List<CambioDescanso> cambios = new ArrayList<>();
        String sql = "SELECT c.*, " +
                "CONCAT(e.nombre, ' ', e.apellido) as nombre_empleado, " +
                "CONCAT(a.nombre, ' ', a.apellido) as nombre_admin " +
                "FROM cambios_descanso c " +
                "JOIN empleados e ON c.id_empleado = e.id_empleado " +
                "JOIN empleados a ON c.id_admin = a.id_empleado " +
                "ORDER BY c.fecha_registro DESC";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                cambios.add(extraerCambioDeResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener cambios de descanso: " + e.getMessage());
        }

        return cambios;
    }

    /**
     * Verifica si ya existe un cambio de descanso para un empleado en una fecha específica
     * @param idEmpleado ID del empleado
     * @param fechaOriginal Fecha original del descanso
     * @return true si ya existe un cambio, false en caso contrario
     */
    public boolean existeCambioEnFecha(int idEmpleado, LocalDate fechaOriginal) {
        String sql = "SELECT COUNT(*) FROM cambios_descanso " +
                "WHERE id_empleado = ? AND fecha_original = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEmpleado);
            stmt.setDate(2, Date.valueOf(fechaOriginal));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error al verificar existencia de cambio: " + e.getMessage());
        }

        return false;
    }

    /**
     * Extrae un cambio de descanso del ResultSet
     */
    private CambioDescanso extraerCambioDeResultSet(ResultSet rs) throws SQLException {
        CambioDescanso cambio = new CambioDescanso();

        cambio.setIdCambio(rs.getInt("id_cambio"));
        cambio.setIdEmpleado(rs.getInt("id_empleado"));
        cambio.setFechaOriginal(rs.getDate("fecha_original").toLocalDate());
        cambio.setFechaNueva(rs.getDate("fecha_nueva").toLocalDate());
        cambio.setMotivo(rs.getString("motivo"));
        cambio.setFechaRegistro(rs.getTimestamp("fecha_registro").toLocalDateTime());
        cambio.setIdAdmin(rs.getInt("id_admin"));

        // Nombres
        cambio.setNombreEmpleado(rs.getString("nombre_empleado"));
        cambio.setNombreAdmin(rs.getString("nombre_admin"));

        return cambio;
    }
}
