package com.alura;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ConversorConsole {

    private static final String PROPERTIES_FILE = "api.properties";
    private static final String HISTORIAL_FILE_JSON = "historial_conversiones.json";
    private static final String TIMESTAMP_FORMAT = "dd-MM-yyyy HH:mm:ss";

    private final Map<String, String> monedas;
    private final List<String> historialConversiones;

    public ConversorConsole() {
        this.monedas = DatosMonedas.getMonedas();
        this.historialConversiones = new ArrayList<>();
    }

    @SuppressWarnings("resource")
    public void startCurrencyConverter() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Bienvenido al Conversor de Monedas!");
        Properties properties = loadProperties(PROPERTIES_FILE);

        if (properties == null) {
            System.err.println("Error: No se pudo cargar el archivo de propiedades.");
            return;
        }

        boolean continuar = true;

        while (continuar) {
            mostrarMenu();
            int opcion = scanner.nextInt();
            scanner.nextLine(); // Consumir la nueva línea después del nextInt()

            switch (opcion) {
                case 1:
                    realizarNuevaConversion(scanner, properties);
                    break;
                case 2:
                    mostrarHistorialConversiones();
                    break;
                case 3:
                    continuar = false;
                    break;
                default:
                    System.out.println("Opción inválida. Intente de nuevo.");
                    break;
            }
        }

        scanner.close();
    }

    private void mostrarMenu() {
        System.out.println("\n===== Menú Principal =====");
        System.out.println("1. Realizar Conversión");
        System.out.println("2. Ver Historial de Conversiones");
        System.out.println("3. Salir");
        System.out.print("Seleccione una opción: ");
    }

    private void realizarNuevaConversion(Scanner scanner, Properties properties) {
        String codigoOrigen = seleccionarMoneda(scanner, "origen");
        if (codigoOrigen == null) {
            System.out.println("Saliendo del programa...");
            return;
        }

        String codigoDestino = seleccionarMoneda(scanner, "destino");
        if (codigoDestino == null) {
            System.out.println("Saliendo del programa...");
            return;
        }

        if (existeConversionEnHistorial(codigoOrigen, codigoDestino)) {
            System.out.println("Ya existe una conversión registrada de " + monedas.get(codigoOrigen) +
                    " a " + monedas.get(codigoDestino));
            return;
        }

        System.out.print("Ingrese la cantidad a convertir de " + monedas.get(codigoOrigen) +
                " a " + monedas.get(codigoDestino) + ": ");
        double monto = scanner.nextDouble();

        realizarConversion(properties, codigoOrigen, codigoDestino, monto);

        historialConversiones.add(generarRegistroConversion(codigoOrigen, codigoDestino, monto));
    }

    private String seleccionarMoneda(Scanner scanner, String tipo) {
        System.out.println("\n===== Seleccionar Moneda de " + tipo + " =====");

        int index = 1;
        for (String nombre : monedas.values()) {
            System.out.println(index + ". " + nombre);
            index++;
        }

        System.out.print("Ingrese el número de la moneda o 'salir': ");
        String seleccion = scanner.nextLine();

        if (seleccion.equalsIgnoreCase("salir")) {
            return null;
        }

        try {
            int monedaIndex = Integer.parseInt(seleccion) - 1;
            String[] monedaArray = monedas.keySet().toArray(new String[0]);
            return monedaArray[monedaIndex];
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            System.out.println("Selección inválida. Intente de nuevo.");
            return seleccionarMoneda(scanner, tipo);
        }
    }

    private boolean existeConversionEnHistorial(String codigoOrigen, String codigoDestino) {
        for (String registro : historialConversiones) {
            String[] partes = registro.split("=");
            String origenDestino = partes[0].trim();
            String[] monedas = origenDestino.split(" ");
            String monedaOrigen = monedas[1];
            String monedaDestino = monedas[3];

            if (monedaOrigen.equals(codigoOrigen) && monedaDestino.equals(codigoDestino)) {
                return true;
            }
        }
        return false;
    }

    private void realizarConversion(Properties properties, String codigoOrigen, String codigoDestino, double monto) {
        String apiUrl = properties.getProperty("api.url");
        String apiKey = properties.getProperty("api.key");

        try {
            URL url = new URL(apiUrl + apiKey + "/latest/" + codigoOrigen);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();

                    if (jsonResponse.get("result").getAsString().equals("success")) {
                        JsonObject rates = jsonResponse.getAsJsonObject("conversion_rates");

                        double tasaConversionOrigen = rates.get(codigoOrigen).getAsDouble();
                        double tasaConversionDestino = rates.get(codigoDestino).getAsDouble();

                        double resultado = (monto / tasaConversionOrigen) * tasaConversionDestino;
                        String registro = generarRegistroConversion(codigoOrigen, codigoDestino, monto);
                        historialConversiones.add(registro); // Agregar al historial

                        System.out.println("\n===== Resultado de la Conversión =====");
                        System.out.printf("%.2f %s = %.2f %s%n", monto, monedas.get(codigoOrigen), resultado, monedas.get(codigoDestino));
                    } else {
                        System.out.println("Error: No se pudo obtener las tasas de conversión.");
                    }
                }
            } else {
                System.out.println("Error: No se pudo conectar con la API de tasas de cambio.");
            }
        } catch (IOException e) {
            System.out.println("Error de conexión: " + e.getMessage());
        }
    }

    private Properties loadProperties(String propertiesFile) {
        Properties properties = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertiesFile)) {
            if (inputStream != null) {
                properties.load(inputStream);
                return properties;
            } else {
                System.err.println("Error: Archivo de propiedades '" + propertiesFile + "' no encontrado en el classpath.");
                return null;
            }
        } catch (IOException e) {
            System.err.println("Error al cargar las propiedades: " + e.getMessage());
            return null;
        }
    }

    private String generarRegistroConversion(String codigoOrigen, String codigoDestino, double monto) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern(TIMESTAMP_FORMAT));
        return String.format("[%s] %.2f %s = %.2f %s", timestamp, monto, monedas.get(codigoOrigen), monto, monedas.get(codigoDestino));
    }

    private void mostrarHistorialConversiones() {
        cargarHistorialDesdeJSON();

        System.out.println("\n===== Historial de Conversiones =====");
        for (String registro : historialConversiones) {
            System.out.println(registro);
        }

        guardarHistorialEnJSON(); // Guardar el historial actualizado en JSON
    }

    private void cargarHistorialDesdeJSON() {
        try (Reader reader = new FileReader(HISTORIAL_FILE_JSON)) {
            Gson gson = new Gson();
            List<String> loadedHistorial = gson.fromJson(reader, new TypeToken<List<String>>() {}.getType());

            // Limpiar duplicados antes de asignar al historialConversiones
            for (String registro : loadedHistorial) {
                String[] partes = registro.split("=");
                String origenDestino = partes[0].trim();
                if (!existeConversionEnHistorial(origenDestino, registro)) {
                    historialConversiones.add(registro);
                }
            }
        } catch (IOException e) {
            System.err.println("Error al cargar el historial desde JSON: " + e.getMessage());
        }
    }

    private void guardarHistorialEnJSON() {
        try (Writer writer = new FileWriter(HISTORIAL_FILE_JSON)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            // Convertir historialConversiones en un JSON y guardar
            gson.toJson(historialConversiones, writer);
        } catch (IOException e) {
            System.err.println("Error al guardar el historial en JSON: " + e.getMessage());
        }
    }
}


