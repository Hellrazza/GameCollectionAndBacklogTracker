import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.BiConsumer;

public class GameListCell extends ListCell<Game> {

    private final boolean showPlayed;
    private final BiConsumer<Game, Boolean> onPlayedToggle;

    private final CheckBox playedCheckBox = new CheckBox("Played");
    private final ImageView imageView = new ImageView();
    private final Label nameLabel = new Label();

    private final VBox textBox = new VBox(nameLabel);
    private final HBox root = new HBox(15);

    public GameListCell(boolean showPlayed, BiConsumer<Game, Boolean> onPlayedToggle) {
        this.showPlayed = showPlayed;
        this.onPlayedToggle = onPlayedToggle;

        imageView.setFitWidth(60);
        imageView.setFitHeight(90);
        imageView.setPreserveRatio(true);

        textBox.setAlignment(Pos.CENTER_LEFT);

        HBox.setHgrow(textBox, Priority.ALWAYS);

        root.setAlignment(Pos.CENTER_LEFT);

        root.getChildren().addAll(imageView, textBox);

        if(showPlayed) {
            root.getChildren().add(playedCheckBox);
            playedCheckBox.setAlignment(Pos.CENTER_RIGHT);
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

        if (game.coverUrl() != null) {
            imageView.setImage(new Image(game.coverUrl(), true));
        } else {
            imageView.setImage(null);
        }

        String dateMode = game.formattedReleaseDate();

        if (showPlayed) {
            playedCheckBox.setSelected(game.played());
            dateMode = game.formattedAddedDate();
        }

        nameLabel.setText(game.name() + "\n" + game.platformNames() + "\n" + dateMode);


        setGraphic(root);
    }
}