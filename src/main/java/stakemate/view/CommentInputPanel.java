package stakemate.view;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A panel containing the comment input field and the Post button.
 */
public class CommentInputPanel extends JPanel {
    private final JTextField inputField;
    private final JButton postButton;

    public CommentInputPanel() {
        this.setLayout(new BorderLayout());
        inputField = new JTextField();
        postButton = new JButton("Post");
        this.add(inputField, BorderLayout.CENTER);
        this.add(postButton, BorderLayout.EAST);
    }

    public String getInputText() {
        return inputField.getText();
    }

    /**
     * Clears the text in the input field.
     */
    public void clearInput() {
        inputField.setText("");
    }

    /**
     * Adds an ActionListener to the Post button.
     *
     * @param listener the ActionListener to attach to the post button
     */
    public void addPostButtonListener(ActionListener listener) {
        postButton.addActionListener(listener);
    }
}
