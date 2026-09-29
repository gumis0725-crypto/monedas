import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Main {

    private static Convertidor convertidor = new Convertidor();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/monedas", Main::manejarMonedas);
        server.createContext("/convertir", Main::manejarConvertir);
        server.createContext("/", Main::manejarInicio);

        server.start();
        System.out.println("Servidor corriendo en http://localhost:8080");
        System.out.println("Presiona Ctrl + C para detenerlo");
    }

    // GET / -> muestra la página index.html
    private static void manejarInicio(HttpExchange ex) throws IOException {
        String ruta = ex.getRequestURI().getPath();
        if (!ruta.equals("/") && !ruta.equals("/index.html")) {
            responder(ex, 404, "{\"error\": \"Ruta no encontrada\"}");
            return;
        }

        Path archivo = Path.of("index.html");
        if (!Files.exists(archivo)) {
            responder(ex, 500, "{\"error\": \"No se encontro index.html\"}");
            return;
        }

        byte[] bytes = Files.readAllBytes(archivo);
        ex.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        ex.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    // GET /monedas -> lista de las 10 monedas
    private static void manejarMonedas(HttpExchange ex) throws IOException {
        if (!ex.getRequestMethod().equals("GET")) {
            responder(ex, 405, "{\"error\": \"Metodo no permitido\"}");
            return;
        }

        StringBuilder json = new StringBuilder("[");
        Moneda[] lista = convertidor.getMonedas();
        for (int i = 0; i < lista.length; i++) {
            json.append(String.format("{\"codigo\": \"%s\", \"nombre\": \"%s\"}",
                    lista[i].getCodigo(), lista[i].getNombre()));
            if (i < lista.length - 1) json.append(", ");
        }
        json.append("]");

        responder(ex, 200, json.toString());
    }

    // GET /convertir?de=MXN&a=USD&monto=500
    private static void manejarConvertir(HttpExchange ex) throws IOException {
        if (!ex.getRequestMethod().equals("GET")) {
            responder(ex, 405, "{\"error\": \"Metodo no permitido\"}");
            return;
        }

        Map<String, String> params = leerParametros(ex.getRequestURI().getRawQuery());

        Moneda origen = convertidor.buscar(params.get("de"));
        Moneda destino = convertidor.buscar(params.get("a"));

        if (origen == null || destino == null) {
            responder(ex, 400, "{\"error\": \"Moneda no valida. Usa GET /monedas para ver las disponibles\"}");
            return;
        }

        double monto;
        try {
            monto = Double.parseDouble(params.get("monto"));
        } catch (NumberFormatException | NullPointerException e) {
            responder(ex, 400, "{\"error\": \"El monto falta o no es un numero\"}");
            return;
        }

        if (monto < 0) {
            responder(ex, 400, "{\"error\": \"El monto no puede ser negativo\"}");
            return;
        }

        double resultado = convertidor.convertir(monto, origen, destino);

        String json = String.format(Locale.US,
                "{\"de\": \"%s\", \"a\": \"%s\", \"monto\": %.2f, \"resultado\": %.2f}",
                origen.getCodigo(), destino.getCodigo(), monto, resultado);

        responder(ex, 200, json);
    }

    // Convierte "de=MXN&a=USD&monto=500" en un mapa
    private static Map<String, String> leerParametros(String query) {
        Map<String, String> mapa = new HashMap<>();
        if (query == null) return mapa;

        for (String par : query.split("&")) {
            String[] partes = par.split("=", 2);
            if (partes.length == 2) {
                String clave = URLDecoder.decode(partes[0], StandardCharsets.UTF_8);
                String valor = URLDecoder.decode(partes[1], StandardCharsets.UTF_8);
                mapa.put(clave, valor);
            }
        }
        return mapa;
    }

    // Envía la respuesta en formato JSON
    private static void responder(HttpExchange ex, int codigo, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        ex.sendResponseHeaders(codigo, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }
}