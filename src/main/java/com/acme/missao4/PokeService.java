package com.acme.missao4;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class PokeService {

    private static final String API_URL = "https://pokeapi.co/api/v2/pokemon/";

    public JsonObject fetchPokemonData(String pokemonName) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + pokemonName))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return JsonParser.parseString(response.body()).getAsJsonObject();
    }
}
