package com.acme.missao4;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Objects;

public class PokeController {

    @FXML
    private StackPane mainStackPane;

    @FXML
    private ImageView logoImageView;

    @FXML
    private TextField pokemonInput;

    @FXML
    private Label pokemonNameLabel;

    @FXML
    private Label abilitiesLabel;

    @FXML
    private Label heightLabel;

    @FXML
    private Label weightLabel;

    @FXML
    private ImageView pokemonImageView;

    @FXML
    private ComboBox<String> typeComboBox;

    @FXML
    private ListView<PokemonDto> pokemonListView;

    @FXML
    private Button nextButton;

    @FXML
    private Button previousButton;

    @FXML
    private VBox homeScreen;

    @FXML
    private VBox searchScreen;

    @FXML
    private VBox filterScreen;

    private final PokeService service = new PokeService();

    private final ObservableList<PokemonDto> allPokemon = FXCollections.observableArrayList();

    private int currentPage = 0;

    private final int pageSize = 4;

    @FXML
    private void showHomeScreen() {
        homeScreen.setVisible(true);
        searchScreen.setVisible(false);
        filterScreen.setVisible(false);
    }

    @FXML
    private void showSearchScreen() {
        homeScreen.setVisible(false);
        searchScreen.setVisible(true);
        filterScreen.setVisible(false);
    }

    @FXML
    private void showFilterScreen() {
        homeScreen.setVisible(false);
        searchScreen.setVisible(false);
        filterScreen.setVisible(true);
    }

    @FXML
    private void onLoadPokemon() {
        String name = pokemonInput.getText().trim();
        if (!name.isEmpty()) {
            loadPokemon(name);
        }
    }

    @FXML
    private void onTypeSelected() {
        String selectedType = typeComboBox.getValue();
        if (selectedType != null) {
            currentPage = 0;
            updatePage();
        }
    }

    @FXML
    private void onNextPage() {
        currentPage++;
        updatePage();
    }

    @FXML
    private void onPreviousPage() {
        if (currentPage > 0) {
            currentPage--;
            updatePage();
        }
    }

    @FXML
    public void initialize() {
        mainStackPane.getStyleClass().add("stack-pane");
        loadTypes();

        Image logo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("logo.png")));

        logoImageView.setImage(logo);
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
                            setText(pokemon.name() + "\n" + pokemon.abilities());
                            imageView.setImage(new Image(pokemon.imageUrl()));
                            imageView.setFitWidth(50);
                            imageView.setFitHeight(50);
                            setGraphic(imageView);
                        }
                    }
                };
            }
        });

        previousButton.setDisable(true);
    }

    private void loadTypes() {

        try {
            JsonObject jsonResponse = service.fetchTypeData("");
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

    private String getPokemonImage(JsonObject data) throws IOException, InterruptedException {

        return data.getAsJsonObject("sprites")
                .getAsJsonObject("other")
                .getAsJsonObject("official-artwork")
                .get("front_default")
                .getAsString();
    }

    public void loadPokemon(String name) {

        try {
            JsonObject data = service.fetchPokemonData(name);
            pokemonNameLabel.setText(StringUtils.capitalize(data.get("name").getAsString()));

            StringBuilder abilities = new StringBuilder();
            data.getAsJsonArray("abilities").forEach(ability -> {
                String abilityName = ability.getAsJsonObject()
                        .get("ability").getAsJsonObject()
                        .get("name").getAsString();
                abilities.append(StringUtils.capitalize(abilityName)).append(", ");
            });
            abilitiesLabel.setText(!abilities.isEmpty()
                    ? abilities.substring(0, abilities.length() - 2)
                    : "None");

            heightLabel.setText(data.get("height").getAsString());
            weightLabel.setText(data.get("weight").getAsString());

            String imageUrl = getPokemonImage(data);

            pokemonImageView.setImage(new Image(imageUrl));

        } catch (Exception e) {
            pokemonNameLabel.setText("Error loading data");
            pokemonImageView.setImage(null);
            heightLabel.setText("Error loading data");
            weightLabel.setText("Error loading data");
            e.printStackTrace();
        }
    }

    private void loadPokemonByType(String type, int page) {
        int fromIndex = page * pageSize;
        int toIndex = fromIndex + pageSize;

        try {
            JsonObject jsonResponse = service.fetchTypeData(type);
            JsonArray pokemonArray = jsonResponse.getAsJsonArray("pokemon");

            allPokemon.clear();
            for (int i = fromIndex; i < toIndex && i < pokemonArray.size(); i++) {
                JsonObject pokemonObj = pokemonArray.get(i).getAsJsonObject().getAsJsonObject("pokemon");
                String name = pokemonObj.get("name").getAsString();
                JsonObject jsonObject = service.fetchPokemonData(name);

                StringBuilder abilities = new StringBuilder();
                jsonObject.getAsJsonArray("abilities").forEach(ability -> {
                    String abilityName = ability.getAsJsonObject()
                            .get("ability").getAsJsonObject()
                            .get("name").getAsString();
                    abilities.append(StringUtils.capitalize(abilityName)).append(", ");
                });

                String imageUrl = getPokemonImage(jsonObject);

                String s = !abilities.isEmpty()
                        ? abilities.substring(0, abilities.length() - 2)
                        : "None";

                allPokemon.add(new PokemonDto(StringUtils.capitalize(name), s, imageUrl));
            }

            pokemonListView.setItems(allPokemon);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void updatePage() {
        String selectedType = typeComboBox.getValue();
        if (selectedType != null) {
            loadPokemonByType(selectedType, currentPage);
        }

        previousButton.setDisable(currentPage == 0);
        nextButton.setDisable((currentPage + 1) * pageSize >= 200);
    }
}