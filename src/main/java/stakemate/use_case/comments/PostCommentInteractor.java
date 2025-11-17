package stakemate.use_case.comments;

import stakemate.entity.Comment;
import stakemate.use_case.comments.CommentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PostCommentInteractor {

    private final CommentRepository repo;
    private final PostCommentOutputBoundary presenter;

    public PostCommentInteractor(CommentRepository repo, PostCommentOutputBoundary presenter) {
        this.repo = repo;
        this.presenter = presenter;
    }

    public void execute(PostCommentInputData input) {

        Comment comment = new Comment(
                UUID.randomUUID().toString(),
                input.getMarketId(),
                input.getUsername(),
                input.getMessage(),
                LocalDateTime.now()
        );

        repo.save(comment);

        List<Comment> comments = repo.findByMarketId(input.getMarketId());

        presenter.present(new PostCommentOutputData(comments));
    }
}
