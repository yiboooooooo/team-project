package stakemate.use_case.fetch_games;

import stakemate.entity.Game;

import java.util.List;

/**
 * Output boundary for the FetchGames use case.
 * Defines the interface for presenting results to the UI.
 */
public interface FetchGamesOutputBoundary {
    /**
     * Presents successful fetch results.
     *
     * @param responseModel Contains information about the fetch operation
     */
    void presentFetchSuccess(FetchGamesResponseModel responseModel);
    
    /**
     * Presents an error that occurred during fetching.
     *
     * @param errorMessage User-friendly error message
     */
    void presentFetchError(String errorMessage);
    
    /**
     * Indicates that fetching is in progress.
     */
    void presentFetchInProgress();
    
    /**
     * Presents search results.
     *
     * @param games List of matching games
     * @param query The search query that was used
     */
    void presentSearchResults(List<Game> games, String query);
}

