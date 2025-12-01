package stakemate.entity;

import java.time.LocalDateTime;

public class Comment {

    private final String id;
    private final String marketId;
    private final String username;
    private final String message;
    private final LocalDateTime timestamp;

    public Comment(
            String id,
            String marketId,
            String username,
            String message,
            LocalDateTime timestamp
    ) {
        this.id = id;
        this.marketId = marketId;
        this.username = username;
        this.message = message;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }

    // --- Getters ---
    public String getId() {
        return id;
    }

    public String getMarketId() {
        return marketId;
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
