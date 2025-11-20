package stakemate.data_access.api;

import com.google.gson.*;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import stakemate.use_case.fetch_games.ApiException;
import stakemate.use_case.fetch_games.OddsApiEvent;
import stakemate.use_case.fetch_games.OddsApiGateway;
import stakemate.use_case.fetch_games.OddsApiSport;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of OddsApiGateway using OkHttp.
 * Handles HTTP requests to the Odds API and JSON parsing.
 */
public class OddsApiGatewayImpl implements OddsApiGateway {

    private static final String BASE_URL = "https://api.the-odds-api.com/v4/sports";
    private static final int CONNECT_TIMEOUT_SECONDS = 10;
    private static final int READ_TIMEOUT_SECONDS = 30;

    private final OkHttpClient httpClient;
    private final Gson gson;
    private final String apiKey;

    public OddsApiGatewayImpl(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build();

        // Configure Gson with custom LocalDateTime deserializer
        this.gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
            .create();
    }

    @Override
    public List<OddsApiSport> fetchSports() throws ApiException {
        try {
            String url = BASE_URL + "?apiKey=" + apiKey;
            Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

            try (Response response = httpClient.newCall(request).execute()) {
                return handleSportsResponse(response);
            }
        } catch (IOException e) {
            throw new ApiException("Network error while fetching sports: " + e.getMessage(), e);
        }
    }

    @Override
    public List<OddsApiEvent> fetchEvents(String sport, String region, LocalDate dateFrom)
        throws ApiException {
        if (sport == null || sport.isEmpty()) {
            throw new IllegalArgumentException("Sport parameter is required and cannot be null or empty");
        }

        try {
            String url = buildUrl(sport, region, dateFrom);
            Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

            try (Response response = httpClient.newCall(request).execute()) {
                return handleResponse(response);
            }
        } catch (IOException e) {
            throw new ApiException("Network error while fetching events: " + e.getMessage(), e);
        }
    }

    /**
     * Builds the API URL with query parameters.
     */
    private String buildUrl(String sport, String region, LocalDate dateFrom) {
        StringBuilder url = new StringBuilder(BASE_URL);
        url.append("/").append(sport);
        url.append("/events");

        StringBuilder queryParams = new StringBuilder();
        queryParams.append("apiKey=").append(apiKey);

        if (region != null && !region.isEmpty()) {
            queryParams.append("&regions=").append(region);
        }

        if (dateFrom != null) {
            queryParams.append("&dateFormat=iso");
            // Note: The API expects dateFrom parameter, but we'll use the default behavior
            // which is to return events from today onwards
        }

        url.append("?").append(queryParams);
        return url.toString();
    }

    /**
     * Handles the HTTP response for sports endpoint and parses JSON.
     */
    private List<OddsApiSport> handleSportsResponse(Response response) throws ApiException {
        if (!response.isSuccessful()) {
            String errorBody = "";
            try (ResponseBody body = response.body()) {
                if (body != null) {
                    errorBody = body.string();
                }
            } catch (IOException e) {
                // Ignore
            }

            int code = response.code();
            if (code == 401) {
                throw new ApiException("Invalid API key. Check your Odds API credentials.");
            } else if (code == 429) {
                throw new ApiException("Rate limit exceeded. Please wait before making more requests.");
            } else if (code >= 500) {
                throw new ApiException("Server error from Odds API. Please try again later.");
            } else {
                throw new ApiException("API request failed with code " + code + ": " + errorBody);
            }
        }

        try (ResponseBody body = response.body()) {
            if (body == null) {
                throw new ApiException("Empty response from API");
            }

            String json = body.string();
            if (json == null || json.trim().isEmpty()) {
                return new ArrayList<>();
            }

            JsonArray jsonArray = gson.fromJson(json, JsonArray.class);
            List<OddsApiSport> sports = new ArrayList<>();

            for (JsonElement element : jsonArray) {
                OddsApiSport sport = gson.fromJson(element, OddsApiSport.class);
                sports.add(sport);
            }

            return sports;
        } catch (IOException e) {
            throw new ApiException("Error reading response: " + e.getMessage(), e);
        } catch (JsonParseException e) {
            throw new ApiException("Error parsing JSON response: " + e.getMessage(), e);
        }
    }

    /**
     * Handles the HTTP response and parses JSON.
     */
    private List<OddsApiEvent> handleResponse(Response response) throws ApiException {
        if (!response.isSuccessful()) {
            String errorBody = "";
            try (ResponseBody body = response.body()) {
                if (body != null) {
                    errorBody = body.string();
                }
            } catch (IOException e) {
                // Ignore
            }

            int code = response.code();
            if (code == 401) {
                throw new ApiException("Invalid API key. Check your Odds API credentials.");
            } else if (code == 429) {
                throw new ApiException("Rate limit exceeded. Please wait before making more requests.");
            } else if (code >= 500) {
                throw new ApiException("Server error from Odds API. Please try again later.");
            } else {
                throw new ApiException("API request failed with code " + code + ": " + errorBody);
            }
        }

        try (ResponseBody body = response.body()) {
            if (body == null) {
                throw new ApiException("Empty response from API");
            }

            String json = body.string();
            if (json == null || json.trim().isEmpty()) {
                return new ArrayList<>();
            }

            JsonArray jsonArray = gson.fromJson(json, JsonArray.class);
            List<OddsApiEvent> events = new ArrayList<>();

            for (JsonElement element : jsonArray) {
                OddsApiEvent event = gson.fromJson(element, OddsApiEvent.class);
                events.add(event);
            }

            return events;
        } catch (IOException e) {
            throw new ApiException("Error reading response: " + e.getMessage(), e);
        } catch (JsonParseException e) {
            throw new ApiException("Error parsing JSON response: " + e.getMessage(), e);
        }
    }

    /**
     * Custom deserializer for LocalDateTime from ISO 8601 strings.
     */
    private static class LocalDateTimeDeserializer implements JsonDeserializer<LocalDateTime> {
        private static final DateTimeFormatter[] FORMATTERS = {
            DateTimeFormatter.ISO_DATE_TIME,
            DateTimeFormatter.ISO_OFFSET_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        };

        @Override
        public LocalDateTime deserialize(JsonElement json, java.lang.reflect.Type typeOfT,
                                         JsonDeserializationContext context) throws JsonParseException {
            String dateString = json.getAsString();

            for (DateTimeFormatter formatter : FORMATTERS) {
                try {
                    return LocalDateTime.parse(dateString, formatter);
                } catch (DateTimeParseException e) {
                    // Try next formatter
                }
            }

            throw new JsonParseException("Unable to parse date: " + dateString);
        }
    }
}

