package stakemate.view;

import stakemate.entity.Comment;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

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
}
