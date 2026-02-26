import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class MainApp extends Application {
    private IGDBService service;
    private DatabaseManager databaseManager;

    @Override
    public void start(Stage stage) {

        String clientID = System.getenv("IGDB_CLIENT_ID");
        String clientSecret = System.getenv("IGDB_CLIENT_SECRET");
        String accessToken = TwitchAuth.getAccessToken(clientID, clientSecret);

        service = new IGDBService(clientID, accessToken);
        databaseManager = new DatabaseManager();

        BorderPane root = new BorderPane();
        root.setStyle("-fx-padding: 20;");

        Button collectionModeButton = new Button("My collection");
        Button searchModeButton = new Button("Search Games");

        HBox navBar = new HBox(10, collectionModeButton, searchModeButton);
        navBar.setStyle("-fx-padding: 10; -fx-alignment: center;");
        root.setTop(navBar);

        CollectionView collectionView = new CollectionView();
        SearchView searchView = new SearchView();

        CollectionController collectionController = new CollectionController(databaseManager, collectionView);
        SearchController searchController = new SearchController(service, databaseManager, searchView, collectionController);

        collectionModeButton.setOnAction(e -> root.setCenter(collectionView));
        searchModeButton.setOnAction(e -> root.setCenter(searchView));

        root.setCenter(collectionView);

        Scene scene = new Scene(root, 400, 500);

        stage.setTitle("Game Tracker");
        stage.setScene(scene);
        stage.show();

    }

    public static void main(String[] args) {
        launch();
    }
}
