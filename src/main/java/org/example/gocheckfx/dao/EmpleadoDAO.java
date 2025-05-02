package org.example.gocheckfx.dao;

import org.example.gocheckfx.config.DatabaseConfig;
import org.example.gocheckfx.models.Empleado;
import org.example.gocheckfx.utils.BarcodeGenerator;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase DAO para acceder a los datos de empleados en la base de datos.
 */
public class EmpleadoDAO {


    private static final String CODES_DIRECTORY = "codigos";
    /**
     * Busca un empleado por su código único (QR/barras)
     * @param codigo El código único del empleado
     * @return Empleado encontrado o null si no existe
     */
    public Empleado buscarPorCodigo(String codigo) {
        String sql = "SELECT e.*, p.nombre_puesto, t.nombre_turno, t.hora_entrada, t.hora_salida " +
                "FROM empleados e " +
                "JOIN puestos p ON e.id_puesto = p.id_puesto " +
                "JOIN turnos t ON e.id_turno = t.id_turno " +
                "WHERE e.codigo_unico = ? AND e.activo = 1";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, codigo);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extraerEmpleadoDeResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar empleado por código: " + e.getMessage());
        }

        return null;
    }

    /**
     * Busca un empleado por su ID
     * @param id El ID del empleado
     * @return Empleado encontrado o null si no existe
     */
    public Empleado buscarPorId(int id) {
        String sql = "SELECT e.*, p.nombre_puesto, t.nombre_turno, t.hora_entrada, t.hora_salida " +
                "FROM empleados e " +
                "JOIN puestos p ON e.id_puesto = p.id_puesto " +
                "JOIN turnos t ON e.id_turno = t.id_turno " +
                "WHERE e.id_empleado = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extraerEmpleadoDeResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar empleado por ID: " + e.getMessage());
        }

        return null;
    }

    /**
     * Obtiene la lista de todos los empleados activos
     * @return Lista de empleados
     */
    public List<Empleado> listarEmpleadosActivos() {
        List<Empleado> empleados = new ArrayList<>();
        String sql = "SELECT e.*, p.nombre_puesto, t.nombre_turno, t.hora_entrada, t.hora_salida " +
                "FROM empleados e " +
                "JOIN puestos p ON e.id_puesto = p.id_puesto " +
                "JOIN turnos t ON e.id_turno = t.id_turno " +
                "WHERE e.activo = 1 " +
                "ORDER BY e.apellido, e.nombre";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                empleados.add(extraerEmpleadoDeResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar empleados: " + e.getMessage());
        }

        return empleados;
    }

    /**
     * Inserta un nuevo empleado en la base de datos
     * @param empleado El empleado a insertar
     * @return true si se insertó correctamente, false en caso contrario
     */
    /**
     * Inserta un nuevo empleado en la base de datos y genera sus códigos QR/barras
     * @param empleado El empleado a insertar
     * @return true si se insertó correctamente, false en caso contrario
     */
    public boolean insertar(Empleado empleado) {
        String sql = "INSERT INTO empleados (codigo_unico, nombre, apellido, id_puesto, id_turno, " +
                "email, telefono, fecha_contratacion, activo) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, empleado.getCodigoUnico());
            stmt.setString(2, empleado.getNombre());
            stmt.setString(3, empleado.getApellido());
            stmt.setInt(4, empleado.getIdPuesto());
            stmt.setInt(5, empleado.getIdTurno());
            stmt.setString(6, empleado.getEmail());
            stmt.setString(7, empleado.getTelefono());
            stmt.setDate(8, empleado.getFechaContratacion() != null ?
                    Date.valueOf(empleado.getFechaContratacion()) : null);
            stmt.setBoolean(9, empleado.isActivo());

            int filasAfectadas = stmt.executeUpdate();

            if (filasAfectadas > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    empleado.setIdEmpleado(rs.getInt(1));

                    // Generar y guardar códigos
                    generarYGuardarCodigos(empleado);
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error al insertar empleado: " + e.getMessage());
        }

        return false;
    }

    /**
     * Actualiza un empleado existente en la base de datos y regenera sus códigos si el código único cambió
     * @param empleado El empleado a actualizar
     * @return true si se actualizó correctamente, false en caso contrario
     */
    public boolean actualizar(Empleado empleado) {
        // Obtener el empleado actual para verificar si el código cambió
        Empleado empleadoActual = buscarPorId(empleado.getIdEmpleado());
        boolean codigoCambiado = empleadoActual != null &&
                !empleadoActual.getCodigoUnico().equals(empleado.getCodigoUnico());

        String sql = "UPDATE empleados SET codigo_unico = ?, nombre = ?, apellido = ?, " +
                "id_puesto = ?, id_turno = ?, email = ?, telefono = ?, " +
                "fecha_contratacion = ?, activo = ? " +
                "WHERE id_empleado = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, empleado.getCodigoUnico());
            stmt.setString(2, empleado.getNombre());
            stmt.setString(3, empleado.getApellido());
            stmt.setInt(4, empleado.getIdPuesto());
            stmt.setInt(5, empleado.getIdTurno());
            stmt.setString(6, empleado.getEmail());
            stmt.setString(7, empleado.getTelefono());
            stmt.setDate(8, empleado.getFechaContratacion() != null ?
                    Date.valueOf(empleado.getFechaContratacion()) : null);
            stmt.setBoolean(9, empleado.isActivo());
            stmt.setInt(10, empleado.getIdEmpleado());

            boolean actualizado = stmt.executeUpdate() > 0;

            // Si el código único cambió, regenerar los códigos
            if (actualizado && codigoCambiado) {
                generarYGuardarCodigos(empleado);
            }

            return actualizado;

        } catch (SQLException e) {
            System.err.println("Error al actualizar empleado: " + e.getMessage());
        }

        return false;
    }

    /**
     * Genera y guarda los códigos QR y de barras para un empleado
     * @param empleado El empleado para el que se generarán los códigos
     */
    public void generarYGuardarCodigos(Empleado empleado) {
        // Crear directorio si no existe
        File directory = new File(CODES_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Generar y guardar código QR
        String qrFilePath = CODES_DIRECTORY + "/qr_" + empleado.getCodigoUnico() + ".png";
        BarcodeGenerator.saveQRCode(empleado.getCodigoUnico(), qrFilePath, 300, 300);

        // Generar y guardar código de barras
        String barcodeFilePath = CODES_DIRECTORY + "/barcode_" + empleado.getCodigoUnico() + ".png";
        BarcodeGenerator.saveBarcode(empleado.getCodigoUnico(), barcodeFilePath, 300, 80);
    }

    /**
     * Obtiene las rutas de los archivos de códigos para un empleado
     * @param codigoUnico El código único del empleado
     * @return Array con las rutas [código QR, código de barras] o null si no existen
     */
    public String[] obtenerRutasCodigos(String codigoUnico) {
        String qrFilePath = CODES_DIRECTORY + "/qr_" + codigoUnico + ".png";
        String barcodeFilePath = CODES_DIRECTORY + "/barcode_" + codigoUnico + ".png";

        File qrFile = new File(qrFilePath);
        File barcodeFile = new File(barcodeFilePath);

        if (qrFile.exists() && barcodeFile.exists()) {
            return new String[]{qrFilePath, barcodeFilePath};
        } else {
            // Si no existen, intentar generarlos
            Empleado empleado = buscarPorCodigo(codigoUnico);
            if (empleado != null) {
                generarYGuardarCodigos(empleado);
                return new String[]{qrFilePath, barcodeFilePath};
            }
        }

        return null;
    }
    public boolean eliminar(int idEmpleado) {
        String sql = "UPDATE empleados SET activo = 0 WHERE id_empleado = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEmpleado);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar empleado: " + e.getMessage());
        }

        return false;
    }

    /**
     * Extrae los datos de un empleado desde un ResultSet
     */
    private Empleado extraerEmpleadoDeResultSet(ResultSet rs) throws SQLException {
        Empleado empleado = new Empleado();

        empleado.setIdEmpleado(rs.getInt("id_empleado"));
        empleado.setCodigoUnico(rs.getString("codigo_unico"));
        empleado.setNombre(rs.getString("nombre"));
        empleado.setApellido(rs.getString("apellido"));
        empleado.setIdPuesto(rs.getInt("id_puesto"));
        empleado.setIdTurno(rs.getInt("id_turno"));
        empleado.setEmail(rs.getString("email"));
        empleado.setTelefono(rs.getString("telefono"));

        Date fechaContratacion = rs.getDate("fecha_contratacion");
        if (fechaContratacion != null) {
            empleado.setFechaContratacion(fechaContratacion.toLocalDate());
        }

        empleado.setActivo(rs.getBoolean("activo"));

        // Campos relacionados
        empleado.setNombrePuesto(rs.getString("nombre_puesto"));
        empleado.setNombreTurno(rs.getString("nombre_turno"));
        empleado.setHoraEntrada(rs.getString("hora_entrada"));
        empleado.setHoraSalida(rs.getString("hora_salida"));

        return empleado;
    }
}
