package org.example.gocheckfx.controllers;

import javafx.scene.image.ImageView;
import org.example.gocheckfx.App;
import org.example.gocheckfx.dao.AsistenciaDAO;
import org.example.gocheckfx.dao.EmpleadoDAO;
import org.example.gocheckfx.models.Asistencia;
import org.example.gocheckfx.models.Empleado;
import org.example.gocheckfx.utils.AlertUtils;
import org.example.gocheckfx.utils.QRCodeScanner;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.ResourceBundle;


/**
 * Controlador para la pantalla de escaneo de empleados.
 * Esta es la pantalla principal donde los empleados registran sus entradas, salidas y descansos.
 */
public class EmployeeScanController implements Initializable {

    @FXML private Pane cameraPane; // Cambio de ImageView a Pane para webcam
    @FXML private TextField codeField;
    @FXML private Button scanButton;
    @FXML private Button adminLoginButton;
    @FXML private VBox employeeInfoBox;
    @FXML private Label employeeNameLabel;
    @FXML private Label positionLabel;
    @FXML private Label shiftLabel;
    @FXML private Label lastActionLabel;
    @FXML private Label nextActionLabel;
    @FXML private Label timeLabel;
    @FXML private Label statusLabel; // Nuevo: estado del escáner

    private QRCodeScanner qrScanner;
    private EmpleadoDAO empleadoDAO;
    private AsistenciaDAO asistenciaDAO;
    private DateTimeFormatter timeFormatter;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicializar DAOs
        empleadoDAO = new EmpleadoDAO();
        asistenciaDAO = new AsistenciaDAO();

        // Configurar el formato de hora
        timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        // Ocultar inicialmente la información del empleado
        employeeInfoBox.setVisible(false);

        // Estado inicial
        statusLabel.setText("Preparando cámara...");

