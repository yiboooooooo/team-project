package stakemate.use_case.fetch_games;

/**
 * Response model containing the results of a fetch operation.
 */
public class FetchGamesResponseModel {
    private final int gamesFetched;
    private final int gamesSaved;
    private final String sport;
    private final String message;

    public FetchGamesResponseModel(final int gamesFetched, final int gamesSaved, final String sport, final String message) {
        this.gamesFetched = gamesFetched;
        this.gamesSaved = gamesSaved;
        this.sport = sport;
        this.message = message;
    }

    public int getGamesFetched() {
        return gamesFetched;
    }

    public int getGamesSaved() {
        return gamesSaved;
    }

    public String getSport() {
        return sport;
    }

    public String getMessage() {
        return message;
    }
}

