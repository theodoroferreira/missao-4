package com.acme.missao4;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class PokeService {

    private static final String API_URL = "https://pokeapi.co/api/v2/";

    public JsonObject fetchPokemonData(String pokemonName) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "pokemon/" + pokemonName))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return JsonParser.parseString(response.body()).getAsJsonObject();
    }

    public JsonObject fetchTypeData(String pokemonType) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "type/" + pokemonType))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return JsonParser.parseString(response.body()).getAsJsonObject();
    }
}
