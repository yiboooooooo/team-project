package stakemate.use_case.fetch_games;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) representing an event from the Odds API.
 * This is a raw representation before conversion to domain entities.
 */
public class OddsApiEvent {
    private String id;
    private String sportKey;
    private LocalDateTime commenceTime;
    private String homeTeam;
    private String awayTeam;

    public OddsApiEvent() {
        // Default constructor for JSON deserialization
    }

    public OddsApiEvent(String id, String sportKey, LocalDateTime commenceTime,
                       String homeTeam, String awayTeam) {
        this.id = id;
        this.sportKey = sportKey;
        this.commenceTime = commenceTime;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSportKey() {
        return sportKey;
    }

    public void setSportKey(String sportKey) {
        this.sportKey = sportKey;
    }

    public LocalDateTime getCommenceTime() {
        return commenceTime;
    }

    public void setCommenceTime(LocalDateTime commenceTime) {
        this.commenceTime = commenceTime;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(String homeTeam) {
        this.homeTeam = homeTeam;
    }

    public String getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(String awayTeam) {
        this.awayTeam = awayTeam;
    }
}

