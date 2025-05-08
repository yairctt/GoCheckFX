package org.example.gocheckfx.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.example.gocheckfx.dao.AsistenciaDAO;
import org.example.gocheckfx.dao.EmpleadoDAO;
import org.example.gocheckfx.dao.TurnoDAO;
import org.example.gocheckfx.models.Asistencia;
import org.example.gocheckfx.models.Empleado;
import org.example.gocheckfx.models.Turno;
import org.example.gocheckfx.utils.AlertUtils;
import org.example.gocheckfx.utils.TimeUtils;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Controlador para el registro de asistencia mediante código de empleado
 */
public class RegistroAsistenciaController implements Initializable {

    @FXML private TextField codigoField;
    @FXML private Button registrarButton;
    @FXML private VBox resultadoBox;
    @FXML private Label nombreLabel;
    @FXML private Label puestoLabel;
    @FXML private Label turnoLabel;
    @FXML private Label mensajeLabel;
    @FXML private Label horaLabel;

    private EmpleadoDAO empleadoDAO;
    private AsistenciaDAO asistenciaDAO;
    private TurnoDAO turnoDAO;
    private DateTimeFormatter timeFormatter;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicializar DAOs
        empleadoDAO = new EmpleadoDAO();
        asistenciaDAO = new AsistenciaDAO();
        turnoDAO = new TurnoDAO();

        // Configurar formateador
        timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        // Ocultar inicialmente el panel de resultados
        resultadoBox.setVisible(false);

        // Configurar eventos
        codigoField.setOnAction(e -> procesarRegistro());
        registrarButton.setOnAction(e -> procesarRegistro());

