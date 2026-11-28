package Interfaz;

import Excepciones.ReglaNegocioException;
import Modelo.Huesped;
import Servicio.GestorReservas;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * Sección para registrar huéspedes.
 * Antes este formulario vivía dentro de FrmPrincipal; ahora tiene su propia clase
 * para que la ventana principal solo se encargue de la navegación.
 * La lógica es la misma: todo se delega al GestorReservas.
 */
public class PanelHuespedes extends JPanel {

    private final GestorReservas gestorReservas;

    // Componentes del formulario de huéspedes
    private JTextField campoNombreCompleto;
    private JTextField campoDni;
    private JTextField campoTelefono;
    private JTextField campoCorreo;
    private JButton botonRegistrar;
    private JButton botonLimpiar;

    public PanelHuespedes(GestorReservas gestorReservas) {
        this.gestorReservas = gestorReservas;
        construirPanel();
    }

    private void construirPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        // Campos de texto
        campoNombreCompleto = new JTextField();
        campoDni = new JTextField();
        campoTelefono = new JTextField();
        campoCorreo = new JTextField();
        Tema.estilizarCampo(campoNombreCompleto);
        Tema.estilizarCampo(campoDni);
        Tema.estilizarCampo(campoTelefono);
        Tema.estilizarCampo(campoCorreo);

        // Formulario: 2 filas x 2 columnas, cada campo con su etiqueta arriba
        JPanel panelFormulario = new JPanel(new GridLayout(2, 2, 20, 16));
        panelFormulario.setOpaque(false);
        panelFormulario.add(Tema.crearCampoFormulario("Nombre completo", campoNombreCompleto));
        panelFormulario.add(Tema.crearCampoFormulario("DNI (8 dígitos)", campoDni));
        panelFormulario.add(Tema.crearCampoFormulario("Teléfono (9 dígitos)", campoTelefono));
        panelFormulario.add(Tema.crearCampoFormulario("Correo electrónico", campoCorreo));

        // Botones
        botonLimpiar = Tema.crearBoton("Limpiar", Tema.COLOR_NEUTRO);
        botonRegistrar = Tema.crearBoton("Registrar", Tema.COLOR_PRIMARIO);

        // LISTENERS: conectan el EVENTO (clic) con su HANDLER (método)
        botonRegistrar.addActionListener(evento -> registrarHuespedDesdeFormulario());
        botonLimpiar.addActionListener(evento -> limpiarFormulario());

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBotones.setOpaque(false);
        panelBotones.add(botonLimpiar);
        panelBotones.add(botonRegistrar);

        // Tarjeta blanca: título, formulario al centro y botones abajo
        JPanel tarjeta = Tema.crearTarjeta(new BorderLayout(0, 20));
        tarjeta.setBorder(new EmptyBorder(24, 28, 24, 28));
        tarjeta.add(Tema.crearTituloTarjeta("Datos del huésped"), BorderLayout.NORTH);
        tarjeta.add(panelFormulario, BorderLayout.CENTER);
        tarjeta.add(panelBotones, BorderLayout.SOUTH);

        // La tarjeta va arriba para que no se estire a todo el alto de la ventana
        add(tarjeta, BorderLayout.NORTH);
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