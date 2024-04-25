package com.alura;

import java.util.HashMap;
import java.util.Map;

public class DatosMonedas {

    private static final Map<String, String> monedas = new HashMap<>();

    static {
        monedas.put("ARS", "Peso argentino");
        monedas.put("BOB", "Boliviano boliviano");
        monedas.put("BRL", "Real brasileño");
        monedas.put("CLP", "Peso chileno");
        monedas.put("COP", "Peso colombiano");
        monedas.put("USD", "Dólar estadounidense");
        monedas.put("EUR", "Euro");
        monedas.put("GBP", "Libra esterlina");
        monedas.put("JPY", "Yen japonés");
        // Agrega más monedas aquí...
    }

    public static Map<String, String> getMonedas() {
        return monedas;
    }
}
