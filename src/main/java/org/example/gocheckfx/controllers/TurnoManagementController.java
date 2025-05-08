package org.example.gocheckfx.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.example.gocheckfx.dao.TurnoDAO;
import org.example.gocheckfx.models.Turno;
import org.example.gocheckfx.utils.AlertUtils;

import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador para la gestión de turnos
 */
public class TurnoManagementController implements Initializable {

    @FXML private TableView<Turno> turnoTable;
    @FXML private TableColumn<Turno, String> nombreCol;
    @FXML private TableColumn<Turno, String> entradaCol;
    @FXML private TableColumn<Turno, String> salidaCol;
    @FXML private TableColumn<Turno, Integer> desayunoCol;
    @FXML private TableColumn<Turno, Integer> comidaCol;
    @FXML private TableColumn<Turno, Boolean> combinarCol;

    @FXML private Button newButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button closeButton;

    private TurnoDAO turnoDAO;
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

        // Inicializar DAO
        turnoDAO = new TurnoDAO();

        // Configurar formateador de tiempo
        timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        // Configurar tabla
        configurarTabla();

        // Cargar datos iniciales
        cargarTurnos();

        // Configurar eventos de botones
        newButton.setOnAction(e -> mostrarDialogoTurno(null));

        editButton.setOnAction(e -> {
            Turno turnoSeleccionado = turnoTable.getSelectionModel().getSelectedItem();
            if (turnoSeleccionado != null) {
                mostrarDialogoTurno(turnoSeleccionado);
            } else {
                AlertUtils.mostrarAdvertencia("Selección Requerida",
                        "Por favor, seleccione un turno para editar.");
            }
        });

        deleteButton.setOnAction(e -> {
            Turno turnoSeleccionado = turnoTable.getSelectionModel().getSelectedItem();
            if (turnoSeleccionado != null) {
                eliminarTurno(turnoSeleccionado);
            } else {
                AlertUtils.mostrarAdvertencia("Selección Requerida",
                        "Por favor, seleccione un turno para eliminar.");
            }
        });

