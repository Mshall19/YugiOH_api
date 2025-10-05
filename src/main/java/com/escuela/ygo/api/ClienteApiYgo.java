package com.escuela.ygo.api;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Random;

import com.escuela.ygo.modelo.Carta;

/**
 * Cliente de conexión a la API pública de YGOPRODeck.
 * Permite obtener cartas aleatorias del tipo monstruo
 * para usarlas en los duelos del laboratorio.
 */
public class ClienteApiYgo {

    private final HttpClient cliente;
    private final Random random;

    public ClienteApiYgo() {
        this.cliente = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
        this.random = new Random();
    }

    /**
     * Obtiene una carta aleatoria del tipo Monstruo desde la API.
     *
     * @return Objeto Carta con nombre, ataque, defensa e imagen.
     */
    public Carta obtenerCartaAleatoria() throws IOException, InterruptedException {
        // Endpoint filtrado solo para cartas de tipo monstruo
        String url = "https://db.ygoprodeck.com/api/v7/cardinfo.php?type=Normal%20Monster";

        HttpRequest solicitud = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> respuesta = cliente.send(solicitud, HttpResponse.BodyHandlers.ofString());

        if (respuesta.statusCode() != 200) {
            throw new IOException("Error HTTP: " + respuesta.statusCode());
        }

        JSONObject json = new JSONObject(respuesta.body());
        JSONArray data = json.getJSONArray("data");

        // Selecciona una carta aleatoria del arreglo
        int indice = random.nextInt(data.length());
        JSONObject cartaJson = data.getJSONObject(indice);

        String nombre = cartaJson.optString("name", "Desconocida");
        int atk = cartaJson.optInt("atk", 0);
        int def = cartaJson.optInt("def", 0);
        String imagen = "";

        if (cartaJson.has("card_images")) {
            try {
                imagen = cartaJson
                        .getJSONArray("card_images")
                        .getJSONObject(0)
                        .optString("image_url", "");
            } catch (Exception ignored) {}
        }

        return new Carta(nombre, atk, def, imagen);
    }
}
