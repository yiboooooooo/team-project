package stakemate.view;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import stakemate.entity.Comment;
import stakemate.interface_adapter.view_comments.PostCommentController;
import stakemate.interface_adapter.view_comments.ViewCommentsController;

/**
 * Swing panel for viewing and posting comments.
 */
public class CommentsPanel extends JPanel {

    private final DefaultListModel<String> commentListModel;
    private final JList<String> commentList;
    private final List<Comment> comments;

    private PostCommentController postController;
    private ViewCommentsController viewController;
    private MarketsFrame marketsFrame;

    private final CommentInputPanel inputPanel;
    private final MessageLabelPanel messagePanel;

    public CommentsPanel() {
        this.comments = new ArrayList<>();
        this.setLayout(new BorderLayout());

        // Message panel (top)
        messagePanel = new MessageLabelPanel();
        this.add(messagePanel, BorderLayout.NORTH);

        // Comment list (center)
        commentListModel = new DefaultListModel<>();
        commentList = new JList<>(commentListModel);
        this.add(new JScrollPane(commentList), BorderLayout.CENTER);

        // Input panel (bottom)
        inputPanel = new CommentInputPanel();
        this.add(inputPanel, BorderLayout.SOUTH);
    }

    /**
     * Adds a single comment to the panel and updates the display.
     *
     * @param comment the Comment object to add
     */
    public void addComment(Comment comment) {
        comments.add(comment);
        commentListModel.addElement(formatComment(comment));
    }

    /**
     * Replaces the current list of comments with the provided list and updates the display.
     *
     * @param comments the list of Comment objects to display
     */
    public void setComments(List<Comment> comments) {
        this.comments.clear();
        this.comments.addAll(comments);
        commentListModel.clear();
        for (Comment c : comments) {
            commentListModel.addElement(formatComment(c));
        }
    }

    /**
     * Displays a temporary message in the message label panel.
     * The message will automatically be cleared after a short delay.
     *
     * @param message the message to display
     */
    public void showMessage(String message) {
        messagePanel.showMessage(message);
    }

    /**
     * Sets the controllers responsible for posting and viewing comments.
     * Also hooks the Post button to send comments through the PostCommentController.
     *
     * @param postCtrl the controller responsible for posting comments
     * @param viewCtrl the controller responsible for viewing comments
     */
    public void setControllers(PostCommentController postCtrl, ViewCommentsController viewCtrl) {
        this.postController = postCtrl;
        this.viewController = viewCtrl;
        inputPanel.addPostButtonListener(this::handlePostButtonClick);
    }

    private void handlePostButtonClick(java.awt.event.ActionEvent event) {
        final String text = inputPanel.getInputText();
        if (text == null || text.isBlank()) {
            showMessage("Comment cannot be empty.");
        }
        else if (marketsFrame == null) {
            showMessage("MarketsFrame not set.");
        }
        else {
            String marketId = null;
            if (marketsFrame.getCurrentlySelectedMarket() != null) {
                marketId = marketsFrame.getCurrentlySelectedMarket().getId();
            }

            final String username = marketsFrame.getCurrentUser();

            if (marketId == null || username == null) {
                showMessage("Select a market and log in first.");
            }
            else {
                postController.postComment(marketId, username, text);
                inputPanel.clearInput();
            }
        }
    }

    public void setMarketsFrame(MarketsFrame frame) {
        this.marketsFrame = frame;
    }

    private String formatComment(Comment comment) {
        return String.format("[%s] %s: %s",
            comment.getTimestamp().toLocalTime().withNano(0),
            comment.getUsername(),
            comment.getMessage());
    }

    /**
     * Clears the comment input field.
     */
    public void clearInput() {
        inputPanel.clearInput();
    }
}
