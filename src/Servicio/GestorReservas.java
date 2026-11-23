package Servicio;

import Excepciones.ReglaNegocioException;
import Modelo.Habitacion;
import Modelo.Huesped;
import Modelo.Reserva;
import Modelo.TipoHabitacion;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Contiene toda la lógica de negocio del hotel: registro de huéspedes,
 * consulta de habitaciones disponibles y gestión de reservas.
 * Por ahora los datos viven en memoria (listas).
 */
public class GestorReservas {

    private final ArrayList<Huesped> listaHuespedes = new ArrayList<>();
    private final ArrayList<Habitacion> listaHabitaciones = new ArrayList<>();
    private final ArrayList<Reserva> listaReservas = new ArrayList<>();
    private int contadorReservas = 0;

    public GestorReservas() {
        cargarHabitacionesIniciales();
    }

    /** Habitaciones con las que "abre" el hotel. */
    private void cargarHabitacionesIniciales() {
        listaHabitaciones.add(new Habitacion("101", TipoHabitacion.SIMPLE));
        listaHabitaciones.add(new Habitacion("102", TipoHabitacion.SIMPLE));
        listaHabitaciones.add(new Habitacion("103", TipoHabitacion.DOBLE));
        listaHabitaciones.add(new Habitacion("104", TipoHabitacion.DOBLE));
        listaHabitaciones.add(new Habitacion("201", TipoHabitacion.DOBLE));
        listaHabitaciones.add(new Habitacion("202", TipoHabitacion.FAMILIAR));
        listaHabitaciones.add(new Habitacion("301", TipoHabitacion.SUITE));
        listaHabitaciones.add(new Habitacion("302", TipoHabitacion.SUITE));
    }

    // ---------------------------------------------------------------
    // FUNCIONALIDAD 1: Registrar huésped
    // ---------------------------------------------------------------
    public Huesped registrarHuesped(String nombreCompleto, String dni, String telefono, String correo) {
        // Las validaciones de formato las hace el constructor de Huesped
        Huesped nuevoHuesped = new Huesped(nombreCompleto, dni, telefono, correo);

        if (buscarHuespedPorDni(dni) != null) {
            throw new ReglaNegocioException("Ya existe un huésped registrado con el DNI " + dni + ".");
        }
        listaHuespedes.add(nuevoHuesped);
        return nuevoHuesped;
    }

    public Huesped buscarHuespedPorDni(String dni) {
        for (Huesped huespedActual : listaHuespedes) {
            if (huespedActual.getDni().equals(dni)) {
                return huespedActual;
            }
        }
        return null;
    }

    // ---------------------------------------------------------------
    // FUNCIONALIDAD 2: Consultar habitaciones disponibles
    // ---------------------------------------------------------------
    /**
     * Devuelve las habitaciones libres en un rango de fechas y con capacidad
     * suficiente para la cantidad de personas.
     */
    public ArrayList<Habitacion> obtenerHabitacionesDisponibles(LocalDate fechaEntrada,
                                                                LocalDate fechaSalida,
                                                                int cantidadPersonas) {
        if (fechaEntrada == null || fechaSalida == null || !fechaSalida.isAfter(fechaEntrada)) {
            throw new ReglaNegocioException("El rango de fechas no es válido.");
        }

        ArrayList<Habitacion> habitacionesLibres = new ArrayList<>();
        for (Habitacion habitacionActual : listaHabitaciones) {
            boolean tieneCapacidad = habitacionActual.getTipo().getCapacidadPersonas() >= cantidadPersonas;
            if (tieneCapacidad && !tieneCruceDeFechas(habitacionActual, fechaEntrada, fechaSalida)) {
                habitacionesLibres.add(habitacionActual);
            }
        }
        return habitacionesLibres;
    }

