package GUI;

import Logica.FabricaCartas;
import Logica.Jugador;
import Logica.Mazo;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Font;
import java.awt.GridLayout;

public class Interfaz extends JFrame {
    private final CardLayout pantallas = new CardLayout();
    private final JPanel contenedor = new JPanel(pantallas);

    private final JTextField campoNombre1 = new JTextField();
    private final JTextField campoNombre2 = new JTextField();
    private final JComboBox<String> comboMazo1 = new JComboBox<>(FabricaCartas.nombresMazos());
    private final JComboBox<String> comboMazo2 = new JComboBox<>(FabricaCartas.nombresMazos());

    public Interfaz() {
        setTitle("Pokemon TCG Pocket");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        contenedor.add(crearPantallaInicio(), "inicio");
        add(contenedor);

        setSize(1000, 700);
        setLocationRelativeTo(null);
    }

    private JPanel crearPantallaInicio() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titulo = new JLabel("POKEMON TCG POCKET", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 22));
        panel.add(titulo, BorderLayout.NORTH);

        JPanel formulario = new JPanel(new GridLayout(4, 2, 10, 10));
        formulario.add(new JLabel("Entrenador 1:"));
        formulario.add(campoNombre1);
        formulario.add(new JLabel("Mazo del entrenador 1:"));
        formulario.add(comboMazo1);
        formulario.add(new JLabel("Entrenador 2:"));
        formulario.add(campoNombre2);
        formulario.add(new JLabel("Mazo del entrenador 2:"));
        formulario.add(comboMazo2);
        panel.add(formulario, BorderLayout.CENTER);

        JButton botonIniciar = new JButton("INICIAR COMBATE");
        botonIniciar.addActionListener(e -> intentarIniciar());
        panel.add(botonIniciar, BorderLayout.SOUTH);

        return panel;
    }

    private void intentarIniciar() {
        String nombre1 = campoNombre1.getText().trim();
        String nombre2 = campoNombre2.getText().trim();

        if (nombre1.isEmpty() || nombre2.isEmpty()) {
            mostrarError("Los dos entrenadores deben tener nombre");
            return;
        }

        //combo cuenta desde 0 y crearMazo desde 1 por eso el + 1
        Mazo mazo1 = FabricaCartas.crearMazo(comboMazo1.getSelectedIndex() + 1);
        Mazo mazo2 = FabricaCartas.crearMazo(comboMazo2.getSelectedIndex() + 1);

        if (!mazo1.esValido() || !mazo2.esValido()) {
            mostrarError("Alguno de los mazos no es valido");
            return;
        }

        iniciarPartida(new Jugador(nombre1, mazo1), new Jugador(nombre2, mazo2));
    }

    private void iniciarPartida(Jugador jugador1, Jugador jugador2) {
        
        // POR AHORA UNA PANTALLA DE PRUEBA
        JLabel prueba = new JLabel(jugador1.getNombre() + " vs " + jugador2.getNombre()
                + "  (aqui ira el tablero :v )", SwingConstants.CENTER);
        contenedor.add(prueba, "tablero");
        pantallas.show(contenedor, "tablero");
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Datos invalidos", JOptionPane.ERROR_MESSAGE);
    }
}