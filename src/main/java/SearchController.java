import javafx.concurrent.Task;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SearchController {

    private static final int MIN_SEARCH_LENGTH = 2;

    private final IGDBService service;
    private final SearchView view;
    private final DatabaseManager manager;
    private final CollectionController collectionController;

    private List<Game> originalGames;
    private List<Game> sortedGames;
    private String lastQuery = "";

    public SearchController(IGDBService service, DatabaseManager manager, SearchView view, CollectionController collectionController) {
        this.service = service;
        this.manager = manager;
        this.view = view;
        this.collectionController = collectionController;
        originalGames = null;
        sortedGames = null;

        view.setOnSearch(this::handleSearch);
        view.setOnAdd(this::handleAdd);
        view.setOnSortChanged(this::handleSort);
    }

    private void handleSearch() {
        String searchQuery = view.getSearchText().trim().toLowerCase();

        if (searchQuery.length() < MIN_SEARCH_LENGTH) {
            return;
        }

        lastQuery = searchQuery;

        Task<List<Game>> searchTask = new Task<>() {
            @Override
            protected List<Game> call() throws Exception {
                return service.searchGame(searchQuery, view.getLimit());
            }
        };

        searchTask.setOnSucceeded(e -> {

            if (!searchQuery.equals(lastQuery)) {
                return;
            }

            originalGames = searchTask.getValue();
            handleSort(view.getSort());
        });

        searchTask.setOnFailed(e -> {
            searchTask.getException().printStackTrace();
        });

        new Thread(searchTask).start();
    }

    private void handleAdd(Game game, List<Game.Platform> platforms) {

        boolean confirm = DialogUtil.confirm(
                "Add Game",
                "Add to Collection?",
                "Do you want to add:\n\n" + game.name()
        );

        if (confirm) {
            try {
                manager.saveGame(Session.getActiveUUID(), game, platforms);
                collectionController.loadGames();
                DialogUtil.success(game.name() + " added.");
            } catch (SQLException e) {
                DialogUtil.error("Unable to add " + game.name());
            }
        }
    }


    private void handleSort(String option) {
        if (originalGames == null) return;

        if (option.equals("Relevance")) {
            sortedGames = new ArrayList<>(originalGames);
        } else {
            sortedGames = new ArrayList<>(originalGames);
            Comparator<Game> comparator = getComparator(option);

            if (comparator != null) {
                sortedGames.sort(comparator);
            }
        }

        view.setResults(sortedGames);
    }

    private Comparator<Game> getComparator(String option) {
        return switch (option) {
            case "Newest" -> Comparator.comparing(Game::releaseDate, Comparator.nullsLast(Long::compareTo)).reversed();
            case "Oldest" -> Comparator.comparing(Game::releaseDate, Comparator.nullsLast(Long::compareTo));
            case "Alphabetical" -> Comparator.comparing(Game::name);
            case "Highest Rated" ->
                    Comparator.comparing(Game::rating, Comparator.nullsLast(Double::compareTo)).reversed();
            case "Number of Ratings" ->
                    Comparator.comparing(Game::totalRatings, Comparator.nullsLast(Integer::compareTo)).reversed();

            default -> null;
        };
    }

}
