package stakemate.use_case.fetch_games;

import java.time.LocalDateTime;

import com.google.gson.annotations.SerializedName;

/**
 * Data Transfer Object (DTO) representing an event from the Odds API.
 * This is a raw representation before conversion to domain entities.
 */
public class OddsApiEvent {
    @SerializedName("id")
    private String id;

    @SerializedName("sport_key")
    private String sportKey;

    @SerializedName("commence_time")
    private LocalDateTime commenceTime;

    @SerializedName("home_team")
    private String homeTeam;

    @SerializedName("away_team")
    private String awayTeam;

    public OddsApiEvent() {
        // Default constructor for JSON deserialization
    }

    public OddsApiEvent(final String id, final String sportKey, final LocalDateTime commenceTime,
                        final String homeTeam, final String awayTeam) {
        this.id = id;
        this.sportKey = sportKey;
        this.commenceTime = commenceTime;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getSportKey() {
        return sportKey;
    }

    public void setSportKey(final String sportKey) {
        this.sportKey = sportKey;
    }

    public LocalDateTime getCommenceTime() {
        return commenceTime;
    }

    public void setCommenceTime(final LocalDateTime commenceTime) {
        this.commenceTime = commenceTime;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(final String homeTeam) {
        this.homeTeam = homeTeam;
    }

    public String getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(final String awayTeam) {
        this.awayTeam = awayTeam;
    }
}

