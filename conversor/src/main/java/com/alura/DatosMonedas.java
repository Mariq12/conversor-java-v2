package com.alura;

import java.util.HashMap;
import java.util.Map;

public class DatosMonedas {

    private static final Map<String, String> monedas = new HashMap<>();

    static {
        monedas.put("VES", "Bolívar venezolano");
        monedas.put("BOB", "Boliviano boliviano");
        monedas.put("USD", "Dólar estadounidense");
        monedas.put("EUR", "Euro");
        monedas.put("GBP", "Libra esterlina");
        monedas.put("CLP", "Peso chileno");
        monedas.put("COP", "Peso colombiano");
        monedas.put("CUP", "Peso cubano");
        monedas.put("DOP", "Peso dominicano");
        monedas.put("MXN", "Peso mexicano");        
        monedas.put("UYU", "Peso uruguayo");
        monedas.put("ARS", "Peso argentino");
        monedas.put("BRL", "Real brasileño");
        monedas.put("PEN", "Sol peruano");
        monedas.put("JPY", "Yen japonés");
        monedas.put("CNY", "Yuan chino");
    }

    public static Map<String, String> getMonedas() {
        return monedas;
    }
}
