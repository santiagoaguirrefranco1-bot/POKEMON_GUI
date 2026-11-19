package Logica;

import java.util.List;

public abstract class ClaseEvolucion extends ClasePokemon {
    private final String evolucionaDe;

    protected ClaseEvolucion(String nombre, ClaseTipo tipo, int ps, int costoRetirada,
                             List<ClaseAtaque> ataques, String evolucionaDe) {
        super(nombre, tipo, ps, costoRetirada, ataques);
        this.evolucionaDe = evolucionaDe;
    }

    public String getEvolucionaDe(){ 
        return evolucionaDe; 
    }

    @Override
    public boolean esBasico(){ 
        return false; 
    }
}
