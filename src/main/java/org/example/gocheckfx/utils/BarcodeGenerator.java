package org.example.gocheckfx.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.oned.Code128Writer;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.embed.swing.SwingFXUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase utilitaria para generar y guardar códigos QR y códigos de barras.
 */
public class BarcodeGenerator {

    /**
     * Genera un código QR a partir de un texto
     * @param text Texto a codificar
     * @param width Ancho de la imagen
     * @param height Alto de la imagen
     * @return Imagen del código QR o null si hubo un error
     */
    public static Image generateQRCode(String text, int width, int height) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 2);

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);

            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            return SwingFXUtils.toFXImage(bufferedImage, null);

        } catch (WriterException e) {
            System.err.println("Error al generar código QR: " + e.getMessage());
            return null;
        }
    }

    /**
     * Genera un código de barras (Code 128) a partir de un texto
     * @param text Texto a codificar
     * @param width Ancho de la imagen
     * @param height Alto de la imagen
     * @return Imagen del código de barras o null si hubo un error
     */
    public static Image generateBarcode(String text, int width, int height) {
        Code128Writer barcodeWriter = new Code128Writer();
        BitMatrix bitMatrix = barcodeWriter.encode(text, BarcodeFormat.CODE_128, width, height);

        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

        return SwingFXUtils.toFXImage(bufferedImage, null);

    }

    /**
     * Guarda una imagen de código QR en un archivo PNG
     * @param text Texto a codificar
     * @param filePath Ruta donde guardar el archivo
     * @param width Ancho de la imagen
     * @param height Alto de la imagen
     * @return true si se guardó correctamente, false en caso contrario
     */
    public static boolean saveQRCode(String text, String filePath, int width, int height) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 2);

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);

            // Asegurar que exista el directorio
            File outputFile = new File(filePath);
            File parentDir = outputFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            // Guardar como PNG
            return ImageIO.write(MatrixToImageWriter.toBufferedImage(bitMatrix), "PNG", outputFile);

        } catch (WriterException | IOException e) {
            System.err.println("Error al guardar código QR: " + e.getMessage());
            return false;
        }
    }

    /**
     * Guarda una imagen de código de barras en un archivo PNG
     * @param text Texto a codificar
     * @param filePath Ruta donde guardar el archivo
     * @param width Ancho de la imagen
     * @param height Alto de la imagen
     * @return true si se guardó correctamente, false en caso contrario
     */
    public static boolean saveBarcode(String text, String filePath, int width, int height) {
        try {
            Code128Writer barcodeWriter = new Code128Writer();
            BitMatrix bitMatrix = barcodeWriter.encode(text, BarcodeFormat.CODE_128, width, height);

            // Asegurar que exista el directorio
            File outputFile = new File(filePath);
            File parentDir = outputFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            // Guardar como PNG
            return ImageIO.write(MatrixToImageWriter.toBufferedImage(bitMatrix), "PNG", outputFile);

        } catch (IOException e) {
            System.err.println("Error al guardar código de barras: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene un array de bytes de una imagen de código QR (útil para imprimir o guardar en DB)
     * @param text Texto a codificar
     * @param width Ancho de la imagen
     * @param height Alto de la imagen
     * @return Bytes de la imagen o null si hubo un error
     */
    public static byte[] getQRCodeBytes(String text, int width, int height) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 2);

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);

            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "PNG", baos);

            return baos.toByteArray();

        } catch (WriterException | IOException e) {
            System.err.println("Error al obtener bytes de código QR: " + e.getMessage());
            return null;
        }
    }
}
