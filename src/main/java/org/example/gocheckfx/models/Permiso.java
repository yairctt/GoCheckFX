package org.example.gocheckfx.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Modelo que representa un permiso o solicitud de ausencia
 */
public class Permiso {

    private int idPermiso;
    private int idEmpleado;
    private LocalDate fecha;
    private String motivo;
    private String estado; // PENDIENTE, APROBADO, RECHAZADO
    private LocalDateTime fechaSolicitud;
    private Integer idAdmin; // Puede ser null si está pendiente

    // Campos calculados
    private String nombreEmpleado;
    private String nombreAdmin;

    public Permiso() {
        this.fechaSolicitud = LocalDateTime.now();
        this.estado = "PENDIENTE";
    }

    public Permiso(int idEmpleado, LocalDate fecha, String motivo) {
        this();
        this.idEmpleado = idEmpleado;
        this.fecha = fecha;
        this.motivo = motivo;
    }

    // Getters y Setters
    public int getIdPermiso() {
        return idPermiso;
    }

    public void setIdPermiso(int idPermiso) {
        this.idPermiso = idPermiso;
    }

    public int getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(int idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public Integer getIdAdmin() {
        return idAdmin;
    }

    public void setIdAdmin(Integer idAdmin) {
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
