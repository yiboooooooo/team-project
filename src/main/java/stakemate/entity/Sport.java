package stakemate.entity;

public enum Sport {
    BASKETBALL_NBA("basketball_nba"),
    FOOTBALL_NFL("americanfootball_nfl"),
    SOCCER_EPL("soccer_epl"),
    SOCCER_MLS("soccer_usa_mls"),
    HOCKEY_NHL("icehockey_nhl"),
    BASEBALL_MLB("baseball_mlb");

    private final String oddsApiKey;

    Sport(final String oddsApiKey) {
        this.oddsApiKey = oddsApiKey;
    }

    /**
     * Find Sport enum by its API key
     */
    public static Sport fromApiKey(final String apiKey) {
        for (final Sport sport : values()) {
            if (sport.oddsApiKey.equals(apiKey)) {
                return sport;
            }
        }
        return null;
    }

    public String getOddsApiKey() {
        return oddsApiKey;
    }
}

