package org.example.gocheckfx.controllers;

import org.example.gocheckfx.App;
import org.example.gocheckfx.dao.UsuarioDAO;
import org.example.gocheckfx.models.Usuario;
import org.example.gocheckfx.utils.AlertUtils;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador para la pantalla de inicio de sesión de administradores.
 */
public class AdminLoginController implements Initializable {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button cancelButton;

    private UsuarioDAO usuarioDAO;

    // Variable estática para almacenar el usuario autenticado (sesión)
    private static Usuario usuarioAutenticado;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicializar DAO
        usuarioDAO = new UsuarioDAO();

        // Configurar eventos de botones
        loginButton.setOnAction(e -> autenticar());
        cancelButton.setOnAction(e -> cerrarVentana());

        // Permitir presionar Enter para iniciar sesión
        passwordField.setOnAction(e -> autenticar());
    }

    /**
     * Intenta autenticar al usuario con las credenciales ingresadas
     */
    private void autenticar() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            AlertUtils.mostrarAdvertencia("Campos Vacíos",
                    "Por favor, ingrese su nombre de usuario y contraseña.");
            return;
        }

        // Intentar autenticar
        Usuario usuario = usuarioDAO.autenticar(username, password);

        if (usuario != null) {
            if (usuario.isEsAdmin()) {
                // Almacenar usuario en sesión
                usuarioAutenticado = usuario;

                // Abrir el dashboard de administración
                App.openWindow("/org/example/gocheckfx/dashboard.fxml", "GoCheck - Panel de Administración");

                // Cerrar ventana de login
                cerrarVentana();
            } else {
                AlertUtils.mostrarAdvertencia("Acceso Denegado",
                        "Su cuenta no tiene permisos de administrador.");
            }
        } else {
            AlertUtils.mostrarError("Error de Autenticación",
                    "Nombre de usuario o contraseña incorrectos.");
        }
    }

    /**
     * Cierra la ventana actual
     */
    private void cerrarVentana() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Obtiene el usuario autenticado actualmente
     * @return Usuario autenticado o null si no hay sesión
     */
    public static Usuario getUsuarioAutenticado() {
        return usuarioAutenticado;
    }

    /**
     * Cierra la sesión del usuario actual
     */
    public static void cerrarSesion() {
        usuarioAutenticado = null;
    }

    /**
     * Verifica si hay un usuario autenticado
     * @return true si hay un usuario con sesión activa, false en caso contrario
     */
    public static boolean hayUsuarioAutenticado() {
        return usuarioAutenticado != null;
    }
}