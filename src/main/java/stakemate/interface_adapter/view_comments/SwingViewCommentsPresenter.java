package stakemate.interface_adapter.view_comments;

import stakemate.entity.Comment;
import stakemate.use_case.comments.view.ViewCommentsOutputBoundary;
import stakemate.use_case.comments.view.ViewCommentsOutputData;
import stakemate.view.CommentsPanel;

import javax.swing.*;
import java.util.List;

public class SwingViewCommentsPresenter implements ViewCommentsOutputBoundary {

    private final CommentsPanel commentsPanel;

    public SwingViewCommentsPresenter(CommentsPanel commentsPanel) {
        this.commentsPanel = commentsPanel;
    }

    @Override
    public void present(ViewCommentsOutputData outputData) {
        SwingUtilities.invokeLater(() -> {
            List<Comment> comments = outputData.getComments();
            commentsPanel.setComments(comments);
        });
    }
}
