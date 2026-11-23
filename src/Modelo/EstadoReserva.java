package Modelo;

/**
 * Estados del ciclo de vida de una reserva.
 * Las reservas nunca se borran: cambian de estado y así queda historial.
 */
public enum EstadoReserva {

    ACTIVA("Activa"),
    CANCELADA("Cancelada"),
    FINALIZADA("Finalizada");

    private final String descripcion;

    EstadoReserva(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}