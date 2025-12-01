package stakemate.data_access.api;

import java.time.LocalDate;
import java.util.List;

import stakemate.entity.Game;
import stakemate.use_case.fetch_games.ApiException;
import stakemate.use_case.fetch_games.OddsApiEvent;
import stakemate.use_case.fetch_games.OddsApiGateway;
import stakemate.use_case.fetch_games.OddsApiSport;

public class OddsApiTest {

    private static final String API_KEY = "3672b9111dce17965b928a94129e166d";

    /**
     * Main method to test Odds API integration.
     *
     * @param args command line arguments (not used)
     */
    public static void main(final String[] args) {
        if (!isApiKeyValid()) {
            printApiKeyError();
        }
        else {
            runTests();
        }
    }

    /**
     * Checks if the API key is valid.
     *
     * @return true if API key is not null and not empty, false otherwise
     */
    private static boolean isApiKeyValid() {
        return API_KEY != null && !API_KEY.isEmpty();
    }

    /**
     * Prints API key error messages.
     */
    private static void printApiKeyError() {
        System.err.println("ERROR: API key not found!");
        System.err.println("Please set ODDS_API_KEY environment variable or modify the code.");
        System.err.println("Example: export ODDS_API_KEY=your_key_here");
    }

    /**
     * Runs all API tests.
     */
    private static void runTests() {
        System.out.println("=== Testing Odds API Integration ===\n");

        try {
            final OddsApiGateway gateway = new OddsApiGatewayImpl(API_KEY);
            System.out.println("Test 1: Fetching available sports...");
            testFetchSports(gateway);
            System.out.println("\nTest 2: Fetching NBA basketball events...");
            testFetchEvents(gateway, "basketball_nba", "us");
            System.out.println("\nTest 3: Testing response adapter...");
            testAdapter(gateway, "basketball_nba", "us");

            System.out.println("\n=== All tests completed successfully! ===");

        }
        catch (final ApiException ex) {
            System.err.println("API Error: " + ex.getMessage());
            ex.printStackTrace();
        }
        catch (final RuntimeException ex) {
            System.err.println("Unexpected error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Tests fetching sports from the API.
     *
     * @param gateway the OddsApiGateway to use
     * @throws ApiException if the API request fails
     */
    private static void testFetchSports(final OddsApiGateway gateway) throws ApiException {
        try {
            final List<OddsApiSport> sports = gateway.fetchSports();

            System.out.println("Successfully fetched " + sports.size() + " sports");

            if (!sports.isEmpty()) {
                System.out.println("  First 5 sports:");
                final int count = Math.min(5, sports.size());
                for (int i = 0; i < count; i++) {
                    final OddsApiSport sport = sports.get(i);
                    System.out.println("    - " + sport.getTitle() + " (" + sport.getKey() + ")");
                }
            }
        }
        catch (final ApiException ex) {
            System.err.println("Failed to fetch sports: " + ex.getMessage());
            throw ex;
        }
    }

    /**
     * Tests fetching events from the API.
     *
     * @param gateway the OddsApiGateway to use
     * @param sport the sport key to fetch events for
     * @param region the region code
     * @throws ApiException if the API request fails
     */
    private static void testFetchEvents(final OddsApiGateway gateway, final String sport, final String region)
            throws ApiException {
        try {
            final List<OddsApiEvent> events = gateway.fetchEvents(sport, region, LocalDate.now());

            System.out.println("Successfully fetched " + events.size() + " events");

            if (!events.isEmpty()) {
                final OddsApiEvent firstEvent = events.get(0);
                System.out.println("  First event:");
                System.out.println("    ID: " + firstEvent.getId());
                System.out.println("    Sport: " + firstEvent.getSportKey());
                System.out.println("    Home Team: " + firstEvent.getHomeTeam());
                System.out.println("    Away Team: " + firstEvent.getAwayTeam());
                System.out.println("    Commence Time: " + firstEvent.getCommenceTime());
            }
        }
        catch (final ApiException ex) {
            System.err.println("Failed to fetch events: " + ex.getMessage());
            throw ex;
        }
    }

    /**
     * Tests the response adapter.
     *
     * @param gateway the OddsApiGateway to use
     * @param sport the sport key to fetch events for
     * @param region the region code
     * @throws ApiException if the API request fails
     */
    private static void testAdapter(final OddsApiGateway gateway, final String sport, final String region)
            throws ApiException {
        try {
            final List<OddsApiEvent> events = gateway.fetchEvents(sport, region, LocalDate.now());

            if (events.isEmpty()) {
                System.out.println("  No events to convert");
            }
            else {
                convertAndDisplayEvents(events);
            }
        }
        catch (final RuntimeException ex) {
            System.err.println("Adapter test failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Converts events to games and displays the first game.
     *
     * @param events the list of events to convert
     */
    private static void convertAndDisplayEvents(final List<OddsApiEvent> events) {
        final OddsApiResponseAdapter adapter = new OddsApiResponseAdapter();
        final List<Game> games = adapter.convertToGames(events);

        System.out.println("Converted " + events.size() + " events to " + games.size() + " games");

        if (!games.isEmpty()) {
            final Game firstGame = games.get(0);
            System.out.println("  First game:");
            System.out.println("    ID: " + firstGame.getId());
            System.out.println("    Team A: " + firstGame.getTeamA());
            System.out.println("    Team B: " + firstGame.getTeamB());
            System.out.println("    Sport: " + firstGame.getSport());
            System.out.println("    Status: " + firstGame.getStatus());
            System.out.println("    Game Time: " + firstGame.getGameTime());
            System.out.println("    External ID: " + firstGame.getExternalId());
        }
    }
}

