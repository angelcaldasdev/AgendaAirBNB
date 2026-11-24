package Interfaz;

import Servicio.GestorReservas;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Ventana principal del sistema de reservas.
 *
 * Diseño: barra lateral oscura a la izquierda (navegación) y, a la derecha,
 * el contenido de la sección elegida. Reemplaza las pestañas (JTabbedPane)
 * por un menú lateral, inspirado en el diseño de SistemaEsSalud.
 *
 * Recibe el GestorReservas para reutilizar toda la lógica que ya hicimos,
 * igual que antes: MainGrafico no necesita cambios.
 */
public class FrmPrincipal extends JFrame {

    private final GestorReservas gestorReservas;

    // CardLayout permite mostrar un solo panel a la vez dentro del área de contenido
    private final CardLayout disposicionTarjetas = new CardLayout();
    private final JPanel panelContenido = new JPanel(disposicionTarjetas);
    private final JPanel panelLateral = new JPanel(new BorderLayout());
    private final JPanel panelNavegacion = new JPanel();
    private final List<BotonNavegacion> botonesNavegacion = new ArrayList<>();

    // Acción que se ejecuta cada vez que se entra a una sección (por ejemplo, refrescar datos)
    private final Map<String, Runnable> accionesAlMostrar = new HashMap<>();

    public FrmPrincipal(GestorReservas gestorReservas) {
        super("AgendaAirBNB - Sistema de reservas");
        this.gestorReservas = gestorReservas;

        construirVentana();
        construirSecciones();
    }

    // ============================ CONSTRUCCIÓN ============================

    private void construirVentana() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // cerrar la ventana termina el programa
        setSize(1200, 740);
        setMinimumSize(new Dimension(1000, 620));
        setLocationRelativeTo(null);                    // centrar en pantalla

        getContentPane().setLayout(new BorderLayout());

        construirPanelLateral();

        // Área de contenido: fondo claro con margen alrededor
        panelContenido.setBackground(Tema.COLOR_FONDO);
        panelContenido.setBorder(new EmptyBorder(24, 28, 24, 28));

