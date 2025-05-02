module org.example.gocheckfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.sql;

    requires org.controlsfx.controls;  // Para controles adicionales en la interfaz
    requires org.kordamp.ikonli.javafx; // Para iconos
    requires org.kordamp.bootstrapfx.core; // Para mejorar el estilo visual
    requires eu.hansolo.tilesfx;  // Para elementos gráficos adicionales
    requires com.google.zxing;  // Para leer códigos QR
    requires com.google.zxing.javase;  // Para usar la funcionalidad de ZXing en Java SE
    requires webcam.capture;  // Para interactuar con la cámara web
    requires org.json;
    requires javafx.swing;  // Para manejar JSON

    opens org.example.gocheckfx to javafx.fxml;  // Permite el acceso a la clase principal de la aplicación desde FXML
    opens org.example.gocheckfx.controllers to javafx.fxml;  // Permite el acceso a los controladores FXML

    exports org.example.gocheckfx;  // Exporta la clase principal y otros elementos necesarios
    exports org.example.gocheckfx.controllers;  // Exporta los controladores
    exports org.example.gocheckfx.models;  // Exporta los modelos (si los tienes)
    exports org.example.gocheckfx.dao;  // Exporta los DAO (si los tienes)
    exports org.example.gocheckfx.utils;  // Exporta las utilidades (si las tienes)
}
