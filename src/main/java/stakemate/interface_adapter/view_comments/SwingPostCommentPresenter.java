package stakemate.interface_adapter.view_comments;

import stakemate.use_case.comments.post.PostCommentOutputBoundary;
import stakemate.use_case.comments.post.PostCommentOutputData;
import stakemate.view.CommentsPanel;
import stakemate.interface_adapter.view_comments.ViewCommentsController;

import javax.swing.*;

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
        SwingUtilities.invokeLater(() -> {
            if (outputData.isSuccess()) {
                commentsPanel.clearInput();
                commentsPanel.showMessage(outputData.getMessage());

                // >>> AUTO REFRESH COMMENTS
                viewCommentsController.fetchComments(outputData.getMarketId());

            } else {
                commentsPanel.showMessage("Failed: " + outputData.getMessage());
            }
        });
    }
}
