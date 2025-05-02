package org.example.gocheckfx.controllers;

import org.example.gocheckfx.App;
import org.example.gocheckfx.dao.AsistenciaDAO;
import org.example.gocheckfx.models.Asistencia;
import org.example.gocheckfx.models.Usuario;
import org.example.gocheckfx.utils.AlertUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador para el panel de administración principal.
 */
public class DashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label dateLabel;
    @FXML private TabPane mainTabPane;

    // Tab de Asistencias en tiempo real
    @FXML private Tab realTimeTab;
    @FXML private TableView<Asistencia> realTimeTable;
    @FXML private TableColumn<Asistencia, String> rtEmployeeCol;
    @FXML private TableColumn<Asistencia, String> rtCodeCol;
    @FXML private TableColumn<Asistencia, String> rtStatusCol;
    @FXML private TableColumn<Asistencia, String> rtEntryCol;
    @FXML private TableColumn<Asistencia, String> rtExitCol;
    @FXML private TableColumn<Asistencia, String> rtBreak1Col;
    @FXML private TableColumn<Asistencia, String> rtBreak2Col;
    @FXML private Button refreshRealTimeButton;

    // Botones de navegación
    @FXML private Button employeesButton;
    @FXML private Button shiftsButton;
    @FXML private Button positionsButton;
    @FXML private Button reportsButton;
    @FXML private Button codigosButton; // Nuevo botón para gestión de códigos
    @FXML private Button logoutButton;

    private AsistenciaDAO asistenciaDAO;
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

        // Configurar formateadores
        timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        // Mostrar información de bienvenida
        Usuario usuario = AdminLoginController.getUsuarioAutenticado();
        welcomeLabel.setText("Bienvenido, " + usuario.getNombreEmpleado());
        dateLabel.setText("Fecha: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        // Configurar tabla de asistencias en tiempo real
        configurarTablaRealTime();

        // Cargar datos iniciales
        cargarAsistenciasHoy();

        // Configurar eventos de botones
        refreshRealTimeButton.setOnAction(e -> cargarAsistenciasHoy());

        employeesButton.setOnAction(e ->
                App.openWindow("/org/example/gocheckfx/employee_management.fxml", "GoCheck - Gestión de Empleados"));

        shiftsButton.setOnAction(e ->
                AlertUtils.mostrarInfo("Función en Desarrollo",
                        "La gestión de turnos estará disponible próximamente."));

        positionsButton.setOnAction(e ->
                AlertUtils.mostrarInfo("Función en Desarrollo",
                        "La gestión de puestos estará disponible próximamente."));

        reportsButton.setOnAction(e ->
                App.openWindow("/org/example/gocheckfx/reports.fxml", "GoCheck - Reportes"));

        // Nuevo botón para gestión de códigos
        codigosButton.setOnAction(e ->
                App.openWindow("/org/example/gocheckfx/codigos.fxml", "GoCheck - Gestión de Códigos QR/Barras"));

        logoutButton.setOnAction(e -> {
            AdminLoginController.cerrarSesion();
            cerrarVentana();
        });
    }

    /**
     * Configura la tabla de asistencias en tiempo real
     */
    private void configurarTablaRealTime() {
        // Configurar columnas
        rtEmployeeCol.setCellValueFactory(new PropertyValueFactory<>("nombreEmpleado"));
        rtCodeCol.setCellValueFactory(new PropertyValueFactory<>("codigoEmpleado"));
        rtStatusCol.setCellValueFactory(new PropertyValueFactory<>("estado"));

        // Columnas de horarios
        rtEntryCol.setCellValueFactory(cellData -> {
            Asistencia asistencia = cellData.getValue();
            String valor = asistencia.getHoraEntrada() != null ?
                    asistencia.getHoraEntrada().format(timeFormatter) : "---";
            return javafx.beans.binding.Bindings.createStringBinding(() -> valor);
        });

        rtExitCol.setCellValueFactory(cellData -> {
            Asistencia asistencia = cellData.getValue();
            String valor = asistencia.getHoraSalida() != null ?
                    asistencia.getHoraSalida().format(timeFormatter) : "---";
            return javafx.beans.binding.Bindings.createStringBinding(() -> valor);
        });

        rtBreak1Col.setCellValueFactory(cellData -> {
            Asistencia asistencia = cellData.getValue();
            String inicioDescanso = asistencia.getInicioDescanso1() != null ?
                    asistencia.getInicioDescanso1().format(timeFormatter) : "---";
            String finDescanso = asistencia.getFinDescanso1() != null ?
                    asistencia.getFinDescanso1().format(timeFormatter) : "---";
            String valor = inicioDescanso + " - " + finDescanso;

            return javafx.beans.binding.Bindings.createStringBinding(() -> valor);
        });

        rtBreak2Col.setCellValueFactory(cellData -> {
            Asistencia asistencia = cellData.getValue();
            String inicioDescanso = asistencia.getInicioDescanso2() != null ?
                    asistencia.getInicioDescanso2().format(timeFormatter) : "---";
            String finDescanso = asistencia.getFinDescanso2() != null ?
                    asistencia.getFinDescanso2().format(timeFormatter) : "---";
            String valor = inicioDescanso + " - " + finDescanso;

            return javafx.beans.binding.Bindings.createStringBinding(() -> valor);
        });

        // Configurar menú contextual para justificar faltas/retardos
        realTimeTable.setRowFactory(tv -> {
            TableRow<Asistencia> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();

            MenuItem justificarItem = new MenuItem("Justificar");
            justificarItem.setOnAction(e -> {
                Asistencia asistencia = row.getItem();
                if (asistencia != null &&
                        (asistencia.getEstado().equals("FALTA") || asistencia.getEstado().equals("RETARDO"))) {
                    mostrarDialogoJustificacion(asistencia);
                }
            });

            contextMenu.getItems().add(justificarItem);

            // Solo mostrar el menú si la fila no es null y es una falta o retardo
            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );

            return row;
        });
    }

    /**
     * Carga las asistencias del día actual
     */
    private void cargarAsistenciasHoy() {
        List<Asistencia> asistencias = asistenciaDAO.obtenerAsistenciasPorFecha(LocalDate.now());
        ObservableList<Asistencia> data = FXCollections.observableArrayList(asistencias);
        realTimeTable.setItems(data);
    }

    /**
     * Muestra un diálogo para justificar una falta o retardo
     * @param asistencia La asistencia a justificar
     */
    private void mostrarDialogoJustificacion(Asistencia asistencia) {
        // Crear elementos del diálogo
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Justificar " + asistencia.getEstado());
        dialog.setHeaderText("Justificar asistencia de " + asistencia.getNombreEmpleado());

        // Botones
        ButtonType justificarButtonType = new ButtonType("Justificar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(justificarButtonType, ButtonType.CANCEL);

        // Contenido
        VBox content = new VBox(10);
        Label motivoLabel = new Label("Motivo de la justificación:");
        TextArea motivoArea = new TextArea();
        motivoArea.setPrefRowCount(4);
        content.getChildren().addAll(motivoLabel, motivoArea);

        dialog.getDialogPane().setContent(content);

        // Habilitar/deshabilitar botón de justificar según si hay texto
        Button okButton = (Button) dialog.getDialogPane().lookupButton(justificarButtonType);
        okButton.setDisable(true);
        motivoArea.textProperty().addListener((observable, oldValue, newValue) -> {
            okButton.setDisable(newValue.trim().isEmpty());
        });

        // Convertir el resultado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == justificarButtonType) {
                return motivoArea.getText();
            }
            return null;
        });

        // Mostrar diálogo y procesar resultado
        dialog.showAndWait().ifPresent(motivo -> {
            Usuario admin = AdminLoginController.getUsuarioAutenticado();

            if (asistenciaDAO.justificarAsistencia(asistencia.getIdAsistencia(), motivo, admin.getIdEmpleado())) {
                AlertUtils.mostrarInfo("Justificación Registrada",
                        "Se ha justificado la asistencia correctamente.");

                // Recargar datos
                cargarAsistenciasHoy();
            } else {
                AlertUtils.mostrarError("Error al Justificar",
                        "No se pudo registrar la justificación. Intente nuevamente.");
            }
        });
    }

    /**
     * Cierra la ventana actual
     */
    private void cerrarVentana() {
        Stage stage = (Stage) mainTabPane.getScene().getWindow();
        stage.close();
    }
}
