package stakemate.data_access.supabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import java.sql.Timestamp;

import stakemate.engine.BookOrder;
import stakemate.use_case.PlaceOrderUseCase.OrderRepository;

public class PostgresOrderRepository implements OrderRepository {

    private final DataSource dataSource;

    public PostgresOrderRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(BookOrder order) {
        final String sql = """
                INSERT INTO orders (
                    id, user_id, market_id, side, price,
                    original_qty, remaining_qty, timestamp
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, order.getId());
            stmt.setString(2, order.getUserId());
            stmt.setString(3, order.getMarketId());
            stmt.setString(4, order.getSide().name());
            if (order.getPrice() == null) stmt.setObject(5, null);
            else stmt.setDouble(5, order.getPrice());
            stmt.setDouble(6, order.getOriginalQty());
            stmt.setDouble(7, order.getRemainingQty());
            stmt.setTimestamp(8, Timestamp.from(order.getTimestamp()));

            stmt.executeUpdate();

        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save order", e);
        }
    }

    @Override
    public BookOrder findById(String orderId) {
        final String sql = """
                SELECT id, user_id, market_id, side, price,
                       original_qty, remaining_qty, timestamp
                FROM orders
                WHERE id = ?
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, orderId);

            try (var rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null; // order not found
                }

                String id = rs.getString("id");
                String userId = rs.getString("user_id");
                String marketId = rs.getString("market_id");
                String sideStr = rs.getString("side");
                Double price = (Double) rs.getObject("price"); // null allowed
                double originalQty = rs.getDouble("original_qty");
                double remainingQty = rs.getDouble("remaining_qty");
                java.time.Instant timestamp = rs.getTimestamp("timestamp").toInstant();

                // Rebuild BookOrder
                BookOrder order = new BookOrder(
                    userId,
                    marketId,
                    stakemate.entity.Side.valueOf(sideStr),
                    price,
                    originalQty
                );

                // Now manually set internal fields to match DB state
                // (BookOrder is mutable and supports reduce(...) to modify remainingQty)
                double filledQty = originalQty - remainingQty;
                if (filledQty > 0) {
                    order.reduce(filledQty); // reduces remainingQty appropriately
                }

                return order;
            }

        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to find order by id", e);
        }
    }
}

