package stakemate.use_case.comments;

import stakemate.entity.Comment;

import java.util.List;

public interface CommentRepository {
    void saveComment(Comment comment);
    List<Comment> getCommentsForMarket(String marketId);
}
