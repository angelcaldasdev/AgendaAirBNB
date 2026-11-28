package Modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import Excepciones.ReglaNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Pruebas unitarias de la clase Huesped.
 * No necesitan base de datos: solo comprueban las reglas de validación del modelo.
 *
 * Cada prueba sigue tres pasos: PREPARAR los datos, EJECUTAR la acción y VERIFICAR el resultado.
 */
class HuespedTest {

    // Datos válidos que reutilizamos en varias pruebas
    private static final String NOMBRE_VALIDO = "Ana Pérez";
    private static final String DNI_VALIDO = "12345678";
    private static final String TELEFONO_VALIDO = "912345678";
    private static final String CORREO_VALIDO = "ana@correo.com";

    // ---------------------------------------------------------------
    // Creación correcta
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Un huésped con datos válidos se crea y se quitan los espacios sobrantes")
    void crearHuespedValido_guardaLosDatosSinEspacios() {
        // EJECUTAR
        Huesped huesped = new Huesped("  Ana Pérez  ", DNI_VALIDO, TELEFONO_VALIDO, "  ana@correo.com  ");

        // VERIFICAR
        assertEquals("Ana Pérez", huesped.getNombreCompleto());
        assertEquals(DNI_VALIDO, huesped.getDni());
        assertEquals(TELEFONO_VALIDO, huesped.getTelefono());
        assertEquals("ana@correo.com", huesped.getCorreo());
    }

    // ---------------------------------------------------------------
    // DNI
    // ---------------------------------------------------------------

    // @ParameterizedTest ejecuta la MISMA prueba una vez por cada valor de la lista
    @ParameterizedTest(name = "DNI inválido: \"{0}\"")
    @ValueSource(strings = {"1234567", "123456789", "1234567a", "abcdefgh", ""})
    @DisplayName("Un DNI que no tiene exactamente 8 dígitos numéricos es rechazado")
    void dniInvalido_lanzaExcepcion(String dniInvalido) {
        assertThrows(ReglaNegocioException.class,
                () -> new Huesped(NOMBRE_VALIDO, dniInvalido, TELEFONO_VALIDO, CORREO_VALIDO));
    }

    @Test
    @DisplayName("esDniValido acepta 8 dígitos y rechaza todo lo demás")
    void esDniValido_reconoceFormatosCorrectosEIncorrectos() {
        assertTrue(Huesped.esDniValido("12345678"));
        assertFalse(Huesped.esDniValido("1234567"));
        assertFalse(Huesped.esDniValido("1234567a"));
        assertFalse(Huesped.esDniValido(null));
    }

    // ---------------------------------------------------------------
    // Teléfono
    // ---------------------------------------------------------------

    @ParameterizedTest(name = "Teléfono inválido: \"{0}\"")
    @ValueSource(strings = {"812345678", "91234567", "9123456789", "91234567a", ""})
    @DisplayName("Un teléfono que no tiene 9 dígitos y empieza con 9 es rechazado")
    void telefonoInvalido_lanzaExcepcion(String telefonoInvalido) {
        assertThrows(ReglaNegocioException.class,
                () -> new Huesped(NOMBRE_VALIDO, DNI_VALIDO, telefonoInvalido, CORREO_VALIDO));
    }

    @Test
    @DisplayName("Cambiar el teléfono por uno inválido lanza excepción y conserva el anterior")
    void setTelefonoInvalido_noModificaElTelefono() {
        // PREPARAR
        Huesped huesped = new Huesped(NOMBRE_VALIDO, DNI_VALIDO, TELEFONO_VALIDO, CORREO_VALIDO);

        // EJECUTAR Y VERIFICAR
        assertThrows(ReglaNegocioException.class, () -> huesped.setTelefono("123"));
        assertEquals(TELEFONO_VALIDO, huesped.getTelefono());
    }

    // ---------------------------------------------------------------
    // Correo
    // ---------------------------------------------------------------

    @ParameterizedTest(name = "Correo inválido: \"{0}\"")
    @ValueSource(strings = {"ana.correo.com", "ana@", "@correo.com", "ana@correo", "ana perez@correo.com"})
    @DisplayName("Un correo con formato incorrecto es rechazado")
    void correoInvalido_lanzaExcepcion(String correoInvalido) {
        assertThrows(ReglaNegocioException.class,
                () -> new Huesped(NOMBRE_VALIDO, DNI_VALIDO, TELEFONO_VALIDO, correoInvalido));
    }

    // ---------------------------------------------------------------
    // Nombre
    // ---------------------------------------------------------------

    @ParameterizedTest(name = "Nombre inválido: \"{0}\"")
    @ValueSource(strings = {"", "   ", "Al"})
    @DisplayName("Un nombre con menos de 3 caracteres es rechazado")
    void nombreMuyCorto_lanzaExcepcion(String nombreInvalido) {
        assertThrows(ReglaNegocioException.class,
                () -> new Huesped(nombreInvalido, DNI_VALIDO, TELEFONO_VALIDO, CORREO_VALIDO));
    }

    // ---------------------------------------------------------------
    // Valores nulos
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Ningún dato puede ser nulo")
    void datosNulos_lanzanExcepcion() {
        assertThrows(ReglaNegocioException.class,
                () -> new Huesped(null, DNI_VALIDO, TELEFONO_VALIDO, CORREO_VALIDO));
        assertThrows(ReglaNegocioException.class,
                () -> new Huesped(NOMBRE_VALIDO, null, TELEFONO_VALIDO, CORREO_VALIDO));
        assertThrows(ReglaNegocioException.class,
                () -> new Huesped(NOMBRE_VALIDO, DNI_VALIDO, null, CORREO_VALIDO));
        assertThrows(ReglaNegocioException.class,
                () -> new Huesped(NOMBRE_VALIDO, DNI_VALIDO, TELEFONO_VALIDO, null));
    }

    // ---------------------------------------------------------------
    // Igualdad
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Dos huéspedes con el mismo DNI son el mismo, aunque cambien los demás datos")
    void huespedesConMismoDni_sonIguales() {
        Huesped primerHuesped = new Huesped("Ana Pérez", DNI_VALIDO, "912345678", "ana@correo.com");
        Huesped segundoHuesped = new Huesped("Otra Persona", DNI_VALIDO, "987654321", "otra@correo.com");
        Huesped huespedDistinto = new Huesped("Luis Gómez", "87654321", "912345678", "luis@correo.com");

        assertEquals(primerHuesped, segundoHuesped);
        assertEquals(primerHuesped.hashCode(), segundoHuesped.hashCode());
        assertFalse(primerHuesped.equals(huespedDistinto));
    }
}
