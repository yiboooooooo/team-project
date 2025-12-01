package stakemate.data_access.in_memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import stakemate.entity.Comment;
import stakemate.use_case.comments.CommentRepository;

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
