package modelo;

import excepciones.ReglaNegocioException;
import java.util.Objects;

/**
 * Representa a un huésped del hotel.
 * Valida sus datos desde el constructor y los setters, así nunca
 * existe un huésped con información inválida.
 */
public class Huesped {

    private String nombreCompleto;
    private final String dni;
    private String telefono;
    private String correo;

    public Huesped(String nombreCompleto, String dni, String telefono, String correo) {
        // Se validan todos los datos antes de crear el objeto
        validarNombre(nombreCompleto);
        if (!esDniValido(dni)) {
            throw new ReglaNegocioException("El DNI debe tener exactamente 8 dígitos numéricos.");
        }
        validarTelefono(telefono);
        validarCorreo(correo);

        this.nombreCompleto = nombreCompleto.trim();
        this.dni = dni;
        this.telefono = telefono;
        this.correo = correo.trim();
    }

    /** Regla de negocio: el DNI debe tener exactamente 8 dígitos numéricos. */
    public static boolean esDniValido(String dniAValidar) {
        return dniAValidar != null && dniAValidar.matches("\\d{8}");
    }

    private static void validarNombre(String nombreAValidar) {
        if (nombreAValidar == null || nombreAValidar.trim().length() < 3) {
            throw new ReglaNegocioException("El nombre debe tener al menos 3 caracteres.");
        }
    }

    /** Celular peruano: 9 dígitos y empieza con 9. */
    private static void validarTelefono(String telefonoAValidar) {
        if (telefonoAValidar == null || !telefonoAValidar.matches("9\\d{8}")) {
            throw new ReglaNegocioException("El teléfono debe tener 9 dígitos y empezar con 9.");
        }
    }

    private static void validarCorreo(String correoAValidar) {
        if (correoAValidar == null
                || !correoAValidar.trim().matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) {
            throw new ReglaNegocioException("El correo electrónico no tiene un formato válido.");
        }
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        validarNombre(nombreCompleto);
        this.nombreCompleto = nombreCompleto.trim();
    }

    public String getDni() {
        return dni;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        validarTelefono(telefono);
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        validarCorreo(correo);
        this.correo = correo.trim();
    }

    // Dos huéspedes son el mismo si tienen el mismo DNI
    @Override
    public boolean equals(Object otroObjeto) {
        if (this == otroObjeto) return true;
        if (!(otroObjeto instanceof Huesped)) return false;
        Huesped otroHuesped = (Huesped) otroObjeto;
        return dni.equals(otroHuesped.dni);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dni);
    }

    @Override
    public String toString() {
        return nombreCompleto + " (DNI: " + dni + ")";
    }
}