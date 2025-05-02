package org.example.gocheckfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.gocheckfx.dao.UsuarioDAO;
import org.example.gocheckfx.controllers.EmployeeScanController;

/**
 * GoCheck - Sistema de Control de Asistencia
 * Aplicación principal que inicia la interfaz de usuario.
 */
public class App extends Application {

    // Variable para mantener una referencia a la ventana principal
    private static Stage primaryStage;
    private static EmployeeScanController mainController;

    @Override
    public void start(Stage primaryStage) throws Exception {
        App.primaryStage = primaryStage;

        // Crear administrador por defecto si no existe
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        if (usuarioDAO.crearAdministradorPorDefecto()) {
            System.out.println("Se ha creado un usuario administrador por defecto:");
            System.out.println("Usuario: admin");
            System.out.println("Contraseña: admin123");
        }

        // Cargar la vista de escaneo para empleados (pantalla principal)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/gocheckfx/employee_scan.fxml"));
        Parent root = loader.load();

        // Guardar una referencia al controlador principal
        mainController = loader.getController();

        // Configurar la escena principal
        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/org/example/gocheckfx/css/styles.css").toExternalForm());

        // Configurar la ventana principal
        primaryStage.setTitle("GoCheck - Sistema de Control de Asistencia");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);

        // Manejar el cierre de la aplicación para liberar recursos
        primaryStage.setOnCloseRequest(event -> {
            if (mainController != null) {
                mainController.cleanUp();
            }
        });

        primaryStage.show();
    }

    /**
     * Método principal para iniciar la aplicación
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Método para abrir una nueva ventana con la vista especificada
     */
    public static void openWindow(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(fxmlPath));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(App.class.getResource("/org/example/gocheckfx/css/styles.css").toExternalForm());

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();

            // Si es la ventana de escaneo, manejar su cierre
            if (fxmlPath.contains("employee_scan")) {
                Object controller = loader.getController();
                if (controller instanceof EmployeeScanController) {
                    EmployeeScanController scanController = (EmployeeScanController) controller;
                    stage.setOnCloseRequest(event -> scanController.cleanUp());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Obtiene la ventana principal
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}
