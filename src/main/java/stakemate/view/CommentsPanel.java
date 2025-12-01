package stakemate.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.Timer;

import stakemate.entity.Comment;
import stakemate.interface_adapter.view_comments.PostCommentController;
import stakemate.interface_adapter.view_comments.ViewCommentsController;

/**
 * Swing panel for viewing and posting comments.
 */
public class CommentsPanel extends JPanel {

    private static final int MESSAGE_CLEAR_DELAY_MS = 3000;

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
        final JScrollPane scrollPane = new JScrollPane(commentList);
        this.add(scrollPane, BorderLayout.CENTER);

        // Bottom: input field + post button
        final JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        postButton = new JButton("Post");
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(postButton, BorderLayout.EAST);

        this.add(inputPanel, BorderLayout.SOUTH);
    }

    /**
     * Adds a comment to the list and updates the display.
     *
     * @param comment the Comment object to add to the list
     */
    public void addComment(Comment comment) {
        comments.add(comment);
        commentListModel.addElement(formatComment(comment));
    }

    /**
     * Sets the entire comment list and updates the display.
     * Typically used when loading comments from the database.
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
     * Clears the input field.
     */
    public void clearInput() {
        inputField.setText("");
    }

    /**
     * Displays a temporary message in the message label.
     * The message will be cleared automatically after 3 seconds.
     *
     * @param message the message to display
     */
    public void showMessage(String message) {
        messageLabel.setText(message);
        // Optionally, reset after 3 seconds
        new Timer(MESSAGE_CLEAR_DELAY_MS, event -> messageLabel.setText(" ")).start();
    }

    /**
     * Returns the current text in the input field.
     *
     * @return the text currently entered by the user
     */
    public String getInputText() {
        return inputField.getText();
    }

    /**
     * Adds an {@link ActionListener} to the post button.
     *
     * @param listener the ActionListener to attach to the post button
     */
    public void addPostButtonListener(ActionListener listener) {
        postButton.addActionListener(listener);
    }

    /**
     * Formats a Comment object for display in the JList.
     *
     * @param comment the Comment object to format
     * @return a formatted string representing the comment
     */
    private String formatComment(Comment comment) {
        return String.format("[%s] %s: %s",
            comment.getTimestamp().toLocalTime().withNano(0),
            comment.getUsername(),
            comment.getMessage());
    }

    /**
     * Sets the controllers for handling posting and viewing comments,
     * and hooks the post button to send comments through the PostCommentController.
     *
     * @param postCtrl the controller responsible for posting comments
     * @param viewCtrl the controller responsible for viewing comments
     */
    public void setControllers(PostCommentController postCtrl,
                               ViewCommentsController viewCtrl) {
        this.postController = postCtrl;
        this.viewController = viewCtrl;

        // Hook Post button
        addPostButtonListener(this::handlePostButtonClick);
    }

    /**
     * Handles the Post button click.
     *
     * @param event the action event
     */
    private void handlePostButtonClick(java.awt.event.ActionEvent event) {
        final String text = getInputText();
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
                clearInput();
            }
        }
    }

    public void setMarketsFrame(MarketsFrame frame) {
        this.marketsFrame = frame;
    }
}
