package stakemate.interface_adapter.fetch_games;

import stakemate.entity.Game;

import java.util.List;

/**
 * View interface for displaying fetch games results.
 * Follows the same pattern as MarketsView.
 */
public interface FetchGamesView {
    /**
     * Shows a success message with fetch results.
     *
     * @param message Success message
     * @param gamesCount Number of games fetched/saved
     */
    void showFetchSuccess(String message, int gamesCount);
    
    /**
     * Shows an error message.
     *
     * @param errorMessage Error message to display
     */
    void showError(String errorMessage);
    
    /**
     * Shows that fetching is in progress.
     */
    void showFetchInProgress();
    
    /**
     * Shows search results.
     *
     * @param games List of matching games
     * @param query The search query
     */
    void showSearchResults(List<Game> games, String query);
}

