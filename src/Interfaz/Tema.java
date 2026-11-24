package Interfaz;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.text.JTextComponent;

/**
 * Tema visual de AgendaAirBNB.
 *
 * Centraliza colores, fuentes y estilos para que TODA la interfaz se vea
 * igual de moderna. Si quieres cambiar el "look" del sistema, solo tocas
 * esta clase (por ejemplo, cambiar COLOR_PRIMARIO).
 */
public final class Tema {

    // ======================= PALETA DE COLORES =======================
    public static final Color COLOR_PRIMARIO         = new Color(0xFF5A5F); // coral estilo Airbnb
    public static final Color COLOR_ACENTO_MORADO    = new Color(0x7C3AED); // degradados y acentos
    public static final Color COLOR_LATERAL          = new Color(0x111827); // barra lateral oscura
    public static final Color COLOR_LATERAL_HOVER    = new Color(0x1F2937);
    public static final Color COLOR_TEXTO_LATERAL    = new Color(0xCBD5E1);
    public static final Color COLOR_FONDO            = new Color(0xF4F6FB); // fondo general claro
    public static final Color COLOR_TEXTO            = new Color(0x1F2937);
    public static final Color COLOR_TEXTO_SUAVE      = new Color(0x6B7280);
    public static final Color COLOR_BORDE            = new Color(0xE2E8F0);
    public static final Color COLOR_EXITO            = new Color(0x10B981); // verde
    public static final Color COLOR_PELIGRO          = new Color(0xEF4444); // rojo
    public static final Color COLOR_NEUTRO           = new Color(0x64748B); // gris azulado
    public static final Color COLOR_ENCABEZADO_TABLA = new Color(0x1F2937);
    public static final Color COLOR_FILA_ALTERNA     = new Color(0xF8FAFC);
    public static final Color COLOR_SELECCION        = new Color(0xFFE4E6); // coral muy suave

    // ============================ FUENTES ============================
    public static final Font FUENTE_NORMAL    = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FUENTE_NEGRITA   = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FUENTE_PEQUENA   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FUENTE_SUBTITULO = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FUENTE_TITULO    = new Font("Segoe UI", Font.BOLD, 28);

    // Claves internas (client properties) para marcar componentes
    public static final String PROPIEDAD_NO_ESTILIZAR = "noEstilizar";
    private static final String PROPIEDAD_BOTON_ESTILIZADO = "botonEstilizado";

    private Tema() {
        // Clase de utilidades: no se instancia
    }

    // ===================== COMPONENTES NUEVOS =====================

    /** Título grande de cada sección. */
    public static JLabel crearTitulo(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(FUENTE_TITULO);
        etiqueta.setForeground(COLOR_TEXTO);
        etiqueta.setAlignmentX(Component.LEFT_ALIGNMENT);
        return etiqueta;
    }

    /** Subtítulo gris que acompaña al título de la sección. */
    public static JLabel crearSubtitulo(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(FUENTE_NORMAL);
        etiqueta.setForeground(COLOR_TEXTO_SUAVE);
        etiqueta.setAlignmentX(Component.LEFT_ALIGNMENT);
        return etiqueta;
    }

    /** Título pequeño que va dentro de una tarjeta (por ejemplo "Datos del huésped"). */
    public static JLabel crearTituloTarjeta(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(FUENTE_SUBTITULO);
        etiqueta.setForeground(COLOR_TEXTO);
        return etiqueta;
    }

