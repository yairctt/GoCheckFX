package org.example.gocheckfx.controllers;

import org.example.gocheckfx.dao.AsistenciaDAO;
import org.example.gocheckfx.dao.EmpleadoDAO;
import org.example.gocheckfx.models.Asistencia;
import org.example.gocheckfx.models.Empleado;
import org.example.gocheckfx.utils.AlertUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador para la pantalla de reportes.
 */
public class ReportController implements Initializable {

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<Empleado> employeeCombo;
    @FXML private ComboBox<String> reportTypeCombo;
    @FXML private Button generateButton;
    @FXML private Button exportButton;
    @FXML private Button closeButton;
    @FXML private TableView<Asistencia> reportTable;
    @FXML private TableColumn<Asistencia, String> dateCol;
    @FXML private TableColumn<Asistencia, String> employeeCol;
    @FXML private TableColumn<Asistencia, String> statusCol;
    @FXML private TableColumn<Asistencia, String> entryCol;
    @FXML private TableColumn<Asistencia, String> exitCol;
    @FXML private TableColumn<Asistencia, String> break1Col;
    @FXML private TableColumn<Asistencia, String> break2Col;
    @FXML private Label totalRecordsLabel;

    private AsistenciaDAO asistenciaDAO;
    private EmpleadoDAO empleadoDAO;
    private DateTimeFormatter dateFormatter;
    private DateTimeFormatter timeFormatter;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Verificar que haya un usuario autenticado
        if (!AdminLoginController.hayUsuarioAutenticado()) {
            AlertUtils.mostrarError("Error de Sesión",
                    "No hay una sesión activa. Por favor, inicie sesión.");
            cerrarVentana();
            return;
        }

        // Inicializar DAOs
        asistenciaDAO = new AsistenciaDAO();
        empleadoDAO = new EmpleadoDAO();

        // Configurar formateadores
        dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        // Configurar fechas por defecto
        LocalDate hoy = LocalDate.now();
        startDatePicker.setValue(hoy.minusDays(7)); // Una semana atrás
        endDatePicker.setValue(hoy);

        // Cargar empleados
        List<Empleado> empleados = empleadoDAO.listarEmpleadosActivos();
        ObservableList<Empleado> empleadosData = FXCollections.observableArrayList(empleados);
        empleadosData.add(0, new Empleado(0, "TODOS", "Todos", "los empleados", 0, 0, null, null, null, true));
        employeeCombo.setItems(empleadosData);
        employeeCombo.getSelectionModel().select(0);

        // Configurar tipos de reporte
        ObservableList<String> tiposReporte = FXCollections.observableArrayList(
                "Asistencias", "Faltas", "Retardos", "Justificados", "Todos");
        reportTypeCombo.setItems(tiposReporte);
        reportTypeCombo.getSelectionModel().select("Todos");

        // Configurar tabla
        configurarTabla();

        // Configurar eventos de botones
        generateButton.setOnAction(e -> generarReporte());

        exportButton.setOnAction(e -> {
            AlertUtils.mostrarInfo("Exportar Reporte",
                    "Esta funcionalidad estará disponible próximamente.");
        });

