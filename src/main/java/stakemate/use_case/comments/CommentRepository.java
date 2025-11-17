package stakemate.use_case.comments;

import stakemate.entity.Comment;

import java.util.List;

public interface CommentRepository {
    void save(Comment comment);
    List<Comment> findByMarketId(String marketId);
}
