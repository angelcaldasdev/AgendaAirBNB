package Principal;

import Excepciones.ErrorBaseDatosException;
import Interfaz.FrmPrincipal;
import Servicio.GestorReservas;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Punto de entrada de la versión con interfaz gráfica.
 *
 * Crea el GestorReservas (donde vive toda la lógica y la conexión a la base de datos)
 * y se lo entrega a la ventana principal. Si algo falla al arrancar, por ejemplo
 * que MySQL esté apagado, muestra un mensaje claro en lugar de cerrarse sin avisar.
 */
public class MainGrafico {

    public static void main(String[] args) {
        // Texto suavizado para que las letras se vean más nítidas
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Las ventanas Swing deben crearse en el hilo de eventos de la interfaz
        SwingUtilities.invokeLater(() -> iniciarAplicacion());
    }

    /** Crea el gestor, abre la ventana principal y atiende los errores de arranque. */
    private static void iniciarAplicacion() {
        try {
            GestorReservas gestorReservas = new GestorReservas();
            FrmPrincipal ventanaPrincipal = new FrmPrincipal(gestorReservas);
            ventanaPrincipal.setVisible(true);

        } catch (Exception error) {
            // Se distingue el error de base de datos de cualquier otro problema de arranque
            String tituloError = (error instanceof ErrorBaseDatosException)
                    ? "Error de base de datos"
                    : "Error al iniciar la aplicación";

            JOptionPane.showMessageDialog(null, error.getMessage(), tituloError,
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1); // sin gestor no hay aplicación: se cierra
        }
    }
}