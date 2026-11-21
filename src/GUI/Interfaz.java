package GUI;

import Logica.CartaEntrenador;
import Logica.ClaseAtaque;
import Logica.ClaseCarta;
import Logica.ClaseEvolucion;
import Logica.ClasePokemon;
import Logica.ClaseTipo;
import Logica.FabricaCartas;
import Logica.Jugador;
import Logica.Mazo;
import Logica.Partida;
import Logica.Vista;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

// implementa vista , asi misty y partida pueden mostrar mensajes y pedir datos sin saber que andan en una GUI
public class Interfaz extends JFrame implements Vista {
    private static final int MAX_BANCO = 2;
    private static final int CASILLAS_POR_CAMPO = 1 + MAX_BANCO; 

    private final CardLayout pantallas = new CardLayout();
    private final JPanel contenedor = new JPanel(pantallas);

    private Partida partida;

    private final JTextField campoNombre1 = new JTextField();
    private final JTextField campoNombre2 = new JTextField();
    private final JComboBox<String> comboMazo1 = new JComboBox<>(FabricaCartas.nombresMazos());
    private final JComboBox<String> comboMazo2 = new JComboBox<>(FabricaCartas.nombresMazos());

    private final JLabel lblTransicion = new JLabel("", SwingConstants.CENTER);
    private final JButton btnContinuar = new JButton("Continuar");

    // Tablero 

    private final JLabel[] lblNombre = new JLabel[2 * CASILLAS_POR_CAMPO];
    private final JLabel[] lblEtapa = new JLabel[2 * CASILLAS_POR_CAMPO];
    private final JProgressBar[] barraPs = new JProgressBar[2 * CASILLAS_POR_CAMPO];
    private final JLabel[] lblEnergias = new JLabel[2 * CASILLAS_POR_CAMPO];

    private final JLabel lblTurno = new JLabel("", SwingConstants.CENTER);
    private final JPanel panelRival = new JPanel(new BorderLayout(4, 4));
    private final JPanel panelPropio = new JPanel(new BorderLayout(4, 4));
    private final JLabel lblDatosRival = new JLabel(" ");
    private final JLabel lblDatosPropio = new JLabel(" ");
    private final JLabel lblZonaEnergia = new JLabel(" ");

    private final JPanel panelMano = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
    private final List<JToggleButton> botonesMano = new ArrayList<>();
    private ButtonGroup grupoMano = new ButtonGroup();

    private final JTextArea log = new JTextArea();

    private final JButton btnEnergia = new JButton("Adjuntar energia");
    private final JButton btnEntrenador = new JButton("Jugar entrenador");
    private final JButton btnBanco = new JButton("Colocar en banco");
    private final JButton btnRetirar = new JButton("Retirar activo");
    private final JButton btnEvolucionar = new JButton("Evolucionar");
    private final JButton btnAtacar = new JButton("Atacar");
    private final JButton btnPasar = new JButton("Pasar turno");

    public Interfaz() {
        setTitle("Pokemon TCG Pocket");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        contenedor.add(crearPantallaInicio(), "inicio");
        contenedor.add(crearPantallaTransicion(), "transicion");
        contenedor.add(crearPantallaTablero(), "tablero");
        add(contenedor);

        setSize(1000, 700);
        setLocationRelativeTo(null);
    }

    //  implementacion de Vista 

    @Override
    public void mostrarMensaje(String mensaje) {
        agregarLog(mensaje);
    }

    @Override
    public int elegirOpcion(String titulo, List<String> opciones, boolean permitirCancelar) {
        List<String> botones = new ArrayList<>(opciones);
        if (permitirCancelar) botones.add("Cancelar");

        while (true) {
            int r = JOptionPane.showOptionDialog(this, titulo, "Elige una opcion",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, botones.toArray(), botones.get(0));
            if (r >= 0 && r < opciones.size()) return r;          
            if (permitirCancelar) return -1;                      
        }
    }

    // La GUI no necesita estos
    @Override
    public void mostrarCampo(Jugador actual, Jugador rival, int turno) { }

    @Override
    public int leerAccion() {
        return -1;
    }

