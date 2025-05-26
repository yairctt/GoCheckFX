DROP DATABASE IF EXISTS gocheck_bd;
CREATE DATABASE gocheck_bd;
USE gocheck_bd;

-- Tabla de puestos
CREATE TABLE puestos (
    id_puesto INT AUTO_INCREMENT PRIMARY KEY,
    nombre_puesto VARCHAR(50) NOT NULL UNIQUE,
    descripcion TEXT,
    reglas_descanso JSON, -- Ej: {"sin_desayuno": true, "combinar_descanso": true, "dos_descansos": true}
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT TRUE,
    INDEX idx_nombre_puesto (nombre_puesto)
);

-- Tabla de turnos
CREATE TABLE turnos (
    id_turno INT AUTO_INCREMENT PRIMARY KEY,
    nombre_turno VARCHAR(50) NOT NULL,
    hora_entrada TIME NOT NULL,
    hora_salida TIME NOT NULL,
    duracion_desayuno INT DEFAULT 0, -- En minutos
    duracion_comida INT DEFAULT 0, -- En minutos
    permite_combinar_descanso BOOLEAN DEFAULT FALSE,
    INDEX idx_nombre_turno (nombre_turno)
);

-- Tabla de empleados
CREATE TABLE empleados (
    id_empleado INT AUTO_INCREMENT PRIMARY KEY,
    codigo_unico VARCHAR(50) NOT NULL UNIQUE, -- Código QR o de barras
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    id_puesto INT NOT NULL,
    id_turno INT NOT NULL,
    email VARCHAR(100) UNIQUE,
    telefono VARCHAR(20),
    fecha_contratacion DATE,
    activo BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (id_puesto) REFERENCES puestos(id_puesto),
    FOREIGN KEY (id_turno) REFERENCES turnos(id_turno),
    INDEX idx_codigo_unico (codigo_unico),
    INDEX idx_puesto (id_puesto)
);

-- Tabla de horarios_empleados (días laborables y descanso semanal)
CREATE TABLE horarios_empleados (
    id_horario INT AUTO_INCREMENT PRIMARY KEY,
    id_empleado INT NOT NULL,
    dias_laborables JSON NOT NULL, -- Ej: ["lunes", "martes", ..., "domingo"]
    dia_descanso_semanal ENUM('lunes', 'martes', 'miércoles', 'jueves', 'viernes', 'sábado', 'domingo') NULL, -- Para semana completa
    FOREIGN KEY (id_empleado) REFERENCES empleados(id_empleado),
    INDEX idx_empleado (id_empleado)
);

-- Tabla de permisos (solicitudes de ausencia)
CREATE TABLE permisos (
    id_permiso INT AUTO_INCREMENT PRIMARY KEY,
    id_empleado INT NOT NULL,
    fecha DATE NOT NULL,
    motivo TEXT NOT NULL,
    estado ENUM('PENDIENTE', 'APROBADO', 'RECHAZADO') DEFAULT 'PENDIENTE',
    fecha_solicitud DATETIME DEFAULT CURRENT_TIMESTAMP,
    id_admin INT NULL, -- Admin que aprueba/rechaza
    FOREIGN KEY (id_empleado) REFERENCES empleados(id_empleado),
    FOREIGN KEY (id_admin) REFERENCES empleados(id_empleado),
    INDEX idx_fecha_empleado (fecha, id_empleado)
);

-- Tabla de cambios_descanso (cambios temporales en día de descanso)
CREATE TABLE cambios_descanso (
    id_cambio INT AUTO_INCREMENT PRIMARY KEY,
    id_empleado INT NOT NULL,
    fecha_original DATE NOT NULL, -- Día que iba a descansar (e.g., martes)
    fecha_nueva DATE NOT NULL, -- Nuevo día de descanso (e.g., jueves)
    motivo TEXT, -- Ej: "Cubrir ausencia de Pedro"
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
    id_admin INT NOT NULL, -- Admin que registra el cambio
    FOREIGN KEY (id_empleado) REFERENCES empleados(id_empleado),
    FOREIGN KEY (id_admin) REFERENCES empleados(id_empleado),
    INDEX idx_empleado_fecha (id_empleado, fecha_original)
);

-- Tabla de usuarios (para login)
CREATE TABLE usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    id_empleado INT NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL, -- Hash de la contraseña
    es_admin BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (id_empleado) REFERENCES empleados(id_empleado),
    INDEX idx_username (username)
);

-- Tabla de asistencias
CREATE TABLE asistencias (
    id_asistencia INT AUTO_INCREMENT PRIMARY KEY,
    id_empleado INT NOT NULL,
    fecha DATE NOT NULL,
    hora_entrada DATETIME,
    hora_salida DATETIME,
    inicio_descanso_1 DATETIME, -- Primer descanso (desayuno o comida)
    fin_descanso_1 DATETIME,
    inicio_descanso_2 DATETIME, -- Segundo descanso (si aplica)
    fin_descanso_2 DATETIME,
    estado ENUM('PRESENTE', 'FALTA', 'RETARDO', 'JUSTIFICADO') DEFAULT 'FALTA',
    notas TEXT, -- Ej: "Desayuno y comida separados"
    FOREIGN KEY (id_empleado) REFERENCES empleados(id_empleado),
    INDEX idx_fecha_empleado (fecha, id_empleado)
);

-- Tabla de justificaciones
CREATE TABLE justificaciones (
    id_justificacion INT AUTO_INCREMENT PRIMARY KEY,
    id_asistencia INT NOT NULL,
    id_admin INT NOT NULL, -- Empleado con rol de administrador
    motivo TEXT NOT NULL,
    fecha_justificacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    documento_adjunto VARCHAR(255), -- Ruta o nombre del archivo
    FOREIGN KEY (id_asistencia) REFERENCES asistencias(id_asistencia),
    FOREIGN KEY (id_admin) REFERENCES empleados(id_empleado)
);

-- Agrega un puesto por defecto
INSERT INTO puestos (nombre_puesto, descripcion, reglas_descanso)
VALUES ('Administrador General', 'Puesto de administración con todos los permisos', 
        JSON_OBJECT('sin_desayuno', false, 'combinar_descanso', true, 'dos_descansos', true));

-- Agrega un turno por defecto
INSERT INTO turnos (nombre_turno, hora_entrada, hora_salida, duracion_desayuno, duracion_comida)
VALUES ('Turno completo', '08:00:00', '17:00:00', 30, 60);

INSERT INTO empleados (codigo_unico, nombre, apellido, id_puesto, id_turno, email, telefono, fecha_contratacion)
VALUES ('QR-ADMIN-001', 'Admin', 'General', 1, 1, 'admin@example.com', '555-1234', CURDATE());

INSERT INTO turnos (nombre_turno, hora_entrada, hora_salida, duracion_desayuno, duracion_comida)
VALUES ('Turno completo', '08:00:00', '17:00:00', 30, 60);