        closeButton.setOnAction(e -> cerrarVentana());
    }

    /**
     * Configura la tabla de turnos
     */
    private void configurarTabla() {
        nombreCol.setCellValueFactory(new PropertyValueFactory<>("nombreTurno"));

        entradaCol.setCellValueFactory(cellData -> {
            Turno turno = cellData.getValue();
            String valor = turno.getHoraEntrada().format(timeFormatter);
            return javafx.beans.binding.Bindings.createStringBinding(() -> valor);
        });

        salidaCol.setCellValueFactory(cellData -> {
            Turno turno = cellData.getValue();
            String valor = turno.getHoraSalida().format(timeFormatter);
            return javafx.beans.binding.Bindings.createStringBinding(() -> valor);
        });

        desayunoCol.setCellValueFactory(new PropertyValueFactory<>("duracionDesayuno"));
        comidaCol.setCellValueFactory(new PropertyValueFactory<>("duracionComida"));
        combinarCol.setCellValueFactory(new PropertyValueFactory<>("permiteCombinarDescanso"));

        // Permitir selección única
        turnoTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    /**
     * Carga la lista de turnos
     */
    private void cargarTurnos() {
        List<Turno> turnos = turnoDAO.listarTurnos();
        ObservableList<Turno> data = FXCollections.observableArrayList(turnos);
        turnoTable.setItems(data);
    }

    /**
     * Muestra el diálogo para crear o editar un turno
     * @param turno El turno a editar, o null para crear uno nuevo
     */
    private void mostrarDialogoTurno(Turno turno) {
        boolean esNuevo = (turno == null);

        // Crear diálogo
        Dialog<Turno> dialog = new Dialog<>();
        dialog.setTitle(esNuevo ? "Nuevo Turno" : "Editar Turno");
        dialog.setHeaderText(esNuevo ? "Crear un nuevo turno" : "Editar información del turno");

        // Botones
        ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Formulario
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField nombreField = new TextField();

        // Spinners para tiempo
        Spinner<Integer> horaEntradaSpinner = new Spinner<>(0, 23, 8);
        Spinner<Integer> minEntradaSpinner = new Spinner<>(0, 59, 0);
        Spinner<Integer> horaSalidaSpinner = new Spinner<>(0, 23, 17);
        Spinner<Integer> minSalidaSpinner = new Spinner<>(0, 59, 0);

        // Configurar spinners
        configurarSpinner(horaEntradaSpinner);
        configurarSpinner(minEntradaSpinner);
        configurarSpinner(horaSalidaSpinner);
        configurarSpinner(minSalidaSpinner);

        Spinner<Integer> desayunoSpinner = new Spinner<>(0, 120, 30);
        Spinner<Integer> comidaSpinner = new Spinner<>(0, 120, 60);
        CheckBox combinarCheck = new CheckBox("Permitir combinar descansos");

        configurarSpinner(desayunoSpinner);
        configurarSpinner(comidaSpinner);

        // Llenar datos si es edición
        if (!esNuevo) {
            nombreField.setText(turno.getNombreTurno());

            LocalTime horaEntrada = turno.getHoraEntrada();
            horaEntradaSpinner.getValueFactory().setValue(horaEntrada.getHour());
            minEntradaSpinner.getValueFactory().setValue(horaEntrada.getMinute());

            LocalTime horaSalida = turno.getHoraSalida();
            horaSalidaSpinner.getValueFactory().setValue(horaSalida.getHour());
            minSalidaSpinner.getValueFactory().setValue(horaSalida.getMinute());

            desayunoSpinner.getValueFactory().setValue(turno.getDuracionDesayuno());
            comidaSpinner.getValueFactory().setValue(turno.getDuracionComida());
            combinarCheck.setSelected(turno.isPermiteCombinarDescanso());
        }

        // Añadir campos al grid
        grid.add(new Label("Nombre del Turno:"), 0, 0);
        grid.add(nombreField, 1, 0);

        // Hora de entrada (con HBox para los spinners)
        grid.add(new Label("Hora de Entrada:"), 0, 1);
        HBox entradaBox = new HBox(5);
        entradaBox.getChildren().addAll(
                horaEntradaSpinner,
                new Label(":"),
                minEntradaSpinner
        );
        grid.add(entradaBox, 1, 1);

        // Hora de salida (con HBox para los spinners)
        grid.add(new Label("Hora de Salida:"), 0, 2);
        HBox salidaBox = new HBox(5);
        salidaBox.getChildren().addAll(
                horaSalidaSpinner,
                new Label(":"),
                minSalidaSpinner
        );
        grid.add(salidaBox, 1, 2);

        grid.add(new Label("Duración Desayuno (min):"), 0, 3);
        grid.add(desayunoSpinner, 1, 3);

        grid.add(new Label("Duración Comida (min):"), 0, 4);
        grid.add(comidaSpinner, 1, 4);

        grid.add(combinarCheck, 1, 5);

        dialog.getDialogPane().setContent(grid);

        // Habilitar/deshabilitar botón de guardar según validación
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        // Validar campos obligatorios
        nombreField.textProperty().addListener((observable, oldValue, newValue) ->
                saveButton.setDisable(newValue.trim().isEmpty()));

        // Convertir el resultado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Turno resultado = esNuevo ? new Turno() : turno;

                resultado.setNombreTurno(nombreField.getText().trim());

                LocalTime horaEntrada = LocalTime.of(
                        horaEntradaSpinner.getValue(),
                        minEntradaSpinner.getValue()
                );
                resultado.setHoraEntrada(horaEntrada);

                LocalTime horaSalida = LocalTime.of(
                        horaSalidaSpinner.getValue(),
                        minSalidaSpinner.getValue()
                );
                resultado.setHoraSalida(horaSalida);

                resultado.setDuracionDesayuno(desayunoSpinner.getValue());
                resultado.setDuracionComida(comidaSpinner.getValue());
                resultado.setPermiteCombinarDescanso(combinarCheck.isSelected());

                return resultado;
            }
            return null;
        });

        // Mostrar diálogo y procesar resultado
        dialog.showAndWait().ifPresent(resultado -> {
            boolean operacionExitosa;

            if (esNuevo) {
                operacionExitosa = turnoDAO.insertar(resultado);
            } else {
                operacionExitosa = turnoDAO.actualizar(resultado);
            }

            if (operacionExitosa) {
                AlertUtils.mostrarInfo("Operación Exitosa",
                        "Turno " + (esNuevo ? "creado" : "actualizado") + " correctamente.");
                cargarTurnos();
            } else {
                AlertUtils.mostrarError("Error",
                        "No se pudo " + (esNuevo ? "crear" : "actualizar") + " el turno.");
            }
        });
    }

    /**
     * Configura un spinner para que sea editable y tenga el formato correcto
     */
    private void configurarSpinner(Spinner<Integer> spinner) {
        spinner.setEditable(true);
        spinner.setPrefWidth(80);

        // Formatear el valor si es editable
        TextFormatter<Integer> formatter = new TextFormatter<>(
                change -> {
                    String newText = change.getControlNewText();
                    if (newText.matches("\\d*")) {
                        return change;
                    }
                    return null;
                }
        );

        spinner.getEditor().setTextFormatter(formatter);
    }

    /**
     * Elimina un turno
     * @param turno El turno a eliminar
     */
    private void eliminarTurno(Turno turno) {
        boolean confirmar = AlertUtils.mostrarConfirmacion("Confirmar Eliminación",
                "¿Está seguro de que desea eliminar el turno \"" + turno.getNombreTurno() + "\"?");

        if (confirmar) {
            if (turnoDAO.eliminar(turno.getIdTurno())) {
                AlertUtils.mostrarInfo("Operación Exitosa", "Turno eliminado correctamente.");
                cargarTurnos();
            } else {
                AlertUtils.mostrarError("Error", "No se pudo eliminar el turno. Puede que esté en uso por empleados.");
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
