package stakemate.interface_adapter.view_comments;

import stakemate.use_case.comments.post.PostCommentOutputBoundary;
import stakemate.use_case.comments.post.PostCommentOutputData;
import stakemate.view.CommentsPanel;

import javax.swing.*;

public class SwingPostCommentPresenter implements PostCommentOutputBoundary {

    private final CommentsPanel commentsPanel;

    public SwingPostCommentPresenter(CommentsPanel commentsPanel) {
        this.commentsPanel = commentsPanel;
    }

    @Override
    public void present(PostCommentOutputData outputData) {
        SwingUtilities.invokeLater(() -> {
            if (outputData.isSuccess()) {
                // Optionally clear the input box
                commentsPanel.clearInput();
                // Refresh the list after posting
                commentsPanel.showMessage(outputData.getMessage());
            } else {
                commentsPanel.showMessage("Failed: " + outputData.getMessage());
            }
        });
    }
}
