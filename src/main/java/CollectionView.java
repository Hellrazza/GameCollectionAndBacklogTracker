import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.animation.PauseTransition;
import javafx.util.Duration;


import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CollectionView extends VBox {

    private final TextField searchField = new TextField();
    private final ObservableList<Game> resultsData = FXCollections.observableArrayList();
    private final ListView<Game> resultsList = new ListView<>(resultsData);
    private final ComboBox<String> sortBox = new ComboBox<>();

    private final PauseTransition searchDelay = new PauseTransition(Duration.millis(100));

    private Consumer<Game> onDelete;
    private BiConsumer<Game, Boolean> onPlayedToggle;
    private Runnable onSearch;
    private Consumer<String> onSortChanged;

    public CollectionView() {
        searchField.setText("Enter Game Name");

        searchField.textProperty().addListener((obs, oldText, newText) -> {
            searchDelay.setOnFinished(e -> {
                if (onSearch != null) {
                    onSearch.run();
                }
            });
            searchDelay.playFromStart();
        });

        sortBox.getItems().addAll(
                "Alphabetical",
                "Newest",
                "Oldest",
                "Highest Rated",
                "Oldest Added",
                "Recently Added"
        );

        sortBox.setValue("Recently Added");

        sortBox.setOnAction(e -> {
            if (onSortChanged != null) {
                onSortChanged.accept(sortBox.getValue());
            }
        });


        resultsList.setItems(resultsData);
        resultsList.setCellFactory(param -> new GameListCell(true, onPlayedToggle));

        resultsList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Game selected = resultsList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    onDelete.accept(selected);
                }
            }
        });

        getChildren().addAll(new Label("My Collection"),
                            searchField,
                            sortBox,
                            resultsList);
        setSpacing(10);
    }

    public void setGames(List<Game> games) {
        resultsData.setAll(games);
    }

    public void setOnDelete(Consumer<Game> onDelete) {
        this.onDelete = onDelete;
    }

    public void setOnPlayedToggle(BiConsumer<Game, Boolean> onPlayedToggle) {
        this.onPlayedToggle = onPlayedToggle;

        resultsList.setCellFactory(param -> new GameListCell(true, this.onPlayedToggle));
    }

    public void setOnSearch(Runnable onSearch) { this.onSearch = onSearch;}

    public String getSearchText() {
        return searchField.getText();
    }

    public void setResults(List<Game> games) {resultsData.setAll(games);}

    public void setOnSortChanged(Consumer<String> onSortChanged) {this.onSortChanged = onSortChanged;}

    public String getSortOption() {return sortBox.getValue();}

}