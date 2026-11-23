package Principal;

import Excepciones.ErrorBaseDatosException;
import Interfaz.FrmPrincipal;
import Servicio.GestorReservas;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Punto de entrada de la versión con interfaz gráfica.
 */
public class MainGrafico {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                GestorReservas gestorReservas = new GestorReservas();
                new FrmPrincipal(gestorReservas).setVisible(true);

            } catch (ErrorBaseDatosException error) {
                error.printStackTrace(); // el detalle técnico queda en el Output
                JOptionPane.showMessageDialog(null,
                        error.getMessage() + "\n\nRevisa que MySQL esté iniciado y que "
                        + "config.properties tenga los datos correctos.",
                        "Error de base de datos", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}