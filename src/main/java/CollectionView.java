import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CollectionView extends VBox {

    private final TextField searchField = new TextField();
    private final Button searchButton = new Button("Search");
    private final ObservableList<Game> resultsData = FXCollections.observableArrayList();
    private final ListView<Game> resultsList = new ListView<>(resultsData);


    private Consumer<Game> onDelete;
    private BiConsumer<Game, Boolean> onPlayedToggle;
    private Runnable onSearch;

    public CollectionView() {
        searchField.setText("Enter Game Name");


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

        searchButton.setOnAction(e -> {
            if (onSearch != null) {
                onSearch.run();
            }
        });

        getChildren().addAll(new Label("My Collection"), searchField, searchButton, resultsList);
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
}