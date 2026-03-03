import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.function.BiConsumer;

public class GameListCell extends ListCell<Game> {

    private final boolean showPlayed;
    private final BiConsumer<Game, Boolean> onPlayedToggle;

    private final CheckBox playedCheckBox = new CheckBox("Played");
    private final ImageView imageView = new ImageView();
    private final Label nameLabel = new Label();

    private final HBox content = new HBox(10);

    public GameListCell(boolean showPlayed, BiConsumer<Game, Boolean> onPlayedToggle) {
        this.showPlayed = showPlayed;
        this.onPlayedToggle = onPlayedToggle;

        imageView.setFitWidth(60);
        imageView.setFitHeight(90);
        imageView.setPreserveRatio(true);

        content.getChildren().addAll(imageView, nameLabel);

        if(showPlayed) {
            content.getChildren().add(playedCheckBox);
        }

        playedCheckBox.setOnAction(e ->{
            Game game = getItem();
            if (game != null && onPlayedToggle != null) {
                onPlayedToggle.accept(game, playedCheckBox.isSelected());
            }
        });
    }

    @Override
    protected void updateItem(Game game, boolean empty) {
        super.updateItem(game, empty);

        if (empty || game == null) {
            setGraphic(null);
            return;
        }

        nameLabel.setText(game.name() + "\n" + game.platformNames());

        if (game.coverUrl() != null) {
            imageView.setImage(new Image(game.coverUrl(), true));
        } else {
            imageView.setImage(null);
        }

        if (showPlayed) {
            playedCheckBox.setSelected(game.played());
        }

        setGraphic(content);
    }
}