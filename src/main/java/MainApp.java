import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class MainApp extends Application {
    private IGDBService service;
    private DatabaseManager databaseManager;
    private CredentialsService credentialsService;

    @Override
    public void start(Stage stage) {
        credentialsService = new CredentialsService();

        BorderPane root = new BorderPane();
        root.setStyle("-fx-padding: 20;");

        CredentialsView credentialsView = new CredentialsView();
        CredentialsController credentialsController = new CredentialsController(credentialsService, credentialsView);


        String clientID = System.getenv("IGDB_CLIENT_ID");
        String clientSecret = System.getenv("IGDB_CLIENT_SECRET");
        String accessToken = TwitchAuth.getAccessToken(clientID, clientSecret);

        service = new IGDBService(clientID, accessToken);
        databaseManager = new DatabaseManager();

        connectLogin(credentialsController, root);

        root.setCenter(credentialsView);

        Scene scene = new Scene(root, 400, 500);

        stage.setTitle("Game Tracker");
        stage.setScene(scene);
        stage.show();

    }

    private void connectLogin(CredentialsController credentialsController, BorderPane root) {
        credentialsController.setOnLoginSuccess(() -> {
            Button collectionModeButton = new Button("My Collection");
            Button searchModeButton = new Button("Search Games");
            Button logOutButton = new Button("Log out");

            HBox navBar = new HBox(10, collectionModeButton, searchModeButton, logOutButton);
            navBar.setStyle("-fx-padding: 10; -fx-alignment: center;");
            root.setTop(navBar);

            CollectionView collectionView = new CollectionView();
            SearchView searchView = new SearchView();

            CollectionController collectionController = new CollectionController(databaseManager, collectionView);
            SearchController searchController = new SearchController(service, databaseManager, searchView, collectionController);

            collectionModeButton.setOnAction(e -> root.setCenter(collectionView));
            searchModeButton.setOnAction(e -> root.setCenter(searchView));
            logOutButton.setOnAction(e -> {
                credentialsController.logout();
                CredentialsView newCredentialsView = new CredentialsView();
                CredentialsController newCredentialController = new CredentialsController(credentialsService, newCredentialsView);

                connectLogin(newCredentialController, root);
                root.setCenter(newCredentialsView);
                root.setTop(null);
            });

            root.setCenter(collectionView);
        });
    }



    public static void main(String[] args) {
        launch();
    }
}
