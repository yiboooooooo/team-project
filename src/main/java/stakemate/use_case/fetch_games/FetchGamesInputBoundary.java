package stakemate.use_case.fetch_games;

import java.time.LocalDate;

/**
 * Input boundary for the FetchGames use case.
 * Defines the interface for triggering game fetching and search operations.
 */
public interface FetchGamesInputBoundary {
    /**
     * Fetches games from API and updates database.
     *
     * @param sport    Sport key (e.g., "basketball_nba") - required
     * @param region   Region code (e.g., "us"), or null for all regions
     * @param dateFrom Minimum date for events, or null for today
     */
    void fetchAndUpdateGames(String sport, String region, LocalDate dateFrom);

    /**
     * Manual refresh trigger - fetches games for default sport/region.
     */
    void refreshGames();

    /**
     * Searches for games matching the query.
     *
     * @param query Search query string
     */
    void searchGames(String query);
}

