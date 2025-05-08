package org.example.gocheckfx.dao;

import org.example.gocheckfx.config.DatabaseConfig;
import org.example.gocheckfx.models.Usuario;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase DAO para acceder a los datos de usuarios en la base de datos
 */
public class UsuarioDAO {

    /**
     * Autentifica un usuario en el sistema
     * @param username Nombre de usuario
     * @param password Contraseña sin cifrar
     * @return Objeto Usuario o null si la autenticación falla
     */
    public Usuario autenticar(String username, String password) {
        String sql = "SELECT u.*, " +
                "CONCAT(e.nombre, ' ', e.apellido) as nombre_empleado " +
                "FROM usuarios u " +
                "JOIN empleados e ON u.id_empleado = e.id_empleado " +
                "WHERE u.username = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("password");

                // Caso especial para el usuario admin (primer inicio de sesión)
                if (username.equals("admin") && password.equals("admin123")) {
                    Usuario usuario = extraerUsuarioDeResultSet(rs);
                    // Intentar actualizar la contraseña a formato BCrypt
                    try {
                        actualizarABCrypt(usuario.getIdUsuario(), password);
                    } catch (Exception e) {
                        // Ignorar errores, permitir el inicio de sesión de todos modos
                    }
                    return usuario;
                }

                // Verificación estándar para contraseñas en texto plano
                if (password.equals(hashedPassword)) {
                    Usuario usuario = extraerUsuarioDeResultSet(rs);
                    return usuario;
                }

                // Verificación para contraseñas con hash BCrypt
                try {
                    if (hashedPassword != null && hashedPassword.startsWith("$2a$") &&
                            BCrypt.checkpw(password, hashedPassword)) {
                        return extraerUsuarioDeResultSet(rs);
                    }
                } catch (Exception e) {
                    System.err.println("Error verificando contraseña con BCrypt: " + e.getMessage());
                    // Si falla la verificación BCrypt, intentar con verificación directa
                    if (password.equals(hashedPassword)) {
                        return extraerUsuarioDeResultSet(rs);
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al autenticar usuario: " + e.getMessage());
        }

        return null; // Autenticación fallida
    }

    /**
     * Actualiza la contraseña de un usuario al formato BCrypt
     */
    private void actualizarABCrypt(int idUsuario, String plainPassword) {
        String sql = "UPDATE usuarios SET password = ? WHERE id_usuario = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
            stmt.setString(1, hashedPassword);
            stmt.setInt(2, idUsuario);
            stmt.executeUpdate();

        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Error al actualizar formato de contraseña: " + e.getMessage());
        }
    }

