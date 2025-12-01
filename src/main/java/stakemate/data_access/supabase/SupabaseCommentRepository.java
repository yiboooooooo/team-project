package stakemate.data_access.supabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import stakemate.entity.Comment;
import stakemate.use_case.comments.CommentRepository;

/**
 * Implementation of CommentRepository using Supabase (PostgreSQL) via JDBC.
 */
public class SupabaseCommentRepository implements CommentRepository {

    private static final int PARAM_ID = 1;
    private static final int PARAM_MARKET_ID = 2;
    private static final int PARAM_USER_ID = 3;
    private static final int PARAM_CONTENT = 4;
    private static final int PARAM_CREATED_AT = 5;

    private final SupabaseClientFactory clientFactory;

    public SupabaseCommentRepository() {
        this.clientFactory = new SupabaseClientFactory();
    }

    public SupabaseCommentRepository(SupabaseClientFactory factory) {
        this.clientFactory = factory;
    }

    @Override
    public void saveComment(Comment comment) {
        final String sql = "INSERT INTO comments (id, market_id, user_id, content, created_at) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = clientFactory.createConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            final String commentId;

            if (comment.getId() != null) {
                commentId = comment.getId();
            }
            else {
                commentId = UUID.randomUUID().toString();
            }

            stmt.setString(PARAM_ID, commentId);
            stmt.setString(PARAM_MARKET_ID, comment.getMarketId());
            stmt.setString(PARAM_USER_ID, comment.getUsername());
            stmt.setString(PARAM_CONTENT, comment.getMessage());
            stmt.setTimestamp(PARAM_CREATED_AT, Timestamp.valueOf(comment.getTimestamp()));

            stmt.executeUpdate();
        }
        catch (SQLException event) {
            event.printStackTrace();
        }
    }

    @Override
    public List<Comment> getCommentsForMarket(String marketId) {
        final String sql =
            "SELECT id, market_id, user_id, content, created_at "
                + "FROM comments "
                + "WHERE market_id = ? "
                + "ORDER BY created_at ASC";
        final List<Comment> comments = new ArrayList<>();

        try (Connection conn = clientFactory.createConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, marketId);
            final ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                final Comment comment = new Comment(
                    rs.getString("id"),
                    rs.getString("market_id"),
                    rs.getString("user_id"),
                    rs.getString("content"),
                    rs.getTimestamp("created_at").toLocalDateTime()
                );
                comments.add(comment);
            }
        }
        catch (SQLException event) {
            event.printStackTrace();
        }

        return comments;
    }
}
