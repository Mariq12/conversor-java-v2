# PROYECTO CONVERSOR DE MONEDA CON JAVA CON INTERECCIÓN DESDE CONSOLA

## Conversor de Monedas
Esta aplicación Java te permite realizar conversiones de moneda utilizando tasas de cambio en tiempo real de una API externa. Puedes realizar nuevas conversiones, ver el historial de conversiones y salir del programa.

## Funcionalidades
Realizar Conversión: Permite seleccionar una moneda de origen y una moneda de destino, ingresar una cantidad a convertir y mostrar el resultado de la conversión.
Ver Historial de Conversiones: Muestra un listado de las conversiones realizadas previamente con detalles como la fecha, moneda de origen, moneda de destino y monto convertido.
## Tecnologías Utilizadas
**Java:** El programa está escrito en Java y utiliza las bibliotecas Gson para el manejo de JSON y HttpURLConnection para realizar llamadas HTTP a una API externa.

**API de Tasa de Cambio:** Se integra con una API externa para obtener las tasas de cambio en tiempo real.

## Instrucciones de Uso
    1. Clona el repositorio en tu máquina local.
    2. Asegúrarrse de tener Java instalado.
    3. Configura el archivo api.properties con la URL y la clave de la API de tasas de cambio.
    4. Ejecuta la aplicación con Run Java

## Video de configuración del entorno

Java - Configurar Visual Studio Code y Maven

  https://www.youtube.com/watch?v=3mWGDArNYss

---

## Requisitos
    Java 17.
    
    Acceso a una API de tasas de cambio compatible y configurada correctamente en api.properties.

## Descarga de Maven

  https://maven.apache.org/download.cgi

  	apache-maven-3.9.6-bin.zip

---

## Dependencia agregada

Repositorio Maven

    https://mvnrepository.com/artifact/com.google.code.gson/gson/2.10.1
    
---

*Se agrego la depencia gson*

    <dependency>
        <groupId>com.google.code.gson</groupId>
        <artifactId>gson</artifactId>
        <version>2.10.1</version>
    </dependency>

---

### Ejemplo de Configuración (api.properties)

    api.url=https://api.tasasdecambio.com/
    api.key=your_api_key_here

## API utilizada ExchangeRate-API

La API de Tipo de Cambio Precisa y Confiable

    https://www.exchangerate-api.com/

## Mostrar información

Para mostrar el resultado de la conversión se utiliza la consola.


