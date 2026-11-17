package Logica;

import java.util.ArrayList;
import java.util.List;

public final class FabricaCartas {
    private FabricaCartas() {}

    private static final ClaseTipo F = ClaseTipo.FUEGO;
    private static final ClaseTipo A = ClaseTipo.AGUA;
    private static final ClaseTipo E = ClaseTipo.ELECTRICO;
    private static final ClaseTipo P = ClaseTipo.PSIQUICO;
    private static final ClaseTipo I = ClaseTipo.INCOLORO;

    public static ClaseCarta crear(String nombre) {
        return switch (nombre) {
            case "Pikachu" -> new ClasePokemon("Pikachu", E, 60, 1,
                    List.of(new ClaseAtaque("Mordisco", 1, E, 20)));
            case "Charmander" -> new ClasePokemon("Charmander", F, 60, 1,
                    List.of(new ClaseAtaque("Ascuas", 1, F, 30)));
            case "Squirtle" -> new ClasePokemon("Squirtle", A, 60, 1,
                    List.of(new ClaseAtaque("Placaje", 1, A, 20)));
            case "Jigglypuff" -> new ClasePokemon("Jigglypuff", I, 70, 1,
                    List.of(new ClaseAtaque("Golpe", 1, I, 20)));
            case "Magikarp" -> new ClasePokemon("Magikarp", A, 30, 1,
                    List.of(new ClaseAtaque("Chapoteo", 1, A, 10)));
            case "Snorlax" -> new ClasePokemon("Snorlax", I, 150, 3,
                    List.of(new ClaseAtaque("Golpe Cuerpo", 3, I, 100)));
            case "Mewtwo ex" -> new ClasePokemonEx("Mewtwo ex", P, 150, 2,
                    List.of(new ClaseAtaque("Ataque Psiquico", 2, P, 50),
                            new ClaseAtaque("Psicorrayo", 3, P, 150)));
            case "Zapdos ex" -> new ClasePokemonEx("Zapdos ex", E, 130, 1,
                    List.of(new ClaseAtaque("Picotazo", 1, I, 30),
                            new ClaseAtaque("Tormenta Electrica", 3, E, 100)));
            case "Moltres ex" -> new ClasePokemonEx("Moltres ex", F, 130, 2,
                    List.of(new ClaseAtaque("Aleteo", 1, I, 30),
                            new ClaseAtaque("Danza Infernal", 3, F, 70)));
            case "Articuno ex" -> new ClasePokemonEx("Articuno ex", A, 130, 2,
                    List.of(new ClaseAtaque("Viento Helado", 1, A, 30),
                            new ClaseAtaque("Ventisca", 3, A, 100)));
            // Cartas de evolucion (MP2)
            case "Raichu" -> new ClaseFase1("Raichu", E, 100, 1,
                    List.of(new ClaseAtaque("Trueno", 2, E, 60)), "Pikachu");
            case "Charmeleon" -> new ClaseFase1("Charmeleon", F, 90, 2,
                    List.of(new ClaseAtaque("Garra Ignea", 2, F, 50)), "Charmander");
            case "Charizard" -> new ClaseFase2("Charizard", F, 150, 2,
                    List.of(new ClaseAtaque("Giro Fuego", 4, F, 120)), "Charmeleon");
            case "Wartortle" -> new ClaseFase1("Wartortle", A, 80, 1,
                    List.of(new ClaseAtaque("Salpicadura", 2, A, 40)), "Squirtle");
            case "Blastoise" -> new ClaseFase2("Blastoise", A, 150, 3,
                    List.of(new ClaseAtaque("Hidrobomba", 3, A, 100)), "Wartortle");
            case "Wigglytuff" -> new ClaseFase1("Wigglytuff", I, 110, 2,
                    List.of(new ClaseAtaque("Bofeton", 2, I, 50)), "Jigglypuff");
            case "Gyarados" -> new ClaseFase1("Gyarados", A, 150, 4,
                    List.of(new ClaseAtaque("Hiperrayo", 4, A, 100)), "Magikarp");
            case "Investigacion del Profesor" -> new InvestigacionProfesor();
            case "Pocion" -> new Pocion();
            case "Poke Ball" -> new Pokeball();
            case "Turbo Velocidad" -> new TurboVelocidad();
            case "Misty" -> new Misty();
            default -> throw new IllegalArgumentException("Carta desconocida: " + nombre);
        };
    }

    private static void agregar(List<ClaseCarta> lista, String nombre, int copias) {
        for (int i = 0; i < copias; i++) {
            lista.add(crear(nombre));
        }
    }

    public static String[] nombresMazos() {
        return new String[] { "Mazo Fuego y Rayo", "Mazo Agua", "Mazo Psiquico" };
    }

    public static Mazo crearMazo(int opcion) {
        List<ClaseCarta> c = new ArrayList<>();
        switch (opcion) {
            case 1 -> { // Fuego y Rayo: 8 basicos, 20 cartas
                agregar(c, "Charmander", 2); agregar(c, "Charmeleon", 2); agregar(c, "Charizard", 1);
                agregar(c, "Pikachu", 2);    agregar(c, "Raichu", 1);
                agregar(c, "Moltres ex", 2); agregar(c, "Zapdos ex", 2);
                agregar(c, "Pocion", 2);     agregar(c, "Poke Ball", 2);
                agregar(c, "Turbo Velocidad", 2); agregar(c, "Investigacion del Profesor", 2);
            }
            case 2 -> { // Agua: 8 basicos, 20 cartas
                agregar(c, "Squirtle", 2);   agregar(c, "Wartortle", 2); agregar(c, "Blastoise", 1);
                agregar(c, "Magikarp", 2);   agregar(c, "Gyarados", 1);
                agregar(c, "Articuno ex", 2); agregar(c, "Jigglypuff", 2);
                agregar(c, "Misty", 2);      agregar(c, "Pocion", 2);
                agregar(c, "Poke Ball", 2);  agregar(c, "Turbo Velocidad", 2);
            }
            default -> { // Psiquico y Rayo: 8 basicos, 20 cartas
                agregar(c, "Mewtwo ex", 2);  agregar(c, "Zapdos ex", 2);
                agregar(c, "Pikachu", 2);    agregar(c, "Raichu", 2);
                agregar(c, "Jigglypuff", 2); agregar(c, "Wigglytuff", 2);
                agregar(c, "Snorlax", 2);
                agregar(c, "Pocion", 2);     agregar(c, "Poke Ball", 2);
                agregar(c, "Investigacion del Profesor", 2);
            }
        }
        return new Mazo(c);
    }
}