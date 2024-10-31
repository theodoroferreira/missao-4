package com.acme.missao4;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class PokeController {

    @FXML
    private TextField pokemonInput;

    @FXML
    private Label pokemonNameLabel;

    @FXML
    private Label abilitiesLabel;

    @FXML
    private ImageView pokemonImageView;

    @FXML
    private ComboBox<String> typeComboBox;

    @FXML
    private ListView<PokemonDto> pokemonListView;

    private final PokeService service = new PokeService();

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @FXML
    private void onLoadPokemon() {
        String name = pokemonInput.getText().trim();
        if (!name.isEmpty()) {
            loadPokemon(name);
        }
    }

    public void loadPokemon(String name) {
        try {
            JsonObject data = service.fetchPokemonData(name);
            pokemonNameLabel.setText(data.get("name").getAsString());

            StringBuilder abilities = new StringBuilder();
            data.getAsJsonArray("abilities").forEach(ability -> {
                String abilityName = ability.getAsJsonObject()
                        .get("ability").getAsJsonObject()
                        .get("name").getAsString();
                abilities.append(abilityName).append(", ");
            });
            abilitiesLabel.setText(!abilities.isEmpty()
                    ? abilities.substring(0, abilities.length() - 2)
                    : "None");

            String imageUrl = data.getAsJsonObject("sprites")
                    .getAsJsonObject("other")
                    .getAsJsonObject("official-artwork")
                    .get("front_default")
                    .getAsString();

            pokemonImageView.setImage(new Image(imageUrl));

        } catch (Exception e) {
            pokemonNameLabel.setText("Error loading data");
            abilitiesLabel.setText("-");
            pokemonImageView.setImage(null);
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        loadTypes();

        pokemonListView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<PokemonDto> call(ListView<PokemonDto> listView) {
                return new ListCell<>() {
                    private final ImageView imageView = new ImageView();

                    @Override
                    protected void updateItem(PokemonDto pokemon, boolean empty) {
                        super.updateItem(pokemon, empty);
                        if (empty || pokemon == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(pokemon.name());
                            imageView.setImage(new Image(pokemon.imageUrl()));
                            imageView.setFitWidth(50);
                            imageView.setFitHeight(50);
                            setGraphic(imageView);
                        }
                    }
                };
            }
        });
    }

    private void loadTypes() {
        String url = "https://pokeapi.co/api/v2/type";
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray types = jsonResponse.getAsJsonArray("results");

            ObservableList<String> typeNames = FXCollections.observableArrayList();
            for (var type : types) {
                typeNames.add(type.getAsJsonObject().get("name").getAsString());
            }
            typeComboBox.setItems(typeNames);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String getPokemonImage(String name) throws IOException, InterruptedException {
        JsonObject data = service.fetchPokemonData(name);
        return data.getAsJsonObject("sprites")
                .getAsJsonObject("other")
                .getAsJsonObject("official-artwork")
                .get("front_default")
                .getAsString();
    }

    @FXML
    private void onTypeSelected() {
        String selectedType = typeComboBox.getValue();
        if (selectedType != null) {
            loadPokemonByType(selectedType);
        }
    }

    private void loadPokemonByType(String type) {
        String url = "https://pokeapi.co/api/v2/type/" + type;
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray pokemonArray = jsonResponse.getAsJsonArray("pokemon");

            ObservableList<PokemonDto> pokemonList = FXCollections.observableArrayList();
                JsonObject pokemonObj = pokemonArray.get(0).getAsJsonObject().getAsJsonObject("pokemon");
                String name = pokemonObj.get("name").getAsString();
                String imageUrl = getPokemonImage(name);

                pokemonList.add(new PokemonDto(name, imageUrl));
            pokemonListView.setItems(pokemonList);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}