package stakemate.data_access.in_memory;

import stakemate.entity.Comment;
import stakemate.use_case.comments.CommentRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryCommentRepository implements CommentRepository {

    // Map: marketId → List<Comment>
    private final Map<String, List<Comment>> storage = new ConcurrentHashMap<>();

    @Override
    public void saveComment(Comment comment) {
        storage
            .computeIfAbsent(comment.getMarketId(), id -> new ArrayList<>())
            .add(comment);
    }

    @Override
    public List<Comment> getCommentsForMarket(String marketId) {
        return new ArrayList<>(storage.getOrDefault(marketId, Collections.emptyList()));
    }
}
