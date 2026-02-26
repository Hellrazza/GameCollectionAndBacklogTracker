import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;

public class SearchView extends VBox {
    private final TextField searchField = new TextField();
    private final Button searchButton = new Button("Search");
    private final ComboBox<Integer> limitBox = new ComboBox<>();
    private final ComboBox<String> sortBox = new ComboBox<>();
    private final ListView<Game> resultsList = new ListView<>();

    private final ObservableList<Game> resultsData =
            FXCollections.observableArrayList();

    private Consumer<Game> onAdd;
    private Runnable onSearch;

    public SearchView() {
        searchField.setText("Enter Game Name");

        limitBox.getItems().addAll(10, 25, 50, 100, 200);
        limitBox.setValue(10);

        sortBox.getItems().addAll("Alphabetical",
                "Newest",
                "Oldest",
                "Highest Rated",
                "Number of Ratings",
                "Relevance"
        );
        sortBox.setValue("Relevance");

        resultsList.setItems(resultsData);
        resultsList.setCellFactory(param -> new GameListCell());

        searchButton.setOnAction(e -> {
            if (onSearch != null) {
                onSearch.run();
            }
        });

        resultsList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Game selectedGame = resultsList.getSelectionModel().getSelectedItem();
                if (selectedGame != null && onAdd != null) {
                    onAdd.accept(selectedGame);
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

    public void setOnAdd(Consumer<Game> onAdd) {
        this.onAdd = onAdd;
    }

    public void setOnSearch(Runnable onSearch) {
        this.onSearch = onSearch;
    }
}
