package stakemate.use_case.comments;

import java.util.List;

import stakemate.entity.Comment;

/**
 * Repository interface for saving and retrieving comments.
 */
public interface CommentRepository {

    /**
     * Saves a new comment.
     *
     * @param comment the comment to save
     */
    void saveComment(Comment comment);

    /**
     * Returns all comments for the given market.
     *
     * @param marketId the market identifier
     * @return a list of comments for that market
     */
    List<Comment> getCommentsForMarket(String marketId);
}
