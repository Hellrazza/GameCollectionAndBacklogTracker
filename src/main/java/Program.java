import java.util.List;

public class Program {
    public static void main(String[] args) throws Exception {
        String clientID = System.getenv("IGDB_CLIENT_ID");
        String clientSecret = System.getenv("IGDB_CLIENT_SECRET");

        String accessToken = TwitchAuth.getAccessToken(clientID, clientSecret);

        IGDBService service = new IGDBService(clientID, accessToken);

        List<Game> games = service.searchGame("Dark Souls");

        for (Game game : games) {
            System.out.println("ID: " + game.id());
            System.out.println("Name: " + game.name());
            System.out.println("Cover: " + game.cover());
            System.out.println("----------");
        }
    }
}
