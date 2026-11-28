package Persistencia;

import Excepciones.ErrorBaseDatosException;
import Modelo.EstadoReserva;
import Modelo.Habitacion;
import Modelo.Huesped;
import Modelo.Reserva;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Guarda y consulta reservas en la tabla "reservas".
 * Usa los otros repositorios para reconstruir el huésped y la habitación de cada reserva.
 *
 * Los métodos que reciben una Connection existen para poder usarse dentro de una
 * transacción (ver ConexionBD.ejecutarEnTransaccion).
 */
public class ReservaRepositorio {

    private static final String CONSULTA_BASE =
            "SELECT codigo, dni_huesped, numero_habitacion, fecha_entrada, fecha_salida, "
            + "cantidad_personas, total_pagar, estado FROM reservas";

    private final HuespedRepositorio huespedRepositorio;
    private final HabitacionRepositorio habitacionRepositorio;

    public ReservaRepositorio(HuespedRepositorio huespedRepositorio,
                              HabitacionRepositorio habitacionRepositorio) {
        this.huespedRepositorio = huespedRepositorio;
        this.habitacionRepositorio = habitacionRepositorio;
    }

    /** Guarda la reserva abriendo su propia conexión (fuera de una transacción). */
    public void guardar(Reserva reserva) {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            guardar(conexion, reserva);

        } catch (SQLException error) {
            throw new ErrorBaseDatosException("No se pudo guardar la reserva.", error);
        }
    }

    /**
     * Guarda la reserva usando una conexión que viene de afuera, para que pueda
     * formar parte de una transacción. No cierra la conexión ni captura el error.
     */
    public void guardar(Connection conexion, Reserva reserva) throws SQLException {
        String sql = "INSERT INTO reservas (codigo, dni_huesped, numero_habitacion, fecha_entrada, "
                + "fecha_salida, cantidad_personas, total_pagar, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, reserva.getCodigo());
            sentencia.setString(2, reserva.getHuesped().getDni());
            sentencia.setString(3, reserva.getHabitacion().getNumero());
            sentencia.setObject(4, reserva.getFechaEntrada());
            sentencia.setObject(5, reserva.getFechaSalida());
            sentencia.setInt(6, reserva.getCantidadPersonas());
            sentencia.setDouble(7, reserva.getTotalPagar());
            sentencia.setString(8, reserva.getEstado().name());
            sentencia.executeUpdate();
        }
    }

    /** Actualiza el estado abriendo su propia conexión (fuera de una transacción). */
    public void actualizarEstado(Reserva reserva) {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            actualizarEstado(conexion, reserva);

        } catch (SQLException error) {
            throw new ErrorBaseDatosException("No se pudo actualizar el estado de la reserva.", error);
        }
    }

    /**
     * Actualiza el estado usando una conexión que viene de afuera, para que pueda
     * formar parte de una transacción. No cierra la conexión ni captura el error.
     */
    public void actualizarEstado(Connection conexion, Reserva reserva) throws SQLException {
        String sql = "UPDATE reservas SET estado = ? WHERE codigo = ?";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, reserva.getEstado().name());
            sentencia.setString(2, reserva.getCodigo());
            sentencia.executeUpdate();
        }
    }

    /**
     * Cuenta las reservas ACTIVAS de una habitación usando la conexión de la transacción.
     * Es importante usar la MISMA conexión: así también se cuenta la reserva que se acaba
     * de guardar (o de cancelar) y que todavía no se confirmó con commit.
     */
    public int contarActivasPorHabitacion(Connection conexion, String numeroHabitacion) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservas WHERE estado = 'ACTIVA' AND numero_habitacion = ?";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, numeroHabitacion);
            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                return resultado.getInt(1);
            }
        }
    }

    /** Devuelve la reserva con ese código, o null si no existe. */
    public Reserva buscarPorCodigo(String codigo) {
        ArrayList<Reserva> encontradas = consultar(CONSULTA_BASE + " WHERE codigo = ?", codigo);
        return encontradas.isEmpty() ? null : encontradas.get(0);
    }

    public ArrayList<Reserva> listarTodas() {
        return consultar(CONSULTA_BASE + " ORDER BY codigo");
    }

    public ArrayList<Reserva> listarActivasPorHabitacion(String numeroHabitacion) {
        return consultar(CONSULTA_BASE + " WHERE estado = 'ACTIVA' AND numero_habitacion = ?", numeroHabitacion);
    }

    /** Devuelve la reserva activa del huésped, o null si no tiene ninguna. */
    public Reserva buscarActivaDeHuesped(String dni) {
        ArrayList<Reserva> encontradas = consultar(CONSULTA_BASE + " WHERE estado = 'ACTIVA' AND dni_huesped = ?", dni);
        return encontradas.isEmpty() ? null : encontradas.get(0);
    }

    public int contar() {
        String sql = "SELECT COUNT(*) FROM reservas";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {

            resultado.next();
            return resultado.getInt(1);

        } catch (SQLException error) {
            throw new ErrorBaseDatosException("No se pudo contar las reservas.", error);
        }
    }

    // ---------------- Métodos de apoyo ----------------

    /** Ejecuta una consulta de reservas con parámetros de texto y arma la lista de resultados. */
    private ArrayList<Reserva> consultar(String sql, String... parametros) {
        ArrayList<Reserva> reservasEncontradas = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            for (int i = 0; i < parametros.length; i++) {
                sentencia.setString(i + 1, parametros[i]);
            }
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    reservasEncontradas.add(construirReserva(resultado));
                }
            }
            return reservasEncontradas;

        } catch (SQLException error) {
            throw new ErrorBaseDatosException("No se pudieron consultar las reservas.", error);
        }
    }

    private Reserva construirReserva(ResultSet resultado) throws SQLException {
        Huesped huesped = huespedRepositorio.buscarPorDni(resultado.getString("dni_huesped"));
        Habitacion habitacion = habitacionRepositorio.buscarPorNumero(resultado.getString("numero_habitacion"));

        return Reserva.reconstruir(
                resultado.getString("codigo"),
                huesped,
                habitacion,
                resultado.getObject("fecha_entrada", LocalDate.class),
                resultado.getObject("fecha_salida", LocalDate.class),
                resultado.getInt("cantidad_personas"),
                resultado.getDouble("total_pagar"),
                EstadoReserva.valueOf(resultado.getString("estado")));
    }
}