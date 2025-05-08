package org.example.gocheckfx.dao;

import org.example.gocheckfx.config.DatabaseConfig;
import org.example.gocheckfx.models.Permiso;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase DAO para acceder a los datos de permisos en la base de datos
 */
public class PermisoDAO {

    /**
     * Obtiene los permisos pendientes
     * @return Lista de permisos pendientes
     */
    public List<Permiso> obtenerPermisosPendientes() {
        return obtenerPermisosPorEstado("PENDIENTE");
    }

    /**
     * Obtiene los permisos por estado
     * @param estado Estado de los permisos (PENDIENTE, APROBADO, RECHAZADO)
     * @return Lista de permisos en ese estado
     */
    public List<Permiso> obtenerPermisosPorEstado(String estado) {
        List<Permiso> permisos = new ArrayList<>();
        String sql = "SELECT p.*, " +
                "CONCAT(e.nombre, ' ', e.apellido) as nombre_empleado, " +
                "CONCAT(a.nombre, ' ', a.apellido) as nombre_admin " +
                "FROM permisos p " +
                "JOIN empleados e ON p.id_empleado = e.id_empleado " +
                "LEFT JOIN empleados a ON p.id_admin = a.id_empleado " +
                "WHERE p.estado = ? " +
                "ORDER BY p.fecha_solicitud DESC";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, estado);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                permisos.add(extraerPermisoDeResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener permisos: " + e.getMessage());
        }

        return permisos;
    }

    /**
     * Obtiene los permisos de un empleado específico
     * @param idEmpleado ID del empleado
     * @return Lista de permisos del empleado
     */
    public List<Permiso> obtenerPermisosEmpleado(int idEmpleado) {
        List<Permiso> permisos = new ArrayList<>();
        String sql = "SELECT p.*, " +
                "CONCAT(e.nombre, ' ', e.apellido) as nombre_empleado, " +
                "CONCAT(a.nombre, ' ', a.apellido) as nombre_admin " +
                "FROM permisos p " +
                "JOIN empleados e ON p.id_empleado = e.id_empleado " +
                "LEFT JOIN empleados a ON p.id_admin = a.id_empleado " +
                "WHERE p.id_empleado = ? " +
                "ORDER BY p.fecha_solicitud DESC";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEmpleado);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                permisos.add(extraerPermisoDeResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener permisos del empleado: " + e.getMessage());
        }

        return permisos;
    }

    /**
     * Registra un nuevo permiso en la base de datos
     * @param permiso Permiso a registrar
     * @return true si se registró con éxito, false en caso contrario
     */
    public boolean registrarPermiso(Permiso permiso) {
        String sql = "INSERT INTO permisos (id_empleado, fecha, motivo, estado, fecha_solicitud) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, permiso.getIdEmpleado());
            stmt.setDate(2, Date.valueOf(permiso.getFecha()));
            stmt.setString(3, permiso.getMotivo());
            stmt.setString(4, permiso.getEstado());
            stmt.setTimestamp(5, Timestamp.valueOf(permiso.getFechaSolicitud()));

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    permiso.setIdPermiso(rs.getInt(1));
                }
                return true;
            }

            return false;

        } catch (SQLException e) {
            System.err.println("Error al registrar permiso: " + e.getMessage());
            return false;
        }
    }

    /**
     * Actualiza el estado de un permiso (aprobar/rechazar)
     * @param idPermiso ID del permiso
     * @param estado Nuevo estado (APROBADO/RECHAZADO)
     * @param idAdmin ID del administrador que aprueba/rechaza
     * @return true si se actualizó con éxito, false en caso contrario
     */
    public boolean actualizarEstadoPermiso(int idPermiso, String estado, int idAdmin) {
        String sql = "UPDATE permisos SET estado = ?, id_admin = ? WHERE id_permiso = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, estado);
            stmt.setInt(2, idAdmin);
            stmt.setInt(3, idPermiso);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar estado de permiso: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verifica si ya existe un permiso para un empleado en una fecha específica
     * @param idEmpleado ID del empleado
     * @param fecha Fecha a verificar
     * @return true si ya existe un permiso, false en caso contrario
     */
    public boolean existePermiso(int idEmpleado, LocalDate fecha) {
        String sql = "SELECT COUNT(*) FROM permisos WHERE id_empleado = ? AND fecha = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEmpleado);
            stmt.setDate(2, Date.valueOf(fecha));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error al verificar existencia de permiso: " + e.getMessage());
        }

        return false;
    }

    /**
     * Extrae un permiso del ResultSet
     */
    private Permiso extraerPermisoDeResultSet(ResultSet rs) throws SQLException {
        Permiso permiso = new Permiso();

        permiso.setIdPermiso(rs.getInt("id_permiso"));
        permiso.setIdEmpleado(rs.getInt("id_empleado"));
        permiso.setFecha(rs.getDate("fecha").toLocalDate());
        permiso.setMotivo(rs.getString("motivo"));
        permiso.setEstado(rs.getString("estado"));
        permiso.setFechaSolicitud(rs.getTimestamp("fecha_solicitud").toLocalDateTime());

        // id_admin puede ser NULL
        int idAdmin = rs.getInt("id_admin");
        if (!rs.wasNull()) {
            permiso.setIdAdmin(idAdmin);
        }

        // Nombres
        permiso.setNombreEmpleado(rs.getString("nombre_empleado"));
        permiso.setNombreAdmin(rs.getString("nombre_admin"));

        return permiso;
    }
}
