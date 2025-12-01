package stakemate.service;

import stakemate.data_access.supabase.SupabaseClientFactory;
import stakemate.engine.BookOrder;
import stakemate.engine.Trade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Account service backed by the Supabase "profiles" table.
 * Expects userId to be profiles.id (uuid as String).
 */
public class DbAccountService implements AccountService {

    private final SupabaseClientFactory factory;

    public DbAccountService(SupabaseClientFactory factory) {
        this.factory = factory;
    }

    @Override
    public boolean hasSufficientFunds(String userId, String marketId, double qty, Double price) {
        final double p = (price == null ? 1.0 : price);
        final double needed = p * qty;
        final double current = getBalance(userId);
        return current + 1e-9 >= needed;
    }

    @Override
    public void reserveForOrder(String userId, String orderId, double amount) {
        // For now we don't implement separate reservations in DB.
        // Balance is checked above; settlement happens when trade executes.
    }

    @Override
    public void capture(Trade trade) {
        // We do settlement in PlaceOrderUseCase where we know the BookOrders.
        // No-op here.
    }

    // --- Helpers used by PlaceOrderUseCase settlement ---

    public double getBalance(String userId) {
        final String sql = "SELECT balance FROM public.profiles WHERE id = ?";

        try (Connection conn = factory.createConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, java.util.UUID.fromString(userId));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
                return 0.0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error reading balance", e);
        }
    }

    public void adjustBalance(String userId, double delta) {
        final String sql = "UPDATE public.profiles SET balance = balance + ?, updated_at = now() WHERE id = ?";

        try (Connection conn = factory.createConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, delta);
            ps.setObject(2, java.util.UUID.fromString(userId));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating balance", e);
        }
    }

    /**
     * Apply a trade in a prediction market: both sides pay to enter their
     * positions.
     * Buyer pays price*size (betting YES on the outcome).
     * Seller pays (1-price)*size (betting NO on the outcome).
     */
    public void applyTrade(BookOrder buy, BookOrder sell, Trade trade) {
        final double price = trade.getPrice();
        final double size = trade.getSize();

        // Buyer pays the full execution price
        final double buyerCost = price * size;
        adjustBalance(buy.getUserId(), -buyerCost);

        // Seller pays the complementary amount (1 - price)
        final double sellerCost = (1.0 - price) * size;
        adjustBalance(sell.getUserId(), -sellerCost);
    }
}