        closeButton.setOnAction(e -> cerrarVentana());
    }

    /**
     * Configura la tabla de reportes
     */
    private void configurarTabla() {
        dateCol.setCellValueFactory(cellData -> {
            Asistencia asistencia = cellData.getValue();
            String valor = asistencia.getFecha().format(dateFormatter);
            return javafx.beans.binding.Bindings.createStringBinding(() -> valor);
        });

        employeeCol.setCellValueFactory(new PropertyValueFactory<>("nombreEmpleado"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("estado"));

        entryCol.setCellValueFactory(cellData -> {
            Asistencia asistencia = cellData.getValue();
            String valor = asistencia.getHoraEntrada() != null ?
                    asistencia.getHoraEntrada().format(timeFormatter) : "---";
            return javafx.beans.binding.Bindings.createStringBinding(() -> valor);
        });

        exitCol.setCellValueFactory(cellData -> {
            Asistencia asistencia = cellData.getValue();
            String valor = asistencia.getHoraSalida() != null ?
                    asistencia.getHoraSalida().format(timeFormatter) : "---";
            return javafx.beans.binding.Bindings.createStringBinding(() -> valor);
        });

        break1Col.setCellValueFactory(cellData -> {
            Asistencia asistencia = cellData.getValue();
            String inicioDescanso = asistencia.getInicioDescanso1() != null ?
                    asistencia.getInicioDescanso1().format(timeFormatter) : "---";
            String finDescanso = asistencia.getFinDescanso1() != null ?
                    asistencia.getFinDescanso1().format(timeFormatter) : "---";
            String valor = inicioDescanso + " - " + finDescanso;

            return javafx.beans.binding.Bindings.createStringBinding(() -> valor);
        });

        break2Col.setCellValueFactory(cellData -> {
            Asistencia asistencia = cellData.getValue();
            String inicioDescanso = asistencia.getInicioDescanso2() != null ?
                    asistencia.getInicioDescanso2().format(timeFormatter) : "---";
            String finDescanso = asistencia.getFinDescanso2() != null ?
                    asistencia.getFinDescanso2().format(timeFormatter) : "---";
            String valor = inicioDescanso + " - " + finDescanso;

            return javafx.beans.binding.Bindings.createStringBinding(() -> valor);
        });
    }

    /**
     * Genera un reporte según los filtros seleccionados
     */
    private void generarReporte() {
        LocalDate fechaInicio = startDatePicker.getValue();
        LocalDate fechaFin = endDatePicker.getValue();
        Empleado empleadoSeleccionado = employeeCombo.getValue();
        String tipoReporte = reportTypeCombo.getValue();

        if (fechaInicio == null || fechaFin == null) {
            AlertUtils.mostrarAdvertencia("Fechas Requeridas",
                    "Por favor, seleccione las fechas de inicio y fin.");
            return;
        }

        if (fechaInicio.isAfter(fechaFin)) {
            AlertUtils.mostrarAdvertencia("Fechas Inválidas",
                    "La fecha de inicio debe ser anterior o igual a la fecha de fin.");
            return;
        }

        // Lista para almacenar las asistencias filtradas
        List<Asistencia> asistencias;

        // Obtener asistencias según el empleado seleccionado
        if (empleadoSeleccionado.getIdEmpleado() == 0) {
            // Todos los empleados
            asistencias = obtenerAsistenciasPeriodo(fechaInicio, fechaFin);
        } else {
            // Empleado específico
            asistencias = asistenciaDAO.obtenerAsistenciasEmpleado(empleadoSeleccionado.getIdEmpleado(),
                    fechaInicio, fechaFin);
        }

        // Filtrar por tipo de reporte
        List<Asistencia> asistenciasFiltradas = filtrarPorTipo(asistencias, tipoReporte);

        // Mostrar resultados
        ObservableList<Asistencia> data = FXCollections.observableArrayList(asistenciasFiltradas);
        reportTable.setItems(data);

        // Actualizar contador
        totalRecordsLabel.setText("Total de registros: " + asistenciasFiltradas.size());
    }

    /**
     * Obtiene todas las asistencias en un período
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Lista de asistencias
     */
    private List<Asistencia> obtenerAsistenciasPeriodo(LocalDate fechaInicio, LocalDate fechaFin) {
        // En una implementación real, tendríamos un método en el DAO para esto
        // Simulación simple para demo
        List<Asistencia> resultado = new ArrayList<>();
        LocalDate fechaActual = fechaInicio;

        while (!fechaActual.isAfter(fechaFin)) {
            resultado.addAll(asistenciaDAO.obtenerAsistenciasPorFecha(fechaActual));
            fechaActual = fechaActual.plusDays(1);
        }

        return resultado;
    }

    /**
     * Filtra las asistencias por tipo de reporte
     * @param asistencias Lista de asistencias a filtrar
     * @param tipoReporte Tipo de reporte seleccionado
     * @return Lista filtrada de asistencias
     */
    private List<Asistencia> filtrarPorTipo(List<Asistencia> asistencias, String tipoReporte) {
        if (tipoReporte.equals("Todos")) {
            return asistencias;
        }

        List<Asistencia> filtradas = new ArrayList<>();
        String estadoFiltro;

        switch (tipoReporte) {
            case "Asistencias":
                estadoFiltro = "PRESENTE";
                break;
            case "Faltas":
                estadoFiltro = "FALTA";
                break;
            case "Retardos":
                estadoFiltro = "RETARDO";
                break;
            case "Justificados":
                estadoFiltro = "JUSTIFICADO";
                break;
            default:
                return asistencias;
        }

        for (Asistencia asistencia : asistencias) {
            if (asistencia.getEstado().equals(estadoFiltro)) {
                filtradas.add(asistencia);
            }
        }

        return filtradas;
    }

    /**
     * Cierra la ventana actual
     */
    private void cerrarVentana() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Clase interna para encapsular las asistencias con filtros adicionales
     */
    private class ArrayList<T> extends java.util.ArrayList<T> {
        private static final long serialVersionUID = 1L;
    }
}