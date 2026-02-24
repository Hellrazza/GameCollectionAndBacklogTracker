public class Program {
    public static void main(String[] args) {
        String clientID = System.getenv("IGDB_CLIENT_ID");
        String clientSecret = System.getenv("IGDB_CLIENT_SECRET");

        System.out.println(TwitchAuth.getAccessToken(clientID,clientSecret));
    }
}
