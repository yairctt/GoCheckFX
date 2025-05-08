package org.example.gocheckfx.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import org.example.gocheckfx.models.Turno;
import org.example.gocheckfx.models.Asistencia;

/**
 * Utilidad para gestionar las reglas de tiempo y asistencia
 */
public class TimeUtils {

    // Constantes para tolerancias
    public static final int TOLERANCIA_ENTRADA_MINUTOS = 10; // 10 minutos de tolerancia para la entrada
    public static final int TOLERANCIA_DESCANSO_MINUTOS = 5; // 5 minutos de tolerancia para descansos

    /**
     * Determina el tipo de registro basado en el horario del empleado
     * @param horaActual Hora actual del registro
     * @param turno Turno asignado al empleado
     * @param ultimaAsistencia Último registro de asistencia del día (puede ser null)
     * @return Tipo de registro (ENTRADA, SALIDA, INICIO_DESCANSO_1, FIN_DESCANSO_1, INICIO_DESCANSO_2, FIN_DESCANSO_2, NO_PERMITIDO)
     */
    public static String determinarTipoRegistro(LocalDateTime horaActual, Turno turno, Asistencia ultimaAsistencia) {
        LocalTime time = horaActual.toLocalTime();

        // Si no hay asistencia previa en el día, debe ser entrada
        if (ultimaAsistencia == null || ultimaAsistencia.getHoraEntrada() == null) {
            // Verificar si es tiempo de entrada
            if (esTiempoEntrada(time, turno)) {
                return "ENTRADA";
            } else {
                return "NO_PERMITIDO"; // Fuera del rango permitido para entrada
            }
        }

        // Si ya hay hora de entrada pero no hay hora de salida
        if (ultimaAsistencia.getHoraEntrada() != null && ultimaAsistencia.getHoraSalida() == null) {
            // Verificar si es tiempo de descanso
            if (ultimaAsistencia.getInicioDescanso1() == null) {
                if (esTiempoInicioDescanso1(time, turno, ultimaAsistencia.getHoraEntrada().toLocalTime())) {
                    return "INICIO_DESCANSO_1";
                }
            } else if (ultimaAsistencia.getFinDescanso1() == null) {
                if (esTiempoFinDescanso1(time, turno, ultimaAsistencia.getInicioDescanso1().toLocalTime())) {
                    return "FIN_DESCANSO_1";
                }
            } else if (ultimaAsistencia.getInicioDescanso2() == null && turno.getDuracionComida() > 0) {
                if (esTiempoInicioDescanso2(time, turno, ultimaAsistencia.getFinDescanso1().toLocalTime())) {
                    return "INICIO_DESCANSO_2";
                }
            } else if (ultimaAsistencia.getFinDescanso2() == null && ultimaAsistencia.getInicioDescanso2() != null) {
                if (esTiempoFinDescanso2(time, turno, ultimaAsistencia.getInicioDescanso2().toLocalTime())) {
                    return "FIN_DESCANSO_2";
                }
            }

            // Verificar si es tiempo de salida
            if (esTiempoSalida(time, turno)) {
                return "SALIDA";
            }
        }

        return "NO_PERMITIDO"; // Ninguna condición se cumplió
    }

    /**
     * Verifica si es tiempo de entrada
     */
    private static boolean esTiempoEntrada(LocalTime horaActual, Turno turno) {
        LocalTime horaEntradaTurno = turno.getHoraEntrada();
        LocalTime limiteRetardo = horaEntradaTurno.plusMinutes(TOLERANCIA_ENTRADA_MINUTOS);

        // Es tiempo de entrada si está entre 30 minutos antes de la hora oficial y el límite de retardo
        return !horaActual.isBefore(horaEntradaTurno.minusMinutes(30)) && !horaActual.isAfter(limiteRetardo);
    }

    /**
     * Determina el estado de la entrada (A TIEMPO, RETARDO)
     */
    public static String determinarEstadoEntrada(LocalTime horaRegistro, Turno turno) {
        if (horaRegistro.isAfter(turno.getHoraEntrada()) &&
                !horaRegistro.isAfter(turno.getHoraEntrada().plusMinutes(TOLERANCIA_ENTRADA_MINUTOS))) {
            return "RETARDO";
        }
        return "A_TIEMPO";
    }

