import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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

    public List<Game> searchGame(String gameName, int limit) throws IOException, InterruptedException {
        String sortField = switch("sortOptions") {
            case "DATE_DESC" -> "first_release_date desc";
            case "DATE_ASC" -> "first_release_date asc";
            case "ALPHA" -> "name asc";
            default -> "rating_count desc";
        };

        String query = """
                search "%s";
                fields id, name, cover.url, total_rating, rating_count, first_release_date;
                where version_parent = null;
                & (category = 0 | category = 8 | category = 9);
                & first_release_date != null;
                sort %s; 
                limit %d;
                """.formatted(gameName, sortField, limit);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.igdb.com/v4/games"))
                .header("Client-ID", clientID)
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(query))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();

        return mapper.readValue(
                response.body(),
                new TypeReference<List<Game>>() {}
        );
    }
}
