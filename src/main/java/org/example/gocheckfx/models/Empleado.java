package org.example.gocheckfx.models;


import java.time.LocalDate;

/**
 * Modelo de datos para un empleado en el sistema GoCheck.
 */
public class Empleado {
    private int idEmpleado;
    private String codigoUnico;
    private String nombre;
    private String apellido;
    private int idPuesto;
    private int idTurno;
    private String email;
    private String telefono;
    private LocalDate fechaContratacion;
    private boolean activo;

    // Campos calculados/relacionados
    private String nombrePuesto;
    private String nombreTurno;
    private String horaEntrada;
    private String horaSalida;

    // Constructor vacío
    public Empleado() {
    }

    // Constructor con todos los campos
    public Empleado(int idEmpleado, String codigoUnico, String nombre, String apellido,
                    int idPuesto, int idTurno, String email, String telefono,
                    LocalDate fechaContratacion, boolean activo) {
        this.idEmpleado = idEmpleado;
        this.codigoUnico = codigoUnico;
        this.nombre = nombre;
        this.apellido = apellido;
        this.idPuesto = idPuesto;
        this.idTurno = idTurno;
        this.email = email;
        this.telefono = telefono;
        this.fechaContratacion = fechaContratacion;
        this.activo = activo;
    }

    // Getters y setters
    public int getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(int idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getCodigoUnico() {
        return codigoUnico;
    }

    public void setCodigoUnico(String codigoUnico) {
        this.codigoUnico = codigoUnico;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public int getIdPuesto() {
        return idPuesto;
    }

    public void setIdPuesto(int idPuesto) {
        this.idPuesto = idPuesto;
    }

    public int getIdTurno() {
        return idTurno;
    }

    public void setIdTurno(int idTurno) {
        this.idTurno = idTurno;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public LocalDate getFechaContratacion() {
        return fechaContratacion;
    }

    public void setFechaContratacion(LocalDate fechaContratacion) {
        this.fechaContratacion = fechaContratacion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    // Getters y setters para campos calculados
    public String getNombrePuesto() {
        return nombrePuesto;
    }

    public void setNombrePuesto(String nombrePuesto) {
        this.nombrePuesto = nombrePuesto;
    }

    public String getNombreTurno() {
        return nombreTurno;
    }

    public void setNombreTurno(String nombreTurno) {
        this.nombreTurno = nombreTurno;
    }

    public String getHoraEntrada() {
        return horaEntrada;
    }

    public void setHoraEntrada(String horaEntrada) {
        this.horaEntrada = horaEntrada;
    }

    public String getHoraSalida() {
        return horaSalida;
    }

    public void setHoraSalida(String horaSalida) {
        this.horaSalida = horaSalida;
    }

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    @Override
    public String toString() {
        return "Empleado [ID=" + idEmpleado + ", Código=" + codigoUnico + ", Nombre=" + getNombreCompleto() + "]";
    }
}
