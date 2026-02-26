import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
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
        BorderPane root = new BorderPane();
        root.setStyle("-fx-padding: 20;");

        Button collectionButton = new Button("My collection");
        Button searchModeButton = new Button("Search Games");

        HBox navBar = new HBox(10, collectionButton, searchModeButton);
        navBar.setStyle("-fx-padding: 10; -fx-alignment: center;");

        root.setTop(navBar);
        root.setCenter(createCollectionView());

        collectionButton.setOnAction(e ->
                root.setCenter(createCollectionView()));

        searchModeButton.setOnAction(e ->
                root.setCenter(createSearchView()));

        Scene scene = new Scene(root, 400, 500);

        stage.setTitle("Game Tracker");
        stage.setScene(scene);
        stage.show();

    }

    private void showAddConfirmation(Game game) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Add Game");
        alert.setHeaderText("Add to your Collection?");
        alert.setContentText("Do you want to add:\n\n" + game.name());

        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(yesButton, noButton);

        alert.showAndWait().ifPresent(response -> {
            if (response == yesButton) {
                try {
                    databaseManager.saveGame(game);
                    showSuccessMessage(game.name());
                } catch (Exception e) {
                    e.printStackTrace();
                    showErrorMessage();
                }
            }
        });
    }

    private void showSuccessMessage(String gameName) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(gameName + " added to your collection!");
        alert.showAndWait();
    }

    private void showErrorMessage() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Something went wrong");
        alert.setContentText("The game could not be saved.");
        alert.showAndWait();
    }

    private VBox createCollectionView() {
        ListView<Game> collectionList = new ListView<>();
        try {
            List<Game> games = databaseManager.retriveGameList();
            collectionList.getItems().addAll(games);
        } catch (Exception e) {
            e.printStackTrace();
        }

        collectionList.setCellFactory(param -> new ListCell<>() {
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

        return new VBox(10, new Label("My Collection"), collectionList);
    }

    private VBox createSearchView() {
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

        resultsList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Game selectedGame = resultsList.getSelectionModel().getSelectedItem();

                if (selectedGame != null) {
                    showAddConfirmation(selectedGame);
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
        return new VBox(10, searchField, searchButton, limitBox, sortBox, resultsList);
    }
    public static void main(String[] args) {
        launch();
    }
}
