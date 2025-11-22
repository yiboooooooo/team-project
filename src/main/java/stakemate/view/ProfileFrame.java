package stakemate.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import stakemate.interface_adapter.view_profile.ProfileState;
import stakemate.interface_adapter.view_profile.ProfileViewModel;

public class ProfileFrame extends JFrame implements PropertyChangeListener {

    private final JButton backButton = new JButton("Back");

    private final JLabel usernameLabel = new JLabel("Username: -");
    private final JLabel balanceLabel = new JLabel("Balance: -");
    private final JLabel pnlLabel = new JLabel("PnL: 0");
    private final String[] openColumns = {
        "Market Name", "Team", "Buy Price", "Size", "Buy Amt ($)", "Profit if Won ($)"
    };
    private final DefaultTableModel openModel = new DefaultTableModel(openColumns, 0);
    private final JTable openTable = new JTable(openModel);
    private final String[] historyColumns = {
        "Market Name", "Team", "Buy Price", "Size", "Profit ($)"
    };
    private final DefaultTableModel historyModel = new DefaultTableModel(historyColumns, 0);
    private final JTable historyTable = new JTable(historyModel);
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

    public void setViewModel(final ProfileViewModel viewModel) {
        this.viewModel = viewModel;
        this.viewModel.addPropertyChangeListener(this);
    }

    private void initUi() {
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        final JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        final JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        topPanel.setBorder(BorderFactory.createTitledBorder("About Me"));
        final JPanel leftInfo = new JPanel(new GridLayout(2, 1));
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        balanceLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        leftInfo.add(usernameLabel);
        leftInfo.add(balanceLabel);
        final JPanel rightInfo = new JPanel(new GridLayout(1, 1));
        pnlLabel.setFont(new Font("Arial", Font.BOLD, 16));
        rightInfo.add(pnlLabel);

        topPanel.add(leftInfo);
        topPanel.add(rightInfo);

        root.add(topPanel, BorderLayout.NORTH);
        final JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        final JPanel openPanel = new JPanel(new BorderLayout());
        openPanel.setBorder(BorderFactory.createTitledBorder("Open Positions"));

        final JPanel openSortPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        openSortPanel.add(new JLabel("Sort by: "));
        openSortPanel.add(sortOpenDateBtn);
        openSortPanel.add(sortOpenSizeBtn);

        openPanel.add(openSortPanel, BorderLayout.NORTH);
        openPanel.add(new JScrollPane(openTable), BorderLayout.CENTER);
        final JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBorder(BorderFactory.createTitledBorder("Historical Positions"));

        final JPanel historySortPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        historySortPanel.add(new JLabel("Sort by: "));
        historySortPanel.add(sortHistoryDateBtn);
        historySortPanel.add(sortHistorySizeBtn);

        historyPanel.add(historySortPanel, BorderLayout.NORTH);
        historyPanel.add(new JScrollPane(historyTable), BorderLayout.CENTER);

        centerPanel.add(openPanel);
        centerPanel.add(historyPanel);

        root.add(centerPanel, BorderLayout.CENTER);
        final JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(backButton);
        root.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void hookEvents() {
        backButton.addActionListener(e -> setVisible(false));
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
    public void propertyChange(final PropertyChangeEvent evt) {
        final ProfileState state = (ProfileState) evt.getNewValue();
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

    private void updateTable(final DefaultTableModel model, final List<String[]> data) {
        model.setRowCount(0);
        if (data != null) {
            for (final String[] row : data) {
                model.addRow(row);
            }
        }
    }
}
