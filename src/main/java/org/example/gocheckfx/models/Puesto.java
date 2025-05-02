package org.example.gocheckfx.models;

import java.time.LocalDateTime;

/**
 * Modelo de datos para un puesto de trabajo en el sistema GoCheck.
 */
public class Puesto {
    private int idPuesto;
    private String nombrePuesto;
    private String descripcion;
    private String reglasDescansoJSON; // Almacenado como JSON
    private LocalDateTime fechaCreacion;
    private boolean activo;

    // Constructor vacío
    public Puesto() {
    }

    // Constructor principal
    public Puesto(int idPuesto, String nombrePuesto, String descripcion,
                  String reglasDescansoJSON, LocalDateTime fechaCreacion, boolean activo) {
        this.idPuesto = idPuesto;
        this.nombrePuesto = nombrePuesto;
        this.descripcion = descripcion;
        this.reglasDescansoJSON = reglasDescansoJSON;
        this.fechaCreacion = fechaCreacion;
        this.activo = activo;
    }

    // Getters y setters
    public int getIdPuesto() {
        return idPuesto;
    }

    public void setIdPuesto(int idPuesto) {
        this.idPuesto = idPuesto;
    }

    public String getNombrePuesto() {
        return nombrePuesto;
    }

    public void setNombrePuesto(String nombrePuesto) {
        this.nombrePuesto = nombrePuesto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getReglasDescansoJSON() {
        return reglasDescansoJSON;
    }

    public void setReglasDescansoJSON(String reglasDescansoJSON) {
        this.reglasDescansoJSON = reglasDescansoJSON;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    /**
     * Verifica si el puesto permite combinar descansos
     * @return true si permite combinar descansos, false en caso contrario
     */
    public boolean permiteCombinarDescanso() {
        // En una implementación real, aquí analizaríamos el JSON
        // Para simplicidad, asumimos que sí lo permite
        return true;
    }

    /**
     * Verifica si el puesto requiere desayuno
     * @return true si requiere desayuno, false en caso contrario
     */
    public boolean requiereDesayuno() {
        // En una implementación real, aquí analizaríamos el JSON
        return !reglasDescansoJSON.contains("\"sin_desayuno\": true");
    }

    /**
     * Verifica si el puesto permite dos descansos
     * @return true si permite dos descansos, false en caso contrario
     */
    public boolean permiteDosDescansos() {
        // En una implementación real, aquí analizaríamos el JSON
        return reglasDescansoJSON.contains("\"dos_descansos\": true");
    }

    @Override
    public String toString() {
        return nombrePuesto;
    }
}