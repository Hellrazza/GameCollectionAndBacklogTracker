import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class GameListCell extends ListCell<Game> {

    private final ImageView imageView = new ImageView();
    private final Label nameLabel = new Label();
    private final HBox content = new HBox(10, imageView, nameLabel);

    public GameListCell() {
        imageView.setFitWidth(60);
        imageView.setFitHeight(90);
        imageView.setPreserveRatio(true);
    }

    @Override
    protected void updateItem(Game game, boolean empty) {
        super.updateItem(game, empty);

        if (empty || game == null) {
            setGraphic(null);
        } else {
            nameLabel.setText(game.name() + "\n" + game.platformNames());

            if (game.coverUrl() != null) {
                imageView.setImage(new Image(game.coverUrl(), true));
            } else {
                imageView.setImage(null);
            }

            setGraphic(content);
        }
    }
}