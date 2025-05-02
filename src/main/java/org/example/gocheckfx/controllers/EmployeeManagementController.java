package org.example.gocheckfx.controllers;

import org.example.gocheckfx.App;
import org.example.gocheckfx.dao.EmpleadoDAO;
import org.example.gocheckfx.dao.PuestoDAO;
import org.example.gocheckfx.dao.TurnoDAO;
import org.example.gocheckfx.models.Empleado;
import org.example.gocheckfx.models.Puesto;
import org.example.gocheckfx.models.Turno;
import org.example.gocheckfx.utils.AlertUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador para la pantalla de gestión de empleados.
 */
public class EmployeeManagementController implements Initializable {

    @FXML private TableView<Empleado> employeeTable;
    @FXML private TableColumn<Empleado, String> codeCol;
    @FXML private TableColumn<Empleado, String> nameCol;
    @FXML private TableColumn<Empleado, String> lastNameCol;
    @FXML private TableColumn<Empleado, String> positionCol;
    @FXML private TableColumn<Empleado, String> shiftCol;
    @FXML private TableColumn<Empleado, String> emailCol;
    @FXML private TableColumn<Empleado, String> phoneCol;

    @FXML private Button newButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button viewCodesButton; // Nuevo botón para ver códigos
    @FXML private Button closeButton;

    private EmpleadoDAO empleadoDAO;
    private PuestoDAO puestoDAO;
    private TurnoDAO turnoDAO;

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
        empleadoDAO = new EmpleadoDAO();
        puestoDAO = new PuestoDAO();
        turnoDAO = new TurnoDAO();

        // Configurar tabla
        configurarTabla();

        // Cargar datos iniciales
        cargarEmpleados();

        // Configurar eventos de botones
        newButton.setOnAction(e -> mostrarDialogoEmpleado(null));

        editButton.setOnAction(e -> {
            Empleado empleadoSeleccionado = employeeTable.getSelectionModel().getSelectedItem();
            if (empleadoSeleccionado != null) {
                mostrarDialogoEmpleado(empleadoSeleccionado);
            } else {
                AlertUtils.mostrarAdvertencia("Selección Requerida",
                        "Por favor, seleccione un empleado para editar.");
            }
        });

        deleteButton.setOnAction(e -> {
            Empleado empleadoSeleccionado = employeeTable.getSelectionModel().getSelectedItem();
            if (empleadoSeleccionado != null) {
                eliminarEmpleado(empleadoSeleccionado);
            } else {
                AlertUtils.mostrarAdvertencia("Selección Requerida",
                        "Por favor, seleccione un empleado para eliminar.");
            }
        });

        // Nuevo botón para ver códigos
        viewCodesButton.setOnAction(e -> {
            Empleado empleadoSeleccionado = employeeTable.getSelectionModel().getSelectedItem();
            if (empleadoSeleccionado != null) {
                abrirVistaCodigosEmpleado(empleadoSeleccionado);
            } else {
                AlertUtils.mostrarAdvertencia("Selección Requerida",
                        "Por favor, seleccione un empleado para ver sus códigos.");
            }
        });

