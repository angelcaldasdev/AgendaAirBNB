package Excepciones;

/**
 * Se lanza cuando falla una operación con la base de datos
 * (conexión, consulta, etc.). Es un error técnico, distinto de
 * ReglaNegocioException, que es un error de las reglas del hotel.
 */
public class ErrorBaseDatosException extends RuntimeException {

    public ErrorBaseDatosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}