package Modelo;

import Excepciones.ReglaNegocioException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Representa la reserva de una habitación por parte de un huésped
 * para un rango de fechas. Calcula las noches y el total a pagar.
 */
public class Reserva {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final String codigo;
    private final Huesped huesped;
    private final Habitacion habitacion;
    private final LocalDate fechaEntrada;
    private final LocalDate fechaSalida;
    private final int cantidadPersonas;
    private final double totalPagar;
    private EstadoReserva estado;

    public Reserva(String codigo, Huesped huesped, Habitacion habitacion,
                   LocalDate fechaEntrada, LocalDate fechaSalida, int cantidadPersonas) {

        // Validaciones de las reglas de negocio
        if (huesped == null) {
            throw new ReglaNegocioException("La reserva debe tener un huésped.");
        }
        if (habitacion == null) {
            throw new ReglaNegocioException("La reserva debe tener una habitación.");
        }
        if (fechaEntrada == null || fechaSalida == null) {
            throw new ReglaNegocioException("Debe indicar la fecha de entrada y la de salida.");
        }
        if (fechaEntrada.isBefore(LocalDate.now())) {
            throw new ReglaNegocioException("La fecha de entrada no puede ser anterior a hoy.");
        }
        if (!fechaSalida.isAfter(fechaEntrada)) {
            throw new ReglaNegocioException("La fecha de salida debe ser posterior a la fecha de entrada.");
        }
        if (cantidadPersonas < 1 || cantidadPersonas > habitacion.getTipo().getCapacidadPersonas()) {
            throw new ReglaNegocioException("La habitación " + habitacion.getNumero()
                    + " admite de 1 a " + habitacion.getTipo().getCapacidadPersonas() + " persona(s).");
        }

        this.codigo = codigo;
        this.huesped = huesped;
        this.habitacion = habitacion;
        this.fechaEntrada = fechaEntrada;
        this.fechaSalida = fechaSalida;
        this.cantidadPersonas = cantidadPersonas;
        this.estado = EstadoReserva.ACTIVA;

        // El total se fija al crear la reserva con el precio vigente de ese momento
        this.totalPagar = calcularNoches() * habitacion.getPrecioPorNoche();
    }

    /** Cantidad de noches entre la entrada y la salida. */
    public int calcularNoches() {
        return (int) ChronoUnit.DAYS.between(fechaEntrada, fechaSalida);
    }

    public boolean estaActiva() {
        return estado == EstadoReserva.ACTIVA;
    }

    /**
     * Indica si esta reserva se cruza con otro rango de fechas.
     * El día de salida queda libre: alguien puede entrar ese mismo día.
     */
    public boolean seCruzaCon(LocalDate otraEntrada, LocalDate otraSalida) {
        return otraEntrada.isBefore(fechaSalida) && otraSalida.isAfter(fechaEntrada);
    }

    public void cancelar() {
        if (!estaActiva()) {
            throw new ReglaNegocioException("Solo se puede cancelar una reserva activa. Estado actual: " + estado);
        }
        this.estado = EstadoReserva.CANCELADA;
    }

    public void finalizar() {
        if (!estaActiva()) {
            throw new ReglaNegocioException("Solo se puede finalizar una reserva activa. Estado actual: " + estado);
        }
        this.estado = EstadoReserva.FINALIZADA;
    }

    public String getCodigo() {
        return codigo;
    }

    public Huesped getHuesped() {
        return huesped;
    }

    public Habitacion getHabitacion() {
        return habitacion;
    }

    public LocalDate getFechaEntrada() {
        return fechaEntrada;
    }

    public LocalDate getFechaSalida() {
        return fechaSalida;
    }

    public int getCantidadPersonas() {
        return cantidadPersonas;
    }

    public double getTotalPagar() {
        return totalPagar;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    @Override
    public String toString() {
        return codigo + " | " + huesped.getNombreCompleto()
                + " | Hab. " + habitacion.getNumero()
                + " | " + fechaEntrada.format(FORMATO_FECHA) + " al " + fechaSalida.format(FORMATO_FECHA)
                + " | " + calcularNoches() + " noche(s)"
                + " | S/ " + String.format("%.2f", totalPagar)
                + " | " + estado;
    }
}