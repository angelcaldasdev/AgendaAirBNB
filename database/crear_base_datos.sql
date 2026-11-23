-- Ejecutar en MySQL Workbench con el usuario root.
-- Cambia CAMBIA_ESTA_CLAVE por una contraseña segura.
CREATE DATABASE IF NOT EXISTS hotel_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'hotel_app'@'localhost' IDENTIFIED BY 'CAMBIA_ESTA_CLAVE';

GRANT ALL PRIVILEGES ON hotel_db.* TO 'hotel_app'@'localhost';
