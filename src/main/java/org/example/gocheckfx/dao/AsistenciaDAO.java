package org.example.gocheckfx.dao;

import org.example.gocheckfx.config.DatabaseConfig;
import org.example.gocheckfx.models.Asistencia;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase DAO para acceder a los datos de asistencias en la base de datos.
 */
public class AsistenciaDAO {

    /**
     * Busca una asistencia para un empleado en una fecha específica
     * @param idEmpleado El ID del empleado
     * @param fecha La fecha de la asistencia
     * @return Asistencia encontrada o null si no existe
     */
    public Asistencia buscarAsistencia(int idEmpleado, LocalDate fecha) {
        String sql = "SELECT a.*, e.nombre, e.apellido, e.codigo_unico " +
                "FROM asistencias a " +
                "JOIN empleados e ON a.id_empleado = e.id_empleado " +
                "WHERE a.id_empleado = ? AND a.fecha = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEmpleado);
            stmt.setDate(2, Date.valueOf(fecha));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extraerAsistenciaDeResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar asistencia: " + e.getMessage());
        }

        return null;
    }

    /**
     * Crea una nueva asistencia para un empleado
     * @param asistencia La asistencia a crear
     * @return true si se creó correctamente, false en caso contrario
     */
    public boolean crearAsistencia(Asistencia asistencia) {
        String sql = "INSERT INTO asistencias (id_empleado, fecha, hora_entrada, estado) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, asistencia.getIdEmpleado());
            stmt.setDate(2, Date.valueOf(asistencia.getFecha()));

            if (asistencia.getHoraEntrada() != null) {
                stmt.setTimestamp(3, Timestamp.valueOf(asistencia.getHoraEntrada()));
            } else {
                stmt.setNull(3, Types.TIMESTAMP);
            }

            stmt.setString(4, asistencia.getEstado());

            int filasAfectadas = stmt.executeUpdate();

            if (filasAfectadas > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    asistencia.setIdAsistencia(rs.getInt(1));
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error al crear asistencia: " + e.getMessage());
        }

        return false;
    }

    /**
     * Registra una acción de entrada, salida o descanso
     * @param idAsistencia El ID de la asistencia
     * @param tipoAccion El tipo de acción (ENTRADA, SALIDA, INICIO_DESCANSO1, etc.)
     * @param horaAccion La hora de la acción
     * @return true si se registró correctamente, false en caso contrario
     */
    public boolean registrarAccion(int idAsistencia, String tipoAccion, LocalDateTime horaAccion) {
        // Determinar qué campo actualizar según el tipo de acción
        String campoActualizar;

        switch (tipoAccion) {
            case "ENTRADA":
                campoActualizar = "hora_entrada";
                break;
            case "SALIDA":
                campoActualizar = "hora_salida";
                break;
            case "INICIO_DESCANSO1":
                campoActualizar = "inicio_descanso_1";
                break;
            case "FIN_DESCANSO1":
                campoActualizar = "fin_descanso_1";
                break;
            case "INICIO_DESCANSO2":
                campoActualizar = "inicio_descanso_2";
                break;
            case "FIN_DESCANSO2":
                campoActualizar = "fin_descanso_2";
                break;
            default:
                return false;
        }

        String sql = "UPDATE asistencias SET " + campoActualizar + " = ?, estado = 'PRESENTE' " +
                "WHERE id_asistencia = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(horaAccion));
            stmt.setInt(2, idAsistencia);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al registrar acción: " + e.getMessage());
        }

        return false;
    }

    /**
     * Obtiene las asistencias de un día específico
     * @param fecha La fecha a consultar
     * @return Lista de asistencias
     */
    public List<Asistencia> obtenerAsistenciasPorFecha(LocalDate fecha) {
        List<Asistencia> asistencias = new ArrayList<>();

        String sql = "SELECT a.*, e.nombre, e.apellido, e.codigo_unico " +
                "FROM asistencias a " +
                "JOIN empleados e ON a.id_empleado = e.id_empleado " +
                "WHERE a.fecha = ? " +
                "ORDER BY e.apellido, e.nombre";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(fecha));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                asistencias.add(extraerAsistenciaDeResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener asistencias por fecha: " + e.getMessage());
        }

        return asistencias;
    }

    /**
     * Obtiene las asistencias de un empleado en un rango de fechas
     * @param idEmpleado El ID del empleado
     * @param fechaInicio La fecha de inicio
     * @param fechaFin La fecha de fin
     * @return Lista de asistencias
     */
    public List<Asistencia> obtenerAsistenciasEmpleado(int idEmpleado, LocalDate fechaInicio, LocalDate fechaFin) {
        List<Asistencia> asistencias = new ArrayList<>();

        String sql = "SELECT a.*, e.nombre, e.apellido, e.codigo_unico " +
                "FROM asistencias a " +
                "JOIN empleados e ON a.id_empleado = e.id_empleado " +
                "WHERE a.id_empleado = ? AND a.fecha BETWEEN ? AND ? " +
                "ORDER BY a.fecha";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEmpleado);
            stmt.setDate(2, Date.valueOf(fechaInicio));
            stmt.setDate(3, Date.valueOf(fechaFin));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                asistencias.add(extraerAsistenciaDeResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener asistencias del empleado: " + e.getMessage());
        }

        return asistencias;
    }

    /**
     * Justifica una falta o retardo
     * @param idAsistencia El ID de la asistencia
     * @param motivo El motivo de la justificación
     * @param idAdmin El ID del administrador que justifica
     * @return true si se justificó correctamente, false en caso contrario
     */
    public boolean justificarAsistencia(int idAsistencia, String motivo, int idAdmin) {
        // Primero actualizamos el estado de la asistencia
        String sqlAsistencia = "UPDATE asistencias SET estado = 'JUSTIFICADO' WHERE id_asistencia = ?";

        // Luego registramos la justificación
        String sqlJustificacion = "INSERT INTO justificaciones (id_asistencia, id_admin, motivo) " +
                "VALUES (?, ?, ?)";

        Connection conn = null;

        try {
            conn = DatabaseConfig.getInstance().getConnection();
            conn.setAutoCommit(false); // Iniciar transacción

            // Actualizar asistencia
            try (PreparedStatement stmt = conn.prepareStatement(sqlAsistencia)) {
                stmt.setInt(1, idAsistencia);
                stmt.executeUpdate();
            }

            // Insertar justificación
            try (PreparedStatement stmt = conn.prepareStatement(sqlJustificacion)) {
                stmt.setInt(1, idAsistencia);
                stmt.setInt(2, idAdmin);
                stmt.setString(3, motivo);
                stmt.executeUpdate();
            }

            conn.commit(); // Confirmar transacción
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Revertir transacción en caso de error
                } catch (SQLException ex) {
                    System.err.println("Error al hacer rollback: " + ex.getMessage());
                }
            }
            System.err.println("Error al justificar asistencia: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Restaurar autocommit
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error al cerrar conexión: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Extrae los datos de una asistencia desde un ResultSet
     */
    private Asistencia extraerAsistenciaDeResultSet(ResultSet rs) throws SQLException {
        Asistencia asistencia = new Asistencia();

        asistencia.setIdAsistencia(rs.getInt("id_asistencia"));
        asistencia.setIdEmpleado(rs.getInt("id_empleado"));
        asistencia.setFecha(rs.getDate("fecha").toLocalDate());

        Timestamp horaEntrada = rs.getTimestamp("hora_entrada");
        if (horaEntrada != null) {
            asistencia.setHoraEntrada(horaEntrada.toLocalDateTime());
        }

        Timestamp horaSalida = rs.getTimestamp("hora_salida");
        if (horaSalida != null) {
            asistencia.setHoraSalida(horaSalida.toLocalDateTime());
        }

        Timestamp inicioDescanso1 = rs.getTimestamp("inicio_descanso_1");
        if (inicioDescanso1 != null) {
            asistencia.setInicioDescanso1(inicioDescanso1.toLocalDateTime());
        }

        Timestamp finDescanso1 = rs.getTimestamp("fin_descanso_1");
        if (finDescanso1 != null) {
            asistencia.setFinDescanso1(finDescanso1.toLocalDateTime());
        }

        Timestamp inicioDescanso2 = rs.getTimestamp("inicio_descanso_2");
        if (inicioDescanso2 != null) {
            asistencia.setInicioDescanso2(inicioDescanso2.toLocalDateTime());
        }

        Timestamp finDescanso2 = rs.getTimestamp("fin_descanso_2");
        if (finDescanso2 != null) {
            asistencia.setFinDescanso2(finDescanso2.toLocalDateTime());
        }

        asistencia.setEstado(rs.getString("estado"));
        asistencia.setNotas(rs.getString("notas"));

        // Campos calculados/relacionados
        asistencia.setNombreEmpleado(rs.getString("nombre") + " " + rs.getString("apellido"));
        asistencia.setCodigoEmpleado(rs.getString("codigo_unico"));

        return asistencia;
    }
}
