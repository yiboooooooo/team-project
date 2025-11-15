package stakemate.data_access.supabase;

import stakemate.entity.Game;
import stakemate.entity.GameStatus;
import stakemate.use_case.fetch_games.GameRepository;
import stakemate.use_case.fetch_games.RepositoryException;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Supabase implementation of GameRepository.
 * Handles database operations for Game entities using JDBC.
 */
public class SupabaseGameRepository implements GameRepository {
    
    private final SupabaseClientFactory connectionFactory;
    
    public SupabaseGameRepository(SupabaseClientFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }
    
    @Override
    public void upsertGames(List<Game> games) throws RepositoryException {
        if (games == null || games.isEmpty()) {
            return;
        }
        
        String upsertSql = "INSERT INTO public.games (id, market_id, game_time, team_a, team_b, sport, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?::game_status) " +
                "ON CONFLICT (id) DO UPDATE SET " +
                "market_id = EXCLUDED.market_id, " +
                "game_time = EXCLUDED.game_time, " +
                "team_a = EXCLUDED.team_a, " +
                "team_b = EXCLUDED.team_b, " +
                "sport = EXCLUDED.sport, " +
                "status = EXCLUDED.status";
        
        try (Connection conn = connectionFactory.createConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmt = conn.prepareStatement(upsertSql)) {
                for (Game game : games) {
                    // Ensure market exists before inserting game
                    ensureMarketExists(conn, game.getMarketId());
                    
                    stmt.setObject(1, game.getId());
                    stmt.setObject(2, game.getMarketId());
                    stmt.setTimestamp(3, Timestamp.valueOf(game.getGameTime()));
                    stmt.setString(4, game.getTeamA());
                    stmt.setString(5, game.getTeamB());
                    stmt.setString(6, game.getSport());
                    stmt.setString(7, mapGameStatusToDb(game.getStatus()));
                    stmt.addBatch();
                }
                
                stmt.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RepositoryException("Failed to upsert games: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RepositoryException("Database connection error: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Optional<Game> findByExternalId(String externalId) throws RepositoryException {
        // Note: This assumes external_id is stored somewhere (e.g., in a separate column or table)
        // For now, we'll search by a combination of fields that should be unique
        // In a production system, you'd want an external_id column in the games table
        
        String sql = "SELECT id, market_id, game_time, team_a, team_b, sport, status " +
                "FROM public.games " +
                "WHERE id::text = ? OR (team_a = ? AND team_b = ? AND game_time = ?) " +
                "LIMIT 1";
        
        try (Connection conn = connectionFactory.createConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Try to parse externalId as UUID first, otherwise use as search term
            UUID gameId = null;
            try {
                gameId = UUID.fromString(externalId);
            } catch (IllegalArgumentException e) {
                // Not a UUID, will search by other fields
            }
            
            if (gameId != null) {
                stmt.setObject(1, gameId);
                stmt.setString(2, "");
                stmt.setString(3, "");
                stmt.setTimestamp(4, null);
            } else {
                stmt.setString(1, "");
                stmt.setString(2, externalId);
                stmt.setString(3, externalId);
                stmt.setTimestamp(4, null);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToGame(rs, externalId));
                }
            }
            
            return Optional.empty();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to find game by external ID: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<Game> findFutureGames() throws RepositoryException {
        String sql = "SELECT id, market_id, game_time, team_a, team_b, sport, status " +
                "FROM public.games " +
                "WHERE game_time >= CURRENT_DATE " +
                "ORDER BY game_time ASC";
        
        return executeQuery(sql, null);
    }
    
    @Override
    public List<Game> searchGames(String query) throws RepositoryException {
        if (query == null || query.trim().isEmpty()) {
            return findFutureGames();
        }
        
        String searchTerm = "%" + query.trim() + "%";
        String sql = "SELECT id, market_id, game_time, team_a, team_b, sport, status " +
                "FROM public.games " +
                "WHERE (team_a ILIKE ? OR team_b ILIKE ? OR sport ILIKE ?) " +
                "AND game_time >= CURRENT_DATE " +
                "ORDER BY game_time ASC";
        
        return executeQuery(sql, searchTerm);
    }
    
    /**
     * Executes a query and maps results to Game entities.
     */
    private List<Game> executeQuery(String sql, String searchTerm) throws RepositoryException {
        List<Game> games = new ArrayList<>();
        
        try (Connection conn = connectionFactory.createConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            if (searchTerm != null) {
                stmt.setString(1, searchTerm);
                stmt.setString(2, searchTerm);
                stmt.setString(3, searchTerm);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    games.add(mapResultSetToGame(rs, null));
                }
            }
            
            return games;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to execute query: " + e.getMessage(), e);
        }
    }
    
    /**
     * Maps a ResultSet row to a Game entity.
     */
    private Game mapResultSetToGame(ResultSet rs, String externalId) throws SQLException {
        UUID id = (UUID) rs.getObject("id");
        UUID marketId = (UUID) rs.getObject("market_id");
        Timestamp gameTime = rs.getTimestamp("game_time");
        String teamA = rs.getString("team_a");
        String teamB = rs.getString("team_b");
        String sport = rs.getString("sport");
        String statusStr = rs.getString("status");
        
        GameStatus status = mapDbStatusToGameStatus(statusStr);
        LocalDateTime gameTimeLocal = gameTime != null ? gameTime.toLocalDateTime() : null;
        
        return new Game(id, marketId, gameTimeLocal, teamA, teamB, sport, status, externalId);
    }
    
    /**
     * Ensures a market exists in the database, creating it if necessary.
     */
    private void ensureMarketExists(Connection conn, UUID marketId) throws SQLException {
        String checkSql = "SELECT id FROM public.markets WHERE id = ?";
        
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setObject(1, marketId);
            
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (!rs.next()) {
                    // Market doesn't exist, create a default one
                    String insertSql = "INSERT INTO public.markets (id, name, category, created_at) " +
                            "VALUES (?, ?, ?, NOW())";
                    
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setObject(1, marketId);
                        insertStmt.setString(2, "Default Market");
                        insertStmt.setString(3, "general");
                        insertStmt.executeUpdate();
                    }
                }
            }
        }
    }
    
    /**
     * Maps GameStatus enum to database enum string.
     */
    private String mapGameStatusToDb(GameStatus status) {
        if (status == null) {
            return "upcoming";
        }
        
        switch (status) {
            case UPCOMING:
                return "upcoming";
            case LIVE:
                return "live";
            case FINISHED:
                return "finished";
            default:
                return "upcoming";
        }
    }
    
    /**
     * Maps database enum string to GameStatus enum.
     */
    private GameStatus mapDbStatusToGameStatus(String statusStr) {
        if (statusStr == null) {
            return GameStatus.UPCOMING;
        }
        
        switch (statusStr.toLowerCase()) {
            case "upcoming":
                return GameStatus.UPCOMING;
            case "live":
                return GameStatus.LIVE;
            case "finished":
                return GameStatus.FINISHED;
            default:
                return GameStatus.UPCOMING;
        }
    }
}

