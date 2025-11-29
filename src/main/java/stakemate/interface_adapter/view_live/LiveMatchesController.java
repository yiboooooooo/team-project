package stakemate.interface_adapter.view_live;

import stakemate.use_case.view_live.LiveMatchesInputBoundary;

/**
 * Controller for the Live Matches view.
 * Handles user interactions (Start/Stop tracking).
 */
public class LiveMatchesController {
    private final LiveMatchesInputBoundary interactor;

    public LiveMatchesController(final LiveMatchesInputBoundary interactor) {
        this.interactor = interactor;
    }

    /**
     * Handles the "Start Tracking" action.
     */
    public void startTracking() {
        interactor.startTracking();
    }

    /**
     * Handles the "Stop Tracking" action.
     */
    public void stopTracking() {
        interactor.stopTracking();
    }
}
