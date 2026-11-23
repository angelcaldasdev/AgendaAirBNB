package Servicio;

import Excepciones.ReglaNegocioException;
import Modelo.Habitacion;
import Modelo.Huesped;
import Modelo.Reserva;
import Modelo.TipoHabitacion;
import Persistencia.ConexionBD;
import Persistencia.HabitacionRepositorio;
import Persistencia.HuespedRepositorio;
import Persistencia.ReservaRepositorio;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Contiene la lógica de negocio del hotel: registro de huéspedes,
 * consulta de habitaciones disponibles y gestión de reservas.
 * Los datos se guardan en MySQL a través de los repositorios.
 */
public class GestorReservas {

    private final HuespedRepositorio huespedRepositorio = new HuespedRepositorio();
    private final HabitacionRepositorio habitacionRepositorio = new HabitacionRepositorio();
    private final ReservaRepositorio reservaRepositorio =
            new ReservaRepositorio(huespedRepositorio, habitacionRepositorio);

    public GestorReservas() {
        // Crea las tablas si es la primera vez que se ejecuta el sistema
        ConexionBD.inicializarBaseDeDatos();

        // Si el hotel aún no tiene habitaciones, se cargan las iniciales
        if (habitacionRepositorio.contar() == 0) {
            cargarHabitacionesIniciales();
        }
    }

    /** Habitaciones con las que "abre" el hotel. Solo se cargan una vez. */
    private void cargarHabitacionesIniciales() {
        habitacionRepositorio.guardar(new Habitacion("101", TipoHabitacion.SIMPLE));
        habitacionRepositorio.guardar(new Habitacion("102", TipoHabitacion.SIMPLE));
        habitacionRepositorio.guardar(new Habitacion("103", TipoHabitacion.DOBLE));
        habitacionRepositorio.guardar(new Habitacion("104", TipoHabitacion.DOBLE));
        habitacionRepositorio.guardar(new Habitacion("201", TipoHabitacion.DOBLE));
        habitacionRepositorio.guardar(new Habitacion("202", TipoHabitacion.FAMILIAR));
        habitacionRepositorio.guardar(new Habitacion("301", TipoHabitacion.SUITE));
        habitacionRepositorio.guardar(new Habitacion("302", TipoHabitacion.SUITE));
    }

    // ---------------------------------------------------------------
    // FUNCIONALIDAD 1: Registrar huésped
    // ---------------------------------------------------------------
    public Huesped registrarHuesped(String nombreCompleto, String dni, String telefono, String correo) {
        // Las validaciones de formato las hace el constructor de Huesped
        Huesped nuevoHuesped = new Huesped(nombreCompleto, dni, telefono, correo);

        if (huespedRepositorio.buscarPorDni(dni) != null) {
            throw new ReglaNegocioException("Ya existe un huésped registrado con el DNI " + dni + ".");
        }
        huespedRepositorio.guardar(nuevoHuesped);
        return nuevoHuesped;
    }

    public Huesped buscarHuespedPorDni(String dni) {
        return huespedRepositorio.buscarPorDni(dni);
    }

