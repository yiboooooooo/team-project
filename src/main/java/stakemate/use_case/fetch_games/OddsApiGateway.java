package stakemate.use_case.fetch_games;

import java.time.LocalDate;
import java.util.List;

/**
 * Gateway interface for the Odds API.
 * Encapsulates external API communication following the Gateway pattern.
 */
public interface OddsApiGateway {
    /**
     * Fetches events from the Odds API.
     *
     * @param sport Sport key (e.g., "basketball_nba"), or null for all sports
     * @param region Region code (e.g., "us"), or null for all regions
     * @param dateFrom Minimum date for events, or null for today
     * @return List of raw event data (DTOs) from the API
     * @throws ApiException if API call fails
     */
    List<OddsApiEvent> fetchEvents(String sport, String region, LocalDate dateFrom) 
            throws ApiException;
}

