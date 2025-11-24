package stakemate.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import stakemate.entity.Game;
import stakemate.interface_adapter.view_live.LiveMatchesController;

/**
 * Frame for displaying live matches.
 * Allows starting and stopping the live tracking.
 */
public class LiveMatchesFrame extends JFrame {

    private final JTable matchesTable;
    private final DefaultTableModel tableModel;
    private LiveMatchesController controller;

    public LiveMatchesFrame() {
        setTitle("StakeMate - Live Matches");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Table Setup
        final String[] columnNames = {"Date", "Sport", "Home Team", "Away Team", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0);
        matchesTable = new JTable(tableModel);
        add(new JScrollPane(matchesTable), BorderLayout.CENTER);

        // Auto-stop tracking on close
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(final java.awt.event.WindowEvent windowEvent) {
                if (controller != null) {
                    controller.stopTracking();
                }
            }
        });
    }

    public void setController(final LiveMatchesController controller) {
        this.controller = controller;
    }

    /**
     * Updates the table with the list of matches.
     *
     * @param matches The list of games to display.
     */
    public void updateMatches(final List<Game> matches) {
        tableModel.setRowCount(0); // Clear existing rows
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (final Game game : matches) {
            final Object[] row = {
                game.getGameTime().format(formatter),
                game.getSport(),
                game.getTeamA(),
                game.getTeamB(),
                game.getStatus()
            };
            tableModel.addRow(row);
        }
    }

    /**
     * Shows an error message dialog.
     *
     * @param message The error message.
     */
    public void showError(final String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
