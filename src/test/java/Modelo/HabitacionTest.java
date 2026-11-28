package Modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import Excepciones.ReglaNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias de la clase Habitacion.
 * No necesitan base de datos.
 */
class HabitacionTest {

    // Diferencia máxima aceptada al comparar números decimales (double)
    private static final double MARGEN_DECIMAL = 0.001;

    @Test
    @DisplayName("Una habitación nueva queda disponible y toma el precio base de su tipo")
    void crearHabitacionValida_iniciaDisponibleConPrecioDelTipo() {
        // EJECUTAR
        Habitacion habitacion = new Habitacion("  101  ", TipoHabitacion.SIMPLE);

        // VERIFICAR
        assertEquals("101", habitacion.getNumero()); // se quitan los espacios
        assertEquals(TipoHabitacion.SIMPLE, habitacion.getTipo());
        assertEquals(TipoHabitacion.SIMPLE.getPrecioBasePorNoche(),
                habitacion.getPrecioPorNoche(), MARGEN_DECIMAL);
        assertEquals(EstadoHabitacion.DISPONIBLE, habitacion.getEstado());
        assertTrue(habitacion.estaDisponible());
    }

    @Test
    @DisplayName("El número de la habitación no puede ser nulo, vacío ni solo espacios")
    void numeroInvalido_lanzaExcepcion() {
        assertThrows(ReglaNegocioException.class, () -> new Habitacion(null, TipoHabitacion.DOBLE));
        assertThrows(ReglaNegocioException.class, () -> new Habitacion("", TipoHabitacion.DOBLE));
        assertThrows(ReglaNegocioException.class, () -> new Habitacion("   ", TipoHabitacion.DOBLE));
    }

    @Test
    @DisplayName("La habitación debe tener un tipo")
    void tipoNulo_lanzaExcepcion() {
        assertThrows(ReglaNegocioException.class, () -> new Habitacion("101", null));
    }

    @Test
    @DisplayName("Marcar como reservada y volver a disponible cambia el estado")
    void cambiarEstado_reservadaYDisponible() {
        // PREPARAR
        Habitacion habitacion = new Habitacion("201", TipoHabitacion.DOBLE);

        // EJECUTAR Y VERIFICAR: primero reservada...
        habitacion.marcarComoReservada();
        assertEquals(EstadoHabitacion.RESERVADA, habitacion.getEstado());
        assertFalse(habitacion.estaDisponible());

        // ...y luego disponible otra vez
        habitacion.marcarComoDisponible();
        assertEquals(EstadoHabitacion.DISPONIBLE, habitacion.getEstado());
        assertTrue(habitacion.estaDisponible());
    }

    @Test
    @DisplayName("Un precio mayor a 0 se puede asignar")
    void setPrecioValido_actualizaElPrecio() {
        Habitacion habitacion = new Habitacion("301", TipoHabitacion.SUITE);

        habitacion.setPrecioPorNoche(450.50);

        assertEquals(450.50, habitacion.getPrecioPorNoche(), MARGEN_DECIMAL);
    }

    @Test
    @DisplayName("Un precio de 0 o negativo lanza excepción y conserva el precio anterior")
    void setPrecioInvalido_noModificaElPrecio() {
        // PREPARAR
        Habitacion habitacion = new Habitacion("301", TipoHabitacion.SUITE);
        double precioOriginal = habitacion.getPrecioPorNoche();

        // EJECUTAR Y VERIFICAR
        assertThrows(ReglaNegocioException.class, () -> habitacion.setPrecioPorNoche(0));
        assertThrows(ReglaNegocioException.class, () -> habitacion.setPrecioPorNoche(-50));
        assertEquals(precioOriginal, habitacion.getPrecioPorNoche(), MARGEN_DECIMAL);
    }
}
