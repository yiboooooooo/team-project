package stakemate.entity;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a sports game/event.
 * Maps to the 'games' table in the database.
 */
public class Game {
    private final UUID id;
    private final UUID marketId;
    private final LocalDateTime gameTime;
    private final String teamA;
    private final String teamB;
    private final String sport;
    private final GameStatus status;
    private final String externalId;

    // -@cs[ParameterNumber] Entity constructor requires all fields to ensure immutability.
    public Game(final UUID id,
                final UUID marketId,
                final LocalDateTime gameTime,
                final String teamA,
                final String teamB,
                final String sport,
                final GameStatus status,
                final String externalId) {
        this.id = id;
        this.marketId = marketId;
        this.gameTime = gameTime;
        this.teamA = teamA;
        this.teamB = teamB;
        this.sport = sport;
        this.status = status;
        this.externalId = externalId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getMarketId() {
        return marketId;
    }

    public LocalDateTime getGameTime() {
        return gameTime;
    }

    public String getTeamA() {
        return teamA;
    }

    public String getTeamB() {
        return teamB;
    }

    public String getSport() {
        return sport;
    }

    public GameStatus getStatus() {
        return status;
    }

    public String getExternalId() {
        return externalId;
    }

    @Override
    public boolean equals(final Object o) {
        boolean result = false;
        if (this == o) {
            result = true;
        }
        else if (o != null && getClass() == o.getClass()) {
            final Game game = (Game) o;
            result = Objects.equals(id, game.id)
                && Objects.equals(externalId, game.externalId);
        }
        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, externalId);
    }

    @Override
    public String toString() {
        return "Game{"
            + "id=" + id
            + ", teamA='" + teamA + '\''
            + ", teamB='" + teamB + '\''
            + ", sport='" + sport + '\''
            + ", gameTime=" + gameTime
            + ", status=" + status
            + '}';
    }
}
