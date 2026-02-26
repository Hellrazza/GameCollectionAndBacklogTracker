import java.util.List;

public class Program {
    public static void main(String[] args) throws Exception {
        String clientID = System.getenv("IGDB_CLIENT_ID");
        String clientSecret = System.getenv("IGDB_CLIENT_SECRET");
        String accessToken = TwitchAuth.getAccessToken(clientID, clientSecret);
        DatabaseManager manager = new DatabaseManager();
        IGDBService service = new IGDBService(clientID, accessToken);


        List<Game> games = manager.retriveGameList();

        for (Game game : games) {
            System.out.println(game);
        }
    }
}
