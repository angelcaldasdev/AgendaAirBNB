package Modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import Excepciones.ReglaNegocioException;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Pruebas unitarias de la clase Reserva.
 * No necesitan base de datos: se crean huésped y habitación "de mentira" en memoria.
 *
 * Las fechas se calculan a partir de HOY (LocalDate.now()), porque la regla de negocio
 * no permite reservar con una entrada anterior a hoy. Así las pruebas no caducan con el tiempo.
 */
class ReservaTest {

    private static final double MARGEN_DECIMAL = 0.001;

    private Huesped huespedDePrueba;
    private Habitacion habitacionDePrueba;
    private LocalDate hoy;

    // @BeforeEach se ejecuta antes de CADA prueba: cada una empieza con datos nuevos y limpios
    @BeforeEach
    void prepararDatosBase() {
        huespedDePrueba = new Huesped("Ana Pérez", "12345678", "912345678", "ana@correo.com");
        habitacionDePrueba = new Habitacion("201", TipoHabitacion.DOBLE);
        hoy = LocalDate.now();
    }

    /** Reserva válida de 3 noches: entra dentro de 2 días y sale dentro de 5 días. */
    private Reserva crearReservaValida() {
        return new Reserva("RES-0001", huespedDePrueba, habitacionDePrueba,
                hoy.plusDays(2), hoy.plusDays(5), 1);
    }

    // ---------------------------------------------------------------
    // Creación correcta, noches y total
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Una reserva válida inicia ACTIVA y calcula noches y total a pagar")
    void crearReservaValida_calculaNochesYTotal() {
        // EJECUTAR
        Reserva reserva = crearReservaValida();

        // VERIFICAR
        assertEquals(3, reserva.calcularNoches());
        assertEquals(3 * habitacionDePrueba.getPrecioPorNoche(), reserva.getTotalPagar(), MARGEN_DECIMAL);
        assertEquals(EstadoReserva.ACTIVA, reserva.getEstado());
        assertTrue(reserva.estaActiva());
        assertEquals("RES-0001", reserva.getCodigo());
    }

    @Test
    @DisplayName("El total queda fijo aunque después cambie el precio de la habitación")
    void totalPagar_noCambiaSiSubeElPrecioDeLaHabitacion() {
        // PREPARAR
        Reserva reserva = crearReservaValida();
        double totalOriginal = reserva.getTotalPagar();

        // EJECUTAR
        habitacionDePrueba.setPrecioPorNoche(habitacionDePrueba.getPrecioPorNoche() + 100);

        // VERIFICAR
        assertEquals(totalOriginal, reserva.getTotalPagar(), MARGEN_DECIMAL);
    }

    // ---------------------------------------------------------------
    // Validaciones de fechas
    // ---------------------------------------------------------------

    @Test
    @DisplayName("La entrada puede ser hoy (el límite está permitido)")
    void fechaEntradaHoy_esPermitida() {
        Reserva reserva = new Reserva("RES-0002", huespedDePrueba, habitacionDePrueba,
                hoy, hoy.plusDays(1), 1);

        assertEquals(1, reserva.calcularNoches());
    }

    @Test
    @DisplayName("La entrada no puede ser anterior a hoy")
    void fechaEntradaPasada_lanzaExcepcion() {
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> new Reserva("RES-0003", huespedDePrueba, habitacionDePrueba,
                        hoy.minusDays(1), hoy.plusDays(2), 1));

