package Principal;

import Excepciones.ReglaNegocioException;
import Modelo.Habitacion;
import Modelo.Huesped;
import Modelo.TipoHabitacion;

public class Main {

    public static void main(String[] args) {

        // Prueba 1: huésped y habitación válidos
        Huesped huespedPrueba = new Huesped("Angel Caldas", "12345678", "987654321", "angel@correo.com");
        Habitacion habitacionPrueba = new Habitacion("101", TipoHabitacion.DOBLE);
        System.out.println(huespedPrueba);
        System.out.println(habitacionPrueba);

        // Prueba 2: DNI inválido, debe mostrar el mensaje de error
        try {
            new Huesped("Maria Lopez", "123", "987654321", "maria@correo.com");
        } catch (ReglaNegocioException error) {
            System.out.println("Error: " + error.getMessage());
        }
    }
}