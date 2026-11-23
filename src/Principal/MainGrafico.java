package Principal;

import Interfaz.FrmPrincipal;
import Servicio.GestorReservas;
import javax.swing.SwingUtilities;

/**
 * Punto de entrada de la versión con interfaz gráfica.
 */
public class MainGrafico {

    public static void main(String[] args) {
        // Las ventanas Swing se crean en el hilo de eventos, como recomienda Java
        SwingUtilities.invokeLater(() -> {
            GestorReservas gestorReservas = new GestorReservas();
            new FrmPrincipal(gestorReservas).setVisible(true);
        });
    }
}