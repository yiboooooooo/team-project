package stakemate.entity;

import org.junit.jupiter.api.Test;
import stakemate.entity.factory.MarketFactory;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EntitiesTest {

    // =========================================================================
    // 1. Game Tests
    // =========================================================================
    @Test
    void testGame() {
        UUID id = UUID.randomUUID();
        UUID marketId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        String extId = "ext_123";

        Game game = new Game(id, marketId, now, "Team A", "Team B", "soccer", GameStatus.UPCOMING, extId);

        assertEquals(id, game.getId());
        assertEquals(marketId, game.getMarketId());
        assertEquals(now, game.getGameTime());
        assertEquals("Team A", game.getTeamA());
        assertEquals("Team B", game.getTeamB());
        assertEquals("soccer", game.getSport());
        assertEquals(GameStatus.UPCOMING, game.getStatus());
        assertEquals(extId, game.getExternalId());

        String toString = game.toString();
        assertTrue(toString.contains("Team A"));
        assertTrue(toString.contains("UPCOMING"));
    }

    @Test
    void testGameEqualsAndHashCode() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        String ext1 = "ext1";
        String ext2 = "ext2";
        LocalDateTime now = LocalDateTime.now();

        Game g1 = new Game(id1, null, now, "A", "B", "s", GameStatus.LIVE, ext1);
        Game g1Copy = new Game(id1, null, now, "A", "B", "s", GameStatus.LIVE, ext1);
        Game g2 = new Game(id2, null, now, "A", "B", "s", GameStatus.LIVE, ext1); // Diff ID
        Game g3 = new Game(id1, null, now, "A", "B", "s", GameStatus.LIVE, ext2); // Diff ExtID
        Object otherObj = new Object();

        // Test equals
        assertTrue(g1.equals(g1));          // Same object
        assertTrue(g1.equals(g1Copy));      // Equal values
        assertFalse(g1.equals(null));       // Null
        assertFalse(g1.equals(otherObj));   // Different class
        assertFalse(g1.equals(g2));         // Different ID
        assertFalse(g1.equals(g3));         // Different External ID

        // Test hashCode
        assertEquals(g1.hashCode(), g1Copy.hashCode());
        assertNotEquals(g1.hashCode(), g2.hashCode());
    }

    // =========================================================================
    // 2. Enum Tests (GameStatus, MarketStatus, MatchStatus, Side, Sport)
    // =========================================================================
    @Test
    void testEnums() {
        // GameStatus
        assertEquals(3, GameStatus.values().length);
        assertEquals(GameStatus.LIVE, GameStatus.valueOf("LIVE"));

        // MarketStatus
        assertEquals(2, MarketStatus.values().length);
        assertEquals(MarketStatus.OPEN, MarketStatus.valueOf("OPEN"));

        // MatchStatus
        assertEquals(3, MatchStatus.values().length);
        assertEquals(MatchStatus.CLOSED, MatchStatus.valueOf("CLOSED"));

        // Side (stakemate.entity.Side)
        assertEquals(2, Side.values().length);
        assertEquals(Side.BUY, Side.valueOf("BUY"));
    }

    // =========================================================================
    // 3. Sport Enum & Logic
    // =========================================================================
    @Test
    void testSport() {
        // Test getter
        assertEquals("basketball_nba", Sport.BASKETBALL_NBA.getOddsApiKey());

        // Test lookup success
        assertEquals(Sport.SOCCER_EPL, Sport.fromApiKey("soccer_epl"));

        // Test lookup fail
        assertNull(Sport.fromApiKey("invalid_sport_key"));

        // Test values
        assertTrue(Sport.values().length > 0);
        assertEquals(Sport.HOCKEY_NHL, Sport.valueOf("HOCKEY_NHL"));
    }

    // =========================================================================
    // 4. Market Tests
    // =========================================================================
    @Test
    void testMarket() {
        Market market = new Market("mkt1", "match1", "Moneyline", MarketStatus.OPEN);

        assertEquals("mkt1", market.getId());
        assertEquals("match1", market.getMatchId());
        assertEquals("Moneyline", market.getName());
        assertEquals(MarketStatus.OPEN, market.getStatus());
    }

    // =========================================================================
    // 5. Match Tests
    // =========================================================================
    @Test
    void testMatch() {
        LocalDateTime now = LocalDateTime.now();
        Match match = new Match("match1", "Home", "Away", MatchStatus.LIVE, now);

        assertEquals("match1", match.getId());
        assertEquals("Home", match.getHomeTeam());
        assertEquals("Away", match.getAwayTeam());
        assertEquals(MatchStatus.LIVE, match.getStatus());
        assertEquals(now, match.getCommenceTime());
    }

    // =========================================================================
    // 6. Order Tests (Including inner Enum)
    // =========================================================================
    @Test
    void testOrder() {
        // Note: Order class has an inner enum Side { BACK, LAY }
        Order order = new Order("user1", Order.Side.BACK, 1.5, 100.0);

        assertEquals("user1", order.getUserId());
        assertEquals(Order.Side.BACK, order.getSide());
        assertEquals(1.5, order.getOdds());
        assertEquals(100.0, order.getStake());

        String str = order.toString();
        assertTrue(str.contains("user1"));
        assertTrue(str.contains("BACK"));
        assertTrue(str.contains("1.5"));

        // Test Inner Enum
        assertEquals(Order.Side.LAY, Order.Side.valueOf("LAY"));
        assertEquals(2, Order.Side.values().length);
    }

    // =========================================================================
    // 7. OrderBook & OrderBookEntry Tests
    // =========================================================================
    @Test
    void testOrderBookAndEntry() {
        // Entries
        OrderBookEntry bid = new OrderBookEntry(Side.BUY, 10.5, 50);
        OrderBookEntry ask = new OrderBookEntry(Side.SELL, 11.0, 20);

        assertEquals(Side.BUY, bid.getSide());
        assertEquals(10.5, bid.getPrice());
        assertEquals(50, bid.getQuantity());

        // Book
        List<OrderBookEntry> bids = Collections.singletonList(bid);
        List<OrderBookEntry> asks = Collections.singletonList(ask);
        OrderBook book = new OrderBook("mkt_1", bids, asks);

        assertEquals("mkt_1", book.getMarketId());
        assertEquals(1, book.getBids().size());
        assertEquals(1, book.getAsks().size());
        assertEquals(bid, book.getBids().get(0));
    }

    // =========================================================================
    // 8. User Tests
    // =========================================================================
    @Test
    void testUser() {
        User user = new User("alice", "secret", 500);

        assertEquals("alice", user.getUsername());
        assertEquals("secret", user.getPassword());
        assertEquals(500, user.getBalance());

        user.setBalance(1000);
        assertEquals(1000, user.getBalance());
    }

    // =========================================================================
    // 9. Factory Tests (MarketFactory)
    // =========================================================================
    @Test
    void testMarketFactory() {
        // Test createMarket with Open status
        Market openMkt = MarketFactory.createMarket("match1", "Prop Bet", true);
        assertNotNull(openMkt.getId());
        assertEquals("match1", openMkt.getMatchId());
        assertEquals("Prop Bet", openMkt.getName());
        assertEquals(MarketStatus.OPEN, openMkt.getStatus());

        // Test createMarket with Closed status
        Market closedMkt = MarketFactory.createMarket("match1", "Prop Bet", false);
        assertEquals(MarketStatus.CLOSED, closedMkt.getStatus());

        // Test createMoneylineMarket
        Market mlMkt = MarketFactory.createMoneylineMarket("match2");
        assertEquals("match2", mlMkt.getMatchId());
        assertEquals("Moneyline", mlMkt.getName());
        assertEquals(MarketStatus.OPEN, mlMkt.getStatus());
    }

    // =========================================================================
    // 10. Comment Tests
    // =========================================================================
    @Test
    void testComment() {
        String id = "cmt_123";
        String marketId = "mkt_abc";
        String username = "user_xyz";
        String message = "Great market!";
        LocalDateTime timestamp = LocalDateTime.of(2025, 1, 1, 12, 0);

        // Test constructor with explicit timestamp
        Comment comment = new Comment(id, marketId, username, message, timestamp);

        assertEquals(id, comment.getId());
        assertEquals(marketId, comment.getMarketId());
        assertEquals(username, comment.getUsername());
        assertEquals(message, comment.getMessage());
        assertEquals(timestamp, comment.getTimestamp());

        // Test constructor with null timestamp (should default to now)
        Comment commentNow = new Comment(id, marketId, username, message, null);
        assertNotNull(commentNow.getTimestamp());
        // Ensure it was created roughly now (within last second)
        assertTrue(commentNow.getTimestamp().isAfter(LocalDateTime.now().minusSeconds(1)));
    }
}
