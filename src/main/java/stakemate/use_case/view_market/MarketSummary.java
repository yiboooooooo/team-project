package stakemate.use_case.view_market;

public class MarketSummary {
    private final String id;
    private final String name;
    private final String statusLabel;
    private final boolean buySellEnabled;

    public MarketSummary(String id,
                         String name,
                         String statusLabel,
                         boolean buySellEnabled) {
        this.id = id;
        this.name = name;
        this.statusLabel = statusLabel;
        this.buySellEnabled = buySellEnabled;
    }

    public String getId() { return id; }

    public String getName() { return name; }

    public String getStatusLabel() { return statusLabel; }

    public boolean isBuySellEnabled() { return buySellEnabled; }

    @Override
    public String toString() {
        return name + " (" + statusLabel + ")";
    }
}
