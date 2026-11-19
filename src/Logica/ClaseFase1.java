package Logica;

import java.util.List;

public class ClaseFase1 extends ClaseEvolucion {
    public ClaseFase1(String nombre, ClaseTipo tipo, int ps, int costoRetirada,
                      List<ClaseAtaque> ataques, String evolucionaDe) {
        super(nombre, tipo, ps, costoRetirada, ataques, evolucionaDe);
    }

    @Override
    public String getEtapa(){ 
        return "Fase 1"; 
    }
}