    /**
     * Verifica si es tiempo de inicio del primer descanso (desayuno)
     */
    private static boolean esTiempoInicioDescanso1(LocalTime horaActual, Turno turno, LocalTime horaEntrada) {
        // Si no hay desayuno programado, no se permite
        if (turno.getDuracionDesayuno() <= 0) {
            return false;
        }

        // El tiempo apropiado para desayuno normalmente es 2-3 horas después de la entrada
        LocalTime tiempoIdealDesayuno = horaEntrada.plusHours(2);

        // Se permite registrar desayuno desde 1 hora después de entrada hasta 4 horas después
        return !horaActual.isBefore(horaEntrada.plusHours(1)) && !horaActual.isAfter(horaEntrada.plusHours(4));
    }

    /**
     * Verifica si es tiempo de fin del primer descanso
     */
    private static boolean esTiempoFinDescanso1(LocalTime horaActual, Turno turno, LocalTime inicioDescanso) {
        // La duración máxima del desayuno es la configurada en el turno
        LocalTime tiempoMaximoRegreso = inicioDescanso.plusMinutes(turno.getDuracionDesayuno() + TOLERANCIA_DESCANSO_MINUTOS);

        // Se considera tiempo válido para registrar el fin del descanso si no excede el tiempo máximo
        return !horaActual.isBefore(inicioDescanso.plusMinutes(10)) && !horaActual.isAfter(tiempoMaximoRegreso);
    }

    /**
     * Verifica si es tiempo de inicio del segundo descanso (comida)
     */
    private static boolean esTiempoInicioDescanso2(LocalTime horaActual, Turno turno, LocalTime finDescanso1) {
        // Si no hay comida programada, no se permite
        if (turno.getDuracionComida() <= 0) {
            return false;
        }

        // El tiempo apropiado para comida normalmente es 3-4 horas después del fin del desayuno
        LocalTime tiempoIdealComida = finDescanso1.plusHours(3);

        // Se permite registrar comida desde 2 horas después del fin del desayuno hasta 5 horas después
        return !horaActual.isBefore(finDescanso1.plusHours(2)) && !horaActual.isAfter(finDescanso1.plusHours(5));
    }

    /**
     * Verifica si es tiempo de fin del segundo descanso
     */
    private static boolean esTiempoFinDescanso2(LocalTime horaActual, Turno turno, LocalTime inicioDescanso) {
        // La duración máxima de la comida es la configurada en el turno
        LocalTime tiempoMaximoRegreso = inicioDescanso.plusMinutes(turno.getDuracionComida() + TOLERANCIA_DESCANSO_MINUTOS);

        // Se considera tiempo válido para registrar el fin del descanso si no excede el tiempo máximo
        return !horaActual.isBefore(inicioDescanso.plusMinutes(20)) && !horaActual.isAfter(tiempoMaximoRegreso);
    }

    /**
     * Verifica si es tiempo de salida
     */
    private static boolean esTiempoSalida(LocalTime horaActual, Turno turno) {
        LocalTime horaSalidaTurno = turno.getHoraSalida();

        // Se permite registrar salida desde 30 minutos antes de la hora oficial hasta 2 horas después
        return !horaActual.isBefore(horaSalidaTurno.minusMinutes(30)) && !horaActual.isAfter(horaSalidaTurno.plusHours(2));
    }

    /**
     * Calcula el estado final de la asistencia
     */
    public static String calcularEstadoAsistencia(Asistencia asistencia, Turno turno) {
        // Si no registró entrada o salida, es falta
        if (asistencia.getHoraEntrada() == null || asistencia.getHoraSalida() == null) {
            return "FALTA";
        }

        // Si llegó tarde, es retardo
        if (asistencia.getHoraEntrada().toLocalTime().isAfter(turno.getHoraEntrada()) &&
                !asistencia.getHoraEntrada().toLocalTime().isAfter(turno.getHoraEntrada().plusMinutes(TOLERANCIA_ENTRADA_MINUTOS))) {
            return "RETARDO";
        }

        // Si debía tomar desayuno pero no lo tomó
        if (turno.getDuracionDesayuno() > 0 &&
                (asistencia.getInicioDescanso1() == null || asistencia.getFinDescanso1() == null)) {
            return "PRESENTE"; // Está presente pero sin registrar descanso
        }

        // Si debía tomar comida pero no lo tomó
        if (turno.getDuracionComida() > 0 &&
                (asistencia.getInicioDescanso2() == null || asistencia.getFinDescanso2() == null)) {
            return "PRESENTE"; // Está presente pero sin registrar descanso
        }

        return "PRESENTE"; // Todo en orden
    }
}
