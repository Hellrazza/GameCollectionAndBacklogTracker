import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

public class CollectionController {
    private final DatabaseManager databaseManager;
    private final CollectionView view;
    private List<Game> currentGames;
    private int uuid = 2;

    public CollectionController(DatabaseManager databaseManager, CollectionView view) {
        this.databaseManager = databaseManager;
        this.view = view;

        view.setOnDelete(this::handleDelete);
        view.setOnSearch(this::handleSearch);
        view.setOnPlayedToggle(this::handlePlayedToggle);
        view.setOnSortChanged(this::handleSort);

        loadGames();
    }

    public void loadGames() {
        try {
            currentGames = databaseManager.retriveGameList(uuid);
            handleSort(view.getSortOption());
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
                databaseManager.removeGame(uuid, game);
                loadGames();
                DialogUtil.success(game.name() + " removed.");
            } catch (Exception e) {
                DialogUtil.error("Could not remove " + game.name());
            }
        }
    }

    public void handlePlayedToggle(Game game, boolean played) {
        try {
            databaseManager.updatePlayedGame(uuid, game.id(), played);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleSearch() {
        if (view.getSearchText().equals("Enter Game Name")) {return;}
        if (view.getSearchText().isEmpty()) {
            loadGames();
            return;
        }

        try {
            currentGames = databaseManager.searchGame(uuid, view.getSearchText());
            handleSort(view.getSortOption());
        } catch (Exception e) {
            e.printStackTrace();
            DialogUtil.error("Search failed");
        }
    }


    private void handleSort(String option) {
        if (currentGames == null) return;

        currentGames.sort(getComparator(option));
        view.setResults(currentGames);
    }

    private Comparator<Game> getComparator(String option) {
        return switch(option) {
            case "Alphabetical" ->
                Comparator.comparing(Game::name, String.CASE_INSENSITIVE_ORDER);
            case "Newest" ->
                Comparator.comparing(Game::releaseDate, Comparator.nullsLast(Long::compareTo).reversed());
            case "Oldest" ->
                Comparator.comparing(Game::releaseDate, Comparator.nullsLast(Long::compareTo));
            case "Highest Rated" ->
                Comparator.comparing(Game::rating, Comparator.nullsLast(Double::compareTo)).reversed();
            case "Number of Ratings" ->
                Comparator.comparing(Game::totalRatings, Comparator.nullsLast(Integer::compareTo)).reversed();
            case "Oldest Added" ->
                Comparator.comparing(Game::addedAt, Comparator.nullsLast(Comparator.naturalOrder()));
            case "Recently Added" ->
                Comparator.comparing(Game::addedAt, Comparator.nullsLast(Comparator.reverseOrder()));

            default ->
                Comparator.comparing(Game::name);
        };

    }
}
