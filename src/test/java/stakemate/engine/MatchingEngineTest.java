package stakemate.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import stakemate.data_access.supabase.PostgresOrderRepository;
import stakemate.entity.Side;
import stakemate.service.DbAccountService;
import stakemate.use_case.PlaceOrderUseCase.PositionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MatchingEngineTest {

    private MatchingEngine engine;
    private StubOrderRepository orderRepo;
    private StubPositionRepository positionRepo;
    private StubAccountService accountService;

    @BeforeEach
    void setUp() {
        orderRepo = new StubOrderRepository();
        positionRepo = new StubPositionRepository();
        accountService = new StubAccountService();
        engine = new MatchingEngine(orderRepo, positionRepo, accountService);
    }

    @Test
    void testLimitOrderPartialFill() {
        // Resting Sell: 10 @ 2.0
        BookOrder sell1 = createOrder("user1", Side.SELL, 2.0, 10.0);
        orderRepo.addOrder(sell1);

        // Resting Sell: 10 @ 2.0
        BookOrder sell2 = createOrder("user2", Side.SELL, 2.0, 10.0);
        orderRepo.addOrder(sell2);

        // Incoming Buy: 15 @ 2.0
        BookOrder buy = createOrder("user3", Side.BUY, 2.0, 15.0);

        List<Trade> trades = engine.placeOrder(buy);

        // Should match 10 from sell1 and 5 from sell2
        assertEquals(2, trades.size());
        assertEquals(10.0, trades.get(0).getSize());
        assertEquals(5.0, trades.get(1).getSize());
        assertEquals(0.0, buy.getRemainingQty());
        assertEquals(0.0, sell1.getRemainingQty());
        assertEquals(5.0, sell2.getRemainingQty());
    }

    @Test
    void testMarketOrderAllOrNone_Success() {
        // Resting Sell: 10 @ 2.0
        BookOrder sell1 = createOrder("user1", Side.SELL, 2.0, 10.0);
        orderRepo.addOrder(sell1);

        // User has enough funds: 10 * 2.0 = 20.0
        accountService.setBalance("user3", 100.0);

        // Incoming Market Buy: 5
        BookOrder buy = createOrder("user3", Side.BUY, null, 5.0);

        List<Trade> trades = engine.placeOrder(buy);

        assertEquals(1, trades.size());
        assertEquals(5.0, trades.get(0).getSize());
        assertEquals(2.0, trades.get(0).getPrice()); // Should take limit price
        assertEquals(0.0, buy.getRemainingQty());
    }

    @Test
    void testMarketOrderResting_NoLiquidity() {
        // Incoming Market Buy: 5
        BookOrder buy = createOrder("user3", Side.BUY, null, 5.0);

        List<Trade> trades = engine.placeOrder(buy);

        // Should be 0 trades, but order should NOT be cancelled (remaining > 0)
        assertEquals(0, trades.size());
        assertEquals(5.0, buy.getRemainingQty());
    }

    @Test
    void testRestingMarketOrder_MatchesLimit() {
        // 1. Place Market Buy (rests)
        BookOrder buy = createOrder("user3", Side.BUY, null, 10.0);
        engine.placeOrder(buy);
        orderRepo.addOrder(buy); // Simulate saving to DB/Repo

        // User has funds: 10 * 2.0 = 20.0
        accountService.setBalance("user3", 100.0);

        // 2. Place Limit Sell: 10 @ 2.0
        BookOrder sell = createOrder("user1", Side.SELL, 2.0, 10.0);

        List<Trade> trades = engine.placeOrder(sell);

        assertEquals(1, trades.size());
        assertEquals(10.0, trades.get(0).getSize());
        assertEquals(2.0, trades.get(0).getPrice());
        assertEquals(0.0, buy.getRemainingQty());
    }

    @Test
    void testRestingMarketOrder_InsufficientFunds() {
        // 1. Place Market Buy (rests)
        BookOrder buy = createOrder("user3", Side.BUY, null, 10.0);
        engine.placeOrder(buy);
        orderRepo.addOrder(buy);

        // User has low funds: 5.0
        accountService.setBalance("user3", 5.0);

        // 2. Place Limit Sell: 10 @ 2.0 (Cost 20.0)
        BookOrder sell = createOrder("user1", Side.SELL, 2.0, 10.0);

        List<Trade> trades = engine.placeOrder(sell);

        // Should be 0 trades because resting market order is cancelled due to
        // insufficient funds
        assertEquals(0, trades.size());
        assertEquals(0.0, buy.getRemainingQty()); // Cancelled
    }

    @Test
    void testMarketOrderAllOrNone_InsufficientLiquidity() {
        // Resting Sell: 2 @ 2.0
        BookOrder sell1 = createOrder("user1", Side.SELL, 2.0, 2.0);
        orderRepo.addOrder(sell1);

        // Incoming Market Buy: 5
        BookOrder buy = createOrder("user3", Side.BUY, null, 5.0);

        List<Trade> trades = engine.placeOrder(buy);

        // Should be 0 trades because cannot fill 5 with 2
        assertEquals(0, trades.size());
        // Order should be cancelled (not added to book, remaining qty not reduced in
        // this context effectively cancelled)
        // In real DB impl, we'd check if it was saved, but here we check result.
    }

    @Test
    void testMarketOrderAllOrNone_InsufficientFunds() {
        // Resting Sell: 10 @ 2.0
        BookOrder sell1 = createOrder("user1", Side.SELL, 2.0, 10.0);
        orderRepo.addOrder(sell1);

        // User has only 10.0, needs 5 * 2.0 = 10.0. Wait, let's make it insufficient.
        // Needs 20.0 for full fill of 10? No, let's buy 5. Cost 10.
        // User has 5.0.
        accountService.setBalance("user3", 5.0);

        // Incoming Market Buy: 5
        BookOrder buy = createOrder("user3", Side.BUY, null, 5.0);

        List<Trade> trades = engine.placeOrder(buy);

        assertEquals(0, trades.size());
    }

    @Test
    void testMarketOrderDoesNotMatchMarketOrder() {
        // Resting Market Sell (shouldn't really happen in book, but for safety)
        BookOrder sell1 = createOrder("user1", Side.SELL, null, 10.0);
        orderRepo.addOrder(sell1);

        // Incoming Market Buy
        BookOrder buy = createOrder("user3", Side.BUY, null, 5.0);

        List<Trade> trades = engine.placeOrder(buy);

        assertEquals(0, trades.size());
    }

    // --- Helpers ---

    private BookOrder createOrder(String userId, Side side, Double price, double qty) {
        return new BookOrder(userId, "market1", side, price, qty);
    }

    // --- Stubs ---

    static class StubOrderRepository extends PostgresOrderRepository {
        List<BookOrder> orders = new ArrayList<>();

        public StubOrderRepository() {
            super(null);
        }

        void addOrder(BookOrder o) {
            orders.add(o);
        }

        @Override
        public List<BookOrder> findOppositeSideOrders(String marketId, Side side) {
            List<BookOrder> result = new ArrayList<>();
            Side opposite = (side == Side.BUY) ? Side.SELL : Side.BUY;
            for (BookOrder o : orders) {
                if (o.getSide() == opposite && o.getRemainingQty() > 0) {
                    result.add(o);
                }
            }
            // Sort by price
            result.sort((o1, o2) -> {
                Double p1 = o1.getPrice();
                Double p2 = o2.getPrice();
                if (p1 == null && p2 == null)
                    return 0;
                if (p1 == null)
                    return -1; // Market first? Or last? Actually we want to skip market in logic.
                if (p2 == null)
                    return 1;
                return (side == Side.BUY) ? Double.compare(p1, p2) : Double.compare(p2, p1);
            });
            return result;
        }

        @Override
        public void reduceRemainingQty(String orderId, double executedQty) {
            for (BookOrder o : orders) {
                if (o.getId().equals(orderId)) {
                    // We don't have a public setter for remainingQty in BookOrder,
                    // but MatchingEngine updates the object passed to it.
                    // The repo update is for DB. In stub we can ignore or try to update if we had
                    // access.
                }
            }
        }
    }

    static class StubPositionRepository implements PositionRepository {
        @Override
        public void savePosition(BookOrder order, double size, double price) {
        }
    }

    static class StubAccountService extends DbAccountService {
        private java.util.Map<String, Double> balances = new java.util.HashMap<>();

        public StubAccountService() {
            super(null);
        }

        void setBalance(String userId, double amount) {
            balances.put(userId, amount);
        }

        @Override
        public double getBalance(String userId) {
            return balances.getOrDefault(userId, 0.0);
        }

        @Override
        public void adjustBalance(String userId, double delta) {
            balances.put(userId, getBalance(userId) + delta);
        }

        @Override
        public boolean hasSufficientFunds(String userId, String marketId, double qty, Double price) {
            // This is used for pre-check in PlaceOrderUseCase, but MatchingEngine calls
            // getBalance directly?
            // MatchingEngine doesn't call hasSufficientFunds, it calls adjustBalance.
            // But we need to implement the check logic inside MatchingEngine for Market
            // Orders.
            return true;
        }
    }
}
