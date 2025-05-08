package org.example.gocheckfx.utils;

import org.example.gocheckfx.models.Asistencia;
import org.example.gocheckfx.models.Empleado;
import org.example.gocheckfx.models.Turno;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

/**
 * Clase para manejar la lógica de marcación de asistencias
 * según horarios establecidos
 */
public class RegistroAsistenciaManager {

    // Constantes para tolerancia de tiempo
    private static final int TOLERANCIA_ENTRADA_MINUTOS = 10; // 10 minutos de tolerancia
    private static final int TOLERANCIA_DESCANSO_MINUTOS = 5; // 5 minutos de tolerancia para descansos

    /**
     * Enumerado para los diferentes tipos de registros
     */
    public enum TipoRegistro {
        ENTRADA,
        INICIO_DESCANSO_1,
        FIN_DESCANSO_1,
        INICIO_DESCANSO_2,
        FIN_DESCANSO_2,
        SALIDA,
        NO_APLICABLE
    }

    /**
     * Determina qué tipo de registro corresponde basado en la hora actual
     * y el turno del empleado
     *
     * @param empleado Empleado que realiza el registro
     * @param turno Turno asignado al empleado
     * @param asistencia Asistencia actual del día (puede ser null si es primer registro)
     * @return Tipo de registro que corresponde
     */
    public static TipoRegistro determinarTipoRegistro(Empleado empleado, Turno turno,
                                                      Asistencia asistencia) {
        LocalDateTime ahora = LocalDateTime.now();
        LocalTime horaActual = ahora.toLocalTime();

        // Si no hay asistencia, es la primera marcación del día
        if (asistencia == null || asistencia.getHoraEntrada() == null) {
            // Verificar si está en horario de entrada
            if (esHoraDeEntrada(horaActual, turno)) {
                return TipoRegistro.ENTRADA;
            } else {
                return TipoRegistro.NO_APLICABLE;
            }
        }

        // Ya tiene entrada registrada
        if (asistencia.getHoraEntrada() != null) {
            // Verificar si corresponde a descanso 1 (desayuno)
            if (turno.getDuracionDesayuno() > 0 && asistencia.getInicioDescanso1() == null) {
                // Si está en horario de desayuno
                if (esHoraDeDesayuno(horaActual, turno)) {
                    return TipoRegistro.INICIO_DESCANSO_1;
                }
            }

            // Verificar si corresponde a fin de descanso 1
            if (asistencia.getInicioDescanso1() != null && asistencia.getFinDescanso1() == null) {
                return TipoRegistro.FIN_DESCANSO_1;
            }

            // Verificar si corresponde a descanso 2 (comida)
            if (turno.getDuracionComida() > 0 &&
                    asistencia.getInicioDescanso2() == null &&
                    (asistencia.getFinDescanso1() != null || turno.getDuracionDesayuno() == 0)) {

                // Si está en horario de comida
                if (esHoraDeComida(horaActual, turno)) {
                    return TipoRegistro.INICIO_DESCANSO_2;
                }
            }

            // Verificar si corresponde a fin de descanso 2
            if (asistencia.getInicioDescanso2() != null && asistencia.getFinDescanso2() == null) {
                return TipoRegistro.FIN_DESCANSO_2;
            }

            // Verificar si corresponde a salida
            if ((asistencia.getFinDescanso2() != null ||
                    (turno.getDuracionComida() == 0 && asistencia.getFinDescanso1() != null) ||
                    (turno.getDuracionComida() == 0 && turno.getDuracionDesayuno() == 0)) &&
                    asistencia.getHoraSalida() == null) {

                // Si está en horario de salida
                if (esHoraDeSalida(horaActual, turno)) {
                    return TipoRegistro.SALIDA;
                }
            }
        }

        // Si no aplica ninguna condición
        return TipoRegistro.NO_APLICABLE;
    }

    /**
     * Determina si la hora actual es válida para registrar entrada
     */
    private static boolean esHoraDeEntrada(LocalTime horaActual, Turno turno) {
        LocalTime horaEntrada = turno.getHoraEntrada();

        // Hora mínima: hora de entrada - 30 minutos (para registros anticipados)
        LocalTime horaMinima = horaEntrada.minusMinutes(30);

        // Hora máxima: hora de entrada + tolerancia
        LocalTime horaMaxima = horaEntrada.plusMinutes(TOLERANCIA_ENTRADA_MINUTOS);

        return !horaActual.isBefore(horaMinima) && !horaActual.isAfter(horaMaxima);
    }

