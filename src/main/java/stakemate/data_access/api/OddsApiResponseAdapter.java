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

    private static final int MINUTES_BUFFER = 30;

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
     *
     * @param event The API event to convert
     * @return A Game entity, or null if conversion fails
     */
    private Game convertToGame(final OddsApiEvent event) {
        Game result = null;

        if (event != null && event.getId() != null) {
            // Generate deterministic UUID based on external API ID
            final UUID gameId = UUID.nameUUIDFromBytes(event.getId().getBytes());
            final UUID marketId = UUID.nameUUIDFromBytes((event.getId() + "_market").getBytes());
            final String teamA = normalizeTeamName(event.getHomeTeam());
            final String teamB = normalizeTeamName(event.getAwayTeam());

            if (!"Unknown".equals(teamA) && !"Unknown".equals(teamB)) {
                final LocalDateTime gameTime = event.getCommenceTime();
                if (gameTime != null) {
                    // Map API status to GameStatus enum
                    final GameStatus status = mapStatus(event);
                    final String sport = normalizeSport(event.getSportKey());

                    result = new Game(
                        gameId,
                        marketId,
                        gameTime,
                        teamA,
                        teamB,
                        sport,
                        status,
                        event.getId()
                    );
                }
            }
        }

        return result;
    }

    /**
     * Normalizes team names by trimming and handling nulls.
     *
     * @param teamName The team name to normalize
     * @return Normalized team name, or "Unknown" if null
     */
    private String normalizeTeamName(final String teamName) {
        String res;
        if (teamName == null) {
            res = "Unknown";
        }
        res = teamName.trim();
        return res;
    }

    /**
     * Normalizes sport key.
     *
     * @param sportKey The sport key to normalize
     * @return Normalized sport key in lowercase
     */
    private String normalizeSport(final String sportKey) {
        String res;
        if (sportKey == null || sportKey.isEmpty()) {
            res = "unknown";
        }
        res = sportKey.toLowerCase().trim();
        return res;
    }

    /**
     * Maps API event status to GameStatus enum.
     * The Odds API events endpoint only returns upcoming events,
     * so we always set them as UPCOMING. The status should be updated
     * by a separate mechanism when games actually start or finish.
     *
     * @param event The API event to map status for
     * @return The appropriate GameStatus based on the event's commence time
     */
    private GameStatus mapStatus(final OddsApiEvent event) {
        final LocalDateTime commenceTime = event.getCommenceTime();
        final GameStatus result;

        if (commenceTime == null) {
            result = GameStatus.UPCOMING;
        }
        else {
            final LocalDateTime now = LocalDateTime.now();

            if (commenceTime.isBefore(now.plusMinutes(MINUTES_BUFFER))
                    && commenceTime.isAfter(now.minusMinutes(MINUTES_BUFFER))) {
                result = GameStatus.LIVE;
            }
            else if (commenceTime.isBefore(now)) {
                result = GameStatus.LIVE;
            }
            else {
                result = GameStatus.UPCOMING;
            }
        }

        return result;
    }
}

