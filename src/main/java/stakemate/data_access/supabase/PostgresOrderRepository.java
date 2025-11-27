package stakemate.data_access.supabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.Instant;

import stakemate.engine.BookOrder;
import stakemate.use_case.PlaceOrderUseCase.OrderRepository;
import stakemate.entity.Side;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.time.Instant;

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
                    return null;
                }

                String id = rs.getString("id");
                String userId = rs.getString("user_id");
                String marketId = rs.getString("market_id");
                String sideStr = rs.getString("side");
                Double price = (Double) rs.getObject("price");
                double originalQty = rs.getDouble("original_qty");
                double remainingQty = rs.getDouble("remaining_qty");
                Instant timestamp = rs.getTimestamp("timestamp").toInstant();

                // ★ This uses your reconstruction constructor ★
                return new BookOrder(
                    id,
                    userId,
                    marketId,
                    stakemate.entity.Side.valueOf(sideStr),
                    price,
                    originalQty,
                    remainingQty,
                    timestamp
                );
            }

        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to find order by id", e);
        }
    }

    @Override
    public List<BookOrder> findOpenOrdersForMarket(String marketId, Side side) {
        final String sql = """
                SELECT id, user_id, market_id, side, price,
                       original_qty, remaining_qty, timestamp
                FROM orders
                WHERE market_id = ? AND side = ? AND remaining_qty > 0
            """;

        List<BookOrder> result = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, marketId);
            stmt.setString(2, side.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRowToBookOrder(rs));
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load open orders for market", e);
        }

        return result;
    }

    @Override
    public List<BookOrder> findOpenOrdersForUser(String userId) {
        final String sql = """
                SELECT id, user_id, market_id, side, price,
                       original_qty, remaining_qty, timestamp
                FROM orders
                WHERE user_id = ? AND remaining_qty > 0
            """;

        List<BookOrder> result = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRowToBookOrder(rs));
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load open orders for user", e);
        }

        return result;
    }

    @Override
    public void updateRemainingQty(String orderId, double newRemainingQty) {
        final String sql = """
                UPDATE orders
                SET remaining_qty = ?
                WHERE id = ?
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, newRemainingQty);
            stmt.setString(2, orderId);
            stmt.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update remaining_qty", e);
        }
    }

    /**
     * Helper: map a ResultSet row to a BookOrder using your full constructor.
     */
    private BookOrder mapRowToBookOrder(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String userId = rs.getString("user_id");
        String marketId = rs.getString("market_id");
        String sideStr = rs.getString("side");
        Double price = (Double) rs.getObject("price");
        double originalQty = rs.getDouble("original_qty");
        double remainingQty = rs.getDouble("remaining_qty");
        Instant ts = rs.getTimestamp("timestamp").toInstant();

        return new BookOrder(
            id,
            userId,
            marketId,
            stakemate.entity.Side.valueOf(sideStr),
            price,
            originalQty,
            remainingQty,
            ts
        );
    }

    @Override
    public List<BookOrder> findOppositeSideOrders(String marketId, Side incomingSide) {

        String opposite = (incomingSide == Side.BUY) ? "SELL" : "BUY";

        String sql = """
            SELECT id, user_id, market_id, side, price,
                   original_qty, remaining_qty, timestamp
            FROM orders
            WHERE market_id = ?
              AND side = ?
              AND remaining_qty > 0
            ORDER BY price ASC, timestamp ASC
        """;

        // For BUY incoming, SELLs sorted ASC (lowest ask first)
        // For SELL incoming, we need DESC (highest bid first)
        if (incomingSide == Side.SELL) {
            sql = sql.replace("ORDER BY price ASC", "ORDER BY price DESC");
        }

        List<BookOrder> list = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, marketId);
            stmt.setString(2, opposite);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToBookOrder(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed loading opposite side orders", e);
        }

        return list;
    }

    @Override
    public void reduceRemainingQty(String orderId, double executedQty) {
        final String sql = """
        UPDATE orders
        SET remaining_qty = remaining_qty - ?
        WHERE id = ? AND remaining_qty > 0
    """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, executedQty);
            stmt.setString(2, orderId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to reduce remaining qty", e);
        }
    }


}



