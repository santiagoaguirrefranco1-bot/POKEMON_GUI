package Logica;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Partida {
    private static final int PREMIOS_PARA_GANAR = 3;
    private static final int MAX_BANCO = 2;

    // Resultado de intentar una accion: exito o el motivo por el que no se pudo.
    public record Resultado(boolean exito, String mensaje) { }

    private final Jugador[] jugadores;
    private final Vista vista;
    private final int[] turnosJugados = new int[2]; // turnos propios de cada jugador
    private int turno = 0;                          // turno global (1, 2, 3...)
    private int indiceActual;
    private boolean turnoEnCurso = false;
    private Jugador ganador;
    private String motivo;

    public Partida(Jugador j1, Jugador j2, Vista vista) {
        this.jugadores = new Jugador[] { j1, j2 };
        this.vista = vista;
        this.indiceActual = new Random().nextInt(2);
    }

    //La vista tambien se usa para narrar eventos (registro) y para las elecciones de las cartas.
    public Vista getVista() {
        return vista;
    }

    public Jugador getJugadorActual() {
        return jugadores[indiceActual];
    }

    public Jugador getRival() {
        return jugadores[1 - indiceActual];
    }

    public int getTurno() {
        return turno;
    }

    public boolean isTurnoEnCurso() {
        return turnoEnCurso;
    }

    public boolean hayGanador() {
        return ganador != null;
    }

    public Jugador getGanador() {
        return ganador;
    }

    //Condicion de victoria, lista para mostrar en el mensaje final.
    public String getMotivo() {
        return motivo;
    }

    public int getPremiosParaGanar() {
        return PREMIOS_PARA_GANAR;
    }

    //Prepara a ambos jugadores. Si la vista quiere una transicion entre ellos, puede llamar prepararJugador() por separado.
    public void iniciarPartida() {
        for (Jugador j : jugadores) {
            prepararJugador(j);
        }
    }

    //Reparte la mano inicial (con mulligan) y pide el activo y el banco inicial.
    public void prepararJugador(Jugador j) {
        int mulligans = j.repartirManoInicial();
        if (mulligans > 0) {
            vista.mostrarMensaje(j.getNombre() + " hizo " + mulligans + " mulligans por no tener Pokemon basico.");
        }
        List<ClasePokemon> basicos = pokemonBasicosEnMano(j);
        int i = vista.elegirOpcion(j.getNombre() + ": elige tu Pokemon activo", nombres(basicos), false);
        j.colocarActivo(basicos.get(i));

        while (j.getBanco().size() < MAX_BANCO) {
            basicos = pokemonBasicosEnMano(j);
            if (basicos.isEmpty()) break;
            i = vista.elegirOpcion(j.getNombre() + ": colocas un Pokemon en el banco?", nombres(basicos), true);
            if (i < 0) break;
            j.colocarEnBanco(basicos.get(i));
        }
    }

    public void comenzarTurno() {
        if (ganador != null || turnoEnCurso) return;

        Jugador actual = getJugadorActual();
        turno++;
        turnosJugados[indiceActual]++;
        turnoEnCurso = true;

        actual.iniciarTurno();

        // Un Pokemon solo puede evolucionar si ya estaba en juego al empezar el turno,
        // y nadie evoluciona en su primer turno.
        if (turnosJugados[indiceActual] >= 2) {
            for (ClasePokemon p : actual.getPokemonEnJuego()) {
                p.habilitarEvolucion();
            }
        }

        if (!actual.robar()) {
            turnoEnCurso = false;
            terminar(getRival(), actual.getNombre() + " no tiene cartas para robar");
            return;
        }
        vista.mostrarMensaje("- Turno " + turno + ": " + actual.getNombre() + " -");
    }

    public Resultado pasarTurno() {
        Resultado r = validarTurnoActivo();
        if (!r.exito()) return r;
        vista.mostrarMensaje(getJugadorActual().getNombre() + " pasa el turno.");
        cerrarTurno();
        return ok();
    }

    private void cerrarTurno() {
        turnoEnCurso = false;
        indiceActual = 1 - indiceActual;
    }

    // Accion: adjuntar energia

    public Resultado validarAdjuntarEnergia() {
        Resultado r = validarTurnoActivo();
        if (!r.exito()) return r;
        if (!getJugadorActual().isEnergiaDisponible()) {
            return error("Ya usaste la energia de este turno.");
        }
        return ok();
    }

    public Resultado adjuntarEnergia(ClasePokemon destino) {
        Resultado r = validarAdjuntarEnergia();
        if (!r.exito()) return r;
        Jugador j = getJugadorActual();
        if (destino == null || !j.getPokemonEnJuego().contains(destino)) {
            return error("Elige uno de tus Pokemon en juego.");
        }
        ClaseTipo tipo = j.getZonaEnergia();
        j.adjuntarEnergia(destino);
        vista.mostrarMensaje("Energia " + tipo.getSimbolo() + " adjuntada a " + destino.getNombre() + ".");
        return ok();
    }

    // Accion: jugar carta de entrenador

    public Resultado validarJugarEntrenador(CartaEntrenador carta) {
        Resultado r = validarTurnoActivo();
        if (!r.exito()) return r;
        Jugador j = getJugadorActual();
        if (carta == null || !j.getMano().contains(carta)) {
            return error("Esa carta no esta en tu mano.");
        }
        if (carta instanceof Soporte && j.isSoporteJugado()) {
            return error("Solo puedes jugar 1 carta de Soporte por turno.");
        }
        return ok();
    }

    public Resultado jugarEntrenador(CartaEntrenador carta) {
        Resultado r = validarJugarEntrenador(carta);
        if (!r.exito()) return r;
        Jugador j = getJugadorActual();

        j.quitarDeMano(carta);
        if (carta.activar(j, this)) {
            j.descartar(carta);
            if (carta instanceof Soporte) j.marcarSoporteJugado();
            return ok();
        }
        j.agregarAMano(carta);
        return error("No puedes usar " + carta.getNombre() + " ahora.");
    }

    // Accion: colocar Pokemon basico en el banco

    public Resultado validarColocarEnBanco() {
        Resultado r = validarTurnoActivo();
        if (!r.exito()) return r;
        Jugador j = getJugadorActual();
        if (j.getBanco().size() >= MAX_BANCO) {
            return error("Tu banco esta lleno.");
        }
        if (pokemonBasicosEnMano(j).isEmpty()) {
            return error("No tienes Pokemon basicos en la mano.");
        }
        return ok();
    }

    public Resultado colocarEnBanco(ClasePokemon pokemon) {
        Resultado r = validarColocarEnBanco();
        if (!r.exito()) return r;
        Jugador j = getJugadorActual();
        if (pokemon == null || !j.getMano().contains(pokemon)) {
            return error("Esa carta no esta en tu mano.");
        }
        if (!pokemon.esBasico()) {
            return error(pokemon.getNombre() + " es una evolucion: solo entra en juego evolucionando.");
        }
        j.colocarEnBanco(pokemon);
        vista.mostrarMensaje(pokemon.getNombre() + " pasa al banco de " + j.getNombre() + ".");
        return ok();
    }

    // Accion: evolucionar Pokemon

    //Pokemon en juego sobre los que se puede colocar esta carta de evolucion en este momento.
    public List<ClasePokemon> getObjetivosEvolucion(ClaseEvolucion carta) {
        List<ClasePokemon> objetivos = new ArrayList<>();
        for (ClasePokemon p : getJugadorActual().getPokemonEnJuego()) {
            if (p.getNombre().equals(carta.getEvolucionaDe()) && p.isListoParaEvolucionar()) {
                objetivos.add(p);
            }
        }
        return objetivos;
    }

    //Para habilitar el boton "Evolucionar Pokemon": hay alguna evolucion en mano que se pueda usar.
    public Resultado validarEvolucionar() {
        Resultado r = validarTurnoActivo();
        if (!r.exito()) return r;
        if (turnosJugados[indiceActual] == 1) {
            return error("No puedes evolucionar en tu primer turno.");
        }
        boolean hayEvolucionEnMano = false;
        for (ClaseCarta c : getJugadorActual().getMano()) {
            if (c instanceof ClaseEvolucion e) {
                hayEvolucionEnMano = true;
                if (!getObjetivosEvolucion(e).isEmpty()) return ok();
            }
        }
        if (!hayEvolucionEnMano) {
            return error("No tienes cartas de evolucion en la mano.");
        }
        return error("Ninguna de tus evoluciones puede usarse ahora (el Pokemon base no esta en juego, entro este turno o ya evoluciono).");
    }

    public Resultado validarEvolucion(ClaseEvolucion carta, ClasePokemon objetivo) {
        Resultado r = validarTurnoActivo();
        if (!r.exito()) return r;
        Jugador j = getJugadorActual();
        if (turnosJugados[indiceActual] == 1) {
            return error("No puedes evolucionar en tu primer turno.");
        }
        if (carta == null || !j.getMano().contains(carta)) {
            return error("Esa carta no esta en tu mano.");
        }
        if (objetivo == null || !j.getPokemonEnJuego().contains(objetivo)) {
            return error("Elige uno de tus Pokemon en juego.");
        }
        if (!carta.getEvolucionaDe().equals(objetivo.getNombre())) {
            return error(carta.getNombre() + " solo evoluciona de " + carta.getEvolucionaDe() + ".");
        }
        if (!objetivo.isListoParaEvolucionar()) {
            return error(objetivo.getNombre() + " no puede evolucionar este turno (entro en juego o ya evoluciono).");
        }
        return ok();
    }

    public Resultado evolucionar(ClaseEvolucion carta, ClasePokemon objetivo) {
        Resultado r = validarEvolucion(carta, objetivo);
        if (!r.exito()) return r;
        Jugador j = getJugadorActual();

        carta.heredarEstado(objetivo);          // conserva energias y dano, PS segun la evolucion
        j.quitarDeMano(carta);
        j.reemplazarPokemon(objetivo, carta);   // ocupa el lugar (activo o banco) del Pokemon base
        vista.mostrarMensaje(objetivo.getNombre() + " evoluciona a " + carta.getNombre()
                + " (" + carta.getPsActual() + "/" + carta.getPsMax() + " PS).");
        return ok();
    }

    // Accion: retirar Pokemon activo

    public Resultado validarRetirar() {
        Resultado r = validarTurnoActivo();
        if (!r.exito()) return r;
        Jugador j = getJugadorActual();
        if (j.getBanco().isEmpty()) {
            return error("No tienes Pokemon en el banco.");
        }
        if (!j.puedeRetirar()) {
            return error("Necesitas " + j.getCostoRetiradaEfectivo() + " energia(s) adjunta(s) para retirar a "
                    + j.getActivo().getNombre() + ".");
        }
        return ok();
    }

    public Resultado retirar(int indiceBanco) {
        Resultado r = validarRetirar();
        if (!r.exito()) return r;
        Jugador j = getJugadorActual();
        if (indiceBanco < 0 || indiceBanco >= j.getBanco().size()) {
            return error("Elige un Pokemon de tu banco.");
        }
        String saliente = j.getActivo().getNombre();
        int costo = j.getCostoRetiradaEfectivo();
        j.retirarActivo(indiceBanco);
        vista.mostrarMensaje(saliente + " se retira (costo " + costo + ") y " + j.getActivo().getNombre()
                + " pasa a ser el activo.");
        return ok();
    }

    // Accion: atacar

    //Para habilitar el boton "Atacar" (antes de elegir cual ataque).
    public Resultado validarAtacar() {
        Resultado r = validarTurnoActivo();
        if (!r.exito()) return r;
        if (turno == 1) {
            return error("El primer jugador no puede atacar en su primer turno.");
        }
        if (getJugadorActual().getActivo() == null) {
            return error("No tienes Pokemon activo.");
        }
        if (getRival().getActivo() == null) {
            return error("El rival no tiene Pokemon activo.");
        }
        return ok();
    }

    //Para habilitar cada ataque en la lista de ataques del Pokemon activo.
    public Resultado validarAtaque(int indiceAtaque) {
        Resultado r = validarAtacar();
        if (!r.exito()) return r;
        ClasePokemon atacante = getJugadorActual().getActivo();
        if (indiceAtaque < 0 || indiceAtaque >= atacante.getAtaques().size()) {
            return error("Elige un ataque valido.");
        }
        ClaseAtaque ataque = atacante.getAtaques().get(indiceAtaque);
        if (!atacante.puedeAtacar(ataque)) {
            return error(atacante.getNombre() + " no tiene la energia necesaria para " + ataque.getNombre() + ".");
        }
        return ok();
    }

    //Ataca con el Pokemon activo. Despues de atacar el turno termina.
    public Resultado atacar(int indiceAtaque) {
        Resultado r = validarAtaque(indiceAtaque);
        if (!r.exito()) return r;

        Jugador j = getJugadorActual();
        Jugador rival = getRival();
        ClasePokemon atacante = j.getActivo();
        ClasePokemon defensor = rival.getActivo();
        ClaseAtaque ataque = atacante.getAtaques().get(indiceAtaque);

        int danio = ataque.getDanioBase();
        boolean debil = defensor.getTipo().esDebilA(atacante.getTipo());
        if (debil) {
            danio += 20;
        }
        defensor.recibirDanio(danio);
        vista.mostrarMensaje(atacante.getNombre() + " usa " + ataque.getNombre() + " y hace " + danio
                + " de danio a " + defensor.getNombre() + (debil ? " (es super efectivo, +20)." : "."));

        if (defensor.estaDerrotado()) {
            resolverDerrota(j, rival);
        }
        if (ganador == null) {
            cerrarTurno();
        }
        return ok();
    }

    private void resolverDerrota(Jugador atacante, Jugador defensor) {
        ClasePokemon derrotado = defensor.getActivo();
        int premio = derrotado.getPuntosPremio();
        atacante.sumarPremio(premio);
        defensor.eliminarActivo(); // sale la carta y todas las de su cadena de evolucion
        vista.mostrarMensaje(derrotado.getNombre() + " fue derrotado. " + atacante.getNombre()
                + " gana " + premio + " punto(s) de premio.");

        if (atacante.getPuntosPremio() >= PREMIOS_PARA_GANAR) {
            terminar(atacante, "acumulo " + PREMIOS_PARA_GANAR + " puntos de premio");
        } else if (defensor.getBanco().isEmpty()) {
            terminar(atacante, "el rival se quedo sin Pokemon en juego");
        } else {
            int i = vista.elegirOpcion(defensor.getNombre() + ": elige tu nuevo Pokemon activo",
                    nombres(defensor.getBanco()), false);
            defensor.promoverDesdeBanco(i);
            vista.mostrarMensaje(defensor.getNombre() + " pone a " + defensor.getActivo().getNombre() + " como activo.");
        }
    }

    // Utilidades internas

    private void terminar(Jugador g, String razon) {
        ganador = g;
        motivo = razon;
    }

    private Resultado validarTurnoActivo() {
        if (ganador != null) return error("La partida ya termino.");
        if (!turnoEnCurso) return error("El turno todavia no ha comenzado.");
        return ok();
    }

    private static Resultado ok() {
        return new Resultado(true, "");
    }

    private static Resultado error(String mensaje) {
        return new Resultado(false, mensaje);
    }

    //Solo los Pokemon basicos pueden salir de la mano al campo (RF8).
    private List<ClasePokemon> pokemonBasicosEnMano(Jugador j) {
        List<ClasePokemon> lista = new ArrayList<>();
        for (ClaseCarta c : j.getMano()) {
            if (c instanceof ClasePokemon p && p.esBasico()) lista.add(p);
        }
        return lista;
    }

    private List<String> nombres(List<? extends ClaseCarta> cartas) {
        List<String> n = new ArrayList<>();
        for (ClaseCarta c : cartas) n.add(c.getNombre());
        return n;
    }
}