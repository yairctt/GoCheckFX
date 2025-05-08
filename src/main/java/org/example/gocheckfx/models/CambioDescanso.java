package org.example.gocheckfx.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Modelo que representa un cambio temporal en el día de descanso de un empleado
 */
public class CambioDescanso {

    private int idCambio;
    private int idEmpleado;
    private LocalDate fechaOriginal;
    private LocalDate fechaNueva;
    private String motivo;
    private LocalDateTime fechaRegistro;
    private int idAdmin;

    // Campos calculados
    private String nombreEmpleado;
    private String nombreAdmin;

    public CambioDescanso() {
        this.fechaRegistro = LocalDateTime.now();
    }

    public CambioDescanso(int idEmpleado, LocalDate fechaOriginal, LocalDate fechaNueva,
                          String motivo, int idAdmin) {
        this();
        this.idEmpleado = idEmpleado;
        this.fechaOriginal = fechaOriginal;
        this.fechaNueva = fechaNueva;
        this.motivo = motivo;
        this.idAdmin = idAdmin;
    }

    // Getters y Setters
    public int getIdCambio() {
        return idCambio;
    }

    public void setIdCambio(int idCambio) {
        this.idCambio = idCambio;
    }

    public int getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(int idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public LocalDate getFechaOriginal() {
        return fechaOriginal;
    }

    public void setFechaOriginal(LocalDate fechaOriginal) {
        this.fechaOriginal = fechaOriginal;
    }

    public LocalDate getFechaNueva() {
        return fechaNueva;
    }

    public void setFechaNueva(LocalDate fechaNueva) {
        this.fechaNueva = fechaNueva;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public int getIdAdmin() {
        return idAdmin;
    }

    public void setIdAdmin(int idAdmin) {
        this.idAdmin = idAdmin;
    }

    public String getNombreEmpleado() {
        return nombreEmpleado;
    }

    public void setNombreEmpleado(String nombreEmpleado) {
        this.nombreEmpleado = nombreEmpleado;
    }

    public String getNombreAdmin() {
        return nombreAdmin;
    }

    public void setNombreAdmin(String nombreAdmin) {
        this.nombreAdmin = nombreAdmin;
    }
}