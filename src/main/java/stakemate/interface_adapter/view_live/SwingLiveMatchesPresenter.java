package stakemate.interface_adapter.view_live;

import java.util.List;

import javax.swing.SwingUtilities;

import stakemate.entity.Game;
import stakemate.use_case.view_live.LiveMatchesOutputBoundary;
import stakemate.view.LiveMatchesFrame;

/**
 * Presenter for the Live Matches view.
 * Updates the Swing UI with the latest match data.
 */
public class SwingLiveMatchesPresenter implements LiveMatchesOutputBoundary {

    private final LiveMatchesFrame view;

    public SwingLiveMatchesPresenter(final LiveMatchesFrame view) {
        this.view = view;
    }

    @Override
    public void presentMatches(final List<Game> matches) {
        SwingUtilities.invokeLater(() -> view.updateMatches(matches));
    }

    @Override
    public void presentError(final String error) {
        SwingUtilities.invokeLater(() -> view.showError(error));
    }
}
