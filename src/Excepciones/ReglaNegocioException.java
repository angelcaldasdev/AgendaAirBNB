package excepciones;

/**
 * Excepción que se lanza cuando se incumple una regla de negocio del hotel
 * (DNI inválido, habitación ocupada, fechas incorrectas, etc.).
 * Hereda de RuntimeException para no obligar a usar "throws" en cada método.
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}