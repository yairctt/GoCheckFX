package org.example.gocheckfx.dao;

import org.example.gocheckfx.config.DatabaseConfig;
import org.example.gocheckfx.models.Asistencia;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase DAO para acceder a los datos de asistencia
 */
public class AsistenciaDAO {

    /**
     * Registra una nueva asistencia
     * @param asistencia Asistencia a registrar
     * @return true si se registró con éxito, false en caso contrario
     */
    public boolean registrarAsistencia(Asistencia asistencia) {
        String sql = "INSERT INTO asistencias (id_empleado, fecha, hora_entrada, estado) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, asistencia.getIdEmpleado());
            stmt.setDate(2, Date.valueOf(asistencia.getFecha()));
            stmt.setTimestamp(3, Timestamp.valueOf(asistencia.getHoraEntrada()));
            stmt.setString(4, asistencia.getEstado());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    asistencia.setIdAsistencia(rs.getInt(1));
                }
                return true;
            }

            return false;

        } catch (SQLException e) {
            System.err.println("Error al registrar asistencia: " + e.getMessage());
            return false;
        }
    }

    /**
     * Actualiza una asistencia existente
     * @param asistencia Asistencia con los nuevos datos
     * @return true si se actualizó con éxito, false en caso contrario
     */
    public boolean actualizarAsistencia(Asistencia asistencia) {
        String sql = "UPDATE asistencias SET " +
                "hora_entrada = ?, " +
                "hora_salida = ?, " +
                "inicio_descanso_1 = ?, " +
                "fin_descanso_1 = ?, " +
                "inicio_descanso_2 = ?, " +
                "fin_descanso_2 = ?, " +
                "estado = ?, " +
                "notas = ? " +
                "WHERE id_asistencia = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, asistencia.getHoraEntrada() != null ?
                    Timestamp.valueOf(asistencia.getHoraEntrada()) : null);

            stmt.setTimestamp(2, asistencia.getHoraSalida() != null ?
                    Timestamp.valueOf(asistencia.getHoraSalida()) : null);

            stmt.setTimestamp(3, asistencia.getInicioDescanso1() != null ?
                    Timestamp.valueOf(asistencia.getInicioDescanso1()) : null);

            stmt.setTimestamp(4, asistencia.getFinDescanso1() != null ?
                    Timestamp.valueOf(asistencia.getFinDescanso1()) : null);

            stmt.setTimestamp(5, asistencia.getInicioDescanso2() != null ?
                    Timestamp.valueOf(asistencia.getInicioDescanso2()) : null);

            stmt.setTimestamp(6, asistencia.getFinDescanso2() != null ?
                    Timestamp.valueOf(asistencia.getFinDescanso2()) : null);

            stmt.setString(7, asistencia.getEstado());
            stmt.setString(8, asistencia.getNotas());
            stmt.setInt(9, asistencia.getIdAsistencia());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar asistencia: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene la asistencia de un empleado en una fecha específica
     * @param idEmpleado ID del empleado
     * @param fecha Fecha a consultar
     * @return Objeto Asistencia o null si no existe
     */
    public Asistencia obtenerAsistenciaPorEmpleadoFecha(int idEmpleado, LocalDate fecha) {
        String sql = "SELECT * FROM asistencias WHERE id_empleado = ? AND fecha = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEmpleado);
            stmt.setDate(2, Date.valueOf(fecha));

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extraerAsistenciaDeResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener asistencia: " + e.getMessage());
        }

        return null;
    }

    /**
     * Obtiene las asistencias de un empleado en un rango de fechas
     * @param idEmpleado ID del empleado
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @return Lista de asistencias
     */
    public List<Asistencia> obtenerAsistenciasPorEmpleadoRango(int idEmpleado, LocalDate fechaInicio, LocalDate fechaFin) {
        List<Asistencia> asistencias = new ArrayList<>();
        String sql = "SELECT a.*, " +
                "CONCAT(e.nombre, ' ', e.apellido) as nombre_empleado " +
                "FROM asistencias a " +
                "JOIN empleados e ON a.id_empleado = e.id_empleado " +
                "WHERE a.id_empleado = ? AND a.fecha BETWEEN ? AND ? " +
                "ORDER BY a.fecha DESC";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEmpleado);
            stmt.setDate(2, Date.valueOf(fechaInicio));
            stmt.setDate(3, Date.valueOf(fechaFin));

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Asistencia asistencia = extraerAsistenciaDeResultSet(rs);
                asistencia.setNombreEmpleado(rs.getString("nombre_empleado"));
                asistencias.add(asistencia);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener asistencias por rango: " + e.getMessage());
        }

        return asistencias;
    }

    /**
     * Justifica una falta o retardo
     * @param idAsistencia ID de la asistencia
     * @param motivo Motivo de la justificación
     * @param idAdmin ID del empleado administrador que justifica
     * @return true si se justificó con éxito, false en caso contrario
     */
    public boolean justificarAsistencia(int idAsistencia, String motivo, int idAdmin) {
        Connection conn = null;

        try {
            conn = DatabaseConfig.getInstance().getConnection();
            conn.setAutoCommit(false);

            // Verificar si existe la asistencia
            String sqlVerificar = "SELECT COUNT(*) FROM asistencias WHERE id_asistencia = ?";
            boolean existeAsistencia = false;

            try (PreparedStatement stmt = conn.prepareStatement(sqlVerificar)) {
                stmt.setInt(1, idAsistencia);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    existeAsistencia = rs.getInt(1) > 0;
                }
            }

            if (!existeAsistencia) {
                // La asistencia no existe, no podemos justificarla
                System.err.println("Error: No existe la asistencia con ID " + idAsistencia);
                return false;
            }

            // Actualizar el estado de la asistencia a JUSTIFICADO
            String sqlUpdateAsistencia = "UPDATE asistencias SET estado = 'JUSTIFICADO' WHERE id_asistencia = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sqlUpdateAsistencia)) {
                stmt.setInt(1, idAsistencia);
                stmt.executeUpdate();
            }

            // Insertar justificación
            String sqlInsertJustificacion =
                    "INSERT INTO justificaciones (id_asistencia, id_admin, motivo, fecha_justificacion) " +
                            "VALUES (?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(sqlInsertJustificacion)) {
                stmt.setInt(1, idAsistencia);
                stmt.setInt(2, idAdmin);
                stmt.setString(3, motivo);
                stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                stmt.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error en rollback: " + ex.getMessage());
                }
            }
            System.err.println("Error al justificar asistencia: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error al cerrar conexión: " + e.getMessage());
                }
            }
        }
    }

    private boolean crearRegistroAsistencia(Asistencia asistencia) {
        // SQL para insertar
        String sql = "INSERT INTO asistencias (id_empleado, fecha, estado) VALUES (?, ?, 'FALTA')";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, asistencia.getIdEmpleado());
            stmt.setDate(2, Date.valueOf(asistencia.getFecha() != null ? asistencia.getFecha() : LocalDate.now()));

            int result = stmt.executeUpdate();

            if (result > 0) {
                // Obtener el ID generado
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    asistencia.setIdAsistencia(rs.getInt(1));
                    return true;
                }
            }

            return false;
        } catch (SQLException e) {
            System.err.println("Error al crear registro de asistencia: " + e.getMessage());
            return false;
        }
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

    private Asistencia extraerAsistenciaDeResultSet(ResultSet rs) throws SQLException {
        Asistencia asistencia = new Asistencia();

        // El id puede ser NULL en un LEFT JOIN
        Object idObj = rs.getObject("id_asistencia");
        if (idObj != null) {
            asistencia.setIdAsistencia(rs.getInt("id_asistencia"));
        }

        asistencia.setIdEmpleado(rs.getInt("id_empleado"));

        // La fecha puede ser NULL en un LEFT JOIN
        Date fechaSQL = rs.getDate("fecha");
        if (fechaSQL != null) {
            asistencia.setFecha(fechaSQL.toLocalDate());
        }

        // Procesar campos de tiempo que pueden ser NULL
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

        String estado = rs.getString("estado");
        asistencia.setEstado(estado != null ? estado : "FALTA");

        asistencia.setNotas(rs.getString("notas"));

        return asistencia;
    }

    /**
     * Verifica si hay una asistencia completa (con hora de entrada y salida) para una fecha
     * @param idEmpleado ID del empleado
     * @param fecha Fecha a verificar
     * @return true si hay asistencia completa, false en caso contrario
     */
    public boolean tieneAsistenciaCompleta(int idEmpleado, LocalDate fecha) {
        String sql = "SELECT COUNT(*) FROM asistencias " +
                "WHERE id_empleado = ? AND fecha = ? AND hora_entrada IS NOT NULL AND hora_salida IS NOT NULL";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEmpleado);
            stmt.setDate(2, Date.valueOf(fecha));

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error al verificar asistencia completa: " + e.getMessage());
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
     * Obtiene asistencias por fecha incluyendo datos completos del empleado
     * @param fecha Fecha a consultar
     * @return Lista de asistencias con datos de empleado
     */
    public List<Asistencia> obtenerAsistenciasConDatosEmpleado(LocalDate fecha) {
        List<Asistencia> asistencias = new ArrayList<>();
        String sql = "SELECT a.*, " +
                "e.codigo_unico AS codigo_empleado, " +
                "CONCAT(e.nombre, ' ', e.apellido) AS nombre_empleado " +
                "FROM asistencias a " +
                "JOIN empleados e ON a.id_empleado = e.id_empleado " +
                "WHERE a.fecha = ? " +
                "ORDER BY a.hora_entrada DESC";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(fecha));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Asistencia asistencia = extraerAsistenciaDeResultSet(rs);

                // Añadir datos del empleado
                asistencia.setCodigoEmpleado(rs.getString("codigo_empleado"));
                asistencia.setNombreEmpleado(rs.getString("nombre_empleado"));

                asistencias.add(asistencia);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener asistencias con datos de empleado: " + e.getMessage());
        }

        return asistencias;
    }

    /**
     * Obtiene asistencias por fecha incluyendo TODOS los empleados (presentes y ausentes)
     * @param fecha Fecha a consultar
     * @return Lista de asistencias con datos de empleado
     */
    public List<Asistencia> obtenerAsistenciasConTodosEmpleados(LocalDate fecha) {
        List<Asistencia> asistencias = new ArrayList<>();

        // Esta consulta usa LEFT JOIN para incluir a TODOS los empleados activos,
        // hayan registrado asistencia o no
        String sql = "SELECT a.id_asistencia, a.fecha, a.hora_entrada, a.hora_salida, " +
                "a.inicio_descanso_1, a.fin_descanso_1, a.inicio_descanso_2, a.fin_descanso_2, " +
                "a.estado, a.notas, " +
                "e.id_empleado, e.codigo_unico AS codigo_empleado, " +
                "CONCAT(e.nombre, ' ', e.apellido) AS nombre_empleado " +
                "FROM empleados e " +
                "LEFT JOIN asistencias a ON e.id_empleado = a.id_empleado AND a.fecha = ? " +
                "WHERE e.activo = 1 " +
                "ORDER BY a.hora_entrada DESC, e.nombre ASC";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(fecha));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                // Verificar si hay registro de asistencia
                boolean tieneAsistencia = (rs.getObject("id_asistencia") != null);
                Asistencia asistencia;

                if (tieneAsistencia) {
                    // Si hay registro, extraer normalmente
                    asistencia = extraerAsistenciaDeResultSet(rs);
                } else {
                    // Si no hay registro, crear uno nuevo con estado FALTA
                    asistencia = new Asistencia();
                    asistencia.setIdEmpleado(rs.getInt("id_empleado"));
                    asistencia.setFecha(fecha);
                    asistencia.setEstado("FALTA");
                }

                // Añadir datos del empleado
                asistencia.setCodigoEmpleado(rs.getString("codigo_empleado"));
                asistencia.setNombreEmpleado(rs.getString("nombre_empleado"));

                asistencias.add(asistencia);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener asistencias con todos los empleados: " + e.getMessage());
        }

        return asistencias;
    }


}