        closeButton.setOnAction(e -> cerrarVentana());
    }

    /**
     * Abre la vista de códigos preseleccionando el empleado seleccionado
     * @param empleado El empleado seleccionado
     */
    private void abrirVistaCodigosEmpleado(Empleado empleado) {
        // En una implementación completa, podríamos pasar el empleado seleccionado
        // directamente a la vista de códigos. Para simplificar, solo abrimos la vista
        // general de códigos.
        App.openWindow("/org/example/gocheckfx/codigos.fxml", "GoCheck - Códigos de " + empleado.getNombreCompleto());

        // Alternativa: Generar y mostrar los códigos directamente
        String[] rutas = empleadoDAO.obtenerRutasCodigos(empleado.getCodigoUnico());

        if (rutas != null) {
            // Mostrar un diálogo con los códigos
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Códigos de " + empleado.getNombreCompleto());
            alert.setHeaderText("Códigos generados correctamente");
            alert.setContentText("Los códigos QR y de barras se han generado y guardado en:\n" +
                    "- QR: " + rutas[0] + "\n" +
                    "- Barras: " + rutas[1]);

            // Añadir botón para abrir la vista completa de códigos
            ButtonType verCodigosButton = new ButtonType("Ver Códigos");
            alert.getButtonTypes().add(verCodigosButton);

            alert.showAndWait().ifPresent(buttonType -> {
                if (buttonType == verCodigosButton) {
                    App.openWindow("/org/example/gocheckfx/codigos.fxml", "GoCheck - Códigos");
                }
            });
        } else {
            AlertUtils.mostrarError("Error", "No se pudieron generar los códigos para el empleado.");
        }
    }

    /**
     * Configura la tabla de empleados
     */
    private void configurarTabla() {
        codeCol.setCellValueFactory(new PropertyValueFactory<>("codigoUnico"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        positionCol.setCellValueFactory(new PropertyValueFactory<>("nombrePuesto"));
        shiftCol.setCellValueFactory(new PropertyValueFactory<>("nombreTurno"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("telefono"));

        // Permitir selección única
        employeeTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    /**
     * Carga la lista de empleados activos
     */
    private void cargarEmpleados() {
        List<Empleado> empleados = empleadoDAO.listarEmpleadosActivos();
        ObservableList<Empleado> data = FXCollections.observableArrayList(empleados);
        employeeTable.setItems(data);
    }

    /**
     * Muestra el diálogo para crear o editar un empleado
     * @param empleado El empleado a editar, o null para crear uno nuevo
     */
    private void mostrarDialogoEmpleado(Empleado empleado) {
        boolean esNuevo = (empleado == null);

        // Crear diálogo
        Dialog<Empleado> dialog = new Dialog<>();
        dialog.setTitle(esNuevo ? "Nuevo Empleado" : "Editar Empleado");
        dialog.setHeaderText(esNuevo ? "Crear un nuevo empleado" : "Editar información del empleado");

        // Botones
        ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Formulario
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField codeField = new TextField();
        TextField nameField = new TextField();
        TextField lastNameField = new TextField();
        ComboBox<Puesto> positionCombo = new ComboBox<>();
        ComboBox<Turno> shiftCombo = new ComboBox<>();
        TextField emailField = new TextField();
        TextField phoneField = new TextField();
        DatePicker hireDatePicker = new DatePicker();

        // Cargar datos de combos
        List<Puesto> puestos = puestoDAO.listarPuestosActivos();
        positionCombo.setItems(FXCollections.observableArrayList(puestos));

        List<Turno> turnos = turnoDAO.listarTurnos();
        shiftCombo.setItems(FXCollections.observableArrayList(turnos));

        // Llenar datos si es edición
        if (!esNuevo) {
            codeField.setText(empleado.getCodigoUnico());
            nameField.setText(empleado.getNombre());
            lastNameField.setText(empleado.getApellido());
            emailField.setText(empleado.getEmail());
            phoneField.setText(empleado.getTelefono());

            if (empleado.getFechaContratacion() != null) {
                hireDatePicker.setValue(empleado.getFechaContratacion());
            }

            // Seleccionar puesto y turno actuales
            for (Puesto p : puestos) {
                if (p.getIdPuesto() == empleado.getIdPuesto()) {
                    positionCombo.setValue(p);
                    break;
                }
            }

            for (Turno t : turnos) {
                if (t.getIdTurno() == empleado.getIdTurno()) {
                    shiftCombo.setValue(t);
                    break;
                }
            }
        } else {
            // Valores por defecto para nuevo empleado
            hireDatePicker.setValue(LocalDate.now());
        }

        // Añadir campos al grid
        grid.add(new Label("Código:"), 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(new Label("Nombre:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Apellido:"), 0, 2);
        grid.add(lastNameField, 1, 2);
        grid.add(new Label("Puesto:"), 0, 3);
        grid.add(positionCombo, 1, 3);
        grid.add(new Label("Turno:"), 0, 4);
        grid.add(shiftCombo, 1, 4);
        grid.add(new Label("Email:"), 0, 5);
        grid.add(emailField, 1, 5);
        grid.add(new Label("Teléfono:"), 0, 6);
        grid.add(phoneField, 1, 6);
        grid.add(new Label("Fecha Contratación:"), 0, 7);
        grid.add(hireDatePicker, 1, 7);

        dialog.getDialogPane().setContent(grid);

        // Habilitar/deshabilitar botón de guardar según validación
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        // Validar campos obligatorios
        Runnable validar = () -> {
            boolean valido = !codeField.getText().trim().isEmpty() &&
                    !nameField.getText().trim().isEmpty() &&
                    !lastNameField.getText().trim().isEmpty() &&
                    positionCombo.getValue() != null &&
                    shiftCombo.getValue() != null;

            saveButton.setDisable(!valido);
        };

        codeField.textProperty().addListener((observable, oldValue, newValue) -> validar.run());
        nameField.textProperty().addListener((observable, oldValue, newValue) -> validar.run());
        lastNameField.textProperty().addListener((observable, oldValue, newValue) -> validar.run());
        positionCombo.valueProperty().addListener((observable, oldValue, newValue) -> validar.run());
        shiftCombo.valueProperty().addListener((observable, oldValue, newValue) -> validar.run());

        // Convertir el resultado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Empleado resultado = esNuevo ? new Empleado() : empleado;

                resultado.setCodigoUnico(codeField.getText().trim());
                resultado.setNombre(nameField.getText().trim());
                resultado.setApellido(lastNameField.getText().trim());
                resultado.setIdPuesto(positionCombo.getValue().getIdPuesto());
                resultado.setIdTurno(shiftCombo.getValue().getIdTurno());
                resultado.setEmail(emailField.getText().trim());
                resultado.setTelefono(phoneField.getText().trim());
                resultado.setFechaContratacion(hireDatePicker.getValue());
                resultado.setActivo(true);

                // Actualizar campos relacionados (para mostrar en la tabla)
                resultado.setNombrePuesto(positionCombo.getValue().getNombrePuesto());
                resultado.setNombreTurno(shiftCombo.getValue().getNombreTurno());

                return resultado;
            }
            return null;
        });

        // Mostrar diálogo y procesar resultado
        dialog.showAndWait().ifPresent(resultado -> {
            boolean operacionExitosa;

            if (esNuevo) {
                operacionExitosa = empleadoDAO.insertar(resultado);
            } else {
                operacionExitosa = empleadoDAO.actualizar(resultado);
            }

            if (operacionExitosa) {
                AlertUtils.mostrarInfo("Operación Exitosa",
                        "Empleado " + (esNuevo ? "creado" : "actualizado") + " correctamente.");
                cargarEmpleados();
            } else {
                AlertUtils.mostrarError("Error",
                        "No se pudo " + (esNuevo ? "crear" : "actualizar") + " el empleado.");
            }
        });
    }

    /**
     * Elimina lógicamente un empleado (marca como inactivo)
     * @param empleado El empleado a eliminar
     */
    private void eliminarEmpleado(Empleado empleado) {
        boolean confirmar = AlertUtils.mostrarConfirmacion("Confirmar Eliminación",
                "¿Está seguro de que desea eliminar al empleado " + empleado.getNombreCompleto() + "?");

        if (confirmar) {
            if (empleadoDAO.eliminar(empleado.getIdEmpleado())) {
                AlertUtils.mostrarInfo("Operación Exitosa", "Empleado eliminado correctamente.");
                cargarEmpleados();
            } else {
                AlertUtils.mostrarError("Error", "No se pudo eliminar el empleado.");
            }
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
