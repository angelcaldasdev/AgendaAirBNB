package Interfaz;

import Excepciones.ReglaNegocioException;
import Modelo.Habitacion;
import Modelo.Reserva;
import Servicio.GestorReservas;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/**
 * Pestaña para consultar habitaciones disponibles y crear reservas.
 * Reutiliza el GestorReservas: aquí solo hay interfaz, la lógica vive en el servicio.
 */
public class PanelReservas extends JPanel {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final GestorReservas gestorReservas;

    // Componentes del formulario
    private JTextField campoDni;
    private JTextField campoFechaEntrada;
    private JTextField campoFechaSalida;
    private JSpinner spinnerPersonas;
    private JComboBox<Habitacion> comboHabitaciones;
    private DefaultComboBoxModel<Habitacion> modeloHabitaciones;
    private JButton botonConsultar;
    private JButton botonReservar;
    private JButton botonLimpiar;

    public PanelReservas(GestorReservas gestorReservas) {
        this.gestorReservas = gestorReservas;
        construirPanel();
    }

    private void construirPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Campos de texto con fechas sugeridas (mañana y pasado mañana)
        campoDni = new JTextField();
        campoFechaEntrada = new JTextField(fechaSugerida(1));
        campoFechaSalida = new JTextField(fechaSugerida(2));

        // Spinner de personas: valor inicial 1, mínimo 1, máximo 4, de 1 en 1
        spinnerPersonas = new JSpinner(new SpinnerNumberModel(1, 1, 4, 1));

        // ComboBox con un modelo que podemos llenar y vaciar dinámicamente
        modeloHabitaciones = new DefaultComboBoxModel<>();
        comboHabitaciones = new JComboBox<>(modeloHabitaciones);
        comboHabitaciones.setRenderer(crearRenderizadorHabitaciones());

        // Formulario: 5 filas x 2 columnas (etiqueta + componente)
        JPanel panelFormulario = new JPanel(new GridLayout(5, 2, 10, 12));
        panelFormulario.add(new JLabel("DNI del huésped:"));
        panelFormulario.add(campoDni);
        panelFormulario.add(new JLabel("Fecha de entrada (dd/MM/yyyy):"));
        panelFormulario.add(campoFechaEntrada);
        panelFormulario.add(new JLabel("Fecha de salida (dd/MM/yyyy):"));
        panelFormulario.add(campoFechaSalida);
        panelFormulario.add(new JLabel("Cantidad de personas:"));
        panelFormulario.add(spinnerPersonas);
        panelFormulario.add(new JLabel("Habitación disponible:"));
        panelFormulario.add(comboHabitaciones);

        // Botones
        botonConsultar = new JButton("Consultar disponibles");
        botonLimpiar = new JButton("Limpiar");
        botonReservar = new JButton("Reservar");
        botonReservar.setEnabled(false); // se activa solo cuando hay habitaciones para elegir

        // LISTENERS: conectan cada EVENTO (clic) con su HANDLER (método)
        botonConsultar.addActionListener(evento -> consultarDisponibles());
        botonReservar.addActionListener(evento -> reservarDesdeFormulario());
        botonLimpiar.addActionListener(evento -> limpiarFormulario());

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.add(botonConsultar);
        panelBotones.add(botonLimpiar);
        panelBotones.add(botonReservar);

        add(panelFormulario, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
    }

    // ---------------- HANDLERS ----------------

    /** Handler de "Consultar disponibles": llena el combo con las habitaciones libres. */
    private void consultarDisponibles() {
        try {
            LocalDate fechaEntrada = leerFecha(campoFechaEntrada, "entrada");
            LocalDate fechaSalida = leerFecha(campoFechaSalida, "salida");
            int cantidadPersonas = (int) spinnerPersonas.getValue();

            ArrayList<Habitacion> habitacionesLibres = gestorReservas
                    .obtenerHabitacionesDisponibles(fechaEntrada, fechaSalida, cantidadPersonas);

            modeloHabitaciones.removeAllElements();
            for (Habitacion habitacionActual : habitacionesLibres) {
                modeloHabitaciones.addElement(habitacionActual);
            }
            botonReservar.setEnabled(!habitacionesLibres.isEmpty());

            if (habitacionesLibres.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No hay habitaciones disponibles para esos criterios.",
                        "Sin disponibilidad", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (ReglaNegocioException error) {
            mostrarError(error.getMessage());
        }
    }

    /** Handler de "Reservar": crea la reserva con la habitación elegida en el combo. */
    private void reservarDesdeFormulario() {
        try {
            Habitacion habitacionElegida = (Habitacion) comboHabitaciones.getSelectedItem();
            if (habitacionElegida == null) {
                throw new ReglaNegocioException("Primero consulta y elige una habitación disponible.");
            }

            Reserva nuevaReserva = gestorReservas.crearReserva(
                    campoDni.getText().trim(),
                    habitacionElegida.getNumero(),
                    leerFecha(campoFechaEntrada, "entrada"),
                    leerFecha(campoFechaSalida, "salida"),
                    (int) spinnerPersonas.getValue());

            JOptionPane.showMessageDialog(this,
                    "Reserva confirmada:\n" + nuevaReserva,
                    "Reserva exitosa", JOptionPane.INFORMATION_MESSAGE);
            limpiarFormulario();

        } catch (ReglaNegocioException error) {
            mostrarError(error.getMessage());
        }
    }

    /** Handler de "Limpiar": deja el formulario como al inicio. */
    private void limpiarFormulario() {
        campoDni.setText("");
        campoFechaEntrada.setText(fechaSugerida(1));
        campoFechaSalida.setText(fechaSugerida(2));
        spinnerPersonas.setValue(1);
        modeloHabitaciones.removeAllElements();
        botonReservar.setEnabled(false);
        campoDni.requestFocus();
    }

    // ---------------- Métodos de apoyo ----------------

    /** Convierte el texto de un campo en fecha; si el formato es incorrecto lanza la excepción de negocio. */
    private LocalDate leerFecha(JTextField campoFecha, String nombreFecha) {
        try {
            return LocalDate.parse(campoFecha.getText().trim(), FORMATO_FECHA);
        } catch (DateTimeParseException error) {
            throw new ReglaNegocioException("La fecha de " + nombreFecha
                    + " no es válida. Usa el formato dd/MM/yyyy, por ejemplo 25/12/2026.");
        }
    }

    /** Fecha de hoy + N días, ya formateada para mostrarla en los campos. */
    private String fechaSugerida(int diasDesdeHoy) {
        return LocalDate.now().plusDays(diasDesdeHoy).format(FORMATO_FECHA);
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "No se pudo completar la operación",
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Define cómo se ve cada habitación dentro del combo.
     * Así mostramos un texto más claro que el toString() del modelo.
     */
    private DefaultListCellRenderer crearRenderizadorHabitaciones() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                    boolean estaSeleccionado, boolean tieneFoco) {
                super.getListCellRendererComponent(lista, valor, indice, estaSeleccionado, tieneFoco);
                if (valor instanceof Habitacion) {
                    Habitacion habitacion = (Habitacion) valor;
                    setText("Hab. " + habitacion.getNumero() + " - " + habitacion.getTipo()
                            + " - S/ " + String.format("%.2f", habitacion.getPrecioPorNoche()) + " por noche");
                }
                return this;
            }
        };
    }
}