    @Override
    public void anunciarGanador(Jugador ganador, String motivo) { }

    //  PANTALLA DE INICIO

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

    private void iniciarPartida(Jugador j1, Jugador j2) {
        partida = new Partida(j1, j2, this);
        log.setText("");
        agregarLog("Comienza el duelo: " + j1.getNombre() + " vs " + j2.getNombre());

        JOptionPane.showMessageDialog(this, j1.getNombre() + ", prepara tu campo, Que " + j2.getNombre()
                + " no mire la pantalla", "Preparacion", JOptionPane.INFORMATION_MESSAGE);
        partida.prepararJugador(j1);

        JOptionPane.showMessageDialog(this, j2.getNombre() + ", prepara tu campo, Que " + j1.getNombre()
                + " no mire la pantalla", "Preparacion", JOptionPane.INFORMATION_MESSAGE);
        partida.prepararJugador(j2);

        siguienteTurno();
    }

    //  Transicion entre turnos

    private JPanel crearPantallaTransicion() {
        JPanel panel = new JPanel(new GridBagLayout());
        lblTransicion.setFont(new Font("Arial", Font.BOLD, 28));
        btnContinuar.setFont(new Font("Arial", Font.BOLD, 18));
        btnContinuar.addActionListener(e -> alContinuar());

        JPanel caja = new JPanel(new GridLayout(2, 1, 10, 25));
        caja.add(lblTransicion);
        caja.add(btnContinuar);
        panel.add(caja);
        return panel;
    }

    private void siguienteTurno() {
        lblTransicion.setText("Turno de " + partida.getJugadorActual().getNombre()
                + " - presiona Continuar");
        pantallas.show(contenedor, "transicion");
    }

    private void alContinuar() {
        partida.comenzarTurno(); 
        if (partida.hayGanador()) {
            anunciarFin();
            return;
        }
        refrescar();
        pantallas.show(contenedor, "tablero");
    }

    private void anunciarFin() {
        Object[] opciones = { "Jugar otra partida", "Salir" };
        int r = JOptionPane.showOptionDialog(this,partida.getGanador().getNombre() + " es el nuevo Maestro Pokemon!\n" + "Gano porque " 
        + partida.getMotivo(),"Fin del duelo",
         JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,null, opciones, opciones[0]);
        if (r == 0) {
            pantallas.show(contenedor, "inicio");
        } else {
            System.exit(0);
        }
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Aviso", JOptionPane.ERROR_MESSAGE);
    }

    //  contrucion de pantalla del tablero

    private JPanel crearPantallaTablero() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        lblTurno.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(lblTurno, BorderLayout.NORTH);

        // centro arriba el rival (casillas 0-2), abajo yo (casillas 3-5)
        JPanel centro = new JPanel(new GridLayout(2, 1, 8, 8));
        armarCampo(panelRival, lblDatosRival, 0);
        armarCampo(panelPropio, lblDatosPropio, CASILLAS_POR_CAMPO);
        centro.add(panelRival);
        centro.add(panelPropio);
        panel.add(centro, BorderLayout.CENTER);

        // derecha registro de eventos 
        log.setEditable(false);
        log.setLineWrap(true);
        log.setWrapStyleWord(true);
        JScrollPane scrollLog = new JScrollPane(log);
        scrollLog.setBorder(BorderFactory.createTitledBorder("Registro de eventos"));
        scrollLog.setPreferredSize(new Dimension(260, 0));
        panel.add(scrollLog, BorderLayout.EAST);

        // abajo zona de energia, mano y botones de accion
        JPanel sur = new JPanel(new BorderLayout(4, 4));
        sur.add(lblZonaEnergia, BorderLayout.NORTH);
        sur.add(panelMano, BorderLayout.CENTER);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
        acciones.add(btnEnergia);
        acciones.add(btnEntrenador);
        acciones.add(btnBanco);
        acciones.add(btnRetirar);
        acciones.add(btnEvolucionar);
        acciones.add(btnAtacar);
        acciones.add(btnPasar);
        sur.add(acciones, BorderLayout.SOUTH);
        panel.add(sur, BorderLayout.SOUTH);

