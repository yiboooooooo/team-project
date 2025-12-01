package stakemate.data_access.supabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import stakemate.entity.Game;
import stakemate.entity.GameStatus;
import stakemate.use_case.fetch_games.GameRepository;
import stakemate.use_case.fetch_games.RepositoryException;

/**
 * Supabase implementation of GameRepository.
 * Handles database operations for Game entities using JDBC.
 *
 * <p>
 * TODO: Fix any Checkstyle violations in this file
 */
public class SupabaseGameRepository implements GameRepository {

    // SQL parameter indices for game upsert
    private static final int GAME_ID_PARAM = 1;
    private static final int MARKET_ID_PARAM = 2;
    private static final int GAME_TIME_PARAM = 3;
    private static final int TEAM_A_PARAM = 4;
    private static final int TEAM_B_PARAM = 5;
    private static final int SPORT_PARAM = 6;
    private static final int STATUS_PARAM = 7;

    // SQL parameter indices for search queries
    private static final int SEARCH_TEAM_A_PARAM = 1;
    private static final int SEARCH_TEAM_B_PARAM = 2;
    private static final int SEARCH_SPORT_PARAM = 3;

    // SQL parameter indices for external ID lookup
    private static final int EXTERNAL_ID_PARAM = 1;
    private static final int EXTERNAL_TEAM_A_PARAM = 2;
    private static final int EXTERNAL_TEAM_B_PARAM = 3;
    private static final int EXTERNAL_GAME_TIME_PARAM = 4;

    // SQL parameter indices for market operations
    private static final int MARKET_CHECK_ID_PARAM = 1;
    private static final int MARKET_INSERT_ID_PARAM = 1;
    private static final int MARKET_NAME_PARAM = 2;
    private static final int MARKET_CATEGORY_PARAM = 3;

    // SQL query string literals
    private static final String SELECT_GAME_COLUMNS = "SELECT id, market_id, game_time, team_a, team_b, sport, status ";
    private static final String FROM_GAMES_TABLE = "FROM public.games ";
    private static final String PENDING_STATUS = "pending";

    private final SupabaseClientFactory connectionFactory;

