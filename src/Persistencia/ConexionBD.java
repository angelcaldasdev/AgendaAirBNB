package Persistencia;

import Excepciones.ErrorBaseDatosException;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Abre conexiones con MySQL leyendo los datos desde config.properties
 * (así la contraseña nunca queda escrita en el código) y crea las tablas
 * la primera vez que se ejecuta el sistema.
 */
public class ConexionBD {

    private static final String ARCHIVO_CONFIGURACION = "config.properties";

    // Se carga una sola vez y se reutiliza
    private static Properties configuracion;

    // Clase de utilidad: no se instancia
    private ConexionBD() {
    }

    private static Properties obtenerConfiguracion() {
        if (configuracion == null) {
            Properties propiedades = new Properties();
            try (FileInputStream lector = new FileInputStream(ARCHIVO_CONFIGURACION)) {
                propiedades.load(lector);
            } catch (IOException error) {
                throw new ErrorBaseDatosException("No se encontró el archivo " + ARCHIVO_CONFIGURACION
                        + ". Copia config.example.properties como config.properties y completa tus datos.", error);
            }
            configuracion = propiedades;
        }
        return configuracion;
    }

    /** Abre una conexión nueva. Quien la use debe cerrarla (try-with-resources). */
    public static Connection obtenerConexion() throws SQLException {
        Properties datos = obtenerConfiguracion();
        return DriverManager.getConnection(
                datos.getProperty("db.url"),
                datos.getProperty("db.usuario"),
                datos.getProperty("db.clave"));
    }

    /** Crea las tablas si no existen. Se puede llamar en cada arranque sin problema. */
    public static void inicializarBaseDeDatos() {

        String crearTablaHuespedes = """
                CREATE TABLE IF NOT EXISTS huespedes (
                    dni             CHAR(8)      PRIMARY KEY,
                    nombre_completo VARCHAR(150) NOT NULL,
                    telefono        CHAR(9)      NOT NULL,
                    correo          VARCHAR(150) NOT NULL
                )
                """;

        String crearTablaHabitaciones = """
                CREATE TABLE IF NOT EXISTS habitaciones (
                    numero           VARCHAR(10)   PRIMARY KEY,
                    tipo             VARCHAR(20)   NOT NULL,
                    precio_por_noche DECIMAL(10,2) NOT NULL,
                    estado           VARCHAR(20)   NOT NULL
                )
                """;

        String crearTablaReservas = """
                CREATE TABLE IF NOT EXISTS reservas (
                    codigo            VARCHAR(20)   PRIMARY KEY,
                    dni_huesped       CHAR(8)       NOT NULL,
                    numero_habitacion VARCHAR(10)   NOT NULL,
                    fecha_entrada     DATE          NOT NULL,
                    fecha_salida      DATE          NOT NULL,
                    cantidad_personas INT           NOT NULL,
                    total_pagar       DECIMAL(10,2) NOT NULL,
                    estado            VARCHAR(20)   NOT NULL,
                    FOREIGN KEY (dni_huesped)       REFERENCES huespedes(dni),
                    FOREIGN KEY (numero_habitacion) REFERENCES habitaciones(numero)
                )
                """;

        try (Connection conexion = obtenerConexion();
             Statement sentencia = conexion.createStatement()) {

            sentencia.execute(crearTablaHuespedes);
            sentencia.execute(crearTablaHabitaciones);
            sentencia.execute(crearTablaReservas);

        } catch (SQLException error) {
            throw new ErrorBaseDatosException("No se pudo inicializar la base de datos.", error);
        }
    }
}