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
import org.example.gocheckfx.dao.UsuarioDAO;
import org.example.gocheckfx.models.Empleado;
import org.example.gocheckfx.models.Usuario;
import org.example.gocheckfx.utils.AlertUtils;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador para la gestión de usuarios del sistema
 */
public class UsuarioManagementController implements Initializable {

    @FXML private TableView<Usuario> usuarioTable;
    @FXML private TableColumn<Usuario, String> usernameCol;
    @FXML private TableColumn<Usuario, String> empleadoCol;
    @FXML private TableColumn<Usuario, Boolean> adminCol;

    @FXML private Button newButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button resetPasswordButton;
    @FXML private Button closeButton;

    private UsuarioDAO usuarioDAO;
    private EmpleadoDAO empleadoDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Verificar que haya un usuario autenticado
        if (!AdminLoginController.hayUsuarioAutenticado()) {
            AlertUtils.mostrarError("Error de Sesión",
                    "No hay una sesión activa. Por favor, inicie sesión.");
            cerrarVentana();
            return;
        }

        // Verificar que el usuario sea administrador
        if (!AdminLoginController.getUsuarioAutenticado().isEsAdmin()) {
            AlertUtils.mostrarError("Acceso Denegado",
                    "Solo los administradores pueden acceder a esta función.");
            cerrarVentana();
            return;
        }

        // Inicializar DAOs
        usuarioDAO = new UsuarioDAO();
        empleadoDAO = new EmpleadoDAO();

        // Configurar tabla
        configurarTabla();

        // Cargar datos iniciales
        cargarUsuarios();

        // Configurar eventos de botones
        newButton.setOnAction(e -> mostrarDialogoUsuario(null));

        editButton.setOnAction(e -> {
            Usuario usuarioSeleccionado = usuarioTable.getSelectionModel().getSelectedItem();
            if (usuarioSeleccionado != null) {
                mostrarDialogoUsuario(usuarioSeleccionado);
            } else {
                AlertUtils.mostrarAdvertencia("Selección Requerida",
                        "Por favor, seleccione un usuario para editar.");
            }
        });

        deleteButton.setOnAction(e -> {
            Usuario usuarioSeleccionado = usuarioTable.getSelectionModel().getSelectedItem();
            if (usuarioSeleccionado != null) {
                eliminarUsuario(usuarioSeleccionado);
            } else {
                AlertUtils.mostrarAdvertencia("Selección Requerida",
                        "Por favor, seleccione un usuario para eliminar.");
            }
        });

        resetPasswordButton.setOnAction(e -> {
            Usuario usuarioSeleccionado = usuarioTable.getSelectionModel().getSelectedItem();
            if (usuarioSeleccionado != null) {
                resetearPassword(usuarioSeleccionado);
            } else {
                AlertUtils.mostrarAdvertencia("Selección Requerida",
                        "Por favor, seleccione un usuario para resetear su contraseña.");
            }
        });

