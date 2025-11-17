package stakemate.use_case.comments;

import stakemate.entity.Comment;
import stakemate.use_case.comments.CommentRepository;

import java.util.List;

public class ViewCommentsInteractor {

    private final CommentRepository repo;
    private final ViewCommentsOutputBoundary presenter;

    public ViewCommentsInteractor(CommentRepository repo, ViewCommentsOutputBoundary presenter) {
        this.repo = repo;
        this.presenter = presenter;
    }

    public void execute(String marketId) {
        List<Comment> comments = repo.findByMarketId(marketId);
        presenter.present(new ViewCommentsOutputData(comments));
    }
}
