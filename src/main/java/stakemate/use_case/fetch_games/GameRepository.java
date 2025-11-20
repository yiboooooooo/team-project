package stakemate.use_case.fetch_games;

import stakemate.entity.Game;
import java.util.List;
import java.util.Optional;

public interface GameRepository {
    /**
     * Updates existing games (matched by externalId) or inserts new ones
     *
     * @param games List of games to upsert
     * @throws RepositoryException if database operation fails
     */
    void upsertGames(List<Game> games) throws RepositoryException;

    /**
     * Find game by external API ID
     *
     * @param externalId The external API's event ID
     * @return Optional containing the game if found
     * @throws RepositoryException if database operation fails
     */
    Optional<Game> findByExternalId(String externalId) throws RepositoryException;

    /**
     * Find all games with game_time >= today
     *
     * @return List of future/upcoming games
     * @throws RepositoryException if database operation fails
     */
    List<Game> findFutureGames() throws RepositoryException;

    /**
     * Search games by team names or sport
     *
     * @param query Search query string
     * @return List of matching games
     * @throws RepositoryException if database operation fails
     */
    List<Game> searchGames(String query) throws RepositoryException;
}