    public SupabaseGameRepository(final SupabaseClientFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void upsertGames(final List<Game> games) throws RepositoryException {
        /*
         * if (games == null || games.isEmpty()) {
         * return;
         * }
         */

        try (Connection conn = connectionFactory.createConnection()) {
            conn.setAutoCommit(false);

            try {
                // Step 1: Update status of old games that are no longer in the API response
                updateOldGameStatuses(conn);

                // Step 2: Upsert new/updated games from API
                final String upsertSql = "INSERT INTO public.games (id, "
                        + "market_id, game_time, team_a, team_b, sport, status) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?::game_status) "
                        + "ON CONFLICT (id) DO UPDATE SET "
                        + "market_id = EXCLUDED.market_id, "
                        + "game_time = EXCLUDED.game_time, "
                        + "team_a = EXCLUDED.team_a, "
                        + "team_b = EXCLUDED.team_b, "
                        + "sport = EXCLUDED.sport, "
                        + "status = EXCLUDED.status";

                // TODO: fix this checkstyle error (nested try)
                try (PreparedStatement stmt = conn.prepareStatement(upsertSql)) {
                    for (final Game game : games) {
                        // Ensure market exists before inserting game
                        ensureMarketExists(conn, game.getMarketId());

                        stmt.setObject(GAME_ID_PARAM, game.getId());
                        stmt.setObject(MARKET_ID_PARAM, game.getMarketId());
                        stmt.setTimestamp(GAME_TIME_PARAM, Timestamp.valueOf(game.getGameTime()));
                        stmt.setString(TEAM_A_PARAM, game.getTeamA());
                        stmt.setString(TEAM_B_PARAM, game.getTeamB());
                        stmt.setString(SPORT_PARAM, game.getSport());
                        stmt.setString(STATUS_PARAM, mapGameStatusToDb(game.getStatus()));
                        stmt.addBatch();
                    }

                    stmt.executeBatch();
                }

                conn.commit();
            } catch (final SQLException ex) {
                conn.rollback();
                throw new RepositoryException("Failed to upsert games: " + ex.getMessage(), ex);
            }
        } catch (final SQLException ex) {
            throw new RepositoryException("Database connection error: " + ex.getMessage(), ex);
        }
    }

    /**
     * Updates the status of old games in the database based on their game time.
     * This is called during upsert to ensure existing games reflect current status.
     * Games older than 1 day are marked as finished.
     *
     * @param conn Active database connection
     * @throws SQLException if database operation fails
     */
    private void updateOldGameStatuses(Connection conn) throws SQLException {
        final String updateSql = "UPDATE public.games "
                + "SET status = 'finished' "
                + "WHERE game_time < (CURRENT_TIMESTAMP - INTERVAL '1 day') "
                + "AND status != 'finished'";

        try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
            final int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Updated " + rowsUpdated + " old game(s) to finished status.");
            }
        }
    }

    @Override
    public Optional<Game> findByExternalId(final String externalId) throws RepositoryException {
        // Note: This assumes external_id is stored somewhere (e.g., in a separate
        // column or table)
        // For now, we'll search by a combination of fields that should be unique
        // In a production system, you'd want an external_id column in the games table

        final String sql = SELECT_GAME_COLUMNS
                + FROM_GAMES_TABLE
                + "WHERE id::text = ? OR (team_a = ? AND team_b = ? AND game_time = ?) "
                + "LIMIT 1";

        try (Connection conn = connectionFactory.createConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Try to parse externalId as UUID first, otherwise use as search term
            UUID gameId = null;
            try {
                gameId = UUID.fromString(externalId);
            } catch (final IllegalArgumentException ex) {
                // Not a UUID, will search by other fields
            }

            if (gameId != null) {
                stmt.setString(EXTERNAL_ID_PARAM, gameId.toString());
                stmt.setString(EXTERNAL_TEAM_A_PARAM, "");
                stmt.setString(EXTERNAL_TEAM_B_PARAM, "");
                stmt.setTimestamp(EXTERNAL_GAME_TIME_PARAM, null);
            } else {
                stmt.setString(EXTERNAL_ID_PARAM, "");
                stmt.setString(EXTERNAL_TEAM_A_PARAM, externalId);
                stmt.setString(EXTERNAL_TEAM_B_PARAM, externalId);
                stmt.setTimestamp(EXTERNAL_GAME_TIME_PARAM, null);
            }

            return executeAndMapFirstResult(stmt, externalId);
        } catch (final SQLException ex) {
            throw new RepositoryException("Failed to find game by external ID: " + ex.getMessage(), ex);
        }
    }

    /**
     * Executes a prepared statement and maps the first result to a Game entity.
     *
     * @param stmt       the prepared statement to execute
     * @param externalId the external ID to associate with the game
     * @return Optional containing the Game if found, empty otherwise
     * @throws SQLException if database access error occurs
     */
    private Optional<Game> executeAndMapFirstResult(PreparedStatement stmt,
            String externalId) throws SQLException {
        final ResultSet rs = stmt.executeQuery();
        final Optional<Game> result;
        if (rs.next()) {
            result = Optional.of(mapResultSetToGame(rs, externalId));
        } else {
            result = Optional.empty();
        }
        rs.close();
        return result;
    }

    @Override
    public List<Game> findFutureGames() throws RepositoryException {
        // First, mark old games as finished (games older than 1 day)
        markOldGamesAsFinished();

        // Then, return ALL games from today onwards (including finished ones)
        // The view should display the actual status from the database
        final String sql = SELECT_GAME_COLUMNS
                + FROM_GAMES_TABLE
                + "WHERE game_time >= CURRENT_DATE "
                + "ORDER BY game_time ASC";

        return executeQuery(sql, null);
    }

    @Override
    public List<Game> searchGames(final String query) throws RepositoryException {
        // Mark old games as finished before searching
        markOldGamesAsFinished();
        final List<Game> res;

        final boolean emptyQuery = query == null || query.trim().isEmpty();
        if (emptyQuery) {
            res = findFutureGames();
        } else {
            final String sql = SELECT_GAME_COLUMNS
                    + FROM_GAMES_TABLE
                    + "WHERE (team_a ILIKE ? OR team_b ILIKE ? OR sport ILIKE ?) "
                    + "AND game_time >= CURRENT_DATE "
                    + "ORDER BY game_time ASC";
            final String searchTerm = "%" + query.trim() + "%";
            res = executeQuery(sql, searchTerm);
        }
        return res;
    }

    /**
     * Marks games older than 1 day as finished.
     * This ensures old games persist in the database but are marked as completed.
     *
     * @throws RepositoryException if database operation fails
     */
    private void markOldGamesAsFinished() throws RepositoryException {
        final String updateSql = "UPDATE public.games "
                + "SET status = 'finished' "
                + "WHERE game_time < (CURRENT_TIMESTAMP - INTERVAL '1 day') "
                + "AND status != 'finished'";

        try (Connection conn = connectionFactory.createConnection();
                PreparedStatement stmt = conn.prepareStatement(updateSql)) {

            final int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Marked " + rowsUpdated + " old game(s) as finished.");
            }
        } catch (final SQLException ex) {
            throw new RepositoryException("Failed to mark old games as finished: " + ex.getMessage(), ex);
        }
    }

    /**
     * Executes a query and maps results to Game entities.
     *
     * @param sql        the SQL query to execute
     * @param searchTerm the search term to use in query parameters, or null if none
     * @return list of Game entities matching the query
     * @throws RepositoryException if database operation fails
     */
    private List<Game> executeQuery(String sql, String searchTerm) throws RepositoryException {
        final List<Game> games = new ArrayList<>();

        try (Connection conn = connectionFactory.createConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (searchTerm != null) {
                stmt.setString(SEARCH_TEAM_A_PARAM, searchTerm);
                stmt.setString(SEARCH_TEAM_B_PARAM, searchTerm);
                stmt.setString(SEARCH_SPORT_PARAM, searchTerm);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    games.add(mapResultSetToGame(rs, null));
                }
            }

            return games;
        } catch (final SQLException ex) {
            throw new RepositoryException("Failed to execute query: " + ex.getMessage(), ex);
        }
    }

    /**
     * Maps a ResultSet row to a Game entity.
     *
     * @param resultSet  the ResultSet positioned at a row to map
     * @param externalId the external ID to associate with the game, or null
     * @return Game entity created from the ResultSet row
     * @throws SQLException if database access error occurs
     */
    private Game mapResultSetToGame(ResultSet resultSet, String externalId) throws SQLException {
        final UUID id = (UUID) resultSet.getObject("id");
        final UUID marketId = (UUID) resultSet.getObject("market_id");
        final Timestamp gameTime = resultSet.getTimestamp("game_time");
        final String teamA = resultSet.getString("team_a");
        final String teamB = resultSet.getString("team_b");
        final String sport = resultSet.getString("sport");
        final String statusStr = resultSet.getString("status");

        final GameStatus status = mapDbStatusToGameStatus(statusStr);
        final LocalDateTime gameTimeLocal;
        if (gameTime != null) {
            gameTimeLocal = gameTime.toLocalDateTime();
        } else {
            gameTimeLocal = null;
        }

        return new Game(id, marketId, gameTimeLocal, teamA, teamB, sport, status, externalId);
    }

    /**
     * Ensures a market exists in the database, creating it if necessary.
     *
     * @param conn     active database connection
     * @param marketId the UUID of the market to check/create
     * @throws SQLException if database operation fails
     */
    private void ensureMarketExists(Connection conn, UUID marketId) throws SQLException {
        final String checkSql = "SELECT id FROM public.markets WHERE id = ?";

        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setObject(MARKET_CHECK_ID_PARAM, marketId);

            final ResultSet rs = checkStmt.executeQuery();
            if (!rs.next()) {
                // Market doesn't exist, create a default one
                insertDefaultMarket(conn, marketId);
            }
            rs.close();
        }
    }

    /**
     * Inserts a default market into the database.
     *
     * @param conn     active database connection
     * @param marketId the UUID of the market to create
     * @throws SQLException if database operation fails
     */
    private void insertDefaultMarket(Connection conn, UUID marketId) throws SQLException {
        final String insertSql = "INSERT INTO public.markets (id, name, category, created_at) "
                + "VALUES (?, ?, ?, NOW())";

        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            insertStmt.setObject(MARKET_INSERT_ID_PARAM, marketId);
            insertStmt.setString(MARKET_NAME_PARAM, "Default Market");
            insertStmt.setString(MARKET_CATEGORY_PARAM, "general");
            insertStmt.executeUpdate();
        }
    }

    /**
     * Maps GameStatus enum to database enum string.
     * Database enum values: pending, in_progress, finished, cancelled
     *
     * @param status the GameStatus enum value to map
     * @return corresponding database enum string
     */
    private String mapGameStatusToDb(GameStatus status) {
        String res;
        if (status == null || status == GameStatus.UPCOMING) {
            res = PENDING_STATUS;
        }
        if (status == GameStatus.LIVE) {
            res = "in_progress";
        }
        if (status == GameStatus.FINISHED) {
            res = "finished";
        } else {
            res = PENDING_STATUS;
        }
        return res;
    }

    /**
     * Maps database enum string to GameStatus enum.
     * Database enum values: pending, in_progress, finished, cancelled
     *
     * @param statusStr the database status string to map
     * @return corresponding GameStatus enum value
     */
    private GameStatus mapDbStatusToGameStatus(String statusStr) {
        GameStatus res;
        if (statusStr == null) {
            res = GameStatus.UPCOMING;
        }
        final String lowerStatus = statusStr.toLowerCase();
        if ("in_progress".equals(lowerStatus)) {
            res = GameStatus.LIVE;
        }
        if ("finished".equals(lowerStatus) || "cancelled".equals(lowerStatus)) {
            res = GameStatus.FINISHED;
        } else {
            res = GameStatus.UPCOMING;
        }
        return res;

    }
}
