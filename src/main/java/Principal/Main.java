package Principal;

import Excepciones.ErrorBaseDatosException;
import Excepciones.ReglaNegocioException;
import Modelo.Habitacion;
import Modelo.Reserva;
import Servicio.GestorReservas;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Punto de entrada del sistema de reservas del hotel.
 * Muestra un menú por consola para que el recepcionista gestione
 * huéspedes y reservas.
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static GestorReservas gestorReservas = new GestorReservas();

    public static void main(String[] args) {

        int opcionElegida;
        
        try {
            gestorReservas = new GestorReservas();
        } catch (ErrorBaseDatosException error) {
            System.out.println("ERROR: " + error.getMessage());
            return;
        }

        do {
            mostrarMenu();
            opcionElegida = leerEntero("Elige una opción: ");

            try {
                switch (opcionElegida) {
                    case 1:
                        registrarHuesped();
                        break;
                    case 2:
                        consultarHabitacionesDisponibles();
                        break;
                    case 3:
                        crearReserva();
                        break;
                    case 4:
                        cancelarReserva();
                        break;
                    case 5:
                        finalizarReserva();
                        break;
                    case 6:
                        verReservas();
                        break;
                    case 7:
                        verHabitaciones();
                        break;
                    case 0:
                        System.out.println("Gracias por usar el sistema. ¡Hasta pronto!");
                        break;
                    default:
                        System.out.println("Opción no válida. Elige un número del menú.");
                }
            } catch (ReglaNegocioException | ErrorBaseDatosException error) {
                // Cualquier regla de negocio incumplida llega aquí con su mensaje
                System.out.println("ERROR: " + error.getMessage());
            }

        } while (opcionElegida != 0);
    }

    private static void mostrarMenu() {
        System.out.println("\n===== SISTEMA DE RESERVAS DEL HOTEL =====");
        System.out.println("1. Registrar huésped");
        System.out.println("2. Consultar habitaciones disponibles");
        System.out.println("3. Crear reserva");
        System.out.println("4. Cancelar reserva");
        System.out.println("5. Finalizar reserva (check-out)");
        System.out.println("6. Ver todas las reservas");
        System.out.println("7. Ver estado de las habitaciones");
        System.out.println("0. Salir");
    }

    // ---------------- Opciones del menú ----------------

    private static void registrarHuesped() {
        String nombreCompleto = leerTexto("Nombre completo: ");
        String dni = leerTexto("DNI (8 dígitos): ");
        String telefono = leerTexto("Teléfono (9 dígitos): ");
        String correo = leerTexto("Correo electrónico: ");

        System.out.println("Huésped registrado: "
                + gestorReservas.registrarHuesped(nombreCompleto, dni, telefono, correo));
    }

    private static void consultarHabitacionesDisponibles() {
        LocalDate fechaEntrada = leerFecha("Fecha de entrada");
        LocalDate fechaSalida = leerFecha("Fecha de salida");
        int cantidadPersonas = leerEntero("Cantidad de personas: ");

        ArrayList<Habitacion> habitacionesLibres =
                gestorReservas.obtenerHabitacionesDisponibles(fechaEntrada, fechaSalida, cantidadPersonas);

        if (habitacionesLibres.isEmpty()) {
            System.out.println("No hay habitaciones disponibles para esos criterios.");
        } else {
            System.out.println("Habitaciones disponibles:");
            for (Habitacion habitacionActual : habitacionesLibres) {
                System.out.println(" - " + habitacionActual);
            }
        }
    }

    private static void crearReserva() {
        String dni = leerTexto("DNI del huésped: ");
        String numeroHabitacion = leerTexto("Número de habitación (solo el número, ej. 103): ");
        LocalDate fechaEntrada = leerFecha("Fecha de entrada");
        LocalDate fechaSalida = leerFecha("Fecha de salida");
        int cantidadPersonas = leerEntero("Cantidad de personas: ");

        Reserva nuevaReserva = gestorReservas.crearReserva(dni, numeroHabitacion,
                fechaEntrada, fechaSalida, cantidadPersonas);
        System.out.println("Reserva confirmada: " + nuevaReserva);
    }

    private static void cancelarReserva() {
        String codigoReserva = leerTexto("Código de la reserva (ej. RES-0001): ");
        System.out.println("Reserva cancelada: " + gestorReservas.cancelarReserva(codigoReserva));
    }

    private static void finalizarReserva() {
        String codigoReserva = leerTexto("Código de la reserva (ej. RES-0001): ");
        System.out.println("Reserva finalizada: " + gestorReservas.finalizarReserva(codigoReserva));
    }

    private static void verReservas() {
        ArrayList<Reserva> todasLasReservas = gestorReservas.getListaReservas();
        if (todasLasReservas.isEmpty()) {
            System.out.println("Aún no hay reservas registradas.");
        } else {
            for (Reserva reservaActual : todasLasReservas) {
                System.out.println(" - " + reservaActual);
            }
        }
    }

    private static void verHabitaciones() {
        for (Habitacion habitacionActual : gestorReservas.getListaHabitaciones()) {
            System.out.println(" - " + habitacionActual);
        }
    }

    // ---------------- Métodos de lectura segura ----------------

    private static String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return scanner.nextLine().trim();
    }

    /** Repite la pregunta hasta que el usuario ingrese un número entero. */
    private static int leerEntero(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException error) {
                System.out.println("Debes ingresar un número entero.");
            }
        }
    }

    /** Repite la pregunta hasta que el usuario ingrese una fecha con formato dd/MM/yyyy. */
    private static LocalDate leerFecha(String mensaje) {
        while (true) {
            System.out.print(mensaje + " (dd/MM/yyyy): ");
            try {
                return LocalDate.parse(scanner.nextLine().trim(), FORMATO_FECHA);
            } catch (DateTimeParseException error) {
                System.out.println("Fecha inválida. Usa el formato dd/MM/yyyy, por ejemplo 25/12/2026.");
            }
        }
    }
}