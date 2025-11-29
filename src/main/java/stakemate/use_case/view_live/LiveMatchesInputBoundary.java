package stakemate.use_case.view_live;

/**
 * Input Boundary for the Live Matches use case.
 * Defines the actions available to the controller.
 */
public interface LiveMatchesInputBoundary {
    /**
     * Starts the continuous tracking of live matches.
     */
    void startTracking();

    /**
     * Stops the continuous tracking of live matches.
     */
    void stopTracking();
}
