package stakemate.use_case.view_live;

import java.util.List;

import stakemate.entity.Game;

/**
 * Output Boundary for the Live Matches use case.
 * Defines how data is presented to the view.
 */
public interface LiveMatchesOutputBoundary {
    /**
     * Presents the list of live/upcoming matches.
     *
     * @param matches The list of games to display.
     */
    void presentMatches(List<Game> matches);

    /**
     * Presents an error message.
     *
     * @param error The error message to display.
     */
    void presentError(String error);
}
