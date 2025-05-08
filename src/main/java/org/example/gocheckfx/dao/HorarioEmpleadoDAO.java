package org.example.gocheckfx.dao;

import org.example.gocheckfx.config.DatabaseConfig;
import org.example.gocheckfx.models.HorarioEmpleado;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase DAO para acceder a los datos de horarios de empleados
 */
public class HorarioEmpleadoDAO {

    /**
     * Obtiene el horario de un empleado específico
     * @param idEmpleado ID del empleado
     * @return HorarioEmpleado o null si no existe
     */
    public HorarioEmpleado obtenerHorarioPorEmpleado(int idEmpleado) {
        String sql = "SELECT h.*, e.nombre, e.apellido " +
                "FROM horarios_empleados h " +
                "JOIN empleados e ON h.id_empleado = e.id_empleado " +
                "WHERE h.id_empleado = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEmpleado);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extraerHorarioDeResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener horario: " + e.getMessage());
        }

        return null;
    }

    /**
     * Lista todos los horarios de empleados
     * @return Lista de horarios
     */
    public List<HorarioEmpleado> listarHorarios() {
        List<HorarioEmpleado> horarios = new ArrayList<>();
        String sql = "SELECT h.*, e.nombre, e.apellido " +
                "FROM horarios_empleados h " +
                "JOIN empleados e ON h.id_empleado = e.id_empleado " +
                "ORDER BY e.apellido, e.nombre";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                horarios.add(extraerHorarioDeResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar horarios: " + e.getMessage());
        }

        return horarios;
    }

    /**
     * Guarda un nuevo horario para un empleado
     * @param horario Horario a guardar
     * @return true si se guardó con éxito, false en caso contrario
     */
    public boolean guardar(HorarioEmpleado horario) {
        // Primero verificar si ya existe un horario para este empleado
        String checkSql = "SELECT id_horario FROM horarios_empleados WHERE id_empleado = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setInt(1, horario.getIdEmpleado());
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // Ya existe, actualizar
                int idHorario = rs.getInt("id_horario");
                return actualizar(idHorario, horario);
            } else {
                // No existe, insertar nuevo
                return insertar(horario);
            }

        } catch (SQLException e) {
            System.err.println("Error al guardar horario: " + e.getMessage());
            return false;
        }
    }

    /**
     * Inserta un nuevo horario
     */
    private boolean insertar(HorarioEmpleado horario) {
        String sql = "INSERT INTO horarios_empleados (id_empleado, dias_laborables, dia_descanso_semanal) " +
                "VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, horario.getIdEmpleado());
            stmt.setString(2, horario.getDiasLaborablesJSON());
            stmt.setString(3, horario.getDiaDescansoSemanal());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    horario.setIdHorario(rs.getInt(1));
                }
                return true;
            }

            return false;

        } catch (SQLException e) {
            System.err.println("Error al insertar horario: " + e.getMessage());
            return false;
        }
    }

    /**
     * Actualiza un horario existente
     */
    private boolean actualizar(int idHorario, HorarioEmpleado horario) {
        String sql = "UPDATE horarios_empleados SET dias_laborables = ?, dia_descanso_semanal = ? " +
                "WHERE id_horario = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, horario.getDiasLaborablesJSON());
            stmt.setString(2, horario.getDiaDescansoSemanal());
            stmt.setInt(3, idHorario);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar horario: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina el horario de un empleado
     * @param idEmpleado ID del empleado
     * @return true si se eliminó con éxito, false en caso contrario
     */
    public boolean eliminar(int idEmpleado) {
        String sql = "DELETE FROM horarios_empleados WHERE id_empleado = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEmpleado);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar horario: " + e.getMessage());
            return false;
        }
    }

    /**
     * Extrae un horario del ResultSet
     */
    private HorarioEmpleado extraerHorarioDeResultSet(ResultSet rs) throws SQLException {
        HorarioEmpleado horario = new HorarioEmpleado();

        horario.setIdHorario(rs.getInt("id_horario"));
        horario.setIdEmpleado(rs.getInt("id_empleado"));
        horario.setDiasLaborablesFromJSON(rs.getString("dias_laborables"));
        horario.setDiaDescansoSemanal(rs.getString("dia_descanso_semanal"));

        // Datos del empleado
        horario.setNombreEmpleado(rs.getString("nombre") + " " + rs.getString("apellido"));

        return horario;
    }
}
