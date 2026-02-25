import java.sql.*;
import java.time.LocalDate;

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
}
