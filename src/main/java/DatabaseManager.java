import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class DatabaseManager {
    private final String url = System.getenv("DB_URL");
    private final String user = System.getenv("DB_USER");
    private final String password = System.getenv("DB_PASSWORD");

    public void saveGame(int uuid, Game game, List<Game.Platform> selectedPlatforms) throws SQLException {
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

        String userGameSql = """
                INSERT INTO user_games (user_id, game_id, played)
                VALUES (?, ?, ?)
                ON CONFLICT (user_id, game_id) DO UPDATE SET
                    played = EXCLUDED.played;
                """;

        String userGamePlatformSQL = """
                INSERT INTO user_game_platform (user_id, game_id, platform_id)
                VALUES (?,?,?)
                ON CONFLICT DO NOTHING;
                """;

        String platformSql = """
                INSERT INTO platforms (id, name)
                VALUES (?, ?)
                ON CONFLICT (id) DO NOTHING;
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

            try(PreparedStatement stmt = conn.prepareStatement(userGameSql)) {
                stmt.setInt(1, uuid);
                stmt.setLong(2, game.id());
                stmt.setBoolean(3, false);
                stmt.executeUpdate();

            }

            if (selectedPlatforms != null && !selectedPlatforms.isEmpty())
            {
                try (PreparedStatement platformStmt = conn.prepareStatement(platformSql);
                PreparedStatement userplatformStmt = conn.prepareStatement(userGamePlatformSQL)) {
                    for (Game.Platform platform : selectedPlatforms) {
                        platformStmt.setLong(1, platform.id());
                        platformStmt.setString(2, platform.name());
                        platformStmt.executeUpdate();

                        userplatformStmt.setInt(1, uuid);
                        userplatformStmt.setLong(2, game.id());
                        userplatformStmt.setLong(3, platform.id());
                        userplatformStmt.executeUpdate();
                    }
                }
            }

            conn.commit();
        }
    }

    public List<Game> retriveGameList(int uuid) throws SQLException {
        String query = """
                SELECT g.id,
                    g.name,
                    g.gametype,
                    g.rating,
                    g.totalratings,
                    g.releasedate,
                    g.cover_url,
                    ug.played,
                    ug.added_at,
                    p.id AS platform_id,
                    p.name AS platform_name
                FROM user_games ug
                JOIN games g ON g.id = ug.game_id
                LEFT JOIN user_game_platform ugp 
                    ON ug.user_id = ugp.user_id AND ug.game_id = ugp.game_id
                LEFT JOIN platforms p ON p.id = ugp.platform_id
                WHERE ug.user_id = ?
                ORDER BY g.name;
                """;


        Map<Long, List<Game.Platform>> platformMap = new HashMap<>();
        Map<Long, Game> gameMap = new LinkedHashMap<>();

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, uuid);

            try (ResultSet rs = stmt.executeQuery()) {

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
                                        new Game.CoverData(rs.getString("cover_url").replace("https:", ""))
                                        : null,
                                rs.getBoolean("played"),
                                rs.getTimestamp("added_at").toInstant()
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
        }

        return new ArrayList<>(gameMap.values());
    }

    public void removeGame(int uuid, Game game) throws SQLException {
        String query = """
                DELETE FROM user_games
                WHERE user_id = ? AND game_id = ?;
                """;

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, uuid);
                stmt.setLong(2, game.id());
                stmt.executeUpdate();
            }
            conn.commit();
        }

    }

    public void updatePlayedGame(int uuid, Long gameId, boolean played) throws SQLException {
        String sql = """
               UPDATE user_games
               SET played = ?
               WHERE user_id = ? AND game_id = ?;
               """;

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, played);
            stmt.setInt(2, uuid);
            stmt.setLong(3, gameId);

            stmt.executeUpdate();
        }

    }

    public List<Game> searchGame(int uuid, String name) throws SQLException {

        String query = """
                SELECT g.id,
                    g.name,
                    g.gametype,
                    g.rating,
                    g.totalratings,
                    g.releasedate,
                    g.cover_url,
                    ug.played,
                    ug.added_at,
                    p.id AS platform_id,
                    p.name AS platform_name
                FROM user_games ug
                JOIN games g on g.id = ug.game_id
                LEFT JOIN user_game_platform ugp ON ug.user_id = ugp.user_id AND ug.game_id = ugp.game_id
                LEFT JOIN platforms p ON p.id = ugp.platform_id
                WHERE ug.user_id = ?
                AND LOWER(g.name) LIKE LOWER(?);
                """;

        Map<Long, Game> gameMap = new LinkedHashMap<>();

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, uuid);
            stmt.setString(2, "%" + name + "%");

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    long gameId = rs.getLong("id");

                    if (!gameMap.containsKey(gameId)) {

                        LocalDate date = rs.getDate("releasedate") != null
                                ? rs.getDate("releasedate").toLocalDate() : null;

                        long unixDate = date != null ?
                                date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
                                : 0;

                        Game game = new Game(
                                gameId,
                                rs.getString("name"),
                                rs.getString("gametype"),
                                rs.getDouble("rating"),
                                rs.getInt("totalratings"),
                                unixDate,
                                new ArrayList<>(),
                                rs.getString("cover_url") != null
                                        ? new Game.CoverData(rs.getString("cover_url").replace("https:",""))
                                        : null,
                                rs.getBoolean("played"),
                                rs.getTimestamp("added_at").toInstant()
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
        }
        return new ArrayList<>(gameMap.values());
    }
    
    public List<Game.Platform> getPlatformsInCollection(int uuid) throws SQLException {
        String sql = """
                SELECT DISTINCT p.id, p.name
                FROM platforms p
                JOIN user_game_platform ugp ON p.id = ugp.platform_id
                WHERE ugp.user_id = ?
                ORDER BY p.name;
                """;

        List<Game.Platform> platforms = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(url, user, password);
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, uuid);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    platforms.add(new Game.Platform(
                            rs.getLong("id"),
                            rs.getString("name")
                    ));
                }
            }
        }
        return platforms;
    }
}
