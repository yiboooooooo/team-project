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
    private final String externalId;  // API's event ID for deduplication

    public Game(UUID id,
                UUID marketId,
                LocalDateTime gameTime,
                String teamA,
                String teamB,
                String sport,
                GameStatus status,
                String externalId) {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Game game = (Game) o;
        return Objects.equals(id, game.id) &&
            Objects.equals(externalId, game.externalId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, externalId);
    }

    @Override
    public String toString() {
        return "Game{" +
            "id=" + id +
            ", teamA='" + teamA + '\'' +
            ", teamB='" + teamB + '\'' +
            ", sport='" + sport + '\'' +
            ", gameTime=" + gameTime +
            ", status=" + status +
            '}';
    }
}