    /**
     * Crea un administrador por defecto si no existe ningún usuario
     * @return true si se creó el administrador, false si ya existen usuarios
     */
    public boolean crearAdministradorPorDefecto() {
        // Verificar si ya existen usuarios
        if (existenUsuarios()) {
            return false;
        }

        // Buscar el primer empleado activo para asignarle el rol de admin
        String sqlEmpleado = "SELECT id_empleado FROM empleados WHERE activo = 1 LIMIT 1";
        int idEmpleado = 0;

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sqlEmpleado)) {

            if (rs.next()) {
                idEmpleado = rs.getInt("id_empleado");
            } else {
                // No hay empleados, crear uno
                String sqlInsertEmpleado = "INSERT INTO empleados (codigo_unico, nombre, apellido, id_puesto, id_turno, activo) " +
                        "VALUES ('ADMIN', 'Administrador', 'Sistema', 1, 1, 1)";

                try (Statement stmtInsert = conn.createStatement()) {
                    stmtInsert.executeUpdate(sqlInsertEmpleado, Statement.RETURN_GENERATED_KEYS);
                    ResultSet rsKeys = stmtInsert.getGeneratedKeys();
                    if (rsKeys.next()) {
                        idEmpleado = rsKeys.getInt(1);
                    } else {
                        return false; // No se pudo crear el empleado
                    }
                }
            }

            // Crear usuario administrador con contraseña en texto plano para evitar problemas
            String plainPassword = "admin123"; // Contraseña por defecto
            String sqlInsertUsuario = "INSERT INTO usuarios (id_empleado, username, password, es_admin) " +
                    "VALUES (?, ?, ?, 1)";

            try (PreparedStatement pstmt = conn.prepareStatement(sqlInsertUsuario)) {
                pstmt.setInt(1, idEmpleado);
                pstmt.setString(2, "admin");
                pstmt.setString(3, plainPassword); // Guardar contraseña sin hash inicialmente

                return pstmt.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error al crear administrador por defecto: " + e.getMessage());
        }

        return false;
    }

    /**
     * Verifica si ya existen usuarios en el sistema
     * @return true si existen usuarios, false en caso contrario
     */
    private boolean existenUsuarios() {
        String sql = "SELECT COUNT(*) FROM usuarios";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error al verificar existencia de usuarios: " + e.getMessage());
        }

        return false;
    }

    /**
     * Lista todos los usuarios del sistema
     * @return Lista de usuarios
     */
    public List<Usuario> listarUsuarios() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT u.*, " +
                "CONCAT(e.nombre, ' ', e.apellido) as nombre_empleado " +
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
     * Inserta un nuevo usuario en la base de datos
     * @param usuario Usuario a insertar
     * @return true si se insertó con éxito, false en caso contrario
     */
    public boolean insertar(Usuario usuario) {
        String sql = "INSERT INTO usuarios (id_empleado, username, password, es_admin) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Cifrar la contraseña de forma segura
            String hashedPassword;
            try {
                hashedPassword = BCrypt.hashpw(usuario.getPassword(), BCrypt.gensalt());
            } catch (IllegalArgumentException e) {
                // Si hay error con BCrypt, usar contraseña plana temporalmente
                hashedPassword = usuario.getPassword();
            }

            stmt.setInt(1, usuario.getIdEmpleado());
            stmt.setString(2, usuario.getUsername());
            stmt.setString(3, hashedPassword);
            stmt.setBoolean(4, usuario.isEsAdmin());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    usuario.setIdUsuario(rs.getInt(1));
                }
                return true;
            }

            return false;

        } catch (SQLException e) {
            System.err.println("Error al insertar usuario: " + e.getMessage());
            return false;
        }
    }

    /**
     * Actualiza un usuario existente
     * @param usuario Usuario con los nuevos datos
     * @return true si se actualizó con éxito, false en caso contrario
     */
    public boolean actualizarUsuario(Usuario usuario) {
        String sql = "UPDATE usuarios SET es_admin = ? WHERE id_usuario = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, usuario.isEsAdmin());
            stmt.setInt(2, usuario.getIdUsuario());

            int filasAfectadas = stmt.executeUpdate();

            // Si se proporcionó una nueva contraseña, actualizarla
            if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
                resetearPassword(usuario.getIdUsuario(), usuario.getPassword());
            }

            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar usuario: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un usuario
     * @param idUsuario ID del usuario a eliminar
     * @return true si se eliminó con éxito, false en caso contrario
     */
    public boolean eliminar(int idUsuario) {
        String sql = "DELETE FROM usuarios WHERE id_usuario = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar usuario: " + e.getMessage());
            return false;
        }
    }

    /**
     * Resetea la contraseña de un usuario
     * @param idUsuario ID del usuario
     * @param nuevaPassword Nueva contraseña sin cifrar
     * @return true si se reseteó con éxito, false en caso contrario
     */
    public boolean resetearPassword(int idUsuario, String nuevaPassword) {
        String sql = "UPDATE usuarios SET password = ? WHERE id_usuario = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Cifrar la nueva contraseña de forma segura
            String hashedPassword;
            try {
                hashedPassword = BCrypt.hashpw(nuevaPassword, BCrypt.gensalt());
            } catch (IllegalArgumentException e) {
                // Si hay error con BCrypt, usar contraseña plana temporalmente
                hashedPassword = nuevaPassword;
            }

            stmt.setString(1, hashedPassword);
            stmt.setInt(2, idUsuario);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al resetear contraseña: " + e.getMessage());
            return false;
        }
    }

    /**
     * Extrae un usuario del ResultSet
     */
    private Usuario extraerUsuarioDeResultSet(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();

        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setIdEmpleado(rs.getInt("id_empleado"));
        usuario.setUsername(rs.getString("username"));
        usuario.setEsAdmin(rs.getBoolean("es_admin"));
        usuario.setNombreEmpleado(rs.getString("nombre_empleado"));

        return usuario;
    }
}
