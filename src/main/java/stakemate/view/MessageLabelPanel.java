package stakemate.view;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * A panel that displays temporary messages and clears them automatically after 3 seconds.
 */
public class MessageLabelPanel extends JPanel {
    private static final int CLEAR_DELAY_MS = 3000;
    private final JLabel label;

    public MessageLabelPanel() {
        this.setLayout(new BorderLayout());
        label = new JLabel(" ");
        label.setForeground(Color.BLUE);
        this.add(label, BorderLayout.CENTER);
    }

    /**
     * Displays a temporary message in the label and clears it automatically after a short delay.
     *
     * @param message the message to display
     */
    public void showMessage(String message) {
        label.setText(message);
        new Timer(CLEAR_DELAY_MS, event -> label.setText(" ")).start();
    }
}