        closeButton.setOnAction(e -> cerrarVentana());
    }

    /**
     * Configura la tabla de usuarios
     */
    private void configurarTabla() {
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        empleadoCol.setCellValueFactory(new PropertyValueFactory<>("nombreEmpleado"));

        adminCol.setCellValueFactory(new PropertyValueFactory<>("esAdmin"));
        adminCol.setCellFactory(column -> new TableCell<Usuario, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "Sí" : "No");
                }
            }
        });

        // Permitir selección única
        usuarioTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    /**
     * Carga la lista de usuarios
     */
    private void cargarUsuarios() {
        List<Usuario> usuarios = usuarioDAO.listarUsuarios();
        ObservableList<Usuario> data = FXCollections.observableArrayList(usuarios);
        usuarioTable.setItems(data);
    }

    /**
     * Muestra el diálogo para crear o editar un usuario
     * @param usuario El usuario a editar, o null para crear uno nuevo
     */
    private void mostrarDialogoUsuario(Usuario usuario) {
        boolean esNuevo = (usuario == null);

        // Crear diálogo
        Dialog<Usuario> dialog = new Dialog<>();
        dialog.setTitle(esNuevo ? "Nuevo Usuario" : "Editar Usuario");
        dialog.setHeaderText(esNuevo ? "Crear un nuevo usuario" : "Editar información del usuario");

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

        // Campos de texto
        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        CheckBox adminCheck = new CheckBox("Es administrador");

        // Cargar empleados para selección
        List<Empleado> empleados = empleadoDAO.listarEmpleadosActivos();
        empleadoCombo.setItems(FXCollections.observableArrayList(empleados));

        // Llenar datos si es edición
        if (!esNuevo) {
            usernameField.setText(usuario.getUsername());
            adminCheck.setSelected(usuario.isEsAdmin());

            // Seleccionar empleado
            for (Empleado empleado : empleados) {
                if (empleado.getIdEmpleado() == usuario.getIdEmpleado()) {
                    empleadoCombo.setValue(empleado);
                    break;
                }
            }

            // Deshabilitar campos que no se pueden editar
            usernameField.setDisable(true);
            empleadoCombo.setDisable(true);

            // Cambiar etiqueta de contraseña para indicar que es opcional en edición
            passwordField.setPromptText("Dejar en blanco para mantener la actual");
        } else {
            // En modo nuevo, la contraseña es obligatoria
            passwordField.setPromptText("Contraseña (obligatoria)");
        }

        // Añadir campos al grid
        grid.add(new Label("Empleado:"), 0, 0);
        grid.add(empleadoCombo, 1, 0);

        grid.add(new Label("Nombre de usuario:"), 0, 1);
        grid.add(usernameField, 1, 1);

        grid.add(new Label("Contraseña:"), 0, 2);
        grid.add(passwordField, 1, 2);

        grid.add(adminCheck, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Habilitar/deshabilitar botón de guardar según validación
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(esNuevo); // Deshabilitar inicialmente si es nuevo

        // Validar campos obligatorios
        empleadoCombo.valueProperty().addListener((observable, oldValue, newValue) ->
                validarCampos(esNuevo, empleadoCombo.getValue(), usernameField.getText(), passwordField.getText(), saveButton));

        usernameField.textProperty().addListener((observable, oldValue, newValue) ->
                validarCampos(esNuevo, empleadoCombo.getValue(), newValue, passwordField.getText(), saveButton));

        passwordField.textProperty().addListener((observable, oldValue, newValue) ->
                validarCampos(esNuevo, empleadoCombo.getValue(), usernameField.getText(), newValue, saveButton));

        // Convertir el resultado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Usuario resultado = esNuevo ? new Usuario() : usuario;

                // Establecer datos
                if (esNuevo) {
                    Empleado empleadoSeleccionado = empleadoCombo.getValue();
                    resultado.setIdEmpleado(empleadoSeleccionado.getIdEmpleado());
                    resultado.setNombreEmpleado(empleadoSeleccionado.getNombreCompleto());
                    resultado.setUsername(usernameField.getText().trim());
                }

                // Solo actualizar contraseña si se ha ingresado una nueva
                String password = passwordField.getText().trim();
                if (!password.isEmpty()) {
                    resultado.setPassword(password);
                }

                resultado.setEsAdmin(adminCheck.isSelected());

                return resultado;
            }
            return null;
        });

        // Mostrar diálogo y procesar resultado
        dialog.showAndWait().ifPresent(resultado -> {
            boolean operacionExitosa;

            if (esNuevo) {
                operacionExitosa = usuarioDAO.insertar(resultado);
            } else {
                operacionExitosa = usuarioDAO.actualizarUsuario(resultado);
            }

            if (operacionExitosa) {
                AlertUtils.mostrarInfo("Operación Exitosa",
                        "Usuario " + (esNuevo ? "creado" : "actualizado") + " correctamente.");
                cargarUsuarios();
            } else {
                AlertUtils.mostrarError("Error",
                        "No se pudo " + (esNuevo ? "crear" : "actualizar") + " el usuario.");
            }
        });
    }

    /**
     * Valida los campos del formulario
     */
    private void validarCampos(boolean esNuevo, Empleado empleado, String username, String password, Button saveButton) {
        if (esNuevo) {
            // En modo nuevo, todos los campos son obligatorios
            boolean camposValidos = empleado != null && !username.trim().isEmpty() && !password.trim().isEmpty();
            saveButton.setDisable(!camposValidos);
        } else {
            // En modo edición, solo verificamos si hay cambios
            boolean hayPosiblesCambios = !password.trim().isEmpty(); // Si hay password, hay cambios
            saveButton.setDisable(!hayPosiblesCambios);
        }
    }

    /**
     * Elimina un usuario del sistema
     */
    private void eliminarUsuario(Usuario usuario) {
        // Evitar eliminar al usuario actual
        Usuario usuarioActual = AdminLoginController.getUsuarioAutenticado();
        if (usuario.getIdUsuario() == usuarioActual.getIdUsuario()) {
            AlertUtils.mostrarAdvertencia("Operación no permitida",
                    "No puede eliminar su propio usuario.");
            return;
        }

        boolean confirmar = AlertUtils.mostrarConfirmacion("Confirmar Eliminación",
                "¿Está seguro de que desea eliminar el usuario \"" + usuario.getUsername() + "\"?");

        if (confirmar) {
            if (usuarioDAO.eliminar(usuario.getIdUsuario())) {
                AlertUtils.mostrarInfo("Operación Exitosa", "Usuario eliminado correctamente.");
                cargarUsuarios();
            } else {
                AlertUtils.mostrarError("Error", "No se pudo eliminar el usuario.");
            }
        }
    }

    /**
     * Resetea la contraseña de un usuario
     */
    private void resetearPassword(Usuario usuario) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Resetear Contraseña");
        dialog.setHeaderText("Ingrese la nueva contraseña para " + usuario.getUsername());

        ButtonType confirmButtonType = new ButtonType("Confirmar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Nueva contraseña");

        grid.add(new Label("Nueva contraseña:"), 0, 0);
        grid.add(passwordField, 1, 0);

        dialog.getDialogPane().setContent(grid);

        Button confirmButton = (Button) dialog.getDialogPane().lookupButton(confirmButtonType);
        confirmButton.setDisable(true);

        passwordField.textProperty().addListener((observable, oldValue, newValue) ->
                confirmButton.setDisable(newValue.trim().isEmpty()));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return passwordField.getText().trim();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(nuevaPassword -> {
            if (usuarioDAO.resetearPassword(usuario.getIdUsuario(), nuevaPassword)) {
                AlertUtils.mostrarInfo("Operación Exitosa", "Contraseña reseteada correctamente.");
            } else {
                AlertUtils.mostrarError("Error", "No se pudo resetear la contraseña.");
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