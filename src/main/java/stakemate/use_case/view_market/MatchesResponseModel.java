package stakemate.use_case.view_market;

import java.util.List;

public class MatchesResponseModel {
    private final List<MatchSummary> matches;
    private final String emptyStateMessage;

    public MatchesResponseModel(final List<MatchSummary> matches, final String emptyStateMessage) {
        this.matches = matches;
        this.emptyStateMessage = emptyStateMessage;
    }

    public List<MatchSummary> getMatches() {
        return matches;
    }

    public String getEmptyStateMessage() {
        return emptyStateMessage;
    }
}
