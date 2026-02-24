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

    public List<Game> searchGame(String gameName) throws IOException, InterruptedException {
        String query = """
                search "%s";
                fields id, name, cover.url;
                where version_parent = null;
                limit 5;
                """.formatted(gameName);

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
