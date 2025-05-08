package org.example.gocheckfx.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.example.gocheckfx.dao.EmpleadoDAO;
import org.example.gocheckfx.dao.PermisoDAO;
import org.example.gocheckfx.models.Empleado;
import org.example.gocheckfx.models.Permiso;
import org.example.gocheckfx.models.Usuario;
import org.example.gocheckfx.utils.AlertUtils;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador para la gestión de permisos
 */
public class PermisoManagementController implements Initializable {

    @FXML private TabPane tabPane;

    // Tab de permisos pendientes
    @FXML private TableView<Permiso> pendingTable;
    @FXML private TableColumn<Permiso, String> pendingEmployeeCol;
    @FXML private TableColumn<Permiso, LocalDate> pendingDateCol;
    @FXML private TableColumn<Permiso, String> pendingReasonCol;
    @FXML private Button approveButton;
    @FXML private Button rejectButton;

    // Tab de permisos aprobados/rechazados
    @FXML private TableView<Permiso> historyTable;
    @FXML private TableColumn<Permiso, String> historyEmployeeCol;
    @FXML private TableColumn<Permiso, LocalDate> historyDateCol;
    @FXML private TableColumn<Permiso, String> historyStatusCol;
    @FXML private TableColumn<Permiso, String> historyAdminCol;
    @FXML private ComboBox<String> statusFilterCombo;

    // Tab para solicitar permiso
    @FXML private ComboBox<Empleado> employeeCombo;
    @FXML private DatePicker permissionDatePicker;
    @FXML private TextArea reasonTextArea;
    @FXML private Button requestButton;

    @FXML private Button closeButton;

    private PermisoDAO permisoDAO;
    private EmpleadoDAO empleadoDAO;
    private DateTimeFormatter dateFormatter;

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
        permisoDAO = new PermisoDAO();
        empleadoDAO = new EmpleadoDAO();

        // Configurar formateador de fechas
        dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Configurar tablas
        configurarTablas();

        // Configurar filtros y combos
        statusFilterCombo.getItems().addAll("APROBADO", "RECHAZADO", "TODOS");
        statusFilterCombo.setValue("TODOS");
        statusFilterCombo.setOnAction(e -> cargarHistorialPermisos());

        // Cargar empleados para selección
        List<Empleado> empleados = empleadoDAO.listarEmpleadosActivos();
        employeeCombo.setItems(FXCollections.observableArrayList(empleados));

        // Establecer fecha mínima para solicitudes (a partir de mañana)
        permissionDatePicker.setValue(LocalDate.now().plusDays(1));

        // Configurar eventos de botones
        approveButton.setOnAction(e -> procesarPermiso("APROBADO"));
        rejectButton.setOnAction(e -> procesarPermiso("RECHAZADO"));
        requestButton.setOnAction(e -> solicitarPermiso());
        closeButton.setOnAction(e -> cerrarVentana());