        try {
            // Configurar el escáner de códigos QR/barras con la webcam
            qrScanner = new QRCodeScanner(cameraPane, codeField, this::procesarCodigoEscaneado);

            // Iniciar el reloj en tiempo real
            iniciarReloj();

            // Configurar los eventos de los botones
            scanButton.setOnAction(e -> {
                if (qrScanner.isScanning()) {
                    qrScanner.stopScanning();
                    scanButton.setText("Iniciar Escaneo");
                    statusLabel.setText("Escáner detenido");
                } else {
                    qrScanner.startScanning();
                    scanButton.setText("Detener Escaneo");
                    statusLabel.setText("Escaneando...");
                }
            });

            // Botón para entrar al modo administrador
            adminLoginButton.setOnAction(e -> {
                App.openWindow("/org/example/gocheckfx/admin_login.fxml", "GoCheck - Login de Administrador");
            });

            // Procesar código manual al presionar Enter en el campo de texto
            codeField.setOnAction(e -> {
                String codigo = codeField.getText().trim();
                if (!codigo.isEmpty()) {
                    procesarCodigoEscaneado(codigo);
                }
            });

            // Iniciar escaneo automáticamente
            qrScanner.startScanning();
            scanButton.setText("Detener Escaneo");
            statusLabel.setText("Escaneando... Apunte un código QR o de barras a la cámara");

        } catch (Exception e) {
            // Error al inicializar el escáner
            statusLabel.setText("Error al inicializar la cámara: " + e.getMessage());
            AlertUtils.mostrarError("Error de Cámara",
                    "No se pudo inicializar la cámara para el escaneo. " +
                            "Utilice el modo de entrada manual.");
        }
    }

    /**
     * Procesa un código escaneado o ingresado manualmente
     * @param codigo El código del empleado
     */
    private void procesarCodigoEscaneado(String codigo) {
        // Buscar el empleado por su código
        Empleado empleado = empleadoDAO.buscarPorCodigo(codigo);

        if (empleado == null) {
            // Empleado no encontrado
            AlertUtils.mostrarError("Empleado no encontrado",
                    "El código escaneado no corresponde a ningún empleado registrado.");
            employeeInfoBox.setVisible(false);
            statusLabel.setText("Código inválido. Intente de nuevo.");
            return;
        }

        // Mostrar información del empleado
        mostrarInformacionEmpleado(empleado);

        // Registrar la asistencia
        registrarAsistencia(empleado);

        // Limpiar el campo después de procesar
        codeField.setText("");

        // Actualizar status
        statusLabel.setText("Registro exitoso para " + empleado.getNombreCompleto());
    }

    /**
     * Muestra la información del empleado en la interfaz
     * @param empleado El empleado a mostrar
     */
    private void mostrarInformacionEmpleado(Empleado empleado) {
        employeeNameLabel.setText(empleado.getNombreCompleto());
        positionLabel.setText("Puesto: " + empleado.getNombrePuesto());
        shiftLabel.setText("Turno: " + empleado.getNombreTurno() + " (" +
                empleado.getHoraEntrada() + " - " + empleado.getHoraSalida() + ")");

        employeeInfoBox.setVisible(true);
    }

    /**
     * Registra la asistencia del empleado
     * @param empleado El empleado que registra asistencia
     */
    private void registrarAsistencia(Empleado empleado) {
        // Obtener la fecha actual
        LocalDate fechaHoy = LocalDate.now();
        LocalDateTime ahora = LocalDateTime.now();

        // Buscar si ya existe una asistencia para este empleado en esta fecha
        Asistencia asistencia = asistenciaDAO.buscarAsistencia(empleado.getIdEmpleado(), fechaHoy);

        // Si no existe, crear una nueva
        if (asistencia == null) {
            asistencia = new Asistencia(empleado.getIdEmpleado(), fechaHoy);
            asistencia.setHoraEntrada(ahora);
            asistencia.setEstado("PRESENTE");

            if (asistenciaDAO.registrarAsistencia(asistencia)) {
                lastActionLabel.setText("Última acción: Entrada registrada a las " +
                        ahora.format(timeFormatter));
                nextActionLabel.setText("Próxima acción: Inicio de descanso");
                AlertUtils.mostrarInfo("Entrada Registrada",
                        "Se ha registrado su entrada correctamente.");
            } else {
                AlertUtils.mostrarError("Error al Registrar",
                        "No se pudo registrar la entrada. Contacte al administrador.");
            }
        } else {
            // Determinar la siguiente acción según el estado actual
            String siguienteAccion = asistencia.determinarSiguienteAccion();

            if (siguienteAccion.equals("COMPLETO")) {
                AlertUtils.mostrarInfo("Registro Completo",
                        "Ya ha completado su jornada de hoy.");
                lastActionLabel.setText("Última acción: Jornada completa");
                nextActionLabel.setText("Próxima acción: Ninguna");
                return;
            }

            // Registrar la acción correspondiente
            if (asistenciaDAO.registrarAccion(asistencia.getIdAsistencia(), siguienteAccion, ahora)) {
                String mensajeAccion;
                String proximaAccion;

                switch (siguienteAccion) {
                    case "ENTRADA":
                        mensajeAccion = "Entrada registrada";
                        proximaAccion = "Inicio de descanso";
                        break;
                    case "INICIO_DESCANSO1":
                        mensajeAccion = "Inicio de descanso registrado";
                        proximaAccion = "Fin de descanso";
                        break;
                    case "FIN_DESCANSO1":
                        mensajeAccion = "Fin de descanso registrado";
                        proximaAccion = "Inicio de segundo descanso";
                        break;
                    case "INICIO_DESCANSO2":
                        mensajeAccion = "Inicio de segundo descanso registrado";
                        proximaAccion = "Fin de segundo descanso";
                        break;
                    case "FIN_DESCANSO2":
                        mensajeAccion = "Fin de segundo descanso registrado";
                        proximaAccion = "Salida";
                        break;
                    case "SALIDA":
                        mensajeAccion = "Salida registrada";
                        proximaAccion = "Ninguna";
                        break;
                    default:
                        mensajeAccion = "Acción registrada";
                        proximaAccion = "Siguiente acción";
                }

                lastActionLabel.setText("Última acción: " + mensajeAccion + " a las " +
                        ahora.format(timeFormatter));
                nextActionLabel.setText("Próxima acción: " + proximaAccion);

                AlertUtils.mostrarInfo("Acción Registrada", mensajeAccion + " correctamente.");
            } else {
                AlertUtils.mostrarError("Error al Registrar",
                        "No se pudo registrar la acción. Contacte al administrador.");
            }
        }
    }

    /**
     * Inicia un reloj en tiempo real en la interfaz
     */
    private void iniciarReloj() {
        // Mostrar hora actual inicial
        actualizarHora();

        // Actualizar la hora cada segundo
        Thread clockThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    actualizarHora();
                } catch (InterruptedException e) {
                    break;
                }
            }
        });

        clockThread.setDaemon(true);
        clockThread.start();
    }

    /**
     * Actualiza la hora mostrada en la interfaz
     */
    private void actualizarHora() {
        LocalDateTime ahora = LocalDateTime.now();
        String horaFormateada = ahora.format(DateTimeFormatter.ofPattern("HH:mm:ss - dd/MM/yyyy"));

        // Actualizar la UI en el hilo de JavaFX
        javafx.application.Platform.runLater(() -> {
            timeLabel.setText(horaFormateada);
        });
    }

    /**
     * Método llamado al cerrar la ventana para liberar recursos
     */
    public void cleanUp() {
        if (qrScanner != null) {
            qrScanner.dispose();
        }
    }
}