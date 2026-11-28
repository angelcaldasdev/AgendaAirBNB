package Modelo;

/**
 * Tipos de habitación que ofrece el hotel, con su capacidad
 * y su precio base por noche (en soles). Los precios son de ejemplo.
 */
public enum TipoHabitacion {

    SIMPLE("Simple", 1, 150.00),
    DOBLE("Doble", 2, 250.00),
    FAMILIAR("Familiar", 4, 380.00),
    SUITE("Suite", 3, 550.00);

    private final String descripcion;
    private final int capacidadPersonas;
    private final double precioBasePorNoche;

    TipoHabitacion(String descripcion, int capacidadPersonas, double precioBasePorNoche) {
        this.descripcion = descripcion;
        this.capacidadPersonas = capacidadPersonas;
        this.precioBasePorNoche = precioBasePorNoche;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getCapacidadPersonas() {
        return capacidadPersonas;
    }

    public double getPrecioBasePorNoche() {
        return precioBasePorNoche;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}