        assertTrue(error.getMessage().contains("anterior a hoy"));
    }

    @Test
    @DisplayName("La salida debe ser posterior a la entrada (ni el mismo día ni antes)")
    void fechaSalidaNoPosterior_lanzaExcepcion() {
        // Salida el mismo día que la entrada
        assertThrows(ReglaNegocioException.class,
                () -> new Reserva("RES-0004", huespedDePrueba, habitacionDePrueba,
                        hoy.plusDays(2), hoy.plusDays(2), 1));

        // Salida antes de la entrada
        assertThrows(ReglaNegocioException.class,
                () -> new Reserva("RES-0005", huespedDePrueba, habitacionDePrueba,
                        hoy.plusDays(3), hoy.plusDays(2), 1));
    }

    @Test
    @DisplayName("Las fechas no pueden ser nulas")
    void fechasNulas_lanzanExcepcion() {
        assertThrows(ReglaNegocioException.class,
                () -> new Reserva("RES-0006", huespedDePrueba, habitacionDePrueba,
                        null, hoy.plusDays(2), 1));
        assertThrows(ReglaNegocioException.class,
                () -> new Reserva("RES-0007", huespedDePrueba, habitacionDePrueba,
                        hoy.plusDays(1), null, 1));
    }

    // ---------------------------------------------------------------
    // Huésped, habitación y capacidad
    // ---------------------------------------------------------------

    @Test
    @DisplayName("La reserva necesita un huésped y una habitación")
    void huespedOHabitacionNulos_lanzanExcepcion() {
        assertThrows(ReglaNegocioException.class,
                () -> new Reserva("RES-0008", null, habitacionDePrueba,
                        hoy.plusDays(1), hoy.plusDays(2), 1));
        assertThrows(ReglaNegocioException.class,
                () -> new Reserva("RES-0009", huespedDePrueba, null,
                        hoy.plusDays(1), hoy.plusDays(2), 1));
    }

    @Test
    @DisplayName("La cantidad de personas debe ser de 1 hasta la capacidad de la habitación")
    void cantidadPersonasFueraDeRango_lanzaExcepcion() {
        int capacidad = habitacionDePrueba.getTipo().getCapacidadPersonas();

        // Menos de 1 persona
        assertThrows(ReglaNegocioException.class,
                () -> new Reserva("RES-0010", huespedDePrueba, habitacionDePrueba,
                        hoy.plusDays(1), hoy.plusDays(2), 0));

        // Una persona más de las que caben
        assertThrows(ReglaNegocioException.class,
                () -> new Reserva("RES-0011", huespedDePrueba, habitacionDePrueba,
                        hoy.plusDays(1), hoy.plusDays(2), capacidad + 1));
    }

    @Test
    @DisplayName("Se permite exactamente la capacidad máxima de la habitación")
    void cantidadPersonasIgualALaCapacidad_esPermitida() {
        int capacidad = habitacionDePrueba.getTipo().getCapacidadPersonas();

        Reserva reserva = new Reserva("RES-0012", huespedDePrueba, habitacionDePrueba,
                hoy.plusDays(1), hoy.plusDays(2), capacidad);

        assertEquals(capacidad, reserva.getCantidadPersonas());
    }

    // ---------------------------------------------------------------
    // Cancelar y finalizar
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Cancelar una reserva activa cambia su estado a CANCELADA")
    void cancelarReservaActiva_cambiaElEstado() {
        Reserva reserva = crearReservaValida();

        reserva.cancelar();

        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
        assertFalse(reserva.estaActiva());
    }

    @Test
    @DisplayName("Finalizar una reserva activa cambia su estado a FINALIZADA")
    void finalizarReservaActiva_cambiaElEstado() {
        Reserva reserva = crearReservaValida();

        reserva.finalizar();

        assertEquals(EstadoReserva.FINALIZADA, reserva.getEstado());
        assertFalse(reserva.estaActiva());
    }

    @Test
    @DisplayName("Una reserva que ya no está activa no se puede cancelar ni finalizar")
    void reservaNoActiva_noSePuedeCancelarNiFinalizar() {
        // Reserva ya cancelada: no admite cancelar de nuevo ni finalizar
        Reserva reservaCancelada = crearReservaValida();
        reservaCancelada.cancelar();
        assertThrows(ReglaNegocioException.class, reservaCancelada::cancelar);
        assertThrows(ReglaNegocioException.class, reservaCancelada::finalizar);

        // Reserva ya finalizada: tampoco admite cancelar ni finalizar
        Reserva reservaFinalizada = crearReservaValida();
        reservaFinalizada.finalizar();
        assertThrows(ReglaNegocioException.class, reservaFinalizada::cancelar);
        assertThrows(ReglaNegocioException.class, reservaFinalizada::finalizar);
    }

    // ---------------------------------------------------------------
    // Cruce de fechas
    // ---------------------------------------------------------------

    // La reserva de la prueba ocupa desde hoy+2 hasta hoy+5 (el día 5 es de salida y queda libre).
    // Cada fila del @CsvSource es: día de entrada, día de salida, ¿se cruza?
    @ParameterizedTest(name = "Rango de hoy+{0} a hoy+{1} -> se cruza: {2}")
    @CsvSource({
        "3, 4, true",   // totalmente dentro de la reserva
        "1, 3, true",   // se cruza con el inicio
        "4, 7, true",   // se cruza con el final
        "1, 6, true",   // rodea toda la reserva
        "2, 5, true",   // exactamente las mismas fechas
        "5, 8, false",  // entra el mismo día que sale la reserva: permitido
        "0, 2, false",  // sale el mismo día que entra la reserva: permitido
        "0, 1, false"   // termina antes de que empiece la reserva
    })
    @DisplayName("seCruzaCon detecta el cruce de fechas y deja libre el día de salida")
    void seCruzaCon_detectaCrucesDeFechas(int diaEntradaOtra, int diaSalidaOtra, boolean seEsperaCruce) {
        // PREPARAR
        Reserva reserva = crearReservaValida();

        // EJECUTAR
        boolean seCruza = reserva.seCruzaCon(hoy.plusDays(diaEntradaOtra), hoy.plusDays(diaSalidaOtra));

        // VERIFICAR
        assertEquals(seEsperaCruce, seCruza);
    }

    // ---------------------------------------------------------------
    // Reconstruir desde la base de datos
    // ---------------------------------------------------------------

    @Test
    @DisplayName("reconstruir respeta el total y el estado guardados, y acepta fechas pasadas")
    void reconstruir_conservaTotalEstadoYFechasPasadas() {
        // PREPARAR: una reserva antigua, como las que se leen de MySQL
        LocalDate entradaPasada = hoy.minusDays(10);
        LocalDate salidaPasada = hoy.minusDays(7);

        // EJECUTAR: no debe lanzar excepción aunque las fechas ya pasaron
        Reserva reservaGuardada = Reserva.reconstruir("RES-0099", huespedDePrueba, habitacionDePrueba,
                entradaPasada, salidaPasada, 2, 999.0, EstadoReserva.FINALIZADA);

        // VERIFICAR
        assertEquals("RES-0099", reservaGuardada.getCodigo());
        assertEquals(999.0, reservaGuardada.getTotalPagar(), MARGEN_DECIMAL);
        assertEquals(EstadoReserva.FINALIZADA, reservaGuardada.getEstado());
        assertEquals(3, reservaGuardada.calcularNoches());
    }
}