    /**
     * Determina si la hora actual es válida para registrar inicio de desayuno
     */
    private static boolean esHoraDeDesayuno(LocalTime horaActual, Turno turno) {
        // Establecer un rango razonable para el desayuno
        // (generalmente 1-2 horas después de entrar)
        LocalTime horaEntrada = turno.getHoraEntrada();

        // Hora mínima: 1 hora después de entrada
        LocalTime horaMinima = horaEntrada.plusHours(1);

        // Hora máxima: 3 horas después de entrada
        LocalTime horaMaxima = horaEntrada.plusHours(3);

        return !horaActual.isBefore(horaMinima) && !horaActual.isAfter(horaMaxima);
    }

    /**
     * Determina si la hora actual es válida para registrar inicio de comida
     */
    private static boolean esHoraDeComida(LocalTime horaActual, Turno turno) {
        // Establecer un rango razonable para la comida
        // (generalmente a la mitad del turno)
        LocalTime horaEntrada = turno.getHoraEntrada();
        LocalTime horaSalida = turno.getHoraSalida();

        // Calculamos la hora media del turno para centrar el horario de comida
        long duracionTurnoMinutos = ChronoUnit.MINUTES.between(horaEntrada, horaSalida);
        LocalTime horaMitadTurno = horaEntrada.plusMinutes(duracionTurnoMinutos / 2);

        // Definir ventana de 2 horas centrada en la mitad del turno
        LocalTime horaMinima = horaMitadTurno.minusHours(1);
        LocalTime horaMaxima = horaMitadTurno.plusHours(1);

        return !horaActual.isBefore(horaMinima) && !horaActual.isAfter(horaMaxima);
    }

    /**
     * Determina si la hora actual es válida para registrar salida
     */
    private static boolean esHoraDeSalida(LocalTime horaActual, Turno turno) {
        LocalTime horaSalida = turno.getHoraSalida();

        // Hora mínima: 1 hora antes de la salida oficial
        LocalTime horaMinima = horaSalida.minusHours(1);

        // Hora máxima: 2 horas después de la salida oficial (para horas extra)
        LocalTime horaMaxima = horaSalida.plusHours(2);

        return !horaActual.isBefore(horaMinima) && !horaActual.isAfter(horaMaxima);
    }

    /**
     * Determina el estado de la asistencia según la hora de entrada
     * @param horaEntrada Hora de entrada registrada
     * @param turno Turno asignado
     * @return Estado de la asistencia: PRESENTE, RETARDO o FALTA
     */
    public static String determinarEstadoAsistencia(LocalDateTime horaEntrada, Turno turno) {
        if (horaEntrada == null) {
            return "FALTA";
        }

        LocalTime horaEntradaTime = horaEntrada.toLocalTime();
        LocalTime horaEntradaLimite = turno.getHoraEntrada().plusMinutes(TOLERANCIA_ENTRADA_MINUTOS);

        if (horaEntradaTime.isAfter(horaEntradaLimite)) {
            return "RETARDO";
        } else {
            return "PRESENTE";
        }
    }

    /**
     * Verifica si un empleado debe tomar descanso separado según las reglas de su puesto
     */
    public static boolean debeTomarDescansoSeparado(Empleado empleado) {
        // Esta lógica dependerá de las reglas configuradas para el puesto
        // Se implementaría obteniendo las reglas desde la base de datos
        return true; // Por defecto asumimos que sí
    }

