package stakemate.data_access.api;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import stakemate.entity.Game;
import stakemate.entity.GameStatus;
import stakemate.use_case.fetch_games.OddsApiEvent;

/**
 * Adapter that converts OddsApiEvent DTOs to Game domain entities.
 * Handles data normalization, ID generation, and status mapping.
 */
public class OddsApiResponseAdapter {

    /**
     * Converts a list of API events to Game entities.
     *
     * @param events List of events from the API
     * @return List of Game entities
     */
    public List<Game> convertToGames(final List<OddsApiEvent> events) {
        final List<Game> games = new ArrayList<>();

        for (final OddsApiEvent event : events) {
            final Game game = convertToGame(event);
            if (game != null) {
                games.add(game);
            }
        }

        return games;
    }

    /**
     * Converts a single API event to a Game entity.
     */
    private Game convertToGame(final OddsApiEvent event) {
        if (event == null || event.getId() == null) {
            return null;
        }

        // Generate deterministic UUID based on external API ID
        final UUID gameId = UUID.nameUUIDFromBytes(event.getId().getBytes());
        // want to create markets based on the event data)
        final UUID marketId = UUID.nameUUIDFromBytes((event.getId() + "_market").getBytes());
        final String teamA = normalizeTeamName(event.getHomeTeam());
        final String teamB = normalizeTeamName(event.getAwayTeam());

        if ("Unknown".equals(teamA) || "Unknown".equals(teamB)) {
            return null;
        }

        // Map API status to GameStatus enum
        final GameStatus status = mapStatus(event);
        final String sport = normalizeSport(event.getSportKey());
        final LocalDateTime gameTime = event.getCommenceTime();
        if (gameTime == null) {
            return null;
        }

        return new Game(
            gameId,
            marketId,
            gameTime,
            teamA,
            teamB,
            sport,
            status,
            event.getId()  // Store external ID for deduplication
        );
    }

    /**
     * Normalizes team names by trimming and handling nulls.
     */
    private String normalizeTeamName(final String teamName) {
        if (teamName == null) {
            return "Unknown";
        }
        return teamName.trim();
    }

    /**
     * Normalizes sport key.
     */
    private String normalizeSport(final String sportKey) {
        if (sportKey == null || sportKey.isEmpty()) {
            return "unknown";
        }
        return sportKey.toLowerCase().trim();
    }

    /**
     * Maps API event status to GameStatus enum.
     * The Odds API events endpoint only returns upcoming events,
     * so we always set them as UPCOMING. The status should be updated
     * by a separate mechanism when games actually start or finish.
     */
    private GameStatus mapStatus(final OddsApiEvent event) {
        // The /events endpoint only returns upcoming events
        // If the API returns an event, it means it hasn't started yet
        final LocalDateTime commenceTime = event.getCommenceTime();
        if (commenceTime == null) {
            return GameStatus.UPCOMING;
        }

        final LocalDateTime now = LocalDateTime.now();

        // Check if game is very close to starting (within 1 hour)
        // This provides a buffer zone where we might consider it as approaching live
        if (commenceTime.isBefore(now.plusMinutes(30)) && commenceTime.isAfter(now.minusMinutes(30))) {
            // Game is starting very soon or just started - mark as LIVE
            return GameStatus.LIVE;
        }
        else if (commenceTime.isBefore(now)) {
            // Game start time has passed but still in API = likely in progress
            return GameStatus.LIVE;
        }
        else {
            // Game hasn't started yet
            return GameStatus.UPCOMING;
        }
    }
}