        // Cargar datos iniciales
        cargarPermisosPendientes();
        cargarHistorialPermisos();
    }

    /**
     * Configura las tablas de permisos
     */
    private void configurarTablas() {
        // Tabla de pendientes
        pendingEmployeeCol.setCellValueFactory(new PropertyValueFactory<>("nombreEmpleado"));

        pendingDateCol.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        pendingDateCol.setCellFactory(column -> new TableCell<Permiso, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(dateFormatter.format(item));
                }
            }
        });

        pendingReasonCol.setCellValueFactory(new PropertyValueFactory<>("motivo"));

        // Tabla de historial
        historyEmployeeCol.setCellValueFactory(new PropertyValueFactory<>("nombreEmpleado"));

        historyDateCol.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        historyDateCol.setCellFactory(column -> new TableCell<Permiso, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(dateFormatter.format(item));
                }
            }
        });

        historyStatusCol.setCellValueFactory(new PropertyValueFactory<>("estado"));
        historyAdminCol.setCellValueFactory(new PropertyValueFactory<>("nombreAdmin"));

        // Permitir selección única
        pendingTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        historyTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    /**
     * Carga los permisos pendientes
     */
    private void cargarPermisosPendientes() {
        List<Permiso> permisos = permisoDAO.obtenerPermisosPendientes();
        ObservableList<Permiso> data = FXCollections.observableArrayList(permisos);
        pendingTable.setItems(data);

        // Habilitar/deshabilitar botones según si hay selección
        boolean hayPermisosPendientes = !permisos.isEmpty();
        approveButton.setDisable(!hayPermisosPendientes);
        rejectButton.setDisable(!hayPermisosPendientes);
    }

    /**
     * Carga el historial de permisos según el filtro seleccionado
     */
    private void cargarHistorialPermisos() {
        String filtro = statusFilterCombo.getValue();
        List<Permiso> permisos;

        if ("TODOS".equals(filtro)) {
            permisos = new ArrayList<>();
            permisos.addAll(permisoDAO.obtenerPermisosPorEstado("APROBADO"));
            permisos.addAll(permisoDAO.obtenerPermisosPorEstado("RECHAZADO"));
        } else {
            permisos = permisoDAO.obtenerPermisosPorEstado(filtro);
        }

        ObservableList<Permiso> data = FXCollections.observableArrayList(permisos);
        historyTable.setItems(data);
    }

    /**
     * Procesa un permiso (aprobar o rechazar)
     * @param estado Nuevo estado del permiso ("APROBADO" o "RECHAZADO")
     */
    private void procesarPermiso(String estado) {
        Permiso permisoSeleccionado = pendingTable.getSelectionModel().getSelectedItem();

        if (permisoSeleccionado == null) {
            AlertUtils.mostrarAdvertencia("Selección Requerida",
                    "Por favor, seleccione un permiso para procesar.");
            return;
        }

        // Obtener usuario administrador actual
        Usuario admin = AdminLoginController.getUsuarioAutenticado();

        if (permisoDAO.actualizarEstadoPermiso(permisoSeleccionado.getIdPermiso(), estado, admin.getIdEmpleado())) {
            AlertUtils.mostrarInfo("Operación Exitosa",
                    "Permiso " + estado.toLowerCase() + " correctamente.");

            // Recargar datos
            cargarPermisosPendientes();
            cargarHistorialPermisos();
        } else {
            AlertUtils.mostrarError("Error",
                    "No se pudo procesar el permiso. Intente nuevamente.");
        }
    }

    /**
     * Solicita un nuevo permiso
     */
    private void solicitarPermiso() {
        Empleado empleadoSeleccionado = employeeCombo.getValue();
        LocalDate fechaPermiso = permissionDatePicker.getValue();
        String motivo = reasonTextArea.getText().trim();

        // Validar campos
        if (empleadoSeleccionado == null) {
            AlertUtils.mostrarAdvertencia("Campo Requerido",
                    "Por favor, seleccione un empleado.");
            return;
        }

        if (fechaPermiso == null) {
            AlertUtils.mostrarAdvertencia("Campo Requerido",
                    "Por favor, seleccione una fecha.");
            return;
        }

        if (fechaPermiso.isBefore(LocalDate.now().plusDays(1))) {
            AlertUtils.mostrarAdvertencia("Fecha Inválida",
                    "Los permisos deben solicitarse al menos con un día de anticipación.");
            return;
        }

        if (motivo.isEmpty()) {
            AlertUtils.mostrarAdvertencia("Campo Requerido",
                    "Por favor, ingrese un motivo para el permiso.");
            return;
        }

        // Verificar si ya existe un permiso para esa fecha
        if (permisoDAO.existePermiso(empleadoSeleccionado.getIdEmpleado(), fechaPermiso)) {
            AlertUtils.mostrarAdvertencia("Permiso Duplicado",
                    "Ya existe un permiso para este empleado en la fecha seleccionada.");
            return;
        }

        // Crear y registrar el permiso
        Permiso permiso = new Permiso(empleadoSeleccionado.getIdEmpleado(), fechaPermiso, motivo);

        if (permisoDAO.registrarPermiso(permiso)) {
            AlertUtils.mostrarInfo("Operación Exitosa",
                    "Permiso solicitado correctamente. Quedará pendiente de aprobación.");

            // Limpiar campos
            employeeCombo.setValue(null);
            permissionDatePicker.setValue(LocalDate.now().plusDays(1));
            reasonTextArea.clear();

            // Recargar datos
            cargarPermisosPendientes();
        } else {
            AlertUtils.mostrarError("Error",
                    "No se pudo registrar el permiso. Intente nuevamente.");
        }
    }

    /**
     * Cierra la ventana actual
     */
    private void cerrarVentana() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