    /** Tarjeta blanca con esquinas redondeadas y borde suave. */
    public static JPanel crearTarjeta(LayoutManager disposicion) {
        JPanel tarjeta = new JPanel(disposicion) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.setColor(COLOR_BORDE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
            }
        };
        tarjeta.setOpaque(false);
        tarjeta.setBorder(new EmptyBorder(16, 20, 16, 20));
        return tarjeta;
    }

    /** Agrupa una etiqueta pequeña arriba y el campo debajo (estilo formulario moderno). */
    public static JPanel crearCampoFormulario(String textoEtiqueta, JComponent campo) {
        JPanel contenedor = new JPanel(new BorderLayout(0, 6));
        contenedor.setOpaque(false);

        JLabel etiqueta = new JLabel(textoEtiqueta);
        etiqueta.setFont(FUENTE_NEGRITA);
        etiqueta.setForeground(COLOR_TEXTO);

        contenedor.add(etiqueta, BorderLayout.NORTH);
        contenedor.add(campo, BorderLayout.CENTER);
        return contenedor;
    }

    /** Crea un botón moderno ya estilizado con el color indicado. */
    public static JButton crearBoton(String texto, Color colorBase) {
        JButton boton = new JButton(texto);
        estilizarBoton(boton, colorBase);
        return boton;
    }

    // ===================== ESTILOS INDIVIDUALES =====================

    /** Convierte un botón normal en un botón moderno redondeado con efecto hover. */
    public static void estilizarBoton(JButton boton, Color colorBase) {
        // Primero el UI (instala valores por defecto) y después nuestras propiedades
        boton.setUI(new BotonModernoUI(colorBase));
        boton.setOpaque(false);
        boton.setContentAreaFilled(false);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setRolloverEnabled(true);
        boton.setForeground(Color.WHITE);
        boton.setFont(FUENTE_NEGRITA);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setBorder(new EmptyBorder(9, 20, 9, 20));
        // Marca para que el estilizado automático no cambie el color elegido
        boton.putClientProperty(PROPIEDAD_BOTON_ESTILIZADO, Boolean.TRUE);
    }

    /** Campos de texto con borde redondeado suave y relleno interno. */
    public static void estilizarCampo(JTextComponent campo) {
        campo.setFont(FUENTE_NORMAL);
        campo.setForeground(COLOR_TEXTO);
        campo.setBackground(Color.WHITE);
        campo.setCaretColor(COLOR_PRIMARIO);
        campo.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDE, 1, true),
                new EmptyBorder(7, 10, 7, 10)));
    }

    /** Listas desplegables con fuente, colores y altura del tema. */
    public static void estilizarCombo(JComboBox<?> combo) {
        combo.setFont(FUENTE_NORMAL);
        combo.setForeground(COLOR_TEXTO);
        combo.setBackground(Color.WHITE);
        combo.setPreferredSize(new Dimension(200, 38));
    }

    /** Selectores numéricos (JSpinner) con el mismo aspecto que los campos de texto. */
    public static void estilizarSpinner(JSpinner spinner) {
        spinner.setFont(FUENTE_NORMAL);
        spinner.setBorder(new LineBorder(COLOR_BORDE, 1, true));
        spinner.setPreferredSize(new Dimension(200, 38));
        if (spinner.getEditor() instanceof JSpinner.DefaultEditor) {
            JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner.getEditor();
            JTextField campoEditor = editor.getTextField();
            campoEditor.setFont(FUENTE_NORMAL);
            campoEditor.setBackground(Color.WHITE);
            campoEditor.setBorder(new EmptyBorder(4, 8, 4, 8));
        }
    }

    /** Tablas sin cuadrícula, con encabezado oscuro y filas alternadas. */
    public static void estilizarTabla(JTable tabla) {
        tabla.setRowHeight(36);
        tabla.setShowGrid(false);
        tabla.setIntercellSpacing(new Dimension(0, 0));
        tabla.setFont(FUENTE_NORMAL);
        tabla.setForeground(COLOR_TEXTO);
        tabla.setSelectionBackground(COLOR_SELECCION);
        tabla.setSelectionForeground(COLOR_TEXTO);
        tabla.setFillsViewportHeight(true);
        tabla.setBackground(Color.WHITE);

        // Filas: fondo blanco / gris muy claro alternado
        FilaRenderer renderizadorFilas = new FilaRenderer();
        tabla.setDefaultRenderer(Object.class, renderizadorFilas);
        tabla.setDefaultRenderer(Number.class, renderizadorFilas);

        // Encabezado oscuro con texto blanco
        JTableHeader encabezado = tabla.getTableHeader();
        if (encabezado != null) {
            encabezado.setReorderingAllowed(false);
            encabezado.setPreferredSize(new Dimension(0, 40));
            encabezado.setDefaultRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object valor,
                        boolean seleccionado, boolean foco, int fila, int columna) {
                    super.getTableCellRendererComponent(t, valor, seleccionado, foco, fila, columna);
                    setOpaque(true);
                    setBackground(COLOR_ENCABEZADO_TABLA);
                    setForeground(Color.WHITE);
                    setFont(FUENTE_NEGRITA);
                    setHorizontalAlignment(SwingConstants.LEFT);
                    setBorder(new EmptyBorder(0, 12, 0, 12));
                    return this;
                }
            });
        }
    }

    // ================== ESTILIZADO AUTOMÁTICO ==================

    /**
     * Recorre un panel (y todo lo que tenga dentro) aplicando el tema.
     * Sirve como "red de seguridad": lo que ya fue estilizado a mano
     * (por ejemplo los botones creados con Tema.crearBoton) se respeta.
     */
    public static void aplicarEstilo(Container raiz) {
        estilizarComponente(raiz);
    }

    private static void estilizarComponente(Component componente) {
        // Si el panel pidió no ser estilizado, lo respetamos
        if (componente instanceof JComponent) {
            JComponent componenteSwing = (JComponent) componente;
            if (Boolean.TRUE.equals(componenteSwing.getClientProperty(PROPIEDAD_NO_ESTILIZAR))) {
                return;
            }
        }

        if (componente instanceof JButton) {
            JButton boton = (JButton) componente;
            if (!Boolean.TRUE.equals(boton.getClientProperty(PROPIEDAD_BOTON_ESTILIZADO))) {
                estilizarBoton(boton, colorSegunTexto(boton.getText()));
            }
        } else if (componente instanceof JTable) {
            estilizarTabla((JTable) componente);
        } else if (componente instanceof JTextComponent) {
            estilizarCampo((JTextComponent) componente);
        } else if (componente instanceof JComboBox) {
            estilizarCombo((JComboBox<?>) componente);
        } else if (componente instanceof JScrollPane) {
            JScrollPane scroll = (JScrollPane) componente;
            scroll.setBorder(new LineBorder(COLOR_BORDE, 1, true));
            scroll.getViewport().setBackground(Color.WHITE);
            Component vista = scroll.getViewport().getView();
            if (vista != null) {
                estilizarComponente(vista);
            }
        } else if (componente instanceof JCheckBox || componente instanceof JRadioButton) {
            ((JComponent) componente).setOpaque(false);
            componente.setFont(FUENTE_NORMAL);
        } else if (componente instanceof JSpinner || componente instanceof JScrollBar) {
            // No se recorren por dentro: tienen botones internos propios
            componente.setFont(FUENTE_NORMAL);
        } else if (componente instanceof JTabbedPane) {
            JTabbedPane pestanas = (JTabbedPane) componente;
            pestanas.setFont(FUENTE_NEGRITA);
            for (Component hijo : pestanas.getComponents()) {
                estilizarComponente(hijo);
            }
        } else if (componente instanceof JPanel) {
            JPanel panel = (JPanel) componente;
            estilizarPanel(panel);
            for (Component hijo : panel.getComponents()) {
                estilizarComponente(hijo);
            }
        } else if (componente instanceof JLabel) {
            // Solo cambia la familia de fuente; conserva el tamaño y el negrita que ya tenía
            JLabel etiqueta = (JLabel) componente;
            Font fuenteActual = etiqueta.getFont();
            etiqueta.setFont(new Font("Segoe UI", fuenteActual.getStyle(), fuenteActual.getSize()));
        } else if (componente instanceof Container) {
            for (Component hijo : ((Container) componente).getComponents()) {
                estilizarComponente(hijo);
            }
        }
    }

    /** Paneles con título se vuelven "tarjetas"; los demás quedan transparentes. */
    private static void estilizarPanel(JPanel panel) {
        if (panel.getBorder() instanceof TitledBorder) {
            TitledBorder bordeConTitulo = (TitledBorder) panel.getBorder();
            panel.setOpaque(true);
            panel.setBackground(Color.WHITE);
            Border bordeInterno = BorderFactory.createTitledBorder(
                    BorderFactory.createEmptyBorder(8, 12, 8, 12),
                    bordeConTitulo.getTitle(),
                    TitledBorder.LEFT, TitledBorder.TOP,
                    FUENTE_NEGRITA, COLOR_TEXTO);
            panel.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(COLOR_BORDE, 1, true), bordeInterno));
        } else {
            panel.setOpaque(false);
        }
    }

    /** Elige el color del botón según lo que dice su texto. */
    private static Color colorSegunTexto(String texto) {
        String textoMinuscula = (texto == null) ? "" : texto.toLowerCase();
        if (textoMinuscula.contains("elimin") || textoMinuscula.contains("cancel")
                || textoMinuscula.contains("borrar") || textoMinuscula.contains("anular")) {
            return COLOR_PELIGRO;
        }
        if (textoMinuscula.contains("actualiz") || textoMinuscula.contains("modific")
                || textoMinuscula.contains("editar") || textoMinuscula.contains("confirm")
                || textoMinuscula.contains("finaliz")) {
            return COLOR_EXITO;
        }
        if (textoMinuscula.contains("busc") || textoMinuscula.contains("limpi")
                || textoMinuscula.contains("consult") || textoMinuscula.contains("refresc")) {
            return COLOR_NEUTRO;
        }
        return COLOR_PRIMARIO; // Registrar, Reservar, Guardar, Agregar...
    }

    // ===================== CLASES INTERNAS =====================

    /**
     * Pinta las filas de la tabla con color alternado y espacio interno.
     * Es pública para poder extenderla (por ejemplo, para colorear una columna de estado).
     */
    public static class FilaRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor,
                boolean seleccionado, boolean foco, int fila, int columna) {
            // Se pasa "false" en foco para quitar el borde de enfoque feo
            super.getTableCellRendererComponent(tabla, valor, seleccionado, false, fila, columna);
            setBorder(new EmptyBorder(0, 12, 0, 12));
            setHorizontalAlignment(SwingConstants.LEFT);
            setForeground(COLOR_TEXTO);
            if (seleccionado) {
                setBackground(COLOR_SELECCION);
            } else {
                setBackground(fila % 2 == 0 ? Color.WHITE : COLOR_FILA_ALTERNA);
            }
            return this;
        }
    }

    /** UI de botón redondeado con efecto al pasar el mouse y al presionar. */
    private static class BotonModernoUI extends BasicButtonUI {
        private final Color colorBase;

        BotonModernoUI(Color colorBase) {
            this.colorBase = colorBase;
        }

        @Override
        public void paint(Graphics g, JComponent componente) {
            JButton boton = (JButton) componente;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color colorFondo;
            if (!boton.isEnabled()) {
                colorFondo = new Color(0x94A3B8); // gris cuando está deshabilitado
            } else if (boton.getModel().isPressed()) {
                colorFondo = ajustar(colorBase, -30);
            } else if (boton.getModel().isRollover()) {
                colorFondo = ajustar(colorBase, 22);
            } else {
                colorFondo = colorBase;
            }

            g2.setColor(colorFondo);
            g2.fillRoundRect(0, 0, boton.getWidth(), boton.getHeight(), 16, 16);
            g2.dispose();

            super.paint(g, componente); // dibuja el texto encima
        }

        /** Dibuja el texto siempre en blanco (también cuando el botón está deshabilitado). */
        @Override
        protected void paintText(Graphics g, AbstractButton boton, Rectangle areaTexto, String texto) {
            FontMetrics metricas = g.getFontMetrics(boton.getFont());
            g.setFont(boton.getFont());
            g.setColor(boton.getForeground());
            BasicGraphicsUtils.drawStringUnderlineCharAt(g, texto, boton.getDisplayedMnemonicIndex(),
                    areaTexto.x + getTextShiftOffset(),
                    areaTexto.y + metricas.getAscent() + getTextShiftOffset());
        }

        /** Aclara (positivo) u oscurece (negativo) un color. */
        private static Color ajustar(Color color, int cantidad) {
            int rojo = Math.max(0, Math.min(255, color.getRed() + cantidad));
            int verde = Math.max(0, Math.min(255, color.getGreen() + cantidad));
            int azul = Math.max(0, Math.min(255, color.getBlue() + cantidad));
            return new Color(rojo, verde, azul);
        }
    }
}