package org.example.gocheckfx.utils;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Clase utilitaria para manejar el escaneo de códigos QR o de barras usando
 * una webcam o un escáner conectado.
 */
public class QRCodeScanner {

    private Webcam webcam;
    private WebcamPanel webcamPanel;
    private SwingNode swingNode;
    private ScheduledExecutorService scanExecutor;
    private boolean isScanning = false;
    private TextField scanResultField;
    private Consumer<String> onCodeScanned;
    private MultiFormatReader multiFormatReader;

    /**
     * Constructor para el escáner de códigos QR/barras
     * @param previewPane Panel donde se mostrará la vista previa de la cámara
     * @param scanResultField Campo donde se mostrará el resultado del escaneo
     * @param onCodeScanned Función a ejecutar cuando se escanee un código
     */
    public QRCodeScanner(Pane previewPane, TextField scanResultField, Consumer<String> onCodeScanned) {
        this.scanResultField = scanResultField;
        this.onCodeScanned = onCodeScanned;

        // Configurar el lector de códigos
        multiFormatReader = new MultiFormatReader();

        // Intentar inicializar la webcam
        try {
            initializeWebcam(previewPane);
        } catch (Exception e) {
            System.err.println("Error al inicializar la webcam: " + e.getMessage());
        }
    }

    /**
     * Inicializa la webcam y la prepara para el escaneo
     * @param previewPane Panel donde se mostrará la vista de la webcam
     */
    private void initializeWebcam(Pane previewPane) {
        try {
            // Obtener webcam por defecto
            webcam = Webcam.getDefault();

            if (webcam != null) {
                // Si hay una resolución HD disponible, usarla
                Dimension[] dimensions = webcam.getViewSizes();
                Dimension bestSize = WebcamResolution.VGA.getSize(); // 640x480 por defecto

                for (Dimension dimension : dimensions) {
                    // Intentar encontrar una resolución HD
                    if (dimension.width >= 1280 && dimension.height >= 720) {
                        bestSize = dimension;
                        break;
                    } else if (dimension.width > bestSize.width) {
                        bestSize = dimension;
                    }
                }

                webcam.setViewSize(bestSize);

                // Crear panel de webcam
                webcamPanel = new WebcamPanel(webcam);
                webcamPanel.setFPSDisplayed(true); // Mostrar FPS
                webcamPanel.setImageSizeDisplayed(true); // Mostrar tamaño de imagen
                webcamPanel.setMirrored(false); // No espejado

                // Integrar panel de webcam en JavaFX
                swingNode = new SwingNode();
                swingNode.setContent(webcamPanel);

                // Agregar a la interfaz
                previewPane.getChildren().clear();
                previewPane.getChildren().add(swingNode);

                // Ajustar tamaño del panel
                webcamPanel.setSize((int)previewPane.getWidth(), (int)previewPane.getHeight());
                previewPane.widthProperty().addListener((observable, oldValue, newValue) -> {
                    webcamPanel.setSize(newValue.intValue(), (int)previewPane.getHeight());
                });
                previewPane.heightProperty().addListener((observable, oldValue, newValue) -> {
                    webcamPanel.setSize((int)previewPane.getWidth(), newValue.intValue());
                });
            } else {
                throw new Exception("No se encontró ninguna webcam disponible");
            }
        } catch (Exception e) {
            System.err.println("Error al configurar webcam: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Inicia el escaneo de códigos
     */
    public void startScanning() {
        if (isScanning || webcam == null) return;

        isScanning = true;

        // Iniciar la webcam si no está activa
        if (!webcam.isOpen()) {
            webcam.open();
        }

        // Iniciar panel si no está activo
        if (webcamPanel != null && !webcamPanel.isStarted()) {
            webcamPanel.start();
        }

        // Crear hilo de escaneo
        scanExecutor = Executors.newSingleThreadScheduledExecutor();
        scanExecutor.scheduleAtFixedRate(() -> {
            try {
                // Capturar imagen de la webcam
                BufferedImage image = webcam.getImage();

                if (image != null) {
                    // Procesar imagen para detectar códigos
                    LuminanceSource source = new BufferedImageLuminanceSource(image);
                    BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

                    try {
                        Result result = multiFormatReader.decodeWithState(bitmap);

                        if (result != null) {
                            String codeText = result.getText();

                            // Actualizar UI en el hilo de JavaFX
                            Platform.runLater(() -> {
                                scanResultField.setText(codeText);

                                // Ejecutar callback con el código escaneado
                                if (onCodeScanned != null) {
                                    onCodeScanned.accept(codeText);
                                }
                            });

                            // Breve pausa para evitar escaneos múltiples del mismo código
                            Thread.sleep(1500);
                        }
                    } catch (NotFoundException e) {
                        // No se encontró ningún código en la imagen, es normal
                    } catch (Exception e) {
                        System.err.println("Error al decodificar imagen: " + e.getMessage());
                    } finally {
                        multiFormatReader.reset();
                    }
                }
            } catch (Exception e) {
                System.err.println("Error en ciclo de escaneo: " + e.getMessage());
            }
        }, 0, 100, TimeUnit.MILLISECONDS); // Escanear cada 100ms
    }

    /**
     * Detiene el escaneo de códigos
     */
    public void stopScanning() {
        if (!isScanning) return;

        isScanning = false;

        // Detener el executor de escaneo
        if (scanExecutor != null) {
            scanExecutor.shutdown();
            scanExecutor = null;
        }

        // Detener panel de webcam
        if (webcamPanel != null && webcamPanel.isStarted()) {
            webcamPanel.stop();
        }

        // Cerrar webcam
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
        }
    }

    /**
     * Comprueba si el escáner está activo
     */
    public boolean isScanning() {
        return isScanning;
    }

    /**
     * Método para procesar un código ingresado manualmente
     * (útil para pruebas o como respaldo)
     */
    public void processManualCode(String code) {
        if (code != null && !code.trim().isEmpty()) {
            if (onCodeScanned != null) {
                onCodeScanned.accept(code.trim());
            }
        }
    }

    /**
     * Libera los recursos al cerrar la aplicación
     */
    public void dispose() {
        stopScanning();

        // Cerrar webcam si está abierta
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
        }
    }
}
