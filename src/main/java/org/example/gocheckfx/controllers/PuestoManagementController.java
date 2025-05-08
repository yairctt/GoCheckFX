package org.example.gocheckfx.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.example.gocheckfx.dao.PuestoDAO;
import org.example.gocheckfx.models.Puesto;
import org.example.gocheckfx.utils.AlertUtils;
import org.json.JSONObject;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador para la gestión de puestos
 */
public class PuestoManagementController implements Initializable {

    @FXML private TableView<Puesto> puestoTable;
    @FXML private TableColumn<Puesto, String> nombreCol;
    @FXML private TableColumn<Puesto, String> descripcionCol;
    @FXML private TableColumn<Puesto, Boolean> sinDesayunoCol;
    @FXML private TableColumn<Puesto, Boolean> combinarDescansoCol;
    @FXML private TableColumn<Puesto, Boolean> dosDescansosCol;

    @FXML private Button newButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button closeButton;

    private PuestoDAO puestoDAO;

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
        puestoDAO = new PuestoDAO();

        // Configurar tabla
        configurarTabla();

        // Cargar datos iniciales
        cargarPuestos();

        // Configurar eventos de botones
        newButton.setOnAction(e -> mostrarDialogoPuesto(null));

        editButton.setOnAction(e -> {
            Puesto puestoSeleccionado = puestoTable.getSelectionModel().getSelectedItem();
            if (puestoSeleccionado != null) {
                mostrarDialogoPuesto(puestoSeleccionado);
            } else {
                AlertUtils.mostrarAdvertencia("Selección Requerida",
                        "Por favor, seleccione un puesto para editar.");
            }
        });

        deleteButton.setOnAction(e -> {
            Puesto puestoSeleccionado = puestoTable.getSelectionModel().getSelectedItem();
            if (puestoSeleccionado != null) {
                eliminarPuesto(puestoSeleccionado);
            } else {
                AlertUtils.mostrarAdvertencia("Selección Requerida",
                        "Por favor, seleccione un puesto para eliminar.");
            }
        });

