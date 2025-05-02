package org.example.gocheckfx.dao;

import org.example.gocheckfx.config.DatabaseConfig;
import org.example.gocheckfx.models.Puesto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase DAO para acceder a los datos de puestos en la base de datos.
 */
public class PuestoDAO {

    /**
     * Obtiene la lista de todos los puestos activos
     * @return Lista de puestos
     */
    public List<Puesto> listarPuestosActivos() {
        List<Puesto> puestos = new ArrayList<>();
        String sql = "SELECT * FROM puestos WHERE activo = 1 ORDER BY nombre_puesto";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                puestos.add(extraerPuestoDeResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar puestos: " + e.getMessage());
        }

        return puestos;
    }

    /**
     * Busca un puesto por su ID
     * @param id El ID del puesto
     * @return Puesto encontrado o null si no existe
     */
    public Puesto buscarPorId(int id) {
        String sql = "SELECT * FROM puestos WHERE id_puesto = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extraerPuestoDeResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar puesto por ID: " + e.getMessage());
        }

        return null;
    }

    /**
     * Inserta un nuevo puesto en la base de datos
     * @param puesto El puesto a insertar
     * @return true si se insertó correctamente, false en caso contrario
     */
    public boolean insertar(Puesto puesto) {
        String sql = "INSERT INTO puestos (nombre_puesto, descripcion, reglas_descanso, activo) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, puesto.getNombrePuesto());
            stmt.setString(2, puesto.getDescripcion());
            stmt.setString(3, puesto.getReglasDescansoJSON());
            stmt.setBoolean(4, puesto.isActivo());

            int filasAfectadas = stmt.executeUpdate();

            if (filasAfectadas > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    puesto.setIdPuesto(rs.getInt(1));
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error al insertar puesto: " + e.getMessage());
        }

        return false;
    }

    /**
     * Actualiza un puesto existente en la base de datos
     * @param puesto El puesto a actualizar
     * @return true si se actualizó correctamente, false en caso contrario
     */
    public boolean actualizar(Puesto puesto) {
        String sql = "UPDATE puestos SET nombre_puesto = ?, descripcion = ?, " +
                "reglas_descanso = ?, activo = ? " +
                "WHERE id_puesto = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, puesto.getNombrePuesto());
            stmt.setString(2, puesto.getDescripcion());
            stmt.setString(3, puesto.getReglasDescansoJSON());
            stmt.setBoolean(4, puesto.isActivo());
            stmt.setInt(5, puesto.getIdPuesto());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar puesto: " + e.getMessage());
        }

        return false;
    }

    /**
     * Elimina lógicamente un puesto (marca como inactivo)
     * @param idPuesto El ID del puesto a eliminar
     * @return true si se eliminó correctamente, false en caso contrario
     */
    public boolean eliminar(int idPuesto) {
        String sql = "UPDATE puestos SET activo = 0 WHERE id_puesto = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPuesto);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar puesto: " + e.getMessage());
        }

        return false;
    }

    /**
     * Extrae los datos de un puesto desde un ResultSet
     */
    private Puesto extraerPuestoDeResultSet(ResultSet rs) throws SQLException {
        Puesto puesto = new Puesto();

        puesto.setIdPuesto(rs.getInt("id_puesto"));
        puesto.setNombrePuesto(rs.getString("nombre_puesto"));
        puesto.setDescripcion(rs.getString("descripcion"));
        puesto.setReglasDescansoJSON(rs.getString("reglas_descanso"));
        puesto.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
        puesto.setActivo(rs.getBoolean("activo"));

        return puesto;
    }
}
