package stakemate.use_case.comments.post;

import java.time.LocalDateTime;

import stakemate.entity.Comment;
import stakemate.use_case.comments.CommentRepository;

public class PostCommentInteractor implements PostCommentInputBoundary {

    private final CommentRepository repository;
    private final PostCommentOutputBoundary presenter;

    public PostCommentInteractor(CommentRepository repository,
                                 PostCommentOutputBoundary presenter) {
        this.repository = repository;
        this.presenter = presenter;
    }

    @Override
    public void execute(PostCommentInputData inputData) {

        final String id = java.util.UUID.randomUUID().toString();

        final Comment comment = new Comment(
            id,
            inputData.getMarketId(),
            inputData.getUsername(),
            inputData.getCommentText(),
            LocalDateTime.now()
        );

        repository.saveComment(comment);

        presenter.present(
            new PostCommentOutputData(
                true,
                "Comment posted.",
                inputData.getMarketId()
            )
        );
    }
}