        getContentPane().add(panelLateral, BorderLayout.WEST);
        getContentPane().add(panelContenido, BorderLayout.CENTER);
    }

    /** Aquí se definen las secciones del menú lateral y qué panel muestra cada una. */
    private void construirSecciones() {
        PanelInicio panelInicio = new PanelInicio(gestorReservas);
        PanelGestionReservas panelGestion = new PanelGestionReservas(gestorReservas);

        // Al entrar a "Inicio" y a "Gestión", los datos se actualizan solos
        agregarSeccion("Inicio", "Resumen general del hotel",
                panelInicio, panelInicio::refrescar);
        agregarSeccion("Huéspedes", "Registra a los huéspedes del hotel",
                new PanelHuespedes(gestorReservas), null);
        agregarSeccion("Reservas", "Consulta disponibilidad y crea nuevas reservas",
                new PanelReservas(gestorReservas), null);
        agregarSeccion("Gestión de reservas", "Revisa, cancela o finaliza las reservas existentes",
                panelGestion, panelGestion::refrescarTabla);
    }

    private void construirPanelLateral() {
        panelLateral.setBackground(Tema.COLOR_LATERAL);
        panelLateral.setPreferredSize(new Dimension(250, 0));
        panelLateral.setBorder(new EmptyBorder(28, 16, 20, 16));

        // ---- Cabecera: logo + subtítulo ----
        JPanel cabecera = new JPanel();
        cabecera.setLayout(new BoxLayout(cabecera, BoxLayout.Y_AXIS));
        cabecera.setOpaque(false);
        cabecera.setBorder(new EmptyBorder(0, 6, 28, 0));

        // HTML permite dos colores en la misma etiqueta
        JLabel etiquetaLogo = new JLabel("<html><span style='color:#FF5A5F'>Agenda</span>AirBNB</html>");
        etiquetaLogo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        etiquetaLogo.setForeground(Color.WHITE);
        etiquetaLogo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel etiquetaSubtitulo = new JLabel("Sistema de reservas");
        etiquetaSubtitulo.setFont(Tema.FUENTE_PEQUENA);
        etiquetaSubtitulo.setForeground(new Color(0x94A3B8));
        etiquetaSubtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        cabecera.add(etiquetaLogo);
        cabecera.add(Box.createRigidArea(new Dimension(0, 4)));
        cabecera.add(etiquetaSubtitulo);

        // ---- Zona de botones de navegación ----
        panelNavegacion.setLayout(new BoxLayout(panelNavegacion, BoxLayout.Y_AXIS));
        panelNavegacion.setOpaque(false);

        // ---- Pie: versión ----
        JLabel etiquetaVersion = new JLabel("AgendaAirBNB v1.0");
        etiquetaVersion.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        etiquetaVersion.setForeground(new Color(0x64748B));
        etiquetaVersion.setBorder(new EmptyBorder(0, 6, 0, 0));

        panelLateral.add(cabecera, BorderLayout.NORTH);
        panelLateral.add(panelNavegacion, BorderLayout.CENTER);
        panelLateral.add(etiquetaVersion, BorderLayout.SOUTH);
    }

    // ============================ SECCIONES ============================

    /**
     * Agrega una sección al menú lateral.
     *
     * @param titulo        texto del botón del menú y título grande de la página
     * @param subtitulo     descripción corta debajo del título
     * @param panel         el panel con el contenido (se le aplica el Tema automáticamente)
     * @param alMostrarla   acción opcional que se ejecuta cada vez que se entra a la sección (puede ser null)
     */
    private void agregarSeccion(String titulo, String subtitulo, JPanel panel, Runnable alMostrarla) {
        Tema.aplicarEstilo(panel);

        // Página = encabezado (título + subtítulo) arriba y el panel debajo
        JPanel pagina = new JPanel(new BorderLayout(0, 18));
        pagina.setOpaque(false);

        JPanel encabezado = new JPanel();
        encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.Y_AXIS));
        encabezado.setOpaque(false);
        encabezado.add(Tema.crearTitulo(titulo));
        encabezado.add(Box.createRigidArea(new Dimension(0, 4)));
        encabezado.add(Tema.crearSubtitulo(subtitulo));

        pagina.add(encabezado, BorderLayout.NORTH);
        pagina.add(panel, BorderLayout.CENTER);
        panelContenido.add(pagina, titulo); // el título funciona como "nombre" de la tarjeta

        if (alMostrarla != null) {
            accionesAlMostrar.put(titulo, alMostrarla);
        }

        // Botón del menú lateral
        BotonNavegacion boton = new BotonNavegacion(titulo);
        boton.addActionListener(evento -> mostrarSeccion(titulo));
        botonesNavegacion.add(boton);
        panelNavegacion.add(boton);
        panelNavegacion.add(Box.createRigidArea(new Dimension(0, 8)));

        // La primera sección agregada se muestra al abrir la ventana
        if (botonesNavegacion.size() == 1) {
            mostrarSeccion(titulo);
        }
    }

    /** Muestra la sección con ese título, marca su botón como activo y ejecuta su acción. */
    private void mostrarSeccion(String titulo) {
        disposicionTarjetas.show(panelContenido, titulo);
        for (BotonNavegacion boton : botonesNavegacion) {
            boton.setActivo(boton.getText().equals(titulo));
        }
        Runnable accion = accionesAlMostrar.get(titulo);
        if (accion != null) {
            accion.run();
        }
    }

    // ========================= BOTÓN DE NAVEGACIÓN =========================

    /** Botón del menú lateral: transparente, se pinta de coral cuando está activo. */
    private static class BotonNavegacion extends JButton {

        private boolean activo = false;
        private boolean mouseEncima = false;

        BotonNavegacion(String texto) {
            super(texto);
            setHorizontalAlignment(LEFT);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setForeground(Tema.COLOR_TEXTO_LATERAL);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(12, 18, 12, 18));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent evento) {
                    mouseEncima = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent evento) {
                    mouseEncima = false;
                    repaint();
                }
            });
        }

        void setActivo(boolean activo) {
            this.activo = activo;
            setForeground(activo ? Color.WHITE : Tema.COLOR_TEXTO_LATERAL);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (activo) {
                g2.setColor(Tema.COLOR_PRIMARIO);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            } else if (mouseEncima) {
                g2.setColor(Tema.COLOR_LATERAL_HOVER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            }
            g2.dispose();
            super.paintComponent(g); // dibuja el texto
        }
    }
}