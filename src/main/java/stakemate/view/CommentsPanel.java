package stakemate.view;

import stakemate.entity.Comment;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import stakemate.interface_adapter.view_comments.PostCommentController;
import stakemate.interface_adapter.view_comments.ViewCommentsController;

/**
 * Swing panel for viewing and posting comments.
 */
public class CommentsPanel extends JPanel {

    private final DefaultListModel<String> commentListModel;
    private final JList<String> commentList;
    private final JTextField inputField;
    private final JButton postButton;
    private final JLabel messageLabel;

    // Keep a copy of Comment objects for reference
    private final List<Comment> comments;

    // Controllers (injected from MarketsFrame)
    private PostCommentController postController;
    private ViewCommentsController viewController;
    private MarketsFrame marketsFrame;

    public CommentsPanel() {
        this.comments = new ArrayList<>();
        this.setLayout(new BorderLayout());

        // Top: message label for success/failure
        messageLabel = new JLabel(" ");
        messageLabel.setForeground(Color.BLUE);
        this.add(messageLabel, BorderLayout.NORTH);

        // Center: scrollable list of comments
        commentListModel = new DefaultListModel<>();
        commentList = new JList<>(commentListModel);
        JScrollPane scrollPane = new JScrollPane(commentList);
        this.add(scrollPane, BorderLayout.CENTER);

        // Bottom: input field + post button
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        postButton = new JButton("Post");
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(postButton, BorderLayout.EAST);

        this.add(inputPanel, BorderLayout.SOUTH);
    }

    /**
     * Adds a comment to the list and updates the display
     */
    public void addComment(Comment comment) {
        comments.add(comment);
        commentListModel.addElement(formatComment(comment));
    }

    /**
     * Sets the entire comment list (e.g., when loading from DB)
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
     * Clears the input field
     */
    public void clearInput() {
        inputField.setText("");
    }

    /**
     * Displays a temporary message (success/failure)
     */
    public void showMessage(String message) {
        messageLabel.setText(message);
        // Optionally, reset after 3 seconds
        new Timer(3000, e -> messageLabel.setText(" ")).start();
    }

    /**
     * Returns the text currently in the input field
     */
    public String getInputText() {
        return inputField.getText();
    }

    /**
     * Adds an ActionListener to the post button
     */
    public void addPostButtonListener(ActionListener listener) {
        postButton.addActionListener(listener);
    }

    /** Formats comment for display */
    private String formatComment(Comment c) {
        return String.format("[%s] %s: %s",
                c.getTimestamp().toLocalTime().withNano(0),
                c.getUsername(),
                c.getMessage());
    }

    public void setControllers(PostCommentController postCtrl,
                               ViewCommentsController viewCtrl) {
        this.postController = postCtrl;
        this.viewController = viewCtrl;

        // Hook Post button
        addPostButtonListener(e -> {
            String text = getInputText();
            if (text == null || text.isBlank()) {
                showMessage("Comment cannot be empty.");
                return;
            }

            if (marketsFrame == null) {
                showMessage("MarketsFrame not set.");
                return;
            }

            String marketId = null;
            if (marketsFrame.getCurrentlySelectedMarket() != null) {
                marketId = marketsFrame.getCurrentlySelectedMarket().getId();
            }

            String username = marketsFrame.getCurrentUser();

            if (marketId == null || username == null) {
                showMessage("Select a market and log in first.");
                return;
            }

            postController.postComment(marketId, username, text);
            clearInput();
        });
    }

    public void setMarketsFrame(MarketsFrame frame) {
        this.marketsFrame = frame;
    }
}
