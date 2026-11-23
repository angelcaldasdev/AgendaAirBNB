package modelo;

/**
 * Estados posibles de una habitación según las reglas del negocio.
 */
public enum EstadoHabitacion {

    DISPONIBLE("Disponible"),
    RESERVADA("Reservada");

    private final String descripcion;

    EstadoHabitacion(String descripcion) {
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