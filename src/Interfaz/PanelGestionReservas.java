package Interfaz;

import Excepciones.ReglaNegocioException;
import Modelo.EstadoReserva;
import Modelo.Reserva;
import Servicio.GestorReservas;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/**
 * Pestaña para ver todas las reservas y cancelarlas o finalizarlas.
 * Muestra los datos en una JTable alimentada por el GestorReservas.
 */
public class PanelGestionReservas extends JPanel {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String[] NOMBRES_COLUMNAS =
            {"Código", "Huésped", "Hab.", "Entrada", "Salida", "Noches", "Total (S/)", "Estado"};

    // Posición de las columnas que usamos después
    private static final int COLUMNA_CODIGO = 0;
    private static final int COLUMNA_ESTADO = 7;

    private final GestorReservas gestorReservas;

    private DefaultTableModel modeloTabla;
    private JTable tablaReservas;
    private JLabel etiquetaResumen;
    private JButton botonActualizar;
    private JButton botonCancelar;
    private JButton botonFinalizar;

    public PanelGestionReservas(GestorReservas gestorReservas) {
        this.gestorReservas = gestorReservas;
        construirPanel();
        refrescarTabla();
    }

    private void construirPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Modelo de la tabla: las celdas no se pueden editar a mano
        modeloTabla = new DefaultTableModel(NOMBRES_COLUMNAS, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tablaReservas = new JTable(modeloTabla);
        tablaReservas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // una fila a la vez
        tablaReservas.getTableHeader().setReorderingAllowed(false);
        tablaReservas.setFillsViewportHeight(true);
        tablaReservas.setRowHeight(24);
        tablaReservas.getColumnModel().getColumn(COLUMNA_CODIGO).setPreferredWidth(90);
        tablaReservas.getColumnModel().getColumn(1).setPreferredWidth(170); // nombre del huésped

        // LISTENER de selección: activa o desactiva los botones según la fila elegida
        tablaReservas.getSelectionModel().addListSelectionListener(evento -> {
            if (!evento.getValueIsAdjusting()) {
                actualizarEstadoBotones();
            }
        });

        etiquetaResumen = new JLabel();

        botonActualizar = new JButton("Actualizar");
        botonCancelar = new JButton("Cancelar reserva");
        botonFinalizar = new JButton("Finalizar (check-out)");

        // LISTENERS de los botones: conectan el clic con su HANDLER
        botonActualizar.addActionListener(evento -> refrescarTabla());
        botonCancelar.addActionListener(evento -> cancelarSeleccionada());
        botonFinalizar.addActionListener(evento -> finalizarSeleccionada());

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.add(botonActualizar);
        panelBotones.add(botonCancelar);
        panelBotones.add(botonFinalizar);

        add(etiquetaResumen, BorderLayout.NORTH);
        add(new JScrollPane(tablaReservas), BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
    }

    // ---------------- HANDLERS ----------------

    /** Vuelve a cargar la tabla con las reservas actuales del gestor. */
    public void refrescarTabla() {
        modeloTabla.setRowCount(0); // vacía la tabla

        int cantidadActivas = 0;
        for (Reserva reservaActual : gestorReservas.getListaReservas()) {
            modeloTabla.addRow(new Object[]{
                reservaActual.getCodigo(),
                reservaActual.getHuesped().getNombreCompleto(),
                reservaActual.getHabitacion().getNumero(),
                reservaActual.getFechaEntrada().format(FORMATO_FECHA),
                reservaActual.getFechaSalida().format(FORMATO_FECHA),
                reservaActual.calcularNoches(),
                String.format("%.2f", reservaActual.getTotalPagar()),
                reservaActual.getEstado().getDescripcion()
            });
            if (reservaActual.estaActiva()) {
                cantidadActivas++;
            }
        }

        etiquetaResumen.setText("Total: " + modeloTabla.getRowCount()
                + " reserva(s)  |  Activas: " + cantidadActivas);
        actualizarEstadoBotones();
    }

    /** Handler de "Cancelar reserva". */
    private void cancelarSeleccionada() {
        String codigoReserva = obtenerCodigoSeleccionado();
        if (codigoReserva == null) {
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Seguro que deseas cancelar la reserva " + codigoReserva + "?",
                "Confirmar cancelación", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            gestorReservas.cancelarReserva(codigoReserva);
            refrescarTabla();
            JOptionPane.showMessageDialog(this, "La reserva " + codigoReserva + " fue cancelada.",
                    "Reserva cancelada", JOptionPane.INFORMATION_MESSAGE);
        } catch (ReglaNegocioException error) {
            mostrarError(error.getMessage());
        }
    }

    /** Handler de "Finalizar (check-out)". */
    private void finalizarSeleccionada() {
        String codigoReserva = obtenerCodigoSeleccionado();
        if (codigoReserva == null) {
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Confirmas el check-out de la reserva " + codigoReserva + "?",
                "Confirmar finalización", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            gestorReservas.finalizarReserva(codigoReserva);
            refrescarTabla();
            JOptionPane.showMessageDialog(this, "La reserva " + codigoReserva + " fue finalizada.",
                    "Check-out realizado", JOptionPane.INFORMATION_MESSAGE);
        } catch (ReglaNegocioException error) {
            mostrarError(error.getMessage());
        }
    }

    // ---------------- Métodos de apoyo ----------------

    /** Devuelve el código de la reserva seleccionada, o null si no hay ninguna fila elegida. */
    private String obtenerCodigoSeleccionado() {
        int filaSeleccionada = tablaReservas.getSelectedRow();
        if (filaSeleccionada < 0) {
            return null;
        }
        return (String) modeloTabla.getValueAt(filaSeleccionada, COLUMNA_CODIGO);
    }

    /** Los botones de cancelar y finalizar solo se activan si la reserva elegida está Activa. */
    private void actualizarEstadoBotones() {
        int filaSeleccionada = tablaReservas.getSelectedRow();
        boolean hayReservaActivaElegida = false;

        if (filaSeleccionada >= 0) {
            String estadoFila = (String) modeloTabla.getValueAt(filaSeleccionada, COLUMNA_ESTADO);
            hayReservaActivaElegida = estadoFila.equals(EstadoReserva.ACTIVA.getDescripcion());
        }

        botonCancelar.setEnabled(hayReservaActivaElegida);
        botonFinalizar.setEnabled(hayReservaActivaElegida);
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "No se pudo completar la operación",
                JOptionPane.ERROR_MESSAGE);
    }
}