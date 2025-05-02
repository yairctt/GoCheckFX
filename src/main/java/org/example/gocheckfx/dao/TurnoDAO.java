package org.example.gocheckfx.dao;

import org.example.gocheckfx.config.DatabaseConfig;
import org.example.gocheckfx.models.Turno;

import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase DAO para acceder a los datos de turnos en la base de datos.
 */
public class TurnoDAO {

    /**
     * Obtiene la lista de todos los turnos
     * @return Lista de turnos
     */
    public List<Turno> listarTurnos() {
        List<Turno> turnos = new ArrayList<>();
        String sql = "SELECT * FROM turnos ORDER BY nombre_turno";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                turnos.add(extraerTurnoDeResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar turnos: " + e.getMessage());
        }

        return turnos;
    }

    /**
     * Busca un turno por su ID
     * @param id El ID del turno
     * @return Turno encontrado o null si no existe
     */
    public Turno buscarPorId(int id) {
        String sql = "SELECT * FROM turnos WHERE id_turno = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extraerTurnoDeResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar turno por ID: " + e.getMessage());
        }

        return null;
    }

    /**
     * Inserta un nuevo turno en la base de datos
     * @param turno El turno a insertar
     * @return true si se insertó correctamente, false en caso contrario
     */
    public boolean insertar(Turno turno) {
        String sql = "INSERT INTO turnos (nombre_turno, hora_entrada, hora_salida, " +
                "duracion_desayuno, duracion_comida, permite_combinar_descanso) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, turno.getNombreTurno());
            stmt.setTime(2, Time.valueOf(turno.getHoraEntrada()));
            stmt.setTime(3, Time.valueOf(turno.getHoraSalida()));
            stmt.setInt(4, turno.getDuracionDesayuno());
            stmt.setInt(5, turno.getDuracionComida());
            stmt.setBoolean(6, turno.isPermiteCombinarDescanso());

            int filasAfectadas = stmt.executeUpdate();

            if (filasAfectadas > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    turno.setIdTurno(rs.getInt(1));
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error al insertar turno: " + e.getMessage());
        }

        return false;
    }

    /**
     * Actualiza un turno existente en la base de datos
     * @param turno El turno a actualizar
     * @return true si se actualizó correctamente, false en caso contrario
     */
    public boolean actualizar(Turno turno) {
        String sql = "UPDATE turnos SET nombre_turno = ?, hora_entrada = ?, hora_salida = ?, " +
                "duracion_desayuno = ?, duracion_comida = ?, permite_combinar_descanso = ? " +
                "WHERE id_turno = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, turno.getNombreTurno());
            stmt.setTime(2, Time.valueOf(turno.getHoraEntrada()));
            stmt.setTime(3, Time.valueOf(turno.getHoraSalida()));
            stmt.setInt(4, turno.getDuracionDesayuno());
            stmt.setInt(5, turno.getDuracionComida());
            stmt.setBoolean(6, turno.isPermiteCombinarDescanso());
            stmt.setInt(7, turno.getIdTurno());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar turno: " + e.getMessage());
        }

        return false;
    }

    /**
     * Elimina un turno de la base de datos
     * @param idTurno El ID del turno a eliminar
     * @return true si se eliminó correctamente, false en caso contrario
     */
    public boolean eliminar(int idTurno) {
        String sql = "DELETE FROM turnos WHERE id_turno = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idTurno);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar turno: " + e.getMessage());
        }

        return false;
    }

    /**
     * Extrae los datos de un turno desde un ResultSet
     */
    private Turno extraerTurnoDeResultSet(ResultSet rs) throws SQLException {
        Turno turno = new Turno();

        turno.setIdTurno(rs.getInt("id_turno"));
        turno.setNombreTurno(rs.getString("nombre_turno"));

        Time horaEntrada = rs.getTime("hora_entrada");
        if (horaEntrada != null) {
            turno.setHoraEntrada(horaEntrada.toLocalTime());
        }

        Time horaSalida = rs.getTime("hora_salida");
        if (horaSalida != null) {
            turno.setHoraSalida(horaSalida.toLocalTime());
        }

        turno.setDuracionDesayuno(rs.getInt("duracion_desayuno"));
        turno.setDuracionComida(rs.getInt("duracion_comida"));
        turno.setPermiteCombinarDescanso(rs.getBoolean("permite_combinar_descanso"));

        return turno;
    }
}
