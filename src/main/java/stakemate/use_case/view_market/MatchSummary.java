package stakemate.use_case.view_market;

public class MatchSummary {
    private final String id;
    private final String label;
    private final String statusLabel;

    public MatchSummary(final String id, final String label, final String statusLabel) {
        this.id = id;
        this.label = label;
        this.statusLabel = statusLabel;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    @Override
    public String toString() {
        return label + " (" + statusLabel + ")";
    }
}
