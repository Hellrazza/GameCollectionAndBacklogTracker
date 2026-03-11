import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SearchView extends VBox {
    private final TextField searchField = new TextField();
    private final Button searchButton = new Button("Search");
    private final ComboBox<Integer> limitBox = new ComboBox<>();
    private final ComboBox<String> sortBox = new ComboBox<>();
    private final ListView<Game> resultsList = new ListView<>();

    private final ObservableList<Game> resultsData =
            FXCollections.observableArrayList();

    private final PauseTransition searchDelay = new PauseTransition(Duration.millis(500));

    private BiConsumer<Game, List<Game.Platform>> onAdd;
    private Consumer<String> onSortChanged;
    private Runnable onSearch;

    public SearchView() {
        searchField.setText("Enter Game Name");

        searchField.textProperty().addListener((obs, oldText, newText) -> {
            searchDelay.setOnFinished(e -> {
                if (onSearch != null) {
                    onSearch.run();
                }
            });
            searchDelay.playFromStart();
        });

        limitBox.getItems().addAll(10, 25, 50, 100, 200);
        limitBox.setValue(25);

        sortBox.getItems().addAll("Alphabetical",
                "Newest",
                "Oldest",
                "Highest Rated",
                "Number of Ratings",
                "Relevance"
        );
        sortBox.setValue("Relevance");

        sortBox.setOnAction(e -> {
            if (onSortChanged != null) {
                onSortChanged.accept(sortBox.getValue());
            }
        });

        resultsList.setItems(resultsData);
        resultsList.setCellFactory(param -> new GameListCell(false, null));

        searchButton.setOnAction(e -> {
            if (onSearch != null) {
                onSearch.run();
            }
        });

        resultsList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {

                Game selectedGame = resultsList.getSelectionModel().getSelectedItem();

                if (selectedGame == null || onAdd == null) {
                    return;
                }

                List<Game.Platform> platforms = selectedGame.platforms();

                if (platforms == null || platforms.isEmpty()) {
                    return;
                }

                if (platforms.size() == 1) {
                    onAdd.accept(selectedGame, platforms);
                } else {
                    List<Game.Platform> selectedPlatforms =
                                showPlatformDialog(selectedGame);

                    if (selectedPlatforms != null && !selectedPlatforms.isEmpty()) {
                        onAdd.accept(selectedGame, selectedPlatforms);
                    }
                }
            }
        });


        getChildren().addAll(
                searchField,
                searchButton,
                limitBox,
                sortBox,
                resultsList
        );

        setSpacing(10);
    }

    private List<Game.Platform> showPlatformDialog(Game game) {
        List<Game.Platform> availablePlatforms = game.platforms();

        if (availablePlatforms == null || availablePlatforms.isEmpty()) {
            return List.of();
        }

        Dialog<List<Game.Platform>> dialog = new Dialog<>();
        dialog.setTitle("Which platforms do you own " + game.name() + " on?");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        VBox content = new VBox(8);

        Map<CheckBox, Game.Platform> checkBoxMap = new HashMap<>();

        for (Game.Platform platform : availablePlatforms) {
            CheckBox checkBox = new CheckBox(platform.name());
            checkBoxMap.put(checkBox, platform);
            content.getChildren().add(checkBox);
        }

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(button -> {
            if (button == saveButtonType) {
                return checkBoxMap.entrySet().stream()
                        .filter(entry -> entry.getKey().isSelected())
                        .map(Map.Entry::getValue)
                        .toList();
            }
            return null;
        });

        Optional<List<Game.Platform>> result = dialog.showAndWait();
        return result.orElse(null);
    }

    public String getSearchText() {
        return searchField.getText();
    }

    public int getLimit() {
        return limitBox.getValue();
    }

    public String getSort() {
        return sortBox.getValue();
    }

    public void setResults(List<Game> games) {
        resultsData.setAll(games);
    }

    public void setOnAdd(BiConsumer<Game, List<Game.Platform>> onAdd) {
        this.onAdd = onAdd;
    }

    public void setOnSearch(Runnable onSearch) {
        this.onSearch = onSearch;
    }

    public void setOnSortChanged(Consumer<String> onSortChanged) {this.onSortChanged = onSortChanged;}
}
