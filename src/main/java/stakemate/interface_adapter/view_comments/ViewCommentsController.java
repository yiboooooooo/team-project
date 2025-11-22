package stakemate.interface_adapter.view_comments;

import stakemate.use_case.comments.view.ViewCommentsInputData;
import stakemate.use_case.comments.view.ViewCommentsInputBoundary;

public class ViewCommentsController {

    private final ViewCommentsInputBoundary interactor;

    public ViewCommentsController(ViewCommentsInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void fetchComments(String marketId) {
        ViewCommentsInputData inputData = new ViewCommentsInputData(marketId);
        interactor.execute(inputData);
    }
}
