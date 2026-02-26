import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Comparator;
import java.util.List;

public class IGDBService {
    private final String clientID;
    private final String accessToken;
    private final HttpClient httpClient;

    public IGDBService(String clientId, String accessToken) {
        this.clientID = clientId;
        this.accessToken = accessToken;
        this.httpClient = HttpClient.newHttpClient();
    }

    public List<Game> searchGame(String gameName, int limit, String sortMode) throws IOException, InterruptedException {
        String query = """
                search "%s";
                fields id, name, cover.url, game_type, rating, rating_count, first_release_date, platforms.name;
                where (game_type = 0 | game_type = 3 | game_type = 8 | game_type = 9 | game_type = 10 | game_type = 11) & (rating > 0);
                limit %d;
                """.formatted(gameName, limit);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.igdb.com/v4/games"))
                .header("Client-ID", clientID)
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(query))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Status: " + response.statusCode());

        System.out.println("RESPONSE: " + response.body());

        ObjectMapper mapper = new ObjectMapper();

        List<Game> games = mapper.readValue(response.body(),
                new TypeReference<List<Game>>() {
                }
        );

        if (!sortMode.equals("Relevance")) {
            switch (sortMode) {
                case "Newest" ->
                        games.sort(Comparator.comparing(Game::releaseDate, Comparator.nullsLast(Long::compareTo)).reversed());

                case "Oldest" ->
                        games.sort(Comparator.comparing(Game::releaseDate, Comparator.nullsLast(Long::compareTo)));
                case "Alphabetical" -> games.sort(Comparator.comparing(Game::name));
                case "Highest Rated" ->
                        games.sort(Comparator.comparing(Game::rating, Comparator.nullsLast(Double::compareTo)).reversed());
                case "Number of Ratings" ->
                    games.sort(Comparator.comparing(Game::totalRatings, Comparator.nullsLast(Integer::compareTo)).reversed());
            }
        }

        return games;
    }
}
