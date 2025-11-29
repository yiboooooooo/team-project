package stakemate.data_access.supabase;

import stakemate.entity.Comment;
import stakemate.use_case.comments.CommentRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of CommentRepository using Supabase (PostgreSQL) via JDBC.
 */
public class SupabaseCommentRepository implements CommentRepository {

    private final SupabaseClientFactory clientFactory;

    public SupabaseCommentRepository() {
        this.clientFactory = new SupabaseClientFactory();
    }

    public SupabaseCommentRepository(SupabaseClientFactory factory) {
        this.clientFactory = factory;
    }

    @Override
    public void saveComment(Comment comment) {
        String sql = "INSERT INTO comments (id, market_id, user_id, content, created_at) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = clientFactory.createConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, comment.getId() != null ? comment.getId() : UUID.randomUUID().toString());
            stmt.setString(2, comment.getMarketId());
            stmt.setString(3, comment.getUsername());
            stmt.setString(4, comment.getMessage());
            stmt.setTimestamp(5, Timestamp.valueOf(comment.getTimestamp()));

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace(); // replace with proper logging
        }
    }

    @Override
    public List<Comment> getCommentsForMarket(String marketId) {
        String sql = "SELECT id, market_id, user_id, content, created_at FROM comments WHERE market_id = ? ORDER BY created_at ASC";
        List<Comment> comments = new ArrayList<>();

        try (Connection conn = clientFactory.createConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, marketId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Comment comment = new Comment(
                    rs.getString("id"),
                    rs.getString("market_id"),
                    rs.getString("user_id"),
                    rs.getString("content"),
                    rs.getTimestamp("created_at").toLocalDateTime()
                );
                comments.add(comment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return comments;
    }
}
