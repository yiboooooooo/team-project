package stakemate.data_access.supabase;

import stakemate.engine.BookOrder;
import stakemate.use_case.PlaceOrderUseCase.PositionRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class PostgresPositionRepository implements PositionRepository {

    private final DataSource ds;

    public PostgresPositionRepository(DataSource ds) {
        this.ds = ds;
    }

    @Override
    public void savePosition(BookOrder order, double executedAmount, double executedPrice) {
        System.out.println("DEBUG: savePosition called for order " + order.getId()
            + ", executedAmount=" + executedAmount
            + ", executedPrice=" + executedPrice);


        final String sql = """
                INSERT INTO positions (
                    user_id,
                    asset_name,
                    amount,
                    settled,
                    market_id,
                    price,
                    won,
                    side
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, java.util.UUID.fromString(order.getUserId()), java.sql.Types.OTHER); // uuid
            ps.setString(2, order.getMarketId());       // asset_name
            ps.setDouble(3, executedAmount);            // amount filled in trade
            ps.setBoolean(4, false);                    // settled = false initially
            ps.setString(5, order.getMarketId());       // market_id

            if (order.getPrice() == null) {
                ps.setNull(6, java.sql.Types.NUMERIC);
            }
            else {
                ps.setDouble(6, executedPrice);         // trade execution price
            }

            ps.setNull(7, java.sql.Types.BOOLEAN);      // won is unknown until settlement logic
            ps.setString(8, order.getSide().name());    // BUY or SELL

            ps.executeUpdate();
            System.out.println("Position inserted for order " + order.getId());

        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save position", e);
        }
    }
}
