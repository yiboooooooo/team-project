package stakemate.entity;

import java.time.LocalDateTime;

public class Match {
    private final String id;
    private final String homeTeam;
    private final String awayTeam;
    private final MatchStatus status;
    private final LocalDateTime commenceTime;

    public Match(String id,
                 String homeTeam,
                 String awayTeam,
                 MatchStatus status,
                 LocalDateTime commenceTime) {
        this.id = id;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.status = status;
        this.commenceTime = commenceTime;
    }

    public String getId() {
        return id;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public String getAwayTeam() {
        return awayTeam;
    }

    public MatchStatus getStatus() {
        return status;
    }

    public LocalDateTime getCommenceTime() {
        return commenceTime;
    }
}
