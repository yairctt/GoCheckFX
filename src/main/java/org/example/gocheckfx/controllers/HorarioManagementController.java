package org.example.gocheckfx.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.gocheckfx.dao.EmpleadoDAO;
import org.example.gocheckfx.dao.HorarioEmpleadoDAO;
import org.example.gocheckfx.models.Empleado;
import org.example.gocheckfx.models.HorarioEmpleado;
import org.example.gocheckfx.utils.AlertUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador para la gestión de horarios de empleados
 */
public class HorarioManagementController implements Initializable {

    @FXML private TableView<HorarioEmpleado> horarioTable;
    @FXML private TableColumn<HorarioEmpleado, String> empleadoCol;
    @FXML private TableColumn<HorarioEmpleado, String> diasLaborablesCol;
    @FXML private TableColumn<HorarioEmpleado, String> diaDescansoCol;

    @FXML private Button newButton;
    @FXML private Button editButton;
    @FXML private Button closeButton;

    private HorarioEmpleadoDAO horarioDAO;
    private EmpleadoDAO empleadoDAO;

    // Días de la semana para selección
    private final String[] diasSemana = {"lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo"};

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
        horarioDAO = new HorarioEmpleadoDAO();
        empleadoDAO = new EmpleadoDAO();

        // Configurar tabla
        configurarTabla();

        // Cargar datos iniciales
        cargarHorarios();

        // Configurar eventos de botones
        newButton.setOnAction(e -> mostrarDialogoHorario(null));

        editButton.setOnAction(e -> {
            HorarioEmpleado horarioSeleccionado = horarioTable.getSelectionModel().getSelectedItem();
            if (horarioSeleccionado != null) {
                mostrarDialogoHorario(horarioSeleccionado);
            } else {
                AlertUtils.mostrarAdvertencia("Selección Requerida",
                        "Por favor, seleccione un horario para editar.");
            }
        });

        closeButton.setOnAction(e -> cerrarVentana());
    }

    /**
     * Configura la tabla de horarios
     */
    private void configurarTabla() {
        empleadoCol.setCellValueFactory(new PropertyValueFactory<>("nombreEmpleado"));
        diasLaborablesCol.setCellValueFactory(cellData -> {
            HorarioEmpleado horario = cellData.getValue();
            String diasStr = horario.getDiasLaborablesString();
            return javafx.beans.binding.Bindings.createStringBinding(() -> diasStr);
        });
        diaDescansoCol.setCellValueFactory(new PropertyValueFactory<>("diaDescansoSemanal"));

        // Permitir selección única
        horarioTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    /**
     * Carga la lista de horarios
     */
    private void cargarHorarios() {
        List<HorarioEmpleado> horarios = horarioDAO.listarHorarios();
        ObservableList<HorarioEmpleado> data = FXCollections.observableArrayList(horarios);
        horarioTable.setItems(data);
    }

    /**
     * Muestra el diálogo para crear o editar un horario
     * @param horario El horario a editar, o null para crear uno nuevo
     */
    private void mostrarDialogoHorario(HorarioEmpleado horario) {
        boolean esNuevo = (horario == null);

        // Crear diálogo
        Dialog<HorarioEmpleado> dialog = new Dialog<>();
        dialog.setTitle(esNuevo ? "Nuevo Horario" : "Editar Horario");
        dialog.setHeaderText(esNuevo ? "Asignar horario a un empleado" : "Editar horario del empleado");

        // Botones
        ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Formulario
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        // ComboBox para seleccionar empleado
        ComboBox<Empleado> empleadoCombo = new ComboBox<>();
        empleadoCombo.setPromptText("Seleccione un empleado");

        // Checkboxes para días laborables
        VBox diasLaborablesBox = new VBox(5);
        List<CheckBox> diasCheckboxes = new ArrayList<>();

        for (String dia : diasSemana) {
            CheckBox checkbox = new CheckBox(dia);
            diasCheckboxes.add(checkbox);
            diasLaborablesBox.getChildren().add(checkbox);
        }

        // ComboBox para día de descanso
        ComboBox<String> diaDescansoCombo = new ComboBox<>();
        diaDescansoCombo.getItems().addAll(diasSemana);
        diaDescansoCombo.setPromptText("Seleccione día de descanso");

        // Cargar empleados para selección
        List<Empleado> empleados = empleadoDAO.listarEmpleadosActivos();
        empleadoCombo.setItems(FXCollections.observableArrayList(empleados));

        // Llenar datos si es edición
        if (!esNuevo) {
            // Seleccionar empleado
            for (Empleado empleado : empleados) {
                if (empleado.getIdEmpleado() == horario.getIdEmpleado()) {
                    empleadoCombo.setValue(empleado);
                    break;
                }
            }

            // Marcar días laborables
            List<String> diasLaborables = horario.getDiasLaborables();
            for (CheckBox checkBox : diasCheckboxes) {
                checkBox.setSelected(diasLaborables.contains(checkBox.getText()));
            }

            // Seleccionar día de descanso
            diaDescansoCombo.setValue(horario.getDiaDescansoSemanal());

            // Deshabilitar selección de empleado en modo edición
            empleadoCombo.setDisable(true);
        }

        // Añadir campos al grid
        grid.add(new Label("Empleado:"), 0, 0);
        grid.add(empleadoCombo, 1, 0);

        grid.add(new Label("Días Laborables:"), 0, 1);
        grid.add(diasLaborablesBox, 1, 1);

        grid.add(new Label("Día de Descanso:"), 0, 2);
        grid.add(diaDescansoCombo, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Habilitar/deshabilitar botón de guardar según validación
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(esNuevo); // Deshabilitar inicialmente si es nuevo (hasta seleccionar empleado)

        // Validar campos obligatorios
        empleadoCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            boolean haySeleccion = newValue != null;
            saveButton.setDisable(!haySeleccion);
        });

        // Convertir el resultado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                HorarioEmpleado resultado = esNuevo ? new HorarioEmpleado() : horario;

                // Establecer el ID del empleado
                if (esNuevo) {
                    Empleado empleadoSeleccionado = empleadoCombo.getValue();
                    resultado.setIdEmpleado(empleadoSeleccionado.getIdEmpleado());
                    resultado.setNombreEmpleado(empleadoSeleccionado.getNombreCompleto());
                }

                // Recopilar días laborables seleccionados
                List<String> diasLaborablesSeleccionados = new ArrayList<>();
                for (CheckBox checkBox : diasCheckboxes) {
                    if (checkBox.isSelected()) {
                        diasLaborablesSeleccionados.add(checkBox.getText());
                    }
                }
                resultado.setDiasLaborables(diasLaborablesSeleccionados);

                // Establecer día de descanso
                resultado.setDiaDescansoSemanal(diaDescansoCombo.getValue());

                return resultado;
            }
            return null;
        });

        // Mostrar diálogo y procesar resultado
        dialog.showAndWait().ifPresent(resultado -> {
            boolean operacionExitosa = horarioDAO.guardar(resultado);

            if (operacionExitosa) {
                AlertUtils.mostrarInfo("Operación Exitosa",
                        "Horario " + (esNuevo ? "creado" : "actualizado") + " correctamente.");
                cargarHorarios();
            } else {
                AlertUtils.mostrarError("Error",
                        "No se pudo " + (esNuevo ? "crear" : "actualizar") + " el horario.");
            }
        });
    }

    /**
     * Cierra la ventana actual
     */
    private void cerrarVentana() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}