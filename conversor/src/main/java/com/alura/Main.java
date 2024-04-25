package com.alura;

public class Main {
    public static void main(String[] args) {
        ConversorConsole conversor = new ConversorConsole();
        conversor.startCurrencyConverter();

        conversor.mostrarHistorialConversiones(); // Mostrar historial al finalizar
    }
}
