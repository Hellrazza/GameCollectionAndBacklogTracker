import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.List;

public class MainApp extends Application {
    String clientID = System.getenv("IGDB_CLIENT_ID");
    String clientSecret = System.getenv("IGDB_CLIENT_SECRET");
    String accessToken = TwitchAuth.getAccessToken(clientID, clientSecret);
    IGDBService service = new IGDBService(clientID, accessToken);
    DatabaseManager databaseManager = new DatabaseManager();

    @Override
    public void start(Stage stage) {
        TextField searchField = new TextField();
        searchField.setText("Enter Game Name");

        Button searchButton = new Button("Search");

        ComboBox<Integer> limitBox = new ComboBox<>();
        limitBox.getItems().addAll(5, 10, 15, 20, 25);
        limitBox.setValue(5);

        ListView<Game> resultsList = new ListView<>();

        ComboBox<String> sortBox = new ComboBox<>();
        sortBox.getItems().addAll("Alphabetical",
                "Newest",
                "Oldest",
                "Highest Rated",
                "Number of Ratings",
                "Relevance");
        sortBox.setValue("Relevance");

        resultsList.setCellFactory(param -> new ListCell<>() {
            private final ImageView imageView = new ImageView();
            private final Label nameLabel = new Label();
            private final HBox content = new HBox(10, imageView, nameLabel);

            {
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
                        String URL = game.coverUrl();

                        Image image = new Image(URL, true);
                        imageView.setImage(image);
                    } else {
                        imageView.setImage(null);
                    }

                    setGraphic(content);
                }
            }
        });

        searchButton.setOnAction(e -> {
            try {
                List<Game> games = service.searchGame(searchField.getText(), limitBox.getValue(), sortBox.getValue());
                resultsList.getItems().clear();

                for (Game game : games) {
                    resultsList.getItems().add(game);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        VBox root = new VBox(10, searchField, limitBox, sortBox, searchButton, resultsList);
        root.setStyle("-fx-padding: 20;");

        Scene scene = new Scene(root, 400, 500);

        stage.setTitle("Game Tracker");
        stage.setScene(scene);
        stage.show();

    }

    public static void main(String[] args) {
        launch();
    }
}
