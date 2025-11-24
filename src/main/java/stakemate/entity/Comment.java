package stakemate.entity;

import java.time.LocalDateTime;

public class Comment {

    private final String id;          // unique comment id (e.g. UUID)
    private final String marketId;    // ties to Market.getId()
    private final String username;    // ties to User.getUsername()
    private final String message;     // comment text
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
    public String getId() { return id; }

    public String getMarketId() { return marketId; }

    public String getUsername() { return username; }

    public String getMessage() { return message; }

    public LocalDateTime getTimestamp() { return timestamp; }
}
