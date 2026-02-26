import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class DatabaseManager {
    private final String url = System.getenv("DB_URL");
    private final String user = System.getenv("DB_USER");
    private final String password = System.getenv("DB_PASSWORD");

    public void saveGame(Game game) throws SQLException {
        String gameSql = """
                INSERT INTO games (id, name, cover_url, gametype, rating, totalratings, releasedate)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET 
                    name = EXCLUDED.name,
                    cover_url = EXCLUDED.cover_url,
                    gametype = EXCLUDED.gametype,
                    rating = EXCLUDED.rating,
                    totalratings = EXCLUDED.totalratings,
                    releasedate = EXCLUDED.releasedate;
                """;

        String platformSql = """
                INSERT INTO platforms (id, name)
                VALUES (?, ?)
                ON CONFLICT (id) DO NOTHING;
                """;

        String deleteJoinSql = """
                DELETE FROM game_platform WHERE game_id = ?;
                """;

        String insertJoinSql = """
                INSERT INTO game_platform (game_id, platform_id)
                VALUES (?, ?)
                ON CONFLICT DO NOTHING;
                """;

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(gameSql)) {
                stmt.setLong(1, game.id());
                stmt.setString(2, game.name());
                stmt.setString(3, game.coverUrl());
                stmt.setString(4, game.gameType());
                stmt.setDouble(5, game.rating());
                stmt.setInt(6, game.totalRatings());

                LocalDate date = game.releaseLocalDate();

                if (date != null) {
                    stmt.setDate(7, Date.valueOf(date));
                } else {
                    stmt.setNull(7, Types.DATE);
                }

                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = conn.prepareStatement(deleteJoinSql)) {
                stmt.setLong(1, game.id());
                stmt.executeUpdate();
            }

            if (game.platforms() != null) {
                for (Game.Platform platform : game.platforms()) {
                    try (PreparedStatement stmt = conn.prepareStatement(platformSql)) {
                        stmt.setLong(1, platform.id());
                        stmt.setString(2, platform.name());
                        stmt.executeUpdate();
                    }

                    try (PreparedStatement stmt = conn.prepareStatement(insertJoinSql)) {
                        stmt.setLong(1, game.id());
                        stmt.setLong(2, platform.id());
                        stmt.executeUpdate();
                    }
                }
            }

            conn.commit();
        }
    }

    public List<Game> retriveGameList() throws SQLException {
        String query = """
                SELECT g.id,
                    g.name,
                    g.gametype,
                    g.rating,
                    g.totalratings,
                    g.releasedate,
                    g.cover_url,
                    p.id AS platform_id,
                    p.name AS platform_name
                FROM games g
                LEFT JOIN game_platform gp ON g.id = gp.game_id
                LEFT JOIN platforms p ON p.id = gp.platform_id
                ORDER BY g.name;
                """;


        Map<Long, List<Game.Platform>> platformMap = new HashMap<>();
        Map<Long, Game> gameMap = new LinkedHashMap<>();

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                long gameId = rs.getLong("id");

                if (!gameMap.containsKey(gameId)) {
                    LocalDate date = rs.getDate("releasedate") != null
                            ? rs.getDate("releasedate").toLocalDate() : null;

                    long unixDate = date != null ?
                            date.atStartOfDay(ZoneId.systemDefault())
                                    .toEpochSecond() : 0;

                    Game game = new Game(
                            gameId,
                            rs.getString("name"),
                            rs.getString("gametype"),
                            rs.getDouble("rating"),
                            rs.getInt("totalratings"),
                            unixDate,
                            new ArrayList<>(),
                            rs.getString("cover_url") != null ?
                                    new Game.CoverData(rs.getString("cover_url").replace("https:",""))
                                    : null
                    );

                    gameMap.put(gameId, game);
                }

                long platformId = rs.getLong("platform_id");
                if (!rs.wasNull()) {
                    Game.Platform platform = new Game.Platform(
                            platformId,
                            rs.getString("platform_name")
                    );

                    gameMap.get(gameId).platforms().add(platform);
                }
            }

        }

        return new ArrayList<>(gameMap.values());
    }

    public void removeGame(long gameId) throws SQLException {
        String query = """
                DELETE FROM games
                WHERE id = %d;
                """.formatted(gameId);

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.executeUpdate();
            }
            conn.commit();
        }

    }
}
