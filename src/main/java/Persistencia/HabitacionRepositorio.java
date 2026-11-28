package Persistencia;

import Excepciones.ErrorBaseDatosException;
import Modelo.EstadoHabitacion;
import Modelo.Habitacion;
import Modelo.TipoHabitacion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Guarda y consulta habitaciones en la tabla "habitaciones".
 */
public class HabitacionRepositorio {

    public void guardar(Habitacion habitacion) {
        String sql = "INSERT INTO habitaciones (numero, tipo, precio_por_noche, estado) VALUES (?, ?, ?, ?)";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, habitacion.getNumero());
            sentencia.setString(2, habitacion.getTipo().name());
            sentencia.setDouble(3, habitacion.getPrecioPorNoche());
            sentencia.setString(4, habitacion.getEstado().name());
            sentencia.executeUpdate();

        } catch (SQLException error) {
            throw new ErrorBaseDatosException("No se pudo guardar la habitación.", error);
        }
    }

    public void actualizarEstado(Habitacion habitacion) {
        String sql = "UPDATE habitaciones SET estado = ? WHERE numero = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, habitacion.getEstado().name());
            sentencia.setString(2, habitacion.getNumero());
            sentencia.executeUpdate();

        } catch (SQLException error) {
            throw new ErrorBaseDatosException("No se pudo actualizar el estado de la habitación.", error);
        }
    }

    /** Devuelve la habitación con ese número, o null si no existe. */
    public Habitacion buscarPorNumero(String numero) {
        String sql = "SELECT numero, tipo, precio_por_noche, estado FROM habitaciones WHERE numero = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, numero);
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    return construirHabitacion(resultado);
                }
            }
            return null;

        } catch (SQLException error) {
            throw new ErrorBaseDatosException("No se pudo buscar la habitación.", error);
        }
    }

    public ArrayList<Habitacion> listarTodas() {
        String sql = "SELECT numero, tipo, precio_por_noche, estado FROM habitaciones ORDER BY numero";
        ArrayList<Habitacion> habitacionesEncontradas = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {
                habitacionesEncontradas.add(construirHabitacion(resultado));
            }
            return habitacionesEncontradas;

        } catch (SQLException error) {
            throw new ErrorBaseDatosException("No se pudieron listar las habitaciones.", error);
        }
    }

    /** Cantidad de habitaciones registradas (sirve para saber si hay que cargar las iniciales). */
    public int contar() {
        String sql = "SELECT COUNT(*) FROM habitaciones";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {

            resultado.next();
            return resultado.getInt(1);

        } catch (SQLException error) {
            throw new ErrorBaseDatosException("No se pudo contar las habitaciones.", error);
        }
    }

    /** Convierte una fila de la tabla en un objeto Habitacion, con su precio y estado guardados. */
    private Habitacion construirHabitacion(ResultSet resultado) throws SQLException {
        Habitacion habitacion = new Habitacion(
                resultado.getString("numero"),
                TipoHabitacion.valueOf(resultado.getString("tipo")));

        habitacion.setPrecioPorNoche(resultado.getDouble("precio_por_noche"));

        if (EstadoHabitacion.valueOf(resultado.getString("estado")) == EstadoHabitacion.RESERVADA) {
            habitacion.marcarComoReservada();
        }
        return habitacion;
    }
}