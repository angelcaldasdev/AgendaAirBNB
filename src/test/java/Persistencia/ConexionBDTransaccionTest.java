package Persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import Excepciones.ErrorBaseDatosException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de INTEGRACIÓN de las transacciones: usan MySQL de verdad.
 *
 * Comprueban que:
 *  - con commit, los cambios quedan guardados;
 *  - con rollback, un fallo a mitad de camino NO deja nada guardado.
 *
 * Si MySQL está apagado, estas pruebas se OMITEN (no fallan).
 * Usan un huésped con un DNI de prueba (99999999) que se borra antes y después de cada prueba.
 */
class ConexionBDTransaccionTest {

    private static final String DNI_DE_PRUEBA = "99999999";

    // @BeforeAll se ejecuta una sola vez, antes de todas las pruebas de la clase
    @BeforeAll
    static void verificarQueMySqlEstaDisponible() {
        boolean baseDeDatosDisponible;
        try {
            ConexionBD.inicializarBaseDeDatos();
            baseDeDatosDisponible = true;
        } catch (RuntimeException error) {
            baseDeDatosDisponible = false;
        }
        // Si no hay base de datos, JUnit marca las pruebas como "omitidas" en lugar de fallidas
        assumeTrue(baseDeDatosDisponible, "MySQL no está disponible: se omiten las pruebas de integración.");
    }

    @BeforeEach
    @AfterEach
    void borrarHuespedDePrueba() throws SQLException {
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement("DELETE FROM huespedes WHERE dni = ?")) {
            sentencia.setString(1, DNI_DE_PRUEBA);
            sentencia.executeUpdate();
        }
    }

    // ---------------------------------------------------------------
    // Pruebas
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Si todo sale bien, la transacción se confirma (commit) y el dato queda guardado")
    void transaccionExitosa_guardaLosCambios() throws SQLException {
        // EJECUTAR
        ConexionBD.ejecutarEnTransaccion("Error de prueba",
                conexion -> insertarHuespedDePrueba(conexion));

        // VERIFICAR
        assertTrue(existeHuespedDePrueba());
    }

    @Test
    @DisplayName("Si falla un paso, se deshace todo (rollback) y no queda nada guardado")
    void falloAMitadDeTransaccion_deshaceLosCambios() throws SQLException {
        // EJECUTAR: se inserta el huésped y DESPUÉS ocurre un fallo simulado
        IllegalStateException error = assertThrows(IllegalStateException.class, () ->
                ConexionBD.ejecutarEnTransaccion("Error de prueba", conexion -> {
                    insertarHuespedDePrueba(conexion);
                    throw new IllegalStateException("Fallo simulado a mitad de la transacción");
                }));

        // VERIFICAR: el error original llega intacto y el huésped NO quedó guardado
        assertEquals("Fallo simulado a mitad de la transacción", error.getMessage());
        assertFalse(existeHuespedDePrueba());
    }

    @Test
    @DisplayName("Si MySQL rechaza un paso (clave duplicada), se deshace todo y se lanza ErrorBaseDatosException")
    void errorDeSqlEnLaTransaccion_deshaceYLanzaErrorBaseDatos() throws SQLException {
        // EJECUTAR: el segundo insert repite el DNI (clave primaria) y MySQL lo rechaza
        ErrorBaseDatosException error = assertThrows(ErrorBaseDatosException.class, () ->
                ConexionBD.ejecutarEnTransaccion("No se pudo guardar el huésped de prueba.", conexion -> {
                    insertarHuespedDePrueba(conexion);
                    insertarHuespedDePrueba(conexion);
                }));

        // VERIFICAR: se usa el mensaje indicado y el primer insert también se deshizo
        assertEquals("No se pudo guardar el huésped de prueba.", error.getMessage());
        assertFalse(existeHuespedDePrueba());
    }

    // ---------------------------------------------------------------
    // Métodos de apoyo
    // ---------------------------------------------------------------

    /** Inserta el huésped de prueba usando la conexión de la transacción. */
    private void insertarHuespedDePrueba(Connection conexion) throws SQLException {
        String sql = "INSERT INTO huespedes (dni, nombre_completo, telefono, correo) VALUES (?, ?, ?, ?)";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, DNI_DE_PRUEBA);
            sentencia.setString(2, "Huesped Prueba Transaccion");
            sentencia.setString(3, "900000000");
            sentencia.setString(4, "prueba@correo.com");
            sentencia.executeUpdate();
        }
    }

    /** Consulta con una conexión NUEVA: solo ve lo que realmente quedó confirmado en la base de datos. */
    private boolean existeHuespedDePrueba() throws SQLException {
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement("SELECT COUNT(*) FROM huespedes WHERE dni = ?")) {

            sentencia.setString(1, DNI_DE_PRUEBA);
            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                return resultado.getInt(1) > 0;
            }
        }
    }
}