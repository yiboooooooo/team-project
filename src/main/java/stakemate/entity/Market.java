package stakemate.entity;

public class Market {
    private final String id;
    private final String matchId;
    private final String name;
    private final MarketStatus status;

    public Market(String id, String matchId, String name, MarketStatus status) {
        this.id = id;
        this.matchId = matchId;
        this.name = name;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getMatchId() {
        return matchId;
    }

    public String getName() {
        return name;
    }

    public MarketStatus getStatus() {
        return status;
    }
}
