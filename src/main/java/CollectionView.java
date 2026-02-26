import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;

public class CollectionView extends VBox {

    private final ObservableList<Game> data = FXCollections.observableArrayList();
    private final ListView<Game> listView = new ListView<>(data);

    private Consumer<Game> onDelete;

    public CollectionView() {
        listView.setCellFactory(param -> new GameListCell());

        listView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Game selected = listView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    onDelete.accept(selected);
                }
            }
        });

        getChildren().addAll(new Label("My Collection"), listView);
        setSpacing(10);
    }

    public void setGames(List<Game> games) {
        data.setAll(games);
    }

    public void setOnDelete(Consumer<Game> onDelete) {
        this.onDelete = onDelete;
    }

}