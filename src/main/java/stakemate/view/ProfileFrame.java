package stakemate.view;

import stakemate.interface_adapter.view_profile.ProfileState;
import stakemate.interface_adapter.view_profile.ProfileViewModel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

public class ProfileFrame extends JFrame implements PropertyChangeListener {

    private final JButton backButton = new JButton("Back");

    private final JLabel usernameLabel = new JLabel("Username: -");
    private final JLabel balanceLabel = new JLabel("Balance: -");
    private final JLabel pnlLabel = new JLabel("PnL: 0");

    // Open Positions Table
    private final String[] openColumns = {
            "Market Name", "Team", "Buy Price", "Size", "Buy Amt ($)", "Profit if Won ($)"
    };
    private final DefaultTableModel openModel = new DefaultTableModel(openColumns, 0);
    private final JTable openTable = new JTable(openModel);

    // Historical Positions Table
    private final String[] historyColumns = {
            "Market Name", "Team", "Buy Price", "Size", "Profit ($)"
    };
    private final DefaultTableModel historyModel = new DefaultTableModel(historyColumns, 0);
    private final JTable historyTable = new JTable(historyModel);

    // Sorting Buttons (UI only for now)
    private final JButton sortOpenDateBtn = new JButton("Sort by Date");
    private final JButton sortOpenSizeBtn = new JButton("Sort by Size");
    private final JButton sortHistoryDateBtn = new JButton("Sort by Date");
    private final JButton sortHistorySizeBtn = new JButton("Sort by Size");

    private ProfileViewModel viewModel;

    public ProfileFrame() {
        super("StakeMate - My Profile");
        initUi();
        hookEvents();
    }

    public void setViewModel(ProfileViewModel viewModel) {
        this.viewModel = viewModel;
        this.viewModel.addPropertyChangeListener(this);
    }

    private void initUi() {
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- TOP: About Me ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        topPanel.setBorder(BorderFactory.createTitledBorder("About Me"));

        // Username and Balance
        JPanel leftInfo = new JPanel(new GridLayout(2, 1));
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        balanceLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        leftInfo.add(usernameLabel);
        leftInfo.add(balanceLabel);

        // PnL (positioned to the right but not far right)
        JPanel rightInfo = new JPanel(new GridLayout(1, 1));
        pnlLabel.setFont(new Font("Arial", Font.BOLD, 16));
        rightInfo.add(pnlLabel);

        topPanel.add(leftInfo);
        topPanel.add(rightInfo);

        root.add(topPanel, BorderLayout.NORTH);

        // --- CENTER: Tables ---
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 10));

        // 1. Open Positions
        JPanel openPanel = new JPanel(new BorderLayout());
        openPanel.setBorder(BorderFactory.createTitledBorder("Open Positions"));

        JPanel openSortPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        openSortPanel.add(new JLabel("Sort by: "));
        openSortPanel.add(sortOpenDateBtn);
        openSortPanel.add(sortOpenSizeBtn);

        openPanel.add(openSortPanel, BorderLayout.NORTH);
        openPanel.add(new JScrollPane(openTable), BorderLayout.CENTER);

        // 2. Historical Positions
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBorder(BorderFactory.createTitledBorder("Historical Positions"));

        JPanel historySortPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        historySortPanel.add(new JLabel("Sort by: "));
        historySortPanel.add(sortHistoryDateBtn);
        historySortPanel.add(sortHistorySizeBtn);

        historyPanel.add(historySortPanel, BorderLayout.NORTH);
        historyPanel.add(new JScrollPane(historyTable), BorderLayout.CENTER);

        centerPanel.add(openPanel);
        centerPanel.add(historyPanel);

        root.add(centerPanel, BorderLayout.CENTER);

        // --- BOTTOM: Back Button ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(backButton);
        root.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void hookEvents() {
        backButton.addActionListener(e -> setVisible(false));

        // Placeholder listeners for sort buttons
        sortOpenDateBtn
                .addActionListener(e -> JOptionPane.showMessageDialog(this, "Sort by Date not implemented yet."));
        sortOpenSizeBtn
                .addActionListener(e -> JOptionPane.showMessageDialog(this, "Sort by Size not implemented yet."));
        sortHistoryDateBtn
                .addActionListener(e -> JOptionPane.showMessageDialog(this, "Sort by Date not implemented yet."));
        sortHistorySizeBtn
                .addActionListener(e -> JOptionPane.showMessageDialog(this, "Sort by Size not implemented yet."));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        ProfileState state = (ProfileState) evt.getNewValue();
        if (state.getError() != null) {
            JOptionPane.showMessageDialog(this, state.getError(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        usernameLabel.setText("Username: " + state.getUsername());
        balanceLabel.setText("Balance: " + state.getBalance());
        pnlLabel.setText("PnL: " + state.getPnl());

        updateTable(openModel, state.getOpenPositions());
        updateTable(historyModel, state.getHistoricalPositions());
    }

    private void updateTable(DefaultTableModel model, List<String[]> data) {
        model.setRowCount(0); // clear existing
        if (data != null) {
            for (String[] row : data) {
                model.addRow(row);
            }
        }
    }
}
