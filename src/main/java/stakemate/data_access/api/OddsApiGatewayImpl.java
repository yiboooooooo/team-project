package stakemate.data_access.api;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import stakemate.use_case.fetch_games.ApiException;
import stakemate.use_case.fetch_games.OddsApiEvent;
import stakemate.use_case.fetch_games.OddsApiGateway;
import stakemate.use_case.fetch_games.OddsApiSport;

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

    public OddsApiGatewayImpl(final String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build();
        this.gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
            .create();
    }

    @Override
    public List<OddsApiSport> fetchSports() throws ApiException {
        try {
            final String url = BASE_URL + "?apiKey=" + apiKey;
            final Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

            try (final Response response = httpClient.newCall(request).execute()) {
                return handleSportsResponse(response);
            }
        }
        catch (final IOException e) {
            throw new ApiException("Network error while fetching sports: " + e.getMessage(), e);
        }
    }

    @Override
    public List<OddsApiEvent> fetchEvents(final String sport, final String region, final LocalDate dateFrom)
        throws ApiException {
        if (sport == null || sport.isEmpty()) {
            throw new IllegalArgumentException("Sport parameter is required and cannot be null or empty");
        }

        try {
            final String url = buildUrl(sport, region, dateFrom);
            final Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

            try (final Response response = httpClient.newCall(request).execute()) {
                return handleResponse(response);
            }
        }
        catch (final IOException e) {
            throw new ApiException("Network error while fetching events: " + e.getMessage(), e);
        }
    }

    /**
     * Builds the API URL with query parameters.
     */
    private String buildUrl(final String sport, final String region, final LocalDate dateFrom) {
        final StringBuilder url = new StringBuilder(BASE_URL);
        url.append("/").append(sport);
        url.append("/events");

        final StringBuilder queryParams = new StringBuilder();
        queryParams.append("apiKey=").append(apiKey);

        if (region != null && !region.isEmpty()) {
            queryParams.append("&regions=").append(region);
        }

        if (dateFrom != null) {
            queryParams.append("&dateFormat=iso");
            // which is to return events from today onwards
        }

        url.append("?").append(queryParams);
        return url.toString();
    }

    /**
     * Handles the HTTP response for sports endpoint and parses JSON.
     */
    private List<OddsApiSport> handleSportsResponse(final Response response) throws ApiException {
        if (!response.isSuccessful()) {
            String errorBody = "";
            try (final ResponseBody body = response.body()) {
                if (body != null) {
                    errorBody = body.string();
                }
            }
            catch (final IOException e) {
                // Ignore
            }

            final int code = response.code();
            if (code == 401) {
                throw new ApiException("Invalid API key. Check your Odds API credentials.");
            }
            else if (code == 429) {
                throw new ApiException("Rate limit exceeded. Please wait before making more requests.");
            }
            else if (code >= 500) {
                throw new ApiException("Server error from Odds API. Please try again later.");
            }
            else {
                throw new ApiException("API request failed with code " + code + ": " + errorBody);
            }
        }

        try (final ResponseBody body = response.body()) {
            if (body == null) {
                throw new ApiException("Empty response from API");
            }

            final String json = body.string();
            if (json == null || json.trim().isEmpty()) {
                return new ArrayList<>();
            }

            final JsonArray jsonArray = gson.fromJson(json, JsonArray.class);
            final List<OddsApiSport> sports = new ArrayList<>();

            for (final JsonElement element : jsonArray) {
                final OddsApiSport sport = gson.fromJson(element, OddsApiSport.class);
                sports.add(sport);
            }

            return sports;
        }
        catch (final IOException e) {
            throw new ApiException("Error reading response: " + e.getMessage(), e);
        }
        catch (final JsonParseException e) {
            throw new ApiException("Error parsing JSON response: " + e.getMessage(), e);
        }
    }

    /**
     * Handles the HTTP response and parses JSON.
     */
    private List<OddsApiEvent> handleResponse(final Response response) throws ApiException {
        if (!response.isSuccessful()) {
            String errorBody = "";
            try (final ResponseBody body = response.body()) {
                if (body != null) {
                    errorBody = body.string();
                }
            }
            catch (final IOException e) {
                // Ignore
            }

            final int code = response.code();
            if (code == 401) {
                throw new ApiException("Invalid API key. Check your Odds API credentials.");
            }
            else if (code == 429) {
                throw new ApiException("Rate limit exceeded. Please wait before making more requests.");
            }
            else if (code >= 500) {
                throw new ApiException("Server error from Odds API. Please try again later.");
            }
            else {
                throw new ApiException("API request failed with code " + code + ": " + errorBody);
            }
        }

        try (final ResponseBody body = response.body()) {
            if (body == null) {
                throw new ApiException("Empty response from API");
            }

            final String json = body.string();
            if (json == null || json.trim().isEmpty()) {
                return new ArrayList<>();
            }

            final JsonArray jsonArray = gson.fromJson(json, JsonArray.class);
            final List<OddsApiEvent> events = new ArrayList<>();

            for (final JsonElement element : jsonArray) {
                final OddsApiEvent event = gson.fromJson(element, OddsApiEvent.class);
                events.add(event);
            }

            return events;
        }
        catch (final IOException e) {
            throw new ApiException("Error reading response: " + e.getMessage(), e);
        }
        catch (final JsonParseException e) {
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
        public LocalDateTime deserialize(final JsonElement json, final java.lang.reflect.Type typeOfT,
                                         final JsonDeserializationContext context) throws JsonParseException {
            final String dateString = json.getAsString();

            for (final DateTimeFormatter formatter : FORMATTERS) {
                try {
                    return LocalDateTime.parse(dateString, formatter);
                }
                catch (final DateTimeParseException e) {
                    // Try next formatter
                }
            }

            throw new JsonParseException("Unable to parse date: " + dateString);
        }
    }
}

