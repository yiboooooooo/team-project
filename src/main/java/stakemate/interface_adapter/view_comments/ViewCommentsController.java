package stakemate.interface_adapter.view_comments;

import stakemate.use_case.comments.view.ViewCommentsInputBoundary;
import stakemate.use_case.comments.view.ViewCommentsInputData;

public class ViewCommentsController {

    private final ViewCommentsInputBoundary interactor;

    public ViewCommentsController(ViewCommentsInputBoundary interactor) {
        this.interactor = interactor;
    }

    /**
     * Requests all comments for the given market.
     *
     * @param marketId the ID of the market whose comments should be fetched
     */
    public void fetchComments(String marketId) {
        final ViewCommentsInputData inputData = new ViewCommentsInputData(marketId);
        interactor.execute(inputData);
    }
}
