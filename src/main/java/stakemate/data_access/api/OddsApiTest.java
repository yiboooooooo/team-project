package stakemate.data_access.api;

import stakemate.entity.Game;
import stakemate.use_case.fetch_games.ApiException;
import stakemate.use_case.fetch_games.OddsApiEvent;
import stakemate.use_case.fetch_games.OddsApiGateway;
import stakemate.use_case.fetch_games.OddsApiSport;

import java.time.LocalDate;
import java.util.List;

public class OddsApiTest {
    
    private static final String API_KEY = "3672b9111dce17965b928a94129e166d";
    
    public static void main(String[] args) {
        if (API_KEY == null || API_KEY.isEmpty()) {
            System.err.println("ERROR: API key not found!");
            System.err.println("Please set ODDS_API_KEY environment variable or modify the code.");
            System.err.println("Example: export ODDS_API_KEY=your_key_here");
            return;
        }
        
        System.out.println("=== Testing Odds API Integration ===\n");
        
        try {
            // Create gateway
            OddsApiGateway gateway = new OddsApiGatewayImpl(API_KEY);
            
            // Test 1: Fetch available sports
            System.out.println("Test 1: Fetching available sports...");
            testFetchSports(gateway);
            
            // Test 2: Fetch NBA events
            System.out.println("\nTest 2: Fetching NBA basketball events...");
            testFetchEvents(gateway, "basketball_nba", "us");
            
            // Test 3: Test the adapter
            System.out.println("\nTest 3: Testing response adapter...");
            testAdapter(gateway, "basketball_nba", "us");
            
            System.out.println("\n=== All tests completed successfully! ===");
            
        } catch (ApiException e) {
            System.err.println("API Error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testFetchSports(OddsApiGateway gateway) throws ApiException {
        try {
            List<OddsApiSport> sports = gateway.fetchSports();
            
            System.out.println("✓ Successfully fetched " + sports.size() + " sports");
            
            if (!sports.isEmpty()) {
                System.out.println("  First 5 sports:");
                int count = Math.min(5, sports.size());
                for (int i = 0; i < count; i++) {
                    OddsApiSport sport = sports.get(i);
                    System.out.println("    - " + sport.getTitle() + " (" + sport.getKey() + ")");
                }
            }
        } catch (ApiException e) {
            System.err.println("✗ Failed to fetch sports: " + e.getMessage());
            throw e;
        }
    }
    
    private static void testFetchEvents(OddsApiGateway gateway, String sport, String region) 
            throws ApiException {
        try {
            List<OddsApiEvent> events = gateway.fetchEvents(sport, region, LocalDate.now());
            
            System.out.println("✓ Successfully fetched " + events.size() + " events");
            
            if (!events.isEmpty()) {
                OddsApiEvent firstEvent = events.get(0);
                System.out.println("  First event:");
                System.out.println("    ID: " + firstEvent.getId());
                System.out.println("    Sport: " + firstEvent.getSportKey());
                System.out.println("    Home Team: " + firstEvent.getHomeTeam());
                System.out.println("    Away Team: " + firstEvent.getAwayTeam());
                System.out.println("    Commence Time: " + firstEvent.getCommenceTime());
            }
        } catch (ApiException e) {
            System.err.println("✗ Failed to fetch events: " + e.getMessage());
            throw e;
        }
    }
    
    private static void testAdapter(OddsApiGateway gateway, String sport, String region) 
            throws ApiException {
        try {
            // Fetch events
            List<OddsApiEvent> events = gateway.fetchEvents(sport, region, LocalDate.now());
            
            if (events.isEmpty()) {
                System.out.println("  No events to convert");
                return;
            }
            
            // Convert to Game entities
            OddsApiResponseAdapter adapter = new OddsApiResponseAdapter();
            List<Game> games = adapter.convertToGames(events);
            
            System.out.println("✓ Converted " + events.size() + " events to " + games.size() + " games");
            
            if (!games.isEmpty()) {
                Game firstGame = games.get(0);
                System.out.println("  First game:");
                System.out.println("    ID: " + firstGame.getId());
                System.out.println("    Team A: " + firstGame.getTeamA());
                System.out.println("    Team B: " + firstGame.getTeamB());
                System.out.println("    Sport: " + firstGame.getSport());
                System.out.println("    Status: " + firstGame.getStatus());
                System.out.println("    Game Time: " + firstGame.getGameTime());
                System.out.println("    External ID: " + firstGame.getExternalId());
            }
        } catch (Exception e) {
            System.err.println("✗ Adapter test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

