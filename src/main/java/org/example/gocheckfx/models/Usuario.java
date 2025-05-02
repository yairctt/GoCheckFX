package org.example.gocheckfx.models;

/**
 * Modelo de datos para un usuario del sistema GoCheck.
 * Representa las credenciales de inicio de sesión para administradores.
 */
public class Usuario {
    private int idUsuario;
    private int idEmpleado;
    private String username;
    private String password;  // Almacenado como hash en la base de datos
    private boolean esAdmin;

    // Campos relacionados
    private String nombreEmpleado;

    // Constructor vacío
    public Usuario() {
    }

    // Constructor básico
    public Usuario(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Constructor completo
    public Usuario(int idUsuario, int idEmpleado, String username, String password, boolean esAdmin) {
        this.idUsuario = idUsuario;
        this.idEmpleado = idEmpleado;
        this.username = username;
        this.password = password;
        this.esAdmin = esAdmin;
    }

    // Getters y setters
    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(int idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isEsAdmin() {
        return esAdmin;
    }

    public void setEsAdmin(boolean esAdmin) {
        this.esAdmin = esAdmin;
    }

    public String getNombreEmpleado() {
        return nombreEmpleado;
    }

    public void setNombreEmpleado(String nombreEmpleado) {
        this.nombreEmpleado = nombreEmpleado;
    }

    @Override
    public String toString() {
        return "Usuario [ID=" + idUsuario + ", Username=" + username + ", Empleado=" + nombreEmpleado + "]";
    }
}
