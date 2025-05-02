package org.example.gocheckfx.models;

import java.time.LocalTime;

/**
 * Modelo de datos para un turno de trabajo en el sistema GoCheck.
 */
public class Turno {
    private int idTurno;
    private String nombreTurno;
    private LocalTime horaEntrada;
    private LocalTime horaSalida;
    private int duracionDesayuno;  // En minutos
    private int duracionComida;    // En minutos
    private boolean permiteCombinarDescanso;

    // Constructor vacío
    public Turno() {
    }

    // Constructor con todos los campos
    public Turno(int idTurno, String nombreTurno, LocalTime horaEntrada, LocalTime horaSalida,
                 int duracionDesayuno, int duracionComida, boolean permiteCombinarDescanso) {
        this.idTurno = idTurno;
        this.nombreTurno = nombreTurno;
        this.horaEntrada = horaEntrada;
        this.horaSalida = horaSalida;
        this.duracionDesayuno = duracionDesayuno;
        this.duracionComida = duracionComida;
        this.permiteCombinarDescanso = permiteCombinarDescanso;
    }

    // Getters y setters
    public int getIdTurno() {
        return idTurno;
    }

    public void setIdTurno(int idTurno) {
        this.idTurno = idTurno;
    }

    public String getNombreTurno() {
        return nombreTurno;
    }

    public void setNombreTurno(String nombreTurno) {
        this.nombreTurno = nombreTurno;
    }

    public LocalTime getHoraEntrada() {
        return horaEntrada;
    }

    public void setHoraEntrada(LocalTime horaEntrada) {
        this.horaEntrada = horaEntrada;
    }

    public LocalTime getHoraSalida() {
        return horaSalida;
    }

    public void setHoraSalida(LocalTime horaSalida) {
        this.horaSalida = horaSalida;
    }

    public int getDuracionDesayuno() {
        return duracionDesayuno;
    }

    public void setDuracionDesayuno(int duracionDesayuno) {
        this.duracionDesayuno = duracionDesayuno;
    }

    public int getDuracionComida() {
        return duracionComida;
    }

    public void setDuracionComida(int duracionComida) {
        this.duracionComida = duracionComida;
    }

    public boolean isPermiteCombinarDescanso() {
        return permiteCombinarDescanso;
    }

    public void setPermiteCombinarDescanso(boolean permiteCombinarDescanso) {
        this.permiteCombinarDescanso = permiteCombinarDescanso;
    }

    /**
     * Calcula el total de minutos de descanso permitidos en este turno
     */
    public int getTotalMinutosDescanso() {
        return duracionDesayuno + duracionComida;
    }

    /**
     * Determina si el turno tiene dos descansos separados
     */
    public boolean tieneDosDescansos() {
        return duracionDesayuno > 0 && duracionComida > 0;
    }

    @Override
    public String toString() {
        return nombreTurno + " (" + horaEntrada + " - " + horaSalida + ")";
    }
}