    // ---------------------------------------------------------------
    // FUNCIONALIDAD 2: Consultar habitaciones disponibles
    // ---------------------------------------------------------------
    public ArrayList<Habitacion> obtenerHabitacionesDisponibles(LocalDate fechaEntrada,
                                                                LocalDate fechaSalida,
                                                                int cantidadPersonas) {
        if (fechaEntrada == null || fechaSalida == null || !fechaSalida.isAfter(fechaEntrada)) {
            throw new ReglaNegocioException("El rango de fechas no es válido.");
        }

        ArrayList<Habitacion> habitacionesLibres = new ArrayList<>();
        for (Habitacion habitacionActual : habitacionRepositorio.listarTodas()) {
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

        Huesped huespedReserva = huespedRepositorio.buscarPorDni(dni);
        if (huespedReserva == null) {
            throw new ReglaNegocioException("No existe un huésped registrado con el DNI " + dni + ".");
        }

        // Regla: máximo una reserva activa por huésped
        if (reservaRepositorio.buscarActivaDeHuesped(dni) != null) {
            throw new ReglaNegocioException("El huésped ya tiene una reserva activa.");
        }

        Habitacion habitacionReserva = habitacionRepositorio.buscarPorNumero(numeroHabitacion);
        if (habitacionReserva == null) {
            throw new ReglaNegocioException("La habitación " + numeroHabitacion + " no existe.");
        }

        // Regla: una habitación no puede tener dos reservas con fechas cruzadas
        if (fechaEntrada != null && fechaSalida != null
                && tieneCruceDeFechas(habitacionReserva, fechaEntrada, fechaSalida)) {
            throw new ReglaNegocioException("La habitación " + numeroHabitacion
                    + " ya está reservada en esas fechas.");
        }

        // El código sigue la cantidad de reservas guardadas (las reservas nunca se borran)
        String codigoReserva = String.format("RES-%04d", reservaRepositorio.contar() + 1);

        // Reserva valida fechas y capacidad por dentro
        Reserva nuevaReserva = new Reserva(codigoReserva, huespedReserva, habitacionReserva,
                fechaEntrada, fechaSalida, cantidadPersonas);

        reservaRepositorio.guardar(nuevaReserva);
        actualizarEstadoHabitacion(habitacionReserva);
        return nuevaReserva;
    }

    // ---------------------------------------------------------------
    // FUNCIONALIDAD 4: Cancelar o finalizar reserva
    // ---------------------------------------------------------------
    public Reserva cancelarReserva(String codigoReserva) {
        Reserva reservaACancelar = obtenerReservaPorCodigo(codigoReserva);
        reservaACancelar.cancelar();
        reservaRepositorio.actualizarEstado(reservaACancelar);
        actualizarEstadoHabitacion(reservaACancelar.getHabitacion());
        return reservaACancelar;
    }

    public Reserva finalizarReserva(String codigoReserva) {
        Reserva reservaAFinalizar = obtenerReservaPorCodigo(codigoReserva);
        reservaAFinalizar.finalizar();
        reservaRepositorio.actualizarEstado(reservaAFinalizar);
        actualizarEstadoHabitacion(reservaAFinalizar.getHabitacion());
        return reservaAFinalizar;
    }

    // ---------------------------------------------------------------
    // Métodos de apoyo
    // ---------------------------------------------------------------
    public Reserva obtenerReservaPorCodigo(String codigoReserva) {
        Reserva reservaEncontrada = reservaRepositorio.buscarPorCodigo(codigoReserva);
        if (reservaEncontrada == null) {
            throw new ReglaNegocioException("No existe la reserva con código " + codigoReserva + ".");
        }
        return reservaEncontrada;
    }

    public Reserva buscarReservaActivaDeHuesped(String dni) {
        return reservaRepositorio.buscarActivaDeHuesped(dni);
    }

    /** Revisa si alguna reserva ACTIVA de la habitación se cruza con las fechas dadas. */
    private boolean tieneCruceDeFechas(Habitacion habitacion, LocalDate fechaEntrada, LocalDate fechaSalida) {
        for (Reserva reservaActual : reservaRepositorio.listarActivasPorHabitacion(habitacion.getNumero())) {
            if (reservaActual.seCruzaCon(fechaEntrada, fechaSalida)) {
                return true;
            }
        }
        return false;
    }

    /** La habitación queda RESERVADA si tiene al menos una reserva activa; si no, DISPONIBLE. */
    private void actualizarEstadoHabitacion(Habitacion habitacion) {
        boolean tieneReservaActiva =
                !reservaRepositorio.listarActivasPorHabitacion(habitacion.getNumero()).isEmpty();

        if (tieneReservaActiva) {
            habitacion.marcarComoReservada();
        } else {
            habitacion.marcarComoDisponible();
        }
        habitacionRepositorio.actualizarEstado(habitacion);
    }

    public ArrayList<Habitacion> getListaHabitaciones() {
        return habitacionRepositorio.listarTodas();
    }

    public ArrayList<Reserva> getListaReservas() {
        return reservaRepositorio.listarTodas();
    }

    public ArrayList<Huesped> getListaHuespedes() {
        return huespedRepositorio.listarTodos();
    }
}