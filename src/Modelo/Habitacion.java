package Modelo;

import Excepciones.ReglaNegocioException;

/**
 * Representa una habitación física del hotel.
 * El precio por noche se toma del tipo de habitación.
 */
public class Habitacion {

    private final String numero;
    private final TipoHabitacion tipo;
    private double precioPorNoche;
    private EstadoHabitacion estado;

    public Habitacion(String numero, TipoHabitacion tipo) {
        if (numero == null || numero.trim().isEmpty()) {
            throw new ReglaNegocioException("La habitación debe tener un número.");
        }
        if (tipo == null) {
            throw new ReglaNegocioException("La habitación debe tener un tipo.");
        }
        this.numero = numero.trim();
        this.tipo = tipo;
        this.precioPorNoche = tipo.getPrecioBasePorNoche();
        this.estado = EstadoHabitacion.DISPONIBLE; // toda habitación nueva inicia disponible
    }

    public boolean estaDisponible() {
        return estado == EstadoHabitacion.DISPONIBLE;
    }

    public void marcarComoReservada() {
        this.estado = EstadoHabitacion.RESERVADA;
    }

    public void marcarComoDisponible() {
        this.estado = EstadoHabitacion.DISPONIBLE;
    }

    public String getNumero() {
        return numero;
    }

    public TipoHabitacion getTipo() {
        return tipo;
    }

    public double getPrecioPorNoche() {
        return precioPorNoche;
    }

    public void setPrecioPorNoche(double precioPorNoche) {
        if (precioPorNoche <= 0) {
            throw new ReglaNegocioException("El precio por noche debe ser mayor a 0.");
        }
        this.precioPorNoche = precioPorNoche;
    }

    public EstadoHabitacion getEstado() {
        return estado;
    }

    @Override
    public String toString() {
        return "Hab. " + numero + " | " + tipo + " | S/ "
                + String.format("%.2f", precioPorNoche) + " | " + estado;
    }
}