        closeButton.setOnAction(e -> cerrarVentana());
    }

    /**
     * Configura la tabla de puestos
     */
    private void configurarTabla() {
        nombreCol.setCellValueFactory(new PropertyValueFactory<>("nombrePuesto"));
        descripcionCol.setCellValueFactory(new PropertyValueFactory<>("descripcion"));

        // Configurar columnas especiales basadas en JSON
        sinDesayunoCol.setCellValueFactory(cellData -> {
            Puesto puesto = cellData.getValue();
            boolean valor = !puesto.requiereDesayuno();
            return javafx.beans.binding.Bindings.createObjectBinding(() -> valor);
        });

        combinarDescansoCol.setCellValueFactory(cellData -> {
            Puesto puesto = cellData.getValue();
            boolean valor = puesto.permiteCombinarDescanso();
            return javafx.beans.binding.Bindings.createObjectBinding(() -> valor);
        });

        dosDescansosCol.setCellValueFactory(cellData -> {
            Puesto puesto = cellData.getValue();
            boolean valor = puesto.permiteDosDescansos();
            return javafx.beans.binding.Bindings.createObjectBinding(() -> valor);
        });

        // Permitir selección única
        puestoTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    /**
     * Carga la lista de puestos activos
     */
    private void cargarPuestos() {
        List<Puesto> puestos = puestoDAO.listarPuestosActivos();
        ObservableList<Puesto> data = FXCollections.observableArrayList(puestos);
        puestoTable.setItems(data);
    }

    /**
     * Muestra el diálogo para crear o editar un puesto
     * @param puesto El puesto a editar, o null para crear uno nuevo
     */
    private void mostrarDialogoPuesto(Puesto puesto) {
        boolean esNuevo = (puesto == null);

        // Crear diálogo
        Dialog<Puesto> dialog = new Dialog<>();
        dialog.setTitle(esNuevo ? "Nuevo Puesto" : "Editar Puesto");
        dialog.setHeaderText(esNuevo ? "Crear un nuevo puesto" : "Editar información del puesto");

        // Botones
        ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Formulario
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField nombreField = new TextField();
        TextArea descripcionArea = new TextArea();
        descripcionArea.setPrefRowCount(4);

        // Checkboxes para reglas de descanso
        CheckBox sinDesayunoCheck = new CheckBox("Sin desayuno");
        CheckBox combinarDescansoCheck = new CheckBox("Permitir combinar descansos");
        CheckBox dosDescansosCheck = new CheckBox("Permitir dos descansos");

        // Llenar datos si es edición
        if (!esNuevo) {
            nombreField.setText(puesto.getNombrePuesto());
            descripcionArea.setText(puesto.getDescripcion());

            // Cargar configuración JSON
            sinDesayunoCheck.setSelected(!puesto.requiereDesayuno());
            combinarDescansoCheck.setSelected(puesto.permiteCombinarDescanso());
            dosDescansosCheck.setSelected(puesto.permiteDosDescansos());
        } else {
            // Defaults para nuevo puesto
            combinarDescansoCheck.setSelected(true);
            dosDescansosCheck.setSelected(true);
        }

        // Añadir campos al grid
        grid.add(new Label("Nombre del Puesto:"), 0, 0);
        grid.add(nombreField, 1, 0);

        grid.add(new Label("Descripción:"), 0, 1);
        grid.add(descripcionArea, 1, 1);

        grid.add(new Label("Reglas de Descanso:"), 0, 2);
        grid.add(sinDesayunoCheck, 1, 2);
        grid.add(combinarDescansoCheck, 1, 3);
        grid.add(dosDescansosCheck, 1, 4);

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
                Puesto resultado = esNuevo ? new Puesto() : puesto;

                resultado.setNombrePuesto(nombreField.getText().trim());
                resultado.setDescripcion(descripcionArea.getText().trim());

                // Generar JSON con reglas de descanso
                JSONObject reglasJson = new JSONObject();
                reglasJson.put("sin_desayuno", sinDesayunoCheck.isSelected());
                reglasJson.put("combinar_descanso", combinarDescansoCheck.isSelected());
                reglasJson.put("dos_descansos", dosDescansosCheck.isSelected());

                resultado.setReglasDescansoJSON(reglasJson.toString());
                resultado.setActivo(true);

                return resultado;
            }
            return null;
        });

        // Mostrar diálogo y procesar resultado
        dialog.showAndWait().ifPresent(resultado -> {
            boolean operacionExitosa;

            if (esNuevo) {
                operacionExitosa = puestoDAO.insertar(resultado);
            } else {
                operacionExitosa = puestoDAO.actualizar(resultado);
            }

            if (operacionExitosa) {
                AlertUtils.mostrarInfo("Operación Exitosa",
                        "Puesto " + (esNuevo ? "creado" : "actualizado") + " correctamente.");
                cargarPuestos();
            } else {
                AlertUtils.mostrarError("Error",
                        "No se pudo " + (esNuevo ? "crear" : "actualizar") + " el puesto.");
            }
        });
    }

    /**
     * Elimina lógicamente un puesto (marca como inactivo)
     * @param puesto El puesto a eliminar
     */
    private void eliminarPuesto(Puesto puesto) {
        boolean confirmar = AlertUtils.mostrarConfirmacion("Confirmar Eliminación",
                "¿Está seguro de que desea eliminar el puesto \"" + puesto.getNombrePuesto() + "\"?");

        if (confirmar) {
            if (puestoDAO.eliminar(puesto.getIdPuesto())) {
                AlertUtils.mostrarInfo("Operación Exitosa", "Puesto eliminado correctamente.");
                cargarPuestos();
            } else {
                AlertUtils.mostrarError("Error", "No se pudo eliminar el puesto. Puede que esté en uso por empleados.");
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