        // cada boton solo llama a su metodo
        btnEnergia.addActionListener(e -> alAdjuntarEnergia());
        btnEntrenador.addActionListener(e -> alJugarEntrenador());
        btnBanco.addActionListener(e -> alColocarEnBanco());
        btnRetirar.addActionListener(e -> alRetirar());
        btnEvolucionar.addActionListener(e -> alEvolucionar());
        btnAtacar.addActionListener(e -> alAtacar());
        btnPasar.addActionListener(e -> despuesDeAccion(partida.pasarTurno()));

        return panel;
    }

    // Un campo = fila con 3 casillas (activo, banco 1, banco 2) + una linea de datos abajo
    // "primera" es el indice de la primera casilla de ese campo 0 rival , 3 propio
    private void armarCampo(JPanel campo, JLabel lblDatos, int primera) {
        JPanel fila = new JPanel(new GridLayout(1, CASILLAS_POR_CAMPO, 8, 8));
        fila.add(crearCasilla(primera, "Activo"));
        for (int b = 1; b <= MAX_BANCO; b++) {
            fila.add(crearCasilla(primera + b, "Banco " + b));
        }
        campo.add(fila, BorderLayout.CENTER);
        campo.add(lblDatos, BorderLayout.SOUTH);
    }

    // crea los componentes de una casilla y los guarda en los arreglos en la posicion i
    private JPanel crearCasilla(int i, String titulo) {
        lblNombre[i] = new JLabel();
        lblNombre[i].setFont(lblNombre[i].getFont().deriveFont(Font.BOLD, 14f));
        lblEtapa[i] = new JLabel();
        barraPs[i] = new JProgressBar();
        barraPs[i].setStringPainted(true);
        lblEnergias[i] = new JLabel();

        JPanel casilla = new JPanel();
        casilla.setLayout(new BoxLayout(casilla, BoxLayout.Y_AXIS));
        casilla.setBorder(BorderFactory.createTitledBorder(titulo));

        // sin setAlignmentX el BoxLayout alinea cada componente distinto
        for (JComponent c : new JComponent[] { lblNombre[i], lblEtapa[i], barraPs[i], lblEnergias[i] }) {
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
            casilla.add(c);
        }
        mostrarPokemon(i, null);
        return casilla;
    }

    //  ACCIONES 

    private void alAdjuntarEnergia() {
        List<ClasePokemon> enJuego = partida.getJugadorActual().getPokemonEnJuego();
        int i = elegirOpcion("¿A que Pokemon adjuntas la energia?", etiquetas(enJuego), true);
        if (i < 0) return;
        despuesDeAccion(partida.adjuntarEnergia(enJuego.get(i)));
    }

    private void alJugarEntrenador() {
        ClaseCarta carta = cartaSeleccionada();
        if (carta == null) {
            mostrarError("Selecciona primero una carta de tu mano");
            return;
        }
        if (!(carta instanceof CartaEntrenador entrenador)) {
            mostrarError(carta.getNombre() + " no es una carta de entrenador");
            return;
        }
        despuesDeAccion(partida.jugarEntrenador(entrenador));
    }

    private void alColocarEnBanco() {
        ClaseCarta carta = cartaSeleccionada();
        if (carta == null) {
            mostrarError("Selecciona primero un Pokemon de tu mano");
            return;
        }
        if (!(carta instanceof ClasePokemon pokemon)) {
            mostrarError(carta.getNombre() + " no es un Pokemon");
            return;
        }
        despuesDeAccion(partida.colocarEnBanco(pokemon));
    }

    private void alEvolucionar() {
        ClaseCarta carta = cartaSeleccionada();
        if (carta == null) {
            mostrarError("Selecciona primero una carta de evolucion de tu mano");
            return;
        }
        if (!(carta instanceof ClaseEvolucion evolucion)) {
            mostrarError(carta.getNombre() + " no es una carta de evolucion");
            return;
        }
        List<ClasePokemon> objetivos = partida.getObjetivosEvolucion(evolucion);
        if (objetivos.isEmpty()) {
            mostrarError(evolucion.getNombre() + " necesita a " + evolucion.getEvolucionaDe()
                    + " en juego, que no haya entrado este turno ni haya evolucionado ya.");
            return;
        }
        ClasePokemon objetivo = objetivos.get(0);
        if (objetivos.size() > 1) { // si hay mas de uno posible, el jugador elige
            int i = elegirOpcion("¿cual Pokemon evolucionas?", etiquetas(objetivos), true);
            if (i < 0) return;
            objetivo = objetivos.get(i);
        }
        despuesDeAccion(partida.evolucionar(evolucion, objetivo));
    }

    private void alRetirar() {
        Partida.Resultado puede = partida.validarRetirar();
        if (!puede.exito()) {
            mostrarError(puede.mensaje());
            return;
        }
        List<ClasePokemon> banco = partida.getJugadorActual().getBanco();
        int i = elegirOpcion("¿Que Pokemon pasa a activo?", etiquetas(banco), true);
        if (i < 0) return;
        despuesDeAccion(partida.retirar(i));
    }

    private void alAtacar() {
        Partida.Resultado puede = partida.validarAtacar();
        if (!puede.exito()) {
            mostrarError(puede.mensaje());
            return;
        }
        ClasePokemon atacante = partida.getJugadorActual().getActivo();
        List<String> opciones = new ArrayList<>();
        for (int k = 0; k < atacante.getAtaques().size(); k++) {
            ClaseAtaque ataque = atacante.getAtaques().get(k);
            // se avisa en la lista cuales ataques todavia no se pueden usar
            opciones.add(ataque + (partida.validarAtaque(k).exito() ? "" : "  [sin energia]"));
        }
        int i = elegirOpcion("Elige un ataque", opciones, true);
        if (i < 0) return;
        despuesDeAccion(partida.atacar(i)); 
    }

    // muestra el error si fallo, y si no, decide que pantalla toca, si fin de partida, siguiente jugador, o seguir en el mismo turno
    private void despuesDeAccion(Partida.Resultado resultado) {
        if (!resultado.exito()) {
            mostrarError(resultado.mensaje());
            return;
        }
        refrescar();
        if (partida.hayGanador()) {
            anunciarFin();
        } else if (!partida.isTurnoEnCurso()) { // atacar y pasar terminan el turno
            siguienteTurno();
        }
    }

    // Nombre + PS de cada Pokemon
    private List<String> etiquetas(List<ClasePokemon> pokemones) {
        List<String> etiquetas = new ArrayList<>();
        for (ClasePokemon p : pokemones) {
            etiquetas.add(p.getNombre() + " (" + p.getPsActual() + "/" + p.getPsMax() + " PS)");
        }
        return etiquetas;
    }

    //  Pantalla de tablero actualizacion
    // Vuelve a pintar todo desde el estado actual del modelo
    private void refrescar() {
        actualizarTablero(partida.getJugadorActual(), partida.getRival(), partida.getTurno());
        actualizarBotones();
    }

    // Habilita cada boton segun lo que diga Partida, si no se puede, el motivo queda
    // como texto al pasar el mouse por encima (tooltip).
    private void actualizarBotones() {
        configurar(btnEnergia, partida.validarAdjuntarEnergia());
        configurar(btnBanco, partida.validarColocarEnBanco());
        configurar(btnRetirar, partida.validarRetirar());
        configurar(btnEvolucionar, partida.validarEvolucionar());
        configurar(btnAtacar, partida.validarAtacar());
        btnEntrenador.setEnabled(true);
        btnPasar.setEnabled(true);
    }

    private void configurar(JButton boton, Partida.Resultado resultado) {
        boton.setEnabled(resultado.exito());
        boton.setToolTipText(resultado.exito() ? null : resultado.mensaje());
    }

    // Solo se muestra la mano del JUGADOR ACTUAL, la del rival no
    private void actualizarTablero(Jugador actual, Jugador rival, int turno) {
        lblTurno.setText("TURNO " + turno + " - " + actual.getNombre());
        mostrarCampo(panelRival, lblDatosRival, 0, "RIVAL (" + rival.getNombre() + ")", rival);
        mostrarCampo(panelPropio, lblDatosPropio, CASILLAS_POR_CAMPO, "TU CAMPO (" + actual.getNombre() + ")", actual);

        if (actual.getZonaEnergia() == null) {
            lblZonaEnergia.setText("Zona de energia: -");
        } else {
            lblZonaEnergia.setText("Zona de energia: " + actual.getZonaEnergia().getSimbolo()
                    + (actual.isEnergiaDisponible() ? " (disponible)" : " (ya usada)"));
        }
        actualizarMano(actual);
    }

    private void mostrarCampo(JPanel panel, JLabel lblDatos, int primera, String titulo, Jugador j) {
        panel.setBorder(BorderFactory.createTitledBorder(titulo));
        mostrarPokemon(primera, j.getActivo());
        for (int b = 0; b < MAX_BANCO; b++) {
            // si hay un Pokemon en esa posicion del banco lo muestra, si no, null = vacio
            mostrarPokemon(primera + 1 + b, b < j.getBanco().size() ? j.getBanco().get(b) : null);
        }
        lblDatos.setText("Puntos de premio: " + j.getPuntosPremio() + "/" + partida.getPremiosParaGanar()
                + "     Cartas en el mazo: " + j.getMazo().getCantidad());
    }

    // Dibuja la casilla i con los datos del Pokemon p, p == null significa "casilla vacia".
    private void mostrarPokemon(int i, ClasePokemon p) {
        if (p == null) {
            lblNombre[i].setText("(vacio)");
            lblEtapa[i].setText(" ");
            barraPs[i].setMaximum(1);
            barraPs[i].setValue(0);
            barraPs[i].setString("-");
            barraPs[i].setForeground(Color.GRAY);
            lblEnergias[i].setText(" ");
            return;
        }
        lblNombre[i].setText(p.getNombre());
        lblEtapa[i].setText(p.getEtapa() + " - " + p.getTipo());
        barraPs[i].setMaximum(p.getPsMax());
        barraPs[i].setValue(p.getPsActual());
        barraPs[i].setString(p.getPsActual() + "/" + p.getPsMax());
        barraPs[i].setForeground(colorSegunVida(p));

        StringBuilder energias = new StringBuilder();
        for (ClaseTipo e : p.getEnergias()) {
            energias.append(e.getSimbolo());
        }
        lblEnergias[i].setText("Energia: " + (energias.length() == 0 ? "ninguna" : energias));
    }

    // Este el para la vida, verde si le queda mas de la mitad, naranja hasta un cuarto, rojo si menos
    private Color colorSegunVida(ClasePokemon p) {
        double porcentaje = (double) p.getPsActual() / p.getPsMax();
        if (porcentaje > 0.5) return new Color(76, 175, 80);
        if (porcentaje > 0.25) return new Color(255, 152, 0);
        return new Color(211, 47, 47);
    }

    // El JToggleButton hace que por carta se puede seleccionar y queda como hundido
    // El ButtonGroup hace que solo una carta este seleccionada a la vez
    private void actualizarMano(Jugador actual) {
        panelMano.removeAll();
        botonesMano.clear();
        grupoMano = new ButtonGroup();

        for (ClaseCarta c : actual.getMano()) {
            JToggleButton boton = new JToggleButton(c.getNombre());
            grupoMano.add(boton);
            botonesMano.add(boton);
            panelMano.add(boton);
        }
        panelMano.setBorder(BorderFactory.createTitledBorder(
                "Tu mano (" + actual.getMano().size() + " cartas) - selecciona una carta y luego una accion"));
        panelMano.revalidate(); 
        panelMano.repaint();
    }

    // La carta de la mano que esta seleccionada, o null si no hay ninguna seleccionada
    private ClaseCarta cartaSeleccionada() {
        for (int i = 0; i < botonesMano.size(); i++) {
            if (botonesMano.get(i).isSelected()) return partida.getJugadorActual().getMano().get(i);
        }
        return null;
    }

    private void agregarLog(String mensaje) {
        log.append(mensaje + "\n");
        log.setCaretPosition(log.getDocument().getLength()); // baja el scroll al final
    }
}