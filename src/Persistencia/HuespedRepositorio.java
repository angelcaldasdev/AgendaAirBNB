package Persistencia;

import Excepciones.ErrorBaseDatosException;
import Modelo.Huesped;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Guarda y consulta huéspedes en la tabla "huespedes".
 * Aquí solo hay SQL: las reglas de negocio viven en el servicio.
 */
public class HuespedRepositorio {

    public void guardar(Huesped huesped) {
        String sql = "INSERT INTO huespedes (dni, nombre_completo, telefono, correo) VALUES (?, ?, ?, ?)";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, huesped.getDni());
            sentencia.setString(2, huesped.getNombreCompleto());
            sentencia.setString(3, huesped.getTelefono());
            sentencia.setString(4, huesped.getCorreo());
            sentencia.executeUpdate();

        } catch (SQLException error) {
            throw new ErrorBaseDatosException("No se pudo guardar el huésped.", error);
        }
    }

    /** Devuelve el huésped con ese DNI, o null si no existe. */
    public Huesped buscarPorDni(String dni) {
        String sql = "SELECT dni, nombre_completo, telefono, correo FROM huespedes WHERE dni = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, dni);
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    return construirHuesped(resultado);
                }
            }
            return null;

        } catch (SQLException error) {
            throw new ErrorBaseDatosException("No se pudo buscar el huésped.", error);
        }
    }

    public ArrayList<Huesped> listarTodos() {
        String sql = "SELECT dni, nombre_completo, telefono, correo FROM huespedes ORDER BY nombre_completo";
        ArrayList<Huesped> huespedesEncontrados = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {
                huespedesEncontrados.add(construirHuesped(resultado));
            }
            return huespedesEncontrados;

        } catch (SQLException error) {
            throw new ErrorBaseDatosException("No se pudieron listar los huéspedes.", error);
        }
    }

    /** Convierte una fila de la tabla en un objeto Huesped. */
    private Huesped construirHuesped(ResultSet resultado) throws SQLException {
        return new Huesped(
                resultado.getString("nombre_completo"),
                resultado.getString("dni"),
                resultado.getString("telefono"),
                resultado.getString("correo"));
    }
}