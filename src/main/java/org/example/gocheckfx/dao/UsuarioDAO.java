package org.example.gocheckfx.dao;

import org.example.gocheckfx.config.DatabaseConfig;
import org.example.gocheckfx.models.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

/**
 * Clase DAO para acceder a los datos de usuarios en la base de datos.
 */
public class UsuarioDAO {

    /**
     * Verifica las credenciales de un usuario
     * @param username El nombre de usuario
     * @param password La contraseña
     * @return Usuario autenticado o null si las credenciales son incorrectas
     */
    public Usuario autenticar(String username, String password) {
        String sql = "SELECT u.*, e.nombre, e.apellido " +
                "FROM usuarios u " +
                "JOIN empleados e ON u.id_empleado = e.id_empleado " +
                "WHERE u.username = ? AND u.password = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, hashPassword(password));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extraerUsuarioDeResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error al autenticar usuario: " + e.getMessage());
        }

        return null;
    }

    /**
     * Crea un nuevo usuario en la base de datos
     * @param usuario El usuario a crear
     * @return true si se creó correctamente, false en caso contrario
     */
    public boolean crear(Usuario usuario) {
        String sql = "INSERT INTO usuarios (id_empleado, username, password, es_admin) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, usuario.getIdEmpleado());
            stmt.setString(2, usuario.getUsername());
            stmt.setString(3, hashPassword(usuario.getPassword()));
            stmt.setBoolean(4, usuario.isEsAdmin());

            int filasAfectadas = stmt.executeUpdate();

            if (filasAfectadas > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    usuario.setIdUsuario(rs.getInt(1));
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error al crear usuario: " + e.getMessage());
        }

        return false;
    }

    /**
     * Actualiza un usuario existente en la base de datos
     * @param usuario El usuario a actualizar
     * @param actualizarPassword Si se debe actualizar también la contraseña
     * @return true si se actualizó correctamente, false en caso contrario
     */
    public boolean actualizar(Usuario usuario, boolean actualizarPassword) {
        StringBuilder sql = new StringBuilder("UPDATE usuarios SET id_empleado = ?, username = ?, es_admin = ?");

        if (actualizarPassword) {
            sql.append(", password = ?");
        }

        sql.append(" WHERE id_usuario = ?");

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            stmt.setInt(1, usuario.getIdEmpleado());
            stmt.setString(2, usuario.getUsername());
            stmt.setBoolean(3, usuario.isEsAdmin());

            if (actualizarPassword) {
                stmt.setString(4, hashPassword(usuario.getPassword()));
                stmt.setInt(5, usuario.getIdUsuario());
            } else {
                stmt.setInt(4, usuario.getIdUsuario());
            }

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar usuario: " + e.getMessage());
        }

        return false;
    }

    /**
     * Elimina un usuario de la base de datos
     * @param idUsuario El ID del usuario a eliminar
     * @return true si se eliminó correctamente, false en caso contrario
     */
    public boolean eliminar(int idUsuario) {
        String sql = "DELETE FROM usuarios WHERE id_usuario = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar usuario: " + e.getMessage());
        }

        return false;
    }

    /**
     * Obtiene la lista de todos los usuarios
     * @return Lista de usuarios
     */
    public List<Usuario> listarUsuarios() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT u.*, e.nombre, e.apellido " +
                "FROM usuarios u " +
                "JOIN empleados e ON u.id_empleado = e.id_empleado " +
                "ORDER BY u.username";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                usuarios.add(extraerUsuarioDeResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar usuarios: " + e.getMessage());
        }

        return usuarios;
    }

    /**
     * Verifica si un nombre de usuario ya existe
     * @param username El nombre de usuario a verificar
     * @return true si el usuario existe, false en caso contrario
     */
    public boolean existeUsername(String username) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE username = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error al verificar username: " + e.getMessage());
        }

        return false;
    }

    /**
     * Extrae los datos de un usuario desde un ResultSet
     */
    private Usuario extraerUsuarioDeResultSet(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();

        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setIdEmpleado(rs.getInt("id_empleado"));
        usuario.setUsername(rs.getString("username"));
        usuario.setPassword(rs.getString("password")); // No mostrar en UI
        usuario.setEsAdmin(rs.getBoolean("es_admin"));

        // Campos relacionados
        if (rs.getMetaData().getColumnCount() > 5) { // Si hay datos del empleado
            usuario.setNombreEmpleado(rs.getString("nombre") + " " + rs.getString("apellido"));
        }

        return usuario;
    }

    /**
     * Genera un hash de la contraseña usando SHA-256
     * @param password La contraseña a hashear
     * @return Hash de la contraseña
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            // Convertir bytes a string hexadecimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error al hashear contraseña: " + e.getMessage());
            return password; // Fallback inseguro, solo para demo
        }
    }

    /**
     * Crea un usuario administrador por defecto si no existe ninguno
     * @return true si se creó correctamente, false si ya existe algún administrador
     */
    public boolean crearAdministradorPorDefecto() {
        // Primero verificar si ya existe algún administrador
        String sqlVerificar = "SELECT COUNT(*) FROM usuarios WHERE es_admin = 1";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sqlVerificar)) {

            if (rs.next() && rs.getInt(1) > 0) {
                // Ya existe al menos un administrador
                return false;
            }

            // No hay administradores, crear uno nuevo

            // 1. Insertar empleado para el administrador
            String sqlEmpleado = "INSERT INTO empleados (codigo_unico, nombre, apellido, id_puesto, id_turno, email, activo) " +
                    "VALUES ('ADMIN001', 'Administrador', 'Sistema', 1, 1, 'admin@gocheck.com', 1)";

            stmt.executeUpdate(sqlEmpleado, Statement.RETURN_GENERATED_KEYS);

            int idEmpleado;
            try (ResultSet rsKeys = stmt.getGeneratedKeys()) {
                if (rsKeys.next()) {
                    idEmpleado = rsKeys.getInt(1);
                } else {
                    throw new SQLException("No se pudo obtener el ID del empleado insertado");
                }
            }

            // 2. Insertar usuario administrador
            // La contraseña es 'admin123' hasheada con SHA-256
            String sqlUsuario = "INSERT INTO usuarios (id_empleado, username, password, es_admin) " +
                    "VALUES (?, 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 1)";

            try (PreparedStatement pstmt = conn.prepareStatement(sqlUsuario)) {
                pstmt.setInt(1, idEmpleado);
                pstmt.executeUpdate();
            }

            return true;

        } catch (SQLException e) {
            System.err.println("Error al crear administrador por defecto: " + e.getMessage());
            return false;
        }
    }
}
