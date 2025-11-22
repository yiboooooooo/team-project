package stakemate.interface_adapter.fetch_games;

import java.time.LocalDate;

import stakemate.use_case.fetch_games.FetchGamesInputBoundary;

/**
 * Controller for the FetchGames use case.
 * Handles user input and delegates to the use case interactor.
 */
public class FetchGamesController {

    private final FetchGamesInputBoundary inputBoundary;

    public FetchGamesController(final FetchGamesInputBoundary inputBoundary) {
        this.inputBoundary = inputBoundary;
    }

    /**
     * Triggers a manual refresh of games.
     */
    public void refreshGames() {
        inputBoundary.refreshGames();
    }

    /**
     * Fetches games for a specific sport and region.
     *
     * @param sport  Sport key (e.g., "basketball_nba")
     * @param region Region code (e.g., "us"), or null for all regions
     */
    public void fetchGames(final String sport, final String region) {
        inputBoundary.fetchAndUpdateGames(sport, region, LocalDate.now());
    }

    /**
     * Fetches games with custom date range.
     *
     * @param sport    Sport key
     * @param region   Region code
     * @param dateFrom Minimum date for events
     */
    public void fetchGames(final String sport, final String region, final LocalDate dateFrom) {
        inputBoundary.fetchAndUpdateGames(sport, region, dateFrom);
    }

    /**
     * Searches for games matching the query.
     *
     * @param query Search query string
     */
    public void searchGames(final String query) {
        inputBoundary.searchGames(query);
    }
}

