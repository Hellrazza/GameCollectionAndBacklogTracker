import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

public class CredentialsService {
    private final String url = System.getenv("DB_URL");
    private final String user = System.getenv("DB_USER");
    private final String dbpassword = System.getenv("DB_PASSWORD");

    public void createUser(String username, String password) throws SQLException {
        String createSQL = """
                INSERT INTO users (username, password_hash)
                VALUES(?, ?)
                ON CONFLICT DO NOTHING;
                """;

        try (Connection conn = DriverManager.getConnection(url, user, dbpassword)) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(createSQL)) {
                stmt.setString(1, username);
                stmt.setString(2, hashPassword(password));

                stmt.executeUpdate();
            }

            conn.commit();
        }
    }

    public int getUUID(String username) throws  SQLException {
        String query = """
                SELECT u.uuid
                FROM users u
                WHERE username = ?;
                """;
        int uuid = -1;

        try (Connection conn = DriverManager.getConnection(url, user, dbpassword);
        PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                uuid = rs.getInt("uuid");
            }
        }
        return uuid;
    }

    public String retrieveHashedPassword(String username) throws SQLException {
        String query = """
                SELECT u.password_hash
                FROM users u
                WHERE username = ?;
                """;

        String hash = "";

        try (Connection conn = DriverManager.getConnection(url, user, dbpassword);
        PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                hash = rs.getString("password_hash");
            }

        }

        return hash;
    }

    public int validLogin(String username, String plainPassword) throws SQLException {
        String hashedPassword = retrieveHashedPassword(username);
        if (hashedPassword == null || hashedPassword.isEmpty()) {
            return -1;
        }

        if (checkPassword(plainPassword, hashedPassword)) {
            return getUUID(username);
        }
        return -2;
    }




    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
