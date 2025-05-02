package org.example.gocheckfx.controllers;

import org.example.gocheckfx.dao.EmpleadoDAO;
import org.example.gocheckfx.models.Empleado;
import org.example.gocheckfx.utils.AlertUtils;
import org.example.gocheckfx.utils.BarcodeGenerator;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador para la pantalla de visualización y gestión de códigos QR/barras.
 */
public class CodigoController implements Initializable {

    @FXML private ComboBox<Empleado> empleadoCombo;
    @FXML private Label infoLabel;
    @FXML private ImageView qrImageView;
    @FXML private ImageView barcodeImageView;
    @FXML private Button regenerarButton;
    @FXML private Button descargarQRButton;
    @FXML private Button descargarBarcodeButton;
    @FXML private Button imprimirButton;
    @FXML private Button cerrarButton;

    private EmpleadoDAO empleadoDAO;
    private Empleado empleadoSeleccionado;

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
        empleadoDAO = new EmpleadoDAO();

        // Cargar lista de empleados
        cargarEmpleados();

        // Configurar evento de selección de empleado
        empleadoCombo.setOnAction(e -> {
            empleadoSeleccionado = empleadoCombo.getValue();
            if (empleadoSeleccionado != null) {
                cargarCodigosEmpleado(empleadoSeleccionado.getCodigoUnico());
                actualizarInfoEmpleado();
            }
        });

        // Configurar eventos de botones
        regenerarButton.setOnAction(e -> regenerarCodigos());
        descargarQRButton.setOnAction(e -> descargarCodigo(true));
        descargarBarcodeButton.setOnAction(e -> descargarCodigo(false));
        imprimirButton.setOnAction(e -> imprimirCodigos());
        cerrarButton.setOnAction(e -> cerrarVentana());

        // Deshabilitar botones inicialmente
        habilitarBotones(false);
    }

    /**
     * Carga la lista de empleados en el combo
     */
    private void cargarEmpleados() {
        List<Empleado> empleados = empleadoDAO.listarEmpleadosActivos();
        empleadoCombo.getItems().clear();
        empleadoCombo.getItems().addAll(empleados);
    }

    /**
     * Carga las imágenes de los códigos para un empleado
     * @param codigoUnico El código único del empleado
     */
    private void cargarCodigosEmpleado(String codigoUnico) {
        String[] rutas = empleadoDAO.obtenerRutasCodigos(codigoUnico);

        if (rutas != null) {
            try {
                // Cargar imágenes en los ImageView
                qrImageView.setImage(new Image(new File(rutas[0]).toURI().toString()));
                barcodeImageView.setImage(new Image(new File(rutas[1]).toURI().toString()));

                // Habilitar botones
                habilitarBotones(true);

            } catch (Exception e) {
                System.err.println("Error al cargar imágenes: " + e.getMessage());
                AlertUtils.mostrarError("Error", "No se pudieron cargar las imágenes de los códigos.");
            }
        } else {
            qrImageView.setImage(null);
            barcodeImageView.setImage(null);
            habilitarBotones(false);
            AlertUtils.mostrarAdvertencia("Códigos no encontrados",
                    "No se pudieron encontrar los códigos para este empleado.");
        }
    }

    /**
     * Actualiza la información del empleado mostrada
     */
    private void actualizarInfoEmpleado() {
        if (empleadoSeleccionado != null) {
            infoLabel.setText("Empleado: " + empleadoSeleccionado.getNombreCompleto() +
                    " | Código: " + empleadoSeleccionado.getCodigoUnico());
        } else {
            infoLabel.setText("Seleccione un empleado");
        }
    }

    /**
     * Regenera los códigos para el empleado seleccionado actualmente
     */
    private void regenerarCodigos() {
        if (empleadoSeleccionado == null) {
            AlertUtils.mostrarAdvertencia("Selección requerida",
                    "Por favor, seleccione un empleado primero.");
            return;
        }

        // Regenerar códigos
        empleadoDAO.generarYGuardarCodigos(empleadoSeleccionado);

        // Recargar imágenes
        cargarCodigosEmpleado(empleadoSeleccionado.getCodigoUnico());

        AlertUtils.mostrarInfo("Códigos Regenerados",
                "Los códigos se han regenerado correctamente.");
    }

    /**
     * Descarga un código (QR o barras) a una ubicación seleccionada por el usuario
     * @param esQR true para descargar código QR, false para código de barras
     */
    private void descargarCodigo(boolean esQR) {
        if (empleadoSeleccionado == null) {
            AlertUtils.mostrarAdvertencia("Selección requerida",
                    "Por favor, seleccione un empleado primero.");
            return;
        }

        // Configurar FileChooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Código");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imágenes PNG", "*.png"));

        String nombreSugerido = (esQR ? "qr_" : "barcode_") +
                empleadoSeleccionado.getCodigoUnico() + ".png";
        fileChooser.setInitialFileName(nombreSugerido);

        // Mostrar diálogo para seleccionar ubicación
        File destino = fileChooser.showSaveDialog(cerrarButton.getScene().getWindow());

        if (destino != null) {
            try {
                // Copiar archivo
                String[] rutas = empleadoDAO.obtenerRutasCodigos(empleadoSeleccionado.getCodigoUnico());

                if (rutas != null) {
                    String rutaOrigen = esQR ? rutas[0] : rutas[1];
                    Path origen = Paths.get(rutaOrigen);
                    Path destinoPath = destino.toPath();

                    Files.copy(origen, destinoPath, StandardCopyOption.REPLACE_EXISTING);

                    AlertUtils.mostrarInfo("Descarga Completa",
                            "El código se ha descargado correctamente.");
                } else {
                    throw new IOException("No se encontraron los archivos de códigos.");
                }
            } catch (IOException e) {
                System.err.println("Error al descargar código: " + e.getMessage());
                AlertUtils.mostrarError("Error", "No se pudo descargar el código: " + e.getMessage());
            }
        }
    }

    /**
     * Imprime los códigos del empleado seleccionado
     */
    private void imprimirCodigos() {
        if (empleadoSeleccionado == null) {
            AlertUtils.mostrarAdvertencia("Selección requerida",
                    "Por favor, seleccione un empleado primero.");
            return;
        }

        // Esta es una implementación básica. En un sistema real,
        // se conectaría con el sistema de impresión de Java.
        AlertUtils.mostrarInfo("Impresión",
                "La funcionalidad de impresión será implementada próximamente.");

        // Implementación real de impresión:
        /*
        try {
            PrinterJob job = PrinterJob.createPrinterJob();
            if (job != null && job.showPrintDialog(null)) {
                // Imprimir
                boolean impreso = job.printPage(qrImageView);
                if (impreso) {
                    job.endJob();
                    AlertUtils.mostrarInfo("Impresión", "Impresión enviada correctamente.");
                }
            }
        } catch (Exception e) {
            AlertUtils.mostrarError("Error", "Error al imprimir: " + e.getMessage());
        }
        */
    }

    /**
     * Habilita o deshabilita los botones según si hay un empleado seleccionado
     * @param habilitar true para habilitar, false para deshabilitar
     */
    private void habilitarBotones(boolean habilitar) {
        regenerarButton.setDisable(!habilitar);
        descargarQRButton.setDisable(!habilitar);
        descargarBarcodeButton.setDisable(!habilitar);
        imprimirButton.setDisable(!habilitar);
    }

    /**
     * Cierra la ventana actual
     */
    private void cerrarVentana() {
        Stage stage = (Stage) cerrarButton.getScene().getWindow();
        stage.close();
    }
}
