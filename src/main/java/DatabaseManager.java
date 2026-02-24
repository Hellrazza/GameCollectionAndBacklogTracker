import java.sql.*;

public class DatabaseManager {
    private final String url = System.getenv("DB_URL");
    private final String user = System.getenv("DB_USER");
    private final String password = System.getenv("DB_PASSWORD");

    public void saveGame(Game game) throws SQLException {
        String sql = """
                INSERT INTO games (id, name, cover_url)
                VALUES (?, ?, ?)
                ON CONFLICT (id) DO UPDATE
                SET name = EXCLUDED.name,
                cover_url = EXCLUDED.cover_url;
                """;

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, game.id());
            stmt.setString(2, game.name());
            stmt.setString(3, game.coverUrl());

            stmt.executeUpdate();
        }
    }

    public void displayGames() throws SQLException {
        String query = """
                SELECT * FROM games;
                """;

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                System.out.println(rs.getLong("id") + " | " +
                        rs.getString("name") + " | " +
                        rs.getString("cover_url"));
            }
        }
    }
}
