package org.example.gocheckfx.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Modelo de datos para una asistencia en el sistema GoCheck.
 */
public class Asistencia {
    private int idAsistencia;
    private int idEmpleado;
    private LocalDate fecha;
    private LocalDateTime horaEntrada;
    private LocalDateTime horaSalida;
    private LocalDateTime inicioDescanso1;
    private LocalDateTime finDescanso1;
    private LocalDateTime inicioDescanso2;
    private LocalDateTime finDescanso2;
    private String estado;  // PRESENTE, FALTA, RETARDO, JUSTIFICADO
    private String notas;

    // Campos calculados/relacionados
    private String nombreEmpleado;
    private String codigoEmpleado;

    // Constructor vacío
    public Asistencia() {
    }

    // Constructor básico
    public Asistencia(int idEmpleado, LocalDate fecha) {
        this.idEmpleado = idEmpleado;
        this.fecha = fecha;
        this.estado = "FALTA"; // Estado por defecto
    }

    // Constructor completo
    public Asistencia(int idAsistencia, int idEmpleado, LocalDate fecha,
                      LocalDateTime horaEntrada, LocalDateTime horaSalida,
                      LocalDateTime inicioDescanso1, LocalDateTime finDescanso1,
                      LocalDateTime inicioDescanso2, LocalDateTime finDescanso2,
                      String estado, String notas) {
        this.idAsistencia = idAsistencia;
        this.idEmpleado = idEmpleado;
        this.fecha = fecha;
        this.horaEntrada = horaEntrada;
        this.horaSalida = horaSalida;
        this.inicioDescanso1 = inicioDescanso1;
        this.finDescanso1 = finDescanso1;
        this.inicioDescanso2 = inicioDescanso2;
        this.finDescanso2 = finDescanso2;
        this.estado = estado;
        this.notas = notas;
    }

    // Getters y setters
    public int getIdAsistencia() {
        return idAsistencia;
    }

    public void setIdAsistencia(int idAsistencia) {
        this.idAsistencia = idAsistencia;
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

    public LocalDateTime getHoraEntrada() {
        return horaEntrada;
    }

    public void setHoraEntrada(LocalDateTime horaEntrada) {
        this.horaEntrada = horaEntrada;
    }

    public LocalDateTime getHoraSalida() {
        return horaSalida;
    }

    public void setHoraSalida(LocalDateTime horaSalida) {
        this.horaSalida = horaSalida;
    }

    public LocalDateTime getInicioDescanso1() {
        return inicioDescanso1;
    }

    public void setInicioDescanso1(LocalDateTime inicioDescanso1) {
        this.inicioDescanso1 = inicioDescanso1;
    }

    public LocalDateTime getFinDescanso1() {
        return finDescanso1;
    }

    public void setFinDescanso1(LocalDateTime finDescanso1) {
        this.finDescanso1 = finDescanso1;
    }

    public LocalDateTime getInicioDescanso2() {
        return inicioDescanso2;
    }

    public void setInicioDescanso2(LocalDateTime inicioDescanso2) {
        this.inicioDescanso2 = inicioDescanso2;
    }

    public LocalDateTime getFinDescanso2() {
        return finDescanso2;
    }

    public void setFinDescanso2(LocalDateTime finDescanso2) {
        this.finDescanso2 = finDescanso2;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    // Getters y setters para campos calculados
    public String getNombreEmpleado() {
        return nombreEmpleado;
    }

    public void setNombreEmpleado(String nombreEmpleado) {
        this.nombreEmpleado = nombreEmpleado;
    }

    public String getCodigoEmpleado() {
        return codigoEmpleado;
    }

    public void setCodigoEmpleado(String codigoEmpleado) {
        this.codigoEmpleado = codigoEmpleado;
    }

    /**
     * Determina la acción que debe registrarse según el estado actual de la asistencia
     * @return Tipo de acción a registrar (ENTRADA, SALIDA, INICIO_DESCANSO1, etc.)
     */
    public String determinarSiguienteAccion() {
        if (horaEntrada == null) {
            return "ENTRADA";
        } else if (inicioDescanso1 == null) {
            return "INICIO_DESCANSO1";
        } else if (finDescanso1 == null) {
            return "FIN_DESCANSO1";
        } else if (inicioDescanso2 == null) {
            return "INICIO_DESCANSO2";
        } else if (finDescanso2 == null) {
            return "FIN_DESCANSO2";
        } else if (horaSalida == null) {
            return "SALIDA";
        }
        return "COMPLETO";
    }

    @Override
    public String toString() {
        return "Asistencia [ID=" + idAsistencia + ", Empleado=" + idEmpleado +
                ", Fecha=" + fecha + ", Estado=" + estado + "]";
    }
}