/*/
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import java.io.*;

public class ConversorConsole {

    private static final String PROPERTIES_FILE = "api.properties";
    private static final Map<String, String> monedas = DatosMonedas.getMonedas();
    private List<String> historialConversiones = new ArrayList<>();
    private static final String TIMESTAMP_FORMAT = "dd-MM-yyyy HH:mm:ss";
    private static final String HISTORIAL_FILE_JSON = "historial_conversiones.json";

    

    @SuppressWarnings("resource")
    public void startCurrencyConverter() {
        Scanner scanner = new Scanner(System.in);
        boolean continuar = true;
    
        System.out.println("Bienvenido al Conversor de Monedas!");
    
        Properties properties = loadProperties(PROPERTIES_FILE);
        if (properties == null) {
            System.err.println("Error: No se pudo cargar el archivo de propiedades.");
            return;
        }
    
        while (continuar) {
            System.out.println("\n===== Menú Principal =====");
            System.out.println("1. Realizar Conversión");
            System.out.println("2. Ver Historial de Conversiones");
            System.out.println("3. Salir");
    
            System.out.print("Seleccione una opción: ");
            int opcion = scanner.nextInt();
    
            switch (opcion) {
                case 1:
                    realizarNuevaConversion(scanner, properties);
                    break;
                case 2:
                    mostrarHistorialConversiones();
                    break;
                case 3:
                    continuar = false;
                    break;
                default:
                    System.out.println("Opción inválida. Intente de nuevo.");
                    break;
            }
        }
    
        scanner.close();
    }
    

    public void mostrarHistorialConversiones() {
        cargarHistorialDesdeJSON();
    
        System.out.println("\n===== Historial de Conversiones =====");
        for (String registro : historialConversiones) {
            System.out.println(registro);
        }
    
        guardarHistorialEnJSON(); // Guardar el historial actualizado en JSON
    }
    


    private String seleccionarMoneda(Scanner scanner, String tipo) {
        System.out.println("\n===== Seleccionar Moneda de " + tipo + " =====");

        int index = 1;
        for (String nombre : monedas.values()) {
            System.out.println(index + ". " + nombre);
            index++;
        }

        System.out.print("Ingrese el número de la moneda o 'salir': ");
        String seleccion = scanner.next();

        if (seleccion.equalsIgnoreCase("salir")) {
            return null;
        }

        int monedaIndex;
        try {
            monedaIndex = Integer.parseInt(seleccion) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Selección inválida. Intente de nuevo.");
            return seleccionarMoneda(scanner, tipo);
        }

        if (monedaIndex >= 0 && monedaIndex < monedas.size()) {
            String[] monedaArray = monedas.keySet().toArray(new String[0]);
            return monedaArray[monedaIndex];
        } else {
            System.out.println("Selección inválida. Intente de nuevo.");
            return seleccionarMoneda(scanner, tipo);
        }
    }

    private void realizarNuevaConversion(Scanner scanner, Properties properties) {
        String codigoOrigen = seleccionarMoneda(scanner, "origen");
        if (codigoOrigen == null) {
            System.out.println("Saliendo del programa...");
            return;
        }
    
        String codigoDestino = seleccionarMoneda(scanner, "destino");
        if (codigoDestino == null) {
            System.out.println("Saliendo del programa...");
            return;
        }
    
        if (existeConversionEnHistorial(codigoOrigen, codigoDestino)) {
            System.out.println("Ya existe una conversión registrada de " + monedas.get(codigoOrigen) +
                               " a " + monedas.get(codigoDestino));
            return;
        }
    
        System.out.print("Ingrese la cantidad a convertir de " + monedas.get(codigoOrigen) +
                         " a " + monedas.get(codigoDestino) + ": ");
        double monto = scanner.nextDouble();
    
        realizarConversion(properties, codigoOrigen, codigoDestino, monto);
    
        historialConversiones.add(generarRegistroConversion(codigoOrigen, codigoDestino, monto));
    }
    
    private boolean existeConversionEnHistorial(String codigoOrigen, String codigoDestino) {
        for (String registro : historialConversiones) {
            String[] partes = registro.split("=");
            String origenDestino = partes[0].trim();
            String[] monedas = origenDestino.split(" ");
            String monedaOrigen = monedas[1];
            String monedaDestino = monedas[3];
    
            if (monedaOrigen.equals(codigoOrigen) && monedaDestino.equals(codigoDestino)) {
                return true;
            }
        }
        return false;
    }

    private void realizarConversion(Properties properties, String codigoOrigen, String codigoDestino, double monto) {        String apiUrl = properties.getProperty("api.url");
        String apiKey = properties.getProperty("api.key");

        try {
            URL url = new URL(apiUrl + apiKey + "/latest/" + codigoOrigen);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();

                if (jsonResponse.get("result").getAsString().equals("success")) {
                    JsonObject rates = jsonResponse.getAsJsonObject("conversion_rates");

                    double tasaConversionOrigen = rates.get(codigoOrigen).getAsDouble();
                    double tasaConversionDestino = rates.get(codigoDestino).getAsDouble();

                    double resultado = (monto / tasaConversionOrigen) * tasaConversionDestino;
                    String registro = generarRegistroConversion(codigoOrigen, codigoDestino, monto);
                    historialConversiones.add(registro); // Agregar al historial

                    System.out.println("\n===== Resultado de la Conversión =====");
                    System.out.printf("%.2f %s = %.2f %s%n", monto, monedas.get(codigoOrigen), resultado, monedas.get(codigoDestino));
                } else {
                    System.out.println("Error: No se pudo obtener las tasas de conversión.");
                }
            } else {
                System.out.println("Error: No se pudo conectar con la API de tasas de cambio.");
            }
        } catch (IOException e) {
            System.out.println("Error de conexión: " + e.getMessage());
        }
    }

    private Properties loadProperties(String propertiesFile) {
        Properties properties = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertiesFile)) {
            if (inputStream != null) {
                properties.load(inputStream);
                return properties;
            } else {
                System.err.println("Error: Archivo de propiedades '" + propertiesFile + "' no encontrado en el classpath.");
                return null;
            }
        } catch (IOException e) {
            System.err.println("Error al cargar las propiedades: " + e.getMessage());
            return null;
        }
    }

    private String generarRegistroConversion(String codigoOrigen, String codigoDestino, double monto) {
        String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern(TIMESTAMP_FORMAT));
        return String.format("[%s] %.2f %s = %.2f %s", timestamp, monto, monedas.get(codigoOrigen), monto, monedas.get(codigoDestino));
    }

    private void cargarHistorialDesdeJSON() {
        try (Reader reader = new FileReader(HISTORIAL_FILE_JSON)) {
            Gson gson = new Gson();
            List<String> loadedHistorial = gson.fromJson(reader, new TypeToken<List<String>>() {}.getType());
    
            // Limpiar duplicados antes de asignar al historialConversiones
            for (String registro : loadedHistorial) {
                String[] partes = registro.split("=");
                String origenDestino = partes[0].trim();
                if (!existeConversionEnHistorial(origenDestino)) {
                    historialConversiones.add(registro);
                }
            }
        } catch (IOException e) {
            System.err.println("Error al cargar el historial desde JSON: " + e.getMessage());
        }
    }
    
    private boolean existeConversionEnHistorial(String origenDestino) {
        for (String registro : historialConversiones) {
            if (registro.startsWith(origenDestino)) {
                return true;
            }
        }
        return false;
    }
    
    private void guardarHistorialEnJSON() {
        try (Writer writer = new FileWriter(HISTORIAL_FILE_JSON)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
            // Convertir historialConversiones en un JSON y guardar
            gson.toJson(historialConversiones, writer);
        } catch (IOException e) {
            System.err.println("Error al guardar el historial en JSON: " + e.getMessage());
        }
    }
}
*/