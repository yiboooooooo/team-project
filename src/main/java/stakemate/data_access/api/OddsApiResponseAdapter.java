package stakemate.data_access.api;

import stakemate.entity.Game;
import stakemate.entity.GameStatus;
import stakemate.use_case.fetch_games.OddsApiEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    public List<Game> convertToGames(List<OddsApiEvent> events) {
        List<Game> games = new ArrayList<>();

        for (OddsApiEvent event : events) {
            Game game = convertToGame(event);
            if (game != null) {
                games.add(game);
            }
        }

        return games;
    }

    /**
     * Converts a single API event to a Game entity.
     */
    private Game convertToGame(OddsApiEvent event) {
        if (event == null || event.getId() == null) {
            return null;
        }

        // Generate deterministic UUID based on external API ID
        UUID gameId = UUID.nameUUIDFromBytes(event.getId().getBytes());

        // For now, we'll generate a market ID (in a real scenario, you might
        // want to create markets based on the event data)
        UUID marketId = UUID.nameUUIDFromBytes((event.getId() + "_market").getBytes());

        // Normalize team names (trim whitespace, handle nulls)
        String teamA = normalizeTeamName(event.getHomeTeam());
        String teamB = normalizeTeamName(event.getAwayTeam());

        if ("Unknown".equals(teamA) || "Unknown".equals(teamB)) {
            return null; // Skip events with missing team names
        }

        // Map API status to GameStatus enum
        GameStatus status = mapStatus(event);

        // Normalize sport key
        String sport = normalizeSport(event.getSportKey());

        // Get commence time
        LocalDateTime gameTime = event.getCommenceTime();
        if (gameTime == null) {
            return null; // Skip events without valid time
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
    private String normalizeTeamName(String teamName) {
        if (teamName == null) {
            return "Unknown";
        }
        return teamName.trim();
    }

    /**
     * Normalizes sport key.
     */
    private String normalizeSport(String sportKey) {
        if (sportKey == null || sportKey.isEmpty()) {
            return "unknown";
        }
        return sportKey.toLowerCase().trim();
    }

    /**
     * Maps API event status to GameStatus enum.
     * The Odds API events endpoint typically returns upcoming events,
     * so we default to UPCOMING. In a real implementation, you might
     * check additional fields or make separate API calls for live events.
     */
    private GameStatus mapStatus(OddsApiEvent event) {
        // The events endpoint typically returns upcoming events
        // For now, we'll check the commence time to determine status
        LocalDateTime commenceTime = event.getCommenceTime();
        if (commenceTime == null) {
            return GameStatus.UPCOMING;
        }

        LocalDateTime now = LocalDateTime.now();
        if (commenceTime.isBefore(now.minusHours(3))) {
            // Game started more than 3 hours ago, likely finished
            return GameStatus.FINISHED;
        } else if (commenceTime.isBefore(now)) {
            // Game has started but less than 3 hours ago, likely live
            return GameStatus.LIVE;
        } else {
            // Game hasn't started yet
            return GameStatus.UPCOMING;
        }
    }
}

