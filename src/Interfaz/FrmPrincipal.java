package Interfaz;

import Excepciones.ReglaNegocioException;
import Modelo.Huesped;
import Servicio.GestorReservas;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

/**
 * Ventana principal del sistema de reservas.
 * Usa pestañas: por ahora solo la de huéspedes; luego sumaremos las demás.
 * Recibe el GestorReservas para reutilizar toda la lógica que ya hicimos.
 */
public class FrmPrincipal extends JFrame {

    private final GestorReservas gestorReservas;

    // Componentes del formulario de huéspedes
    private JTextField campoNombreCompleto;
    private JTextField campoDni;
    private JTextField campoTelefono;
    private JTextField campoCorreo;
    private JButton botonRegistrar;
    private JButton botonLimpiar;

    public FrmPrincipal(GestorReservas gestorReservas) {
        this.gestorReservas = gestorReservas;

        configurarVentana();

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Huéspedes", crearPanelHuespedes());
        add(pestanas);
    }

    private void configurarVentana() {
        setTitle("Sistema de Reservas - Hotel");
        setSize(520, 340);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // cerrar la ventana termina el programa
        setLocationRelativeTo(null);                    // centrar en pantalla
    }

    /** Construye la pestaña con el formulario para registrar huéspedes. */
    private JPanel crearPanelHuespedes() {

        // Formulario: 4 filas x 2 columnas (etiqueta + campo)
        campoNombreCompleto = new JTextField();
        campoDni = new JTextField();
        campoTelefono = new JTextField();
        campoCorreo = new JTextField();

        JPanel panelFormulario = new JPanel(new GridLayout(4, 2, 10, 15));
        panelFormulario.add(new JLabel("Nombre completo:"));
        panelFormulario.add(campoNombreCompleto);
        panelFormulario.add(new JLabel("DNI (8 dígitos):"));
        panelFormulario.add(campoDni);
        panelFormulario.add(new JLabel("Teléfono (9 dígitos):"));
        panelFormulario.add(campoTelefono);
        panelFormulario.add(new JLabel("Correo electrónico:"));
        panelFormulario.add(campoCorreo);

        // Botones
        botonLimpiar = new JButton("Limpiar");
        botonRegistrar = new JButton("Registrar");

        // LISTENERS: conectan el EVENTO (clic) con su HANDLER (método)
        botonRegistrar.addActionListener(evento -> registrarHuespedDesdeFormulario());
        botonLimpiar.addActionListener(evento -> limpiarFormulario());

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.add(botonLimpiar);
        panelBotones.add(botonRegistrar);

        // Panel final: formulario al centro y botones abajo, con margen
        JPanel panelHuespedes = new JPanel(new BorderLayout(10, 10));
        panelHuespedes.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panelHuespedes.add(panelFormulario, BorderLayout.CENTER);
        panelHuespedes.add(panelBotones, BorderLayout.SOUTH);
        return panelHuespedes;
    }

    // ---------------- HANDLERS ----------------

    /** Handler del botón "Registrar": lee el formulario y usa la lógica del gestor. */
    private void registrarHuespedDesdeFormulario() {
        try {
            Huesped nuevoHuesped = gestorReservas.registrarHuesped(
                    campoNombreCompleto.getText().trim(),
                    campoDni.getText().trim(),
                    campoTelefono.getText().trim(),
                    campoCorreo.getText().trim());

            JOptionPane.showMessageDialog(this,
                    "Huésped registrado correctamente:\n" + nuevoHuesped,
                    "Registro exitoso", JOptionPane.INFORMATION_MESSAGE);
            limpiarFormulario();

        } catch (ReglaNegocioException error) {
            // El mensaje de la regla de negocio se muestra directamente al usuario
            JOptionPane.showMessageDialog(this, error.getMessage(),
                    "Datos inválidos", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Handler del botón "Limpiar". */
    private void limpiarFormulario() {
        campoNombreCompleto.setText("");
        campoDni.setText("");
        campoTelefono.setText("");
        campoCorreo.setText("");
        campoNombreCompleto.requestFocus(); // el cursor vuelve al primer campo
    }
}