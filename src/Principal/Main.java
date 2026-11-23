package Principal;

import Excepciones.ReglaNegocioException;
import Modelo.Reserva;
import Servicio.GestorReservas;
import java.time.LocalDate;

public class Main {

    public static void main(String[] args) {

        GestorReservas gestorReservas = new GestorReservas();
        LocalDate hoy = LocalDate.now();

        try {
            gestorReservas.registrarHuesped("Angel Caldas", "12345678", "987654321", "angel@correo.com");
            gestorReservas.registrarHuesped("Maria Lopez", "87654321", "912345678", "maria@correo.com");

            // Prueba 1: reserva válida
            Reserva primeraReserva = gestorReservas.crearReserva("12345678", "103",
                    hoy.plusDays(1), hoy.plusDays(4), 2);
            System.out.println("OK: " + primeraReserva);

            // Prueba 2: otra persona intenta la misma habitación en fechas cruzadas
            try {
                gestorReservas.crearReserva("87654321", "103", hoy.plusDays(2), hoy.plusDays(5), 2);
            } catch (ReglaNegocioException error) {
                System.out.println("Error esperado: " + error.getMessage());
            }

            // Prueba 3: el mismo huésped intenta una segunda reserva activa
            try {
                gestorReservas.crearReserva("12345678", "301", hoy.plusDays(10), hoy.plusDays(12), 2);
            } catch (ReglaNegocioException error) {
                System.out.println("Error esperado: " + error.getMessage());
            }

            // Prueba 4: cancelar y volver a reservar la misma habitación
            gestorReservas.cancelarReserva(primeraReserva.getCodigo());
            System.out.println("Cancelada: " + primeraReserva);

            Reserva segundaReserva = gestorReservas.crearReserva("87654321", "103",
                    hoy.plusDays(2), hoy.plusDays(5), 2);
            System.out.println("OK: " + segundaReserva);

        } catch (ReglaNegocioException error) {
            System.out.println("Error inesperado: " + error.getMessage());
        }
    }
}