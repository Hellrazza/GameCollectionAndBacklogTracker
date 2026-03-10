import java.sql.SQLException;
import java.util.List;

public class CollectionController {
    private final DatabaseManager databaseManager;
    private final CollectionView view;

    public CollectionController(DatabaseManager databaseManager, CollectionView view) {
        this.databaseManager = databaseManager;
        this.view = view;

        view.setOnDelete(this::handleDelete);
        view.setOnSearch(this::handleSearch);
        view.setOnPlayedToggle(this::handlePlayedToggle);
        loadGames();
    }

    public void loadGames() {
        try {
            view.setGames(databaseManager.retriveGameList());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleDelete(Game game) {
        boolean confirm = DialogUtil.confirm(
                "Remove",
                "Remove from collection?",
                "Do you want to remove:\n\n" + game.name()
        );

        if (confirm) {
            try {
                databaseManager.removeGame(game);
                loadGames();
                DialogUtil.success(game.name() + " removed.");
            } catch (Exception e) {
                DialogUtil.error("Could not remove " + game.name());
            }
        }
    }

    public void handlePlayedToggle(Game game, boolean played) {
        try {
            databaseManager.updatePlayedGame(game.id(), played);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleSearch() {
        try {
            List<Game> games = databaseManager.searchGame(view.getSearchText());

            view.setResults(games);
        } catch (Exception e) {
            e.printStackTrace();
            DialogUtil.error("Search failed");
        }
    }
}
