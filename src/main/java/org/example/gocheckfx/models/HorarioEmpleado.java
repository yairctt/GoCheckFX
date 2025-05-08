package org.example.gocheckfx.models;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;

/**
 * Modelo que representa el horario laboral de un empleado
 */
public class HorarioEmpleado {

    private int idHorario;
    private int idEmpleado;
    private List<String> diasLaborables;
    private String diaDescansoSemanal;

    // Nombre del empleado (para mostrar en UI)
    private String nombreEmpleado;

    public HorarioEmpleado() {
        this.diasLaborables = new ArrayList<>();
    }

    public HorarioEmpleado(int idEmpleado, List<String> diasLaborables, String diaDescansoSemanal) {
        this.idEmpleado = idEmpleado;
        this.diasLaborables = diasLaborables;
        this.diaDescansoSemanal = diaDescansoSemanal;
    }

    // Getters y Setters
    public int getIdHorario() {
        return idHorario;
    }

    public void setIdHorario(int idHorario) {
        this.idHorario = idHorario;
    }

    public int getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(int idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public List<String> getDiasLaborables() {
        return diasLaborables;
    }

    public void setDiasLaborables(List<String> diasLaborables) {
        this.diasLaborables = diasLaborables;
    }

    public String getDiaDescansoSemanal() {
        return diaDescansoSemanal;
    }

    public void setDiaDescansoSemanal(String diaDescansoSemanal) {
        this.diaDescansoSemanal = diaDescansoSemanal;
    }

    public String getNombreEmpleado() {
        return nombreEmpleado;
    }

    public void setNombreEmpleado(String nombreEmpleado) {
        this.nombreEmpleado = nombreEmpleado;
    }

    /**
     * Convierte la lista de días laborables a formato JSON
     */
    public String getDiasLaborablesJSON() {
        JSONArray jsonArray = new JSONArray(diasLaborables);
        return jsonArray.toString();
    }

    /**
     * Establece los días laborables desde un string JSON
     */
    public void setDiasLaborablesFromJSON(String jsonString) {
        diasLaborables = new ArrayList<>();
        if (jsonString != null && !jsonString.isEmpty()) {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                diasLaborables.add(jsonArray.getString(i));
            }
        }
    }

    /**
     * Obtiene una representación de cadena de los días laborables
     */
    public String getDiasLaborablesString() {
        if (diasLaborables == null || diasLaborables.isEmpty()) {
            return "Ninguno";
        }
        return String.join(", ", diasLaborables);
    }
}