    /**
     * Actualiza una asistencia según el tipo de registro actual
     * @param asistencia Asistencia a actualizar (puede ser nueva)
     * @param tipoRegistro Tipo de registro a efectuar
     * @param empleado Empleado que registra
     * @param turno Turno del empleado
     * @return Asistencia actualizada
     */
    public static Asistencia registrarAsistencia(Asistencia asistencia, TipoRegistro tipoRegistro,
                                                 Empleado empleado, Turno turno) {

        LocalDateTime ahora = LocalDateTime.now();
        LocalDate hoy = ahora.toLocalDate();

        // Si es nuevo registro, crear nueva asistencia
        if (asistencia == null) {
            asistencia = new Asistencia();
            asistencia.setIdEmpleado(empleado.getIdEmpleado());
            asistencia.setFecha(hoy);
        }

        // Actualizar según tipo de registro
        switch (tipoRegistro) {
            case ENTRADA:
                asistencia.setHoraEntrada(ahora);
                asistencia.setEstado(determinarEstadoAsistencia(ahora, turno));
                break;

            case INICIO_DESCANSO_1:
                asistencia.setInicioDescanso1(ahora);
                break;

            case FIN_DESCANSO_1:
                asistencia.setFinDescanso1(ahora);
                // Verificar si excedió tiempo permitido
                if (asistencia.getInicioDescanso1() != null) {
                    long minutosDesayuno = ChronoUnit.MINUTES.between(
                            asistencia.getInicioDescanso1(), ahora);

                    if (minutosDesayuno > turno.getDuracionDesayuno() + TOLERANCIA_DESCANSO_MINUTOS) {
                        // Agregar nota de exceso
                        String nota = "Excedió tiempo de desayuno por " +
                                (minutosDesayuno - turno.getDuracionDesayuno()) + " minutos. ";

                        if (asistencia.getNotas() != null) {
                            asistencia.setNotas(asistencia.getNotas() + nota);
                        } else {
                            asistencia.setNotas(nota);
                        }
                    }
                }
                break;

            case INICIO_DESCANSO_2:
                asistencia.setInicioDescanso2(ahora);
                break;

            case FIN_DESCANSO_2:
                asistencia.setFinDescanso2(ahora);
                // Verificar si excedió tiempo permitido
                if (asistencia.getInicioDescanso2() != null) {
                    long minutosComida = ChronoUnit.MINUTES.between(
                            asistencia.getInicioDescanso2(), ahora);

                    if (minutosComida > turno.getDuracionComida() + TOLERANCIA_DESCANSO_MINUTOS) {
                        // Agregar nota de exceso
                        String nota = "Excedió tiempo de comida por " +
                                (minutosComida - turno.getDuracionComida()) + " minutos. ";

                        if (asistencia.getNotas() != null) {
                            asistencia.setNotas(asistencia.getNotas() + nota);
                        } else {
                            asistencia.setNotas(nota);
                        }
                    }
                }
                break;

            case SALIDA:
                asistencia.setHoraSalida(ahora);
                // Verificar si no registró descansos obligatorios
                if (turno.getDuracionDesayuno() > 0 && asistencia.getInicioDescanso1() == null) {
                    String nota = "No registró su desayuno. ";

                    if (asistencia.getNotas() != null) {
                        asistencia.setNotas(asistencia.getNotas() + nota);
                    } else {
                        asistencia.setNotas(nota);
                    }
                }

                if (turno.getDuracionComida() > 0 && asistencia.getInicioDescanso2() == null) {
                    String nota = "No registró su comida. ";

                    if (asistencia.getNotas() != null) {
                        asistencia.setNotas(asistencia.getNotas() + nota);
                    } else {
                        asistencia.setNotas(nota);
                    }
                }
                break;

            default:
                // No aplica, no hacer cambios
                break;
        }

        return asistencia;
    }

    /**
     * Obtiene un mensaje descriptivo según el tipo de registro
     */
    public static String obtenerMensajeRegistro(TipoRegistro tipoRegistro, Empleado empleado) {
        switch (tipoRegistro) {
            case ENTRADA:
                return "Bienvenido " + empleado.getNombre() + ". Entrada registrada.";

            case INICIO_DESCANSO_1:
                return "Inicio de desayuno registrado.";

            case FIN_DESCANSO_1:
                return "Fin de desayuno registrado.";

            case INICIO_DESCANSO_2:
                return "Inicio de comida registrado.";

            case FIN_DESCANSO_2:
                return "Fin de comida registrado.";

            case SALIDA:
                return "¡Hasta pronto " + empleado.getNombre() + "! Salida registrada.";

            case NO_APLICABLE:
                return "No es posible registrar en este momento. Fuera de horario.";

            default:
                return "Registro no reconocido.";
        }
    }
}