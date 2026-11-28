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
 * (así la contraseña nunca queda escrita en el código), crea las tablas
 * la primera vez que se ejecuta el sistema y permite ejecutar varias
 * operaciones como una sola TRANSACCIÓN (todo se guarda o nada se guarda).
 */
public class ConexionBD {

    private static final String ARCHIVO_CONFIGURACION = "config.properties";

    // Se carga una sola vez y se reutiliza
    private static Properties configuracion;

    // Clase de utilidad: no se instancia
    private ConexionBD() {
    }

    /**
     * Operación que se ejecuta dentro de una transacción.
     * Recibe la conexión de la transacción y debe usarla en TODAS sus consultas,
     * porque cada conexión ve los cambios aún no confirmados solo de sí misma.
     */
    @FunctionalInterface
    public interface OperacionTransaccional {
        void ejecutar(Connection conexion) throws SQLException;
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

    /**
     * Ejecuta una operación como UNA sola transacción:
     *  - Si todo sale bien, se confirma con commit (todos los cambios quedan guardados).
     *  - Si algo falla, se deshace con rollback (no queda ningún cambio a medias).
     *
     * @param mensajeError mensaje que se usa si ocurre un error de SQL
     * @param operacion    lo que se quiere ejecutar dentro de la transacción
     */
    public static void ejecutarEnTransaccion(String mensajeError, OperacionTransaccional operacion) {
        try (Connection conexion = obtenerConexion()) {

            // Desde aquí, nada se guarda en la base de datos hasta llamar a commit()
            conexion.setAutoCommit(false);

            try {
                operacion.ejecutar(conexion);
                conexion.commit(); // todo salió bien: se guardan los cambios juntos

            } catch (SQLException | RuntimeException error) {
                // Algo falló: se deshacen los cambios que se alcanzaron a hacer
                try {
                    conexion.rollback();
                } catch (SQLException errorAlDeshacer) {
                    // Si el rollback también falla, no se pierde el error original
                    error.addSuppressed(errorAlDeshacer);
                }
                throw error; // se vuelve a lanzar el error original
            }

        } catch (SQLException error) {
            // Errores de SQL (de la operación o de la conexión) se convierten en el error técnico del sistema
            throw new ErrorBaseDatosException(mensajeError, error);
        }
        // Las conexiones no se reutilizan (DriverManager abre una nueva cada vez),
        // por eso no hace falta volver a activar el autoCommit.
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