package com.alura;

import java.util.Scanner;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConversorConsole {

    private static final String PROPERTIES_FILE = "api.properties";
    private static final Map<String, String> monedas = new HashMap<>();

    static {
        monedas.put("ARS", "Peso argentino");
        monedas.put("BOB", "Boliviano boliviano");
        monedas.put("BRL", "Real brasileño");
        monedas.put("CLP", "Peso chileno");
        monedas.put("COP", "Peso colombiano");
        monedas.put("USD", "Dólar estadounidense");
    }

    public void startCurrencyConverter() {
        Scanner scanner = new Scanner(System.in);
        boolean continuar = true;

        System.out.println("Bienvenido al Conversor de Monedas!");

        // Cargar propiedades desde el archivo api.properties
        Properties properties = loadProperties(PROPERTIES_FILE);
        if (properties == null) {
            System.err.println("Error: No se pudo cargar el archivo de propiedades.");
            return;
        }

        while (continuar) {
            System.out.println("\n===== Selección de Monedas =====");
            String codigoOrigen = seleccionarMoneda(scanner, "origen");
            if (codigoOrigen == null) {
                System.out.println("Saliendo del programa...");
                break;
            }

            String codigoDestino = seleccionarMoneda(scanner, "destino");
            if (codigoDestino == null) {
                System.out.println("Saliendo del programa...");
                break;
            }

            System.out.print("Ingrese la cantidad a convertir de " + monedas.get(codigoOrigen) + " a " + monedas.get(codigoDestino) + ": ");
            double monto = scanner.nextDouble();

            realizarConversion(properties, codigoOrigen, codigoDestino, monto);

            System.out.print("¿Desea realizar otra conversión? (s/n): ");
            String respuesta = scanner.next();
            if (!respuesta.equalsIgnoreCase("s")) {
                continuar = false;
            }
        }

        scanner.close();
    }

    private String seleccionarMoneda(Scanner scanner, String tipo) {
        System.out.println("\n===== Seleccionar Moneda de " + tipo + " =====");
        System.out.println("Elija una moneda o escriba 'salir' para terminar:");

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

    private void realizarConversion(Properties properties, String codigoOrigen, String codigoDestino, double monto) {
        String apiUrl = properties.getProperty("api.url");
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
}

// 
/*
import java.util.Scanner;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ConversorConsole {

    private static final String API_URL = "https://v6.exchangerate-api.com/v6/";
    private static final String API_KEY = "edfc1752376623b9854b8b39"; // Reemplaza con tu API key

    private static final Map<String, String> monedas = new HashMap<>();

    static {
        monedas.put("ARS", "Peso argentino");
        monedas.put("BOB", "Boliviano boliviano");
        monedas.put("BRL", "Real brasileño");
        monedas.put("CLP", "Peso chileno");
        monedas.put("COP", "Peso colombiano");
        monedas.put("USD", "Dólar estadounidense");
    }

    public void startCurrencyConverter() {
        Scanner scanner = new Scanner(System.in);
        boolean continuar = true;

        System.out.println("Bienvenido al Conversor de Monedas!");

        while (continuar) {
            System.out.println("\n===== Selección de Monedas =====");
            String codigoOrigen = seleccionarMoneda(scanner, "origen");
            if (codigoOrigen == null) {
                System.out.println("Saliendo del programa...");
                break;
            }

            String codigoDestino = seleccionarMoneda(scanner, "destino");
            if (codigoDestino == null) {
                System.out.println("Saliendo del programa...");
                break;
            }

            System.out.print("Ingrese la cantidad a convertir de " + monedas.get(codigoOrigen) + " a " + monedas.get(codigoDestino) + ": ");
            double monto = scanner.nextDouble();

            realizarConversion(codigoOrigen, codigoDestino, monto);

            System.out.print("¿Desea realizar otra conversión? (s/n): ");
            String respuesta = scanner.next();
            if (!respuesta.equalsIgnoreCase("s")) {
                continuar = false;
            }
        }

        scanner.close();
    }

    private String seleccionarMoneda(Scanner scanner, String tipo) {
        System.out.println("\n===== Seleccionar Moneda de " + tipo + " =====");
        System.out.println("Elija una moneda o escriba 'salir' para terminar:");

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

    private void realizarConversion(String codigoOrigen, String codigoDestino, double monto) {
        try {
            URL url = new URL(API_URL + API_KEY + "/latest/" + codigoOrigen);
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
}
*/

/*
import java.util.Scanner;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ConversorConsole {

    private static final String API_URL = "https://v6.exchangerate-api.com/v6/";

    private static final Map<String, String> monedas = new HashMap<>();

    static {
        monedas.put("ARS", "Peso argentino");
        monedas.put("BOB", "Boliviano boliviano");
        monedas.put("BRL", "Real brasileño");
        monedas.put("CLP", "Peso chileno");
        monedas.put("COP", "Peso colombiano");
        monedas.put("USD", "Dólar estadounidense");
    }

    public void startCurrencyConverter() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Bienvenido al Conversor de Monedas!");

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

        System.out.print("Ingrese la cantidad a convertir de " + monedas.get(codigoOrigen) + " a " + monedas.get(codigoDestino) + ": ");
        double monto = scanner.nextDouble();

        realizarConversion(codigoOrigen, codigoDestino, monto);
    }

    private String seleccionarMoneda(Scanner scanner, String tipo) {
        System.out.println("\n===== Seleccionar Moneda de " + tipo + " =====");
        System.out.println("Elija una moneda o escriba 'salir' para terminar:");

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

    private void realizarConversion(String codigoOrigen, String codigoDestino, double monto) {
        try {
            URL url = new URL(API_URL + "edfc1752376623b9854b8b39/latest/" + codigoOrigen);
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
                    double tasaConversion = jsonResponse.getAsJsonObject("conversion_rates").get(codigoDestino).getAsDouble();
                    double resultado = monto * tasaConversion;

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

    public static void main(String[] args) {
        new ConversorConsole().startCurrencyConverter();
    }
}
*/