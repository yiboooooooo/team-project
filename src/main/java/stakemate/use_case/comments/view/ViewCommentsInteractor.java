package stakemate.use_case.comments.view;

import stakemate.use_case.comments.CommentRepository;

public class ViewCommentsInteractor implements ViewCommentsInputBoundary {

    private final CommentRepository repository;
    private final ViewCommentsOutputBoundary presenter;

    public ViewCommentsInteractor(CommentRepository repository,
                                  ViewCommentsOutputBoundary presenter) {
        this.repository = repository;
        this.presenter = presenter;
    }

    @Override
    public void execute(ViewCommentsInputData inputData) {
        presenter.present(
                new ViewCommentsOutputData(
                        repository.getCommentsForMarket(inputData.getMarketId())
                )
        );
    }
}