        // Enfocar campo de código
        codigoField.requestFocus();
    }

    /**
     * Procesa el registro de asistencia
     */
    private void procesarRegistro() {
        String codigo = codigoField.getText().trim();

        if (codigo.isEmpty()) {
            AlertUtils.mostrarAdvertencia("Campo Vacío", "Por favor, ingrese un código de empleado.");
            return;
        }

        // Buscar empleado por código
        Empleado empleado = empleadoDAO.buscarPorCodigo(codigo);

        if (empleado == null) {
            AlertUtils.mostrarError("Empleado no Encontrado",
                    "No se encontró ningún empleado con el código: " + codigo);
            codigoField.clear();
            codigoField.requestFocus();
            return;
        }

        // Verificar si el empleado está activo
        if (!empleado.isActivo()) {
            AlertUtils.mostrarError("Empleado Inactivo",
                    "El empleado no está activo en el sistema.");
            codigoField.clear();
            codigoField.requestFocus();
            return;
        }

        // Obtener el turno del empleado
        Turno turno = turnoDAO.buscarPorId(empleado.getIdTurno());

        if (turno == null) {
            AlertUtils.mostrarError("Error", "No se pudo obtener el turno del empleado.");
            return;
        }

        // Obtener fecha actual
        LocalDate fechaActual = LocalDate.now();

        // Obtener asistencia del día actual (si existe)
        Asistencia asistenciaDelDia = asistenciaDAO.obtenerAsistenciaPorEmpleadoFecha(
                empleado.getIdEmpleado(), fechaActual);

        // Hora actual
        LocalDateTime horaActual = LocalDateTime.now();

        // Determinar tipo de registro
        String tipoRegistro = TimeUtils.determinarTipoRegistro(horaActual, turno, asistenciaDelDia);

        // Procesar según tipo de registro
        switch (tipoRegistro) {
            case "ENTRADA":
                registrarEntrada(empleado, turno, horaActual);
                break;

            case "INICIO_DESCANSO_1":
                registrarInicioDescanso1(empleado, asistenciaDelDia, horaActual);
                break;

            case "FIN_DESCANSO_1":
                registrarFinDescanso1(empleado, asistenciaDelDia, horaActual);
                break;

            case "INICIO_DESCANSO_2":
                registrarInicioDescanso2(empleado, asistenciaDelDia, horaActual);
                break;

            case "FIN_DESCANSO_2":
                registrarFinDescanso2(empleado, asistenciaDelDia, horaActual);
                break;

            case "SALIDA":
                registrarSalida(empleado, asistenciaDelDia, turno, horaActual);
                break;

            case "NO_PERMITIDO":
                mostrarMensajeNoPermitido(empleado, turno, asistenciaDelDia);
                break;
        }

        // Limpiar campo de código
        codigoField.clear();
        codigoField.requestFocus();
    }

    /**
     * Registra la entrada de un empleado
     */
    private void registrarEntrada(Empleado empleado, Turno turno, LocalDateTime horaActual) {
        // Crear nueva asistencia
        Asistencia asistencia = new Asistencia();
        asistencia.setIdEmpleado(empleado.getIdEmpleado());
        asistencia.setFecha(horaActual.toLocalDate());
        asistencia.setHoraEntrada(horaActual);

        // Determinar si es retardo
        String estadoEntrada = TimeUtils.determinarEstadoEntrada(horaActual.toLocalTime(), turno);
        if (estadoEntrada.equals("RETARDO")) {
            asistencia.setEstado("RETARDO");
        } else {
            asistencia.setEstado("PRESENTE");
        }

        // Guardar asistencia
        if (asistenciaDAO.registrarAsistencia(asistencia)) {
            // Mostrar resultado
            nombreLabel.setText(empleado.getNombreCompleto());
            puestoLabel.setText(empleado.getNombrePuesto());
            turnoLabel.setText(turno.getNombreTurno());

            String mensaje = estadoEntrada.equals("RETARDO") ?
                    "ENTRADA REGISTRADA CON RETARDO" : "ENTRADA REGISTRADA CORRECTAMENTE";
            mensajeLabel.setText(mensaje);

            if (estadoEntrada.equals("RETARDO")) {
                mensajeLabel.setStyle("-fx-text-fill: #FF6600;");
            } else {
                mensajeLabel.setStyle("-fx-text-fill: #009900;");
            }

            horaLabel.setText(horaActual.format(timeFormatter));
            resultadoBox.setVisible(true);
        } else {
            AlertUtils.mostrarError("Error", "No se pudo registrar la entrada.");
        }
    }

    /**
     * Registra el inicio del primer descanso (desayuno)
     */
    private void registrarInicioDescanso1(Empleado empleado, Asistencia asistencia, LocalDateTime horaActual) {
        // Actualizar asistencia
        asistencia.setInicioDescanso1(horaActual);

        if (asistenciaDAO.actualizarAsistencia(asistencia)) {
            // Mostrar resultado
            nombreLabel.setText(empleado.getNombreCompleto());
            puestoLabel.setText(empleado.getNombrePuesto());
            turnoLabel.setText(""); // No tenemos el turno aquí, podríamos buscarlo
            mensajeLabel.setText("INICIO DE DESAYUNO REGISTRADO");
            mensajeLabel.setStyle("-fx-text-fill: #009900;");
            horaLabel.setText(horaActual.format(timeFormatter));
            resultadoBox.setVisible(true);
        } else {
            AlertUtils.mostrarError("Error", "No se pudo registrar el inicio del desayuno.");
        }
    }

    /**
     * Registra el fin del primer descanso
     */
    private void registrarFinDescanso1(Empleado empleado, Asistencia asistencia, LocalDateTime horaActual) {
        // Actualizar asistencia
        asistencia.setFinDescanso1(horaActual);

        if (asistenciaDAO.actualizarAsistencia(asistencia)) {
            // Mostrar resultado
            nombreLabel.setText(empleado.getNombreCompleto());
            puestoLabel.setText(empleado.getNombrePuesto());
            turnoLabel.setText(""); // No tenemos el turno aquí
            mensajeLabel.setText("FIN DE DESAYUNO REGISTRADO");
            mensajeLabel.setStyle("-fx-text-fill: #009900;");
            horaLabel.setText(horaActual.format(timeFormatter));
            resultadoBox.setVisible(true);
        } else {
            AlertUtils.mostrarError("Error", "No se pudo registrar el fin del desayuno.");
        }
    }

    /**
     * Registra el inicio del segundo descanso (comida)
     */
    private void registrarInicioDescanso2(Empleado empleado, Asistencia asistencia, LocalDateTime horaActual) {
        // Actualizar asistencia
        asistencia.setInicioDescanso2(horaActual);

        if (asistenciaDAO.actualizarAsistencia(asistencia)) {
            // Mostrar resultado
            nombreLabel.setText(empleado.getNombreCompleto());
            puestoLabel.setText(empleado.getNombrePuesto());
            turnoLabel.setText(""); // No tenemos el turno aquí
            mensajeLabel.setText("INICIO DE COMIDA REGISTRADO");
            mensajeLabel.setStyle("-fx-text-fill: #009900;");
            horaLabel.setText(horaActual.format(timeFormatter));
            resultadoBox.setVisible(true);
        } else {
            AlertUtils.mostrarError("Error", "No se pudo registrar el inicio de la comida.");
        }
    }

    /**
     * Registra el fin del segundo descanso
     */
    private void registrarFinDescanso2(Empleado empleado, Asistencia asistencia, LocalDateTime horaActual) {
        // Actualizar asistencia
        asistencia.setFinDescanso2(horaActual);

        if (asistenciaDAO.actualizarAsistencia(asistencia)) {
            // Mostrar resultado
            nombreLabel.setText(empleado.getNombreCompleto());
            puestoLabel.setText(empleado.getNombrePuesto());
            turnoLabel.setText(""); // No tenemos el turno aquí
            mensajeLabel.setText("FIN DE COMIDA REGISTRADO");
            mensajeLabel.setStyle("-fx-text-fill: #009900;");
            horaLabel.setText(horaActual.format(timeFormatter));
            resultadoBox.setVisible(true);
        } else {
            AlertUtils.mostrarError("Error", "No se pudo registrar el fin de la comida.");
        }
    }

    /**
     * Registra la salida de un empleado
     */
    private void registrarSalida(Empleado empleado, Asistencia asistencia, Turno turno, LocalDateTime horaActual) {
        // Actualizar asistencia
        asistencia.setHoraSalida(horaActual);

        // Calcular estado final
        String estadoFinal = TimeUtils.calcularEstadoAsistencia(asistencia, turno);
        asistencia.setEstado(estadoFinal);

        if (asistenciaDAO.actualizarAsistencia(asistencia)) {
            // Mostrar resultado
            nombreLabel.setText(empleado.getNombreCompleto());
            puestoLabel.setText(empleado.getNombrePuesto());
            turnoLabel.setText(""); // No tenemos el turno aquí
            mensajeLabel.setText("SALIDA REGISTRADA CORRECTAMENTE");
            mensajeLabel.setStyle("-fx-text-fill: #009900;");
            horaLabel.setText(horaActual.format(timeFormatter));
            resultadoBox.setVisible(true);
        } else {
            AlertUtils.mostrarError("Error", "No se pudo registrar la salida.");
        }
    }

    /**
     * Muestra un mensaje cuando no se permite el registro
     */
    private void mostrarMensajeNoPermitido(Empleado empleado, Turno turno, Asistencia asistencia) {
        nombreLabel.setText(empleado.getNombreCompleto());
        puestoLabel.setText(empleado.getNombrePuesto());
        turnoLabel.setText(turno.getNombreTurno());

        String mensaje;

        if (asistencia == null || asistencia.getHoraEntrada() == null) {
            mensaje = "NO ES HORARIO DE ENTRADA";
        } else if (asistencia.getHoraSalida() != null) {
            mensaje = "YA SE REGISTRÓ LA SALIDA DEL DÍA";
        } else if (asistencia.getInicioDescanso1() == null && turno.getDuracionDesayuno() > 0) {
            mensaje = "AÚN NO ES HORARIO DE DESAYUNO";
        } else if (asistencia.getFinDescanso1() == null && asistencia.getInicioDescanso1() != null) {
            mensaje = "TIEMPO DE DESAYUNO EXCEDIDO";
        } else if (asistencia.getInicioDescanso2() == null && turno.getDuracionComida() > 0) {
            mensaje = "AÚN NO ES HORARIO DE COMIDA";
        } else if (asistencia.getFinDescanso2() == null && asistencia.getInicioDescanso2() != null) {
            mensaje = "TIEMPO DE COMIDA EXCEDIDO";
        } else {
            mensaje = "AÚN NO ES HORARIO DE SALIDA";
        }

        mensajeLabel.setText(mensaje);
        mensajeLabel.setStyle("-fx-text-fill: #CC0000;");
        horaLabel.setText(LocalDateTime.now().format(timeFormatter));
        resultadoBox.setVisible(true);
    }
}
