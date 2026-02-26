import java.sql.SQLException;
import java.util.List;

public class SearchController {

    private final IGDBService service;
    private final SearchView view;
    private final DatabaseManager manager;
    private final CollectionController collectionController;

    public SearchController(IGDBService service, DatabaseManager manager, SearchView view, CollectionController collectionController) {
        this.service = service;
        this.manager = manager;
        this.view = view;
        this.collectionController = collectionController;

        view.setOnSearch(this::handleSearch);
        view.setOnAdd(this::handleAdd);
    }

    private void handleSearch() {
        try {
            List<Game> games = service.searchGame(
                    view.getSearchText(),
                    view.getLimit(),
                    view.getSort()
            );

            view.setResults(games);
        } catch (Exception e) {
            e.printStackTrace();
            DialogUtil.error("Search failed.");
        }
    }

    private void handleAdd(Game game) {

        boolean confirm = DialogUtil.confirm(
                "Add Game",
                "Add to Collection?",
                "Do you want to add:\n\n" + game.name()
        );

        if (confirm) {
            try {
                manager.saveGame(game);
                collectionController.loadGames();
                DialogUtil.success(game.name() + " added.");
            } catch (SQLException e) {
                DialogUtil.error("Unable to add " + game.name());
            }
        }
    }
}
