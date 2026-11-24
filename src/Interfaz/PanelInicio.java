package Interfaz;

import Modelo.Reserva;
import Servicio.GestorReservas;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.time.LocalDate;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Pantalla de bienvenida: banner con degradado y tres tarjetas de resumen.
 * Los números salen del GestorReservas y se recalculan cada vez que
 * se entra a la sección (ver refrescar()).
 */
public class PanelInicio extends JPanel {

    private final GestorReservas gestorReservas;

    private final JLabel etiquetaReservasActivas = new JLabel("-");
    private final JLabel etiquetaHabitacionesLibres = new JLabel("-");
    private final JLabel etiquetaReservasTotales = new JLabel("-");

    public PanelInicio(GestorReservas gestorReservas) {
        this.gestorReservas = gestorReservas;

        // Este panel tiene su propio diseño: Tema.aplicarEstilo no debe tocarlo
        putClientProperty(Tema.PROPIEDAD_NO_ESTILIZAR, true);
        setOpaque(false);
        setLayout(new BorderLayout(0, 22));

        add(crearBanner(), BorderLayout.NORTH);

        // Las tarjetas van en el norte de otro panel para que no se estiren a todo el alto
        JPanel zonaTarjetas = new JPanel(new BorderLayout());
        zonaTarjetas.setOpaque(false);
        zonaTarjetas.add(crearTarjetasResumen(), BorderLayout.NORTH);
        add(zonaTarjetas, BorderLayout.CENTER);

        refrescar();
    }

    /** Recalcula los números de las tarjetas con los datos actuales del gestor. */
    public void refrescar() {
        // Reservas activas y totales
        try {
            int cantidadTotal = 0;
            int cantidadActivas = 0;
            for (Reserva reservaActual : gestorReservas.getListaReservas()) {
                cantidadTotal++;
                if (reservaActual.estaActiva()) {
                    cantidadActivas++;
                }
            }
            etiquetaReservasActivas.setText(String.valueOf(cantidadActivas));
            etiquetaReservasTotales.setText(String.valueOf(cantidadTotal));
        } catch (RuntimeException error) {
            etiquetaReservasActivas.setText("-");
            etiquetaReservasTotales.setText("-");
        }

        // Habitaciones libres para mañana (1 noche, 1 persona)
        try {
            int cantidadLibres = gestorReservas.obtenerHabitacionesDisponibles(
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(2), 1).size();
            etiquetaHabitacionesLibres.setText(String.valueOf(cantidadLibres));
        } catch (RuntimeException error) {
            etiquetaHabitacionesLibres.setText("-");
        }
    }

    // ============================== BANNER ==============================

    private JPanel crearBanner() {
        JPanel banner = new JPanel(new GridLayout(2, 1, 0, 6)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Degradado diagonal de coral a morado
                g2.setPaint(new GradientPaint(0, 0, Tema.COLOR_PRIMARIO,
                        getWidth(), getHeight(), Tema.COLOR_ACENTO_MORADO));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.dispose();
            }
        };
        banner.setOpaque(false);
        banner.setBorder(new EmptyBorder(28, 32, 28, 32));
        banner.setPreferredSize(new Dimension(0, 160));

        JLabel etiquetaBienvenida = new JLabel("Bienvenido a AgendaAirBNB");
        etiquetaBienvenida.setFont(new Font("Segoe UI", Font.BOLD, 30));
        etiquetaBienvenida.setForeground(Color.WHITE);

        JLabel etiquetaDescripcion = new JLabel(
                "Gestiona reservas, huéspedes y habitaciones de tu hotel en un solo lugar.");
        etiquetaDescripcion.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        etiquetaDescripcion.setForeground(new Color(255, 255, 255, 230));

        banner.add(etiquetaBienvenida);
        banner.add(etiquetaDescripcion);
        return banner;
    }

    // ========================= TARJETAS DE RESUMEN =========================

    private JPanel crearTarjetasResumen() {
        JPanel contenedor = new JPanel(new GridLayout(1, 3, 18, 0));
        contenedor.setOpaque(false);
        contenedor.setPreferredSize(new Dimension(0, 130));

        contenedor.add(crearTarjetaResumen("Reservas activas",
                etiquetaReservasActivas, Tema.COLOR_PRIMARIO));
        contenedor.add(crearTarjetaResumen("Habitaciones libres mañana",
                etiquetaHabitacionesLibres, Tema.COLOR_EXITO));
        contenedor.add(crearTarjetaResumen("Reservas registradas",
                etiquetaReservasTotales, Tema.COLOR_ACENTO_MORADO));
        return contenedor;
    }

    private JPanel crearTarjetaResumen(String titulo, JLabel etiquetaValor, Color colorValor) {
        JPanel tarjeta = Tema.crearTarjeta(new GridLayout(2, 1, 0, 4));

        etiquetaValor.setFont(new Font("Segoe UI", Font.BOLD, 38));
        etiquetaValor.setForeground(colorValor);

        JLabel etiquetaTitulo = new JLabel(titulo);
        etiquetaTitulo.setFont(Tema.FUENTE_NORMAL);
        etiquetaTitulo.setForeground(Tema.COLOR_TEXTO_SUAVE);

        tarjeta.add(etiquetaValor);
        tarjeta.add(etiquetaTitulo);
        return tarjeta;
    }
}