    // ---------------------------------------------------------------
    // FUNCIONALIDAD 3: Crear reserva
    // ---------------------------------------------------------------
    public Reserva crearReserva(String dni, String numeroHabitacion,
                                LocalDate fechaEntrada, LocalDate fechaSalida, int cantidadPersonas) {

        Huesped huespedReserva = buscarHuespedPorDni(dni);
        if (huespedReserva == null) {
            throw new ReglaNegocioException("No existe un huésped registrado con el DNI " + dni + ".");
        }

        // Regla: máximo una reserva activa por huésped
        if (buscarReservaActivaDeHuesped(dni) != null) {
            throw new ReglaNegocioException("El huésped ya tiene una reserva activa.");
        }

        Habitacion habitacionReserva = buscarHabitacionPorNumero(numeroHabitacion);
        if (habitacionReserva == null) {
            throw new ReglaNegocioException("La habitación " + numeroHabitacion + " no existe.");
        }

        // Regla: una habitación no puede tener dos reservas con fechas cruzadas
        if (fechaEntrada != null && fechaSalida != null
                && tieneCruceDeFechas(habitacionReserva, fechaEntrada, fechaSalida)) {
            throw new ReglaNegocioException("La habitación " + numeroHabitacion
                    + " ya está reservada en esas fechas.");
        }

        // Reserva valida fechas y capacidad por dentro
        contadorReservas++;
        String codigoReserva = String.format("RES-%04d", contadorReservas);
        Reserva nuevaReserva = new Reserva(codigoReserva, huespedReserva, habitacionReserva,
                fechaEntrada, fechaSalida, cantidadPersonas);

        listaReservas.add(nuevaReserva);
        actualizarEstadoHabitacion(habitacionReserva);
        return nuevaReserva;
    }

    // ---------------------------------------------------------------
    // FUNCIONALIDAD 4: Cancelar o finalizar reserva
    // ---------------------------------------------------------------
    public Reserva cancelarReserva(String codigoReserva) {
        Reserva reservaACancelar = obtenerReservaPorCodigo(codigoReserva);
        reservaACancelar.cancelar();
        actualizarEstadoHabitacion(reservaACancelar.getHabitacion());
        return reservaACancelar;
    }

    public Reserva finalizarReserva(String codigoReserva) {
        Reserva reservaAFinalizar = obtenerReservaPorCodigo(codigoReserva);
        reservaAFinalizar.finalizar();
        actualizarEstadoHabitacion(reservaAFinalizar.getHabitacion());
        return reservaAFinalizar;
    }

    // ---------------------------------------------------------------
    // Métodos de apoyo
    // ---------------------------------------------------------------
    public Reserva obtenerReservaPorCodigo(String codigoReserva) {
        for (Reserva reservaActual : listaReservas) {
            if (reservaActual.getCodigo().equalsIgnoreCase(codigoReserva)) {
                return reservaActual;
            }
        }
        throw new ReglaNegocioException("No existe la reserva con código " + codigoReserva + ".");
    }

    public Reserva buscarReservaActivaDeHuesped(String dni) {
        for (Reserva reservaActual : listaReservas) {
            if (reservaActual.estaActiva() && reservaActual.getHuesped().getDni().equals(dni)) {
                return reservaActual;
            }
        }
        return null;
    }

    private Habitacion buscarHabitacionPorNumero(String numeroHabitacion) {
        for (Habitacion habitacionActual : listaHabitaciones) {
            if (habitacionActual.getNumero().equals(numeroHabitacion)) {
                return habitacionActual;
            }
        }
        return null;
    }

    /** Revisa si alguna reserva ACTIVA de la habitación se cruza con las fechas dadas. */
    private boolean tieneCruceDeFechas(Habitacion habitacion, LocalDate fechaEntrada, LocalDate fechaSalida) {
        for (Reserva reservaActual : listaReservas) {
            if (reservaActual.estaActiva()
                    && reservaActual.getHabitacion().equals(habitacion)
                    && reservaActual.seCruzaCon(fechaEntrada, fechaSalida)) {
                return true;
            }
        }
        return false;
    }

    /** La habitación queda RESERVADA si tiene al menos una reserva activa; si no, DISPONIBLE. */
    private void actualizarEstadoHabitacion(Habitacion habitacion) {
        boolean tieneReservaActiva = false;
        for (Reserva reservaActual : listaReservas) {
            if (reservaActual.estaActiva() && reservaActual.getHabitacion().equals(habitacion)) {
                tieneReservaActiva = true;
            }
        }
        if (tieneReservaActiva) {
            habitacion.marcarComoReservada();
        } else {
            habitacion.marcarComoDisponible();
        }
    }

    public ArrayList<Habitacion> getListaHabitaciones() {
        return new ArrayList<>(listaHabitaciones);
    }

    public ArrayList<Reserva> getListaReservas() {
        return new ArrayList<>(listaReservas);
    }

    public ArrayList<Huesped> getListaHuespedes() {
        return new ArrayList<>(listaHuespedes);
    }
}