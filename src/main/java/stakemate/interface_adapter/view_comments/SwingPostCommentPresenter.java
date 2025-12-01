package stakemate.interface_adapter.view_comments;

import javax.swing.SwingUtilities;

import stakemate.use_case.comments.post.PostCommentOutputBoundary;
import stakemate.use_case.comments.post.PostCommentOutputData;
import stakemate.view.CommentsPanel;

public class SwingPostCommentPresenter implements PostCommentOutputBoundary {

    private final CommentsPanel commentsPanel;
    private final stakemate.interface_adapter.view_comments.ViewCommentsController viewCommentsController;

    public SwingPostCommentPresenter(
        CommentsPanel commentsPanel,
        stakemate.interface_adapter.view_comments.ViewCommentsController viewCommentsController) {
        this.commentsPanel = commentsPanel;
        this.viewCommentsController = viewCommentsController;
    }

    @Override
    public void present(PostCommentOutputData outputData) {
        SwingUtilities.invokeLater(() -> handlePresentation(outputData));
    }

    private void handlePresentation(PostCommentOutputData outputData) {
        if (outputData.isSuccess()) {
            commentsPanel.clearInput();
            commentsPanel.showMessage(outputData.getMessage());

            // AUTO REFRESH COMMENTS
            viewCommentsController.fetchComments(outputData.getMarketId());
        }
        else {
            commentsPanel.showMessage("Failed: " + outputData.getMessage());
        }
    }
}
