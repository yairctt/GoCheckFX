package org.example.gocheckfx.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Clase utilitaria para mostrar diálogos de alerta al usuario.
 */
public class AlertUtils {

    /**
     * Muestra un mensaje de información
     * @param titulo Título del diálogo
     * @param mensaje Mensaje a mostrar
     */
    public static void mostrarInfo(String titulo, String mensaje) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

        // Aplicar estilos si fuera necesario
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);

        alert.showAndWait();
    }

    /**
     * Muestra un mensaje de advertencia
     * @param titulo Título del diálogo
     * @param mensaje Mensaje a mostrar
     */
    public static void mostrarAdvertencia(String titulo, String mensaje) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);

        alert.showAndWait();
    }

    /**
     * Muestra un mensaje de error
     * @param titulo Título del diálogo
     * @param mensaje Mensaje a mostrar
     */
    public static void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);

        alert.showAndWait();
    }

    /**
     * Muestra un mensaje de confirmación y devuelve la respuesta del usuario
     * @param titulo Título del diálogo
     * @param mensaje Mensaje a mostrar
     * @return true si el usuario confirmó, false en caso contrario
     */
    public static boolean mostrarConfirmacion(String titulo, String mensaje) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
