package stakemate.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;

import stakemate.app.StakeMateApp;
import stakemate.entity.OrderBook;
import stakemate.entity.OrderBookEntry;
import stakemate.interface_adapter.controllers.SettleMarketController;
import stakemate.interface_adapter.view_market.MarketsView;
import stakemate.interface_adapter.view_market.SettleMarketView;
import stakemate.interface_adapter.view_market.ViewMarketController;
import stakemate.use_case.view_market.MarketSummary;
import stakemate.use_case.view_market.MarketsResponseModel;
import stakemate.use_case.view_market.MatchSummary;
import stakemate.use_case.view_market.OrderBookResponseModel;

public class MarketsFrame extends JFrame implements MarketsView, SettleMarketView {

    private final DefaultListModel<MatchSummary> matchesListModel = new DefaultListModel<>();
    private final JList<MatchSummary> matchesList = new JList<>(matchesListModel);
    private final JLabel matchesEmptyLabel = new JLabel(" ");
    private final DefaultListModel<MarketSummary> marketsListModel = new DefaultListModel<>();
    private final JList<MarketSummary> marketsList = new JList<>(marketsListModel);
    private final JLabel marketsEmptyLabel = new JLabel(" ");
    private final OrderBookTableModel orderBookTableModel = new OrderBookTableModel();
    private final JTable orderBookTable = new JTable(orderBookTableModel);
    private final JLabel orderBookEmptyLabel = new JLabel("Select a market to see orders.");
    private final JLabel statusLabel = new JLabel(" ");
    private final JButton buyButton = new JButton("Buy");
    private final JButton sellButton = new JButton("Sell");
    private final JButton refreshButton = new JButton("Refresh");
    private final JButton myProfileButton = new JButton("My Profile");
    private final JButton settleButton = new JButton("Settle");
    private final CommentsPanel commentsPanel = new CommentsPanel();
    private ViewMarketController controller;
    private SettleMarketController settleMarketController;
    private MarketSummary currentlySelectedMarket;
    private ProfileFrame profileFrame;
    private stakemate.interface_adapter.view_profile.ViewProfileController profileController;
    private String currentUser;

    public MarketsFrame() {
        super("StakeMate - Markets & Order Book");
        initUi();
    }

    public void setController(final ViewMarketController controller) {
        this.controller = controller;
        hookEvents();
    }

    public void setSettleMarketController(final SettleMarketController controller) {
        this.settleMarketController = controller;
    }

    public void setProfileFrame(final ProfileFrame profileFrame) {
        this.profileFrame = profileFrame;
    }

    public void setProfileController(final stakemate.interface_adapter.view_profile.ViewProfileController profileController) {
        this.profileController = profileController;
    }

    public void setLoggedInUser(final String username) {
        this.currentUser = username;
    }

    private void initUi() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 600);
        setLocationRelativeTo(null);

        final JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        final JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topBar.add(myProfileButton);
        topBar.add(refreshButton);

        matchesEmptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        marketsEmptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        orderBookEmptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        final JPanel leftPanel = new JPanel(new BorderLayout(4, 4));
        final JLabel matchesLabel = new JLabel("Matches");
        leftPanel.add(matchesLabel, BorderLayout.NORTH);
        matchesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        leftPanel.add(new JScrollPane(matchesList), BorderLayout.CENTER);
        leftPanel.add(matchesEmptyLabel, BorderLayout.SOUTH);
        final JPanel marketsPanel = new JPanel(new BorderLayout(4, 4));
        final JLabel marketsLabel = new JLabel("Markets");
        marketsPanel.add(marketsLabel, BorderLayout.NORTH);
        marketsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        marketsPanel.add(new JScrollPane(marketsList), BorderLayout.CENTER);
        marketsPanel.add(marketsEmptyLabel, BorderLayout.SOUTH);
        final JPanel orderBookPanel = new JPanel(new BorderLayout(4, 4));
        final JLabel orderBookLabel = new JLabel("Order Book");
        orderBookPanel.add(orderBookLabel, BorderLayout.NORTH);
        orderBookTable.setFillsViewportHeight(true);
        orderBookPanel.add(new JScrollPane(orderBookTable), BorderLayout.CENTER);
        orderBookPanel.add(orderBookEmptyLabel, BorderLayout.SOUTH);

        final JPanel buySellPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buyButton.setEnabled(false);
        sellButton.setEnabled(false);
        settleButton.setEnabled(false);
        buySellPanel.add(buyButton);
        buySellPanel.add(sellButton);
        buySellPanel.add(settleButton);

        final JPanel rightPanel = new JPanel(new BorderLayout(4, 4));
        rightPanel.add(marketsPanel, BorderLayout.NORTH);
        rightPanel.add(orderBookPanel, BorderLayout.CENTER);
        rightPanel.add(buySellPanel, BorderLayout.SOUTH);

        final JSplitPane splitPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            leftPanel,
            rightPanel);
        splitPane.setDividerLocation(280);

        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);

        root.add(topBar, BorderLayout.NORTH);
        root.add(splitPane, BorderLayout.CENTER);
        root.add(statusLabel, BorderLayout.SOUTH);
        // Add comments panel to the right side
        root.add(commentsPanel, BorderLayout.EAST);

        setContentPane(root);
    }

    private void hookEvents() {
        refreshButton.addActionListener(e -> {
            if (controller != null) {
                controller.refreshWithApi();
            }
        });

        myProfileButton.addActionListener(e -> {
            if (profileFrame != null) {
                if (currentUser != null && profileController != null) {
                    profileController.execute(currentUser);
                }
                profileFrame.setVisible(true);
            }
            else {
                JOptionPane.showMessageDialog(this, "Profile frame not connected.");
            }
        });

        matchesList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && controller != null) {
                final MatchSummary selected = matchesList.getSelectedValue();
                currentlySelectedMarket = null;
                controller.onMatchSelected(selected);
            }
        });

        marketsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && controller != null) {
                final MarketSummary selected = marketsList.getSelectedValue();
                currentlySelectedMarket = selected;
                controller.onMarketSelected(selected);
            }
        });

        buyButton.addActionListener(e -> JOptionPane.showMessageDialog(
            MarketsFrame.this,
            "Buy clicked. (Use Case 4 will handle order placement.)"));

        sellButton.addActionListener(e -> JOptionPane.showMessageDialog(
            MarketsFrame.this,
            "Sell clicked. (Use Case 4 will handle order placement.)"));
        settleButton.addActionListener(e -> {
            if (settleMarketController == null || currentlySelectedMarket == null) {
                return;
            }

            final int choice = JOptionPane.showConfirmDialog(
                MarketsFrame.this,
                "Did the HOME team win this market?",
                "Settle Market",
                JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION || choice == JOptionPane.NO_OPTION) {
                // Removed extra boolean argument 'homeTeamWon' to match controller signature
                settleMarketController.settleMarket(currentlySelectedMarket.getId());
            }
        });
    }

    // ---- MarketsView implementation ----

    @Override
    public void showMatches(final List<MatchSummary> matches, final String emptyStateMessage) {
        matchesListModel.clear();
        if (matches == null || matches.isEmpty()) {
            matchesEmptyLabel.setText(
                emptyStateMessage != null ? emptyStateMessage : "No matches.");
        }
        else {
            for (final MatchSummary m : matches) {
                matchesListModel.addElement(m);
            }
            matchesEmptyLabel.setText(" ");
        }
    }

    @Override
    public void showMarketsForMatch(final MarketsResponseModel responseModel) {
        setTitle("StakeMate - " + responseModel.getMatchTitle());

        marketsListModel.clear();
        currentlySelectedMarket = null;
        if (responseModel.getMarkets() == null || responseModel.getMarkets().isEmpty()) {
            marketsEmptyLabel.setText(
                responseModel.getEmptyStateMessage() != null
                    ? responseModel.getEmptyStateMessage()
                    : "No markets for this match.");
        }
        else {
            for (final MarketSummary m : responseModel.getMarkets()) {
                marketsListModel.addElement(m);
            }
            marketsEmptyLabel.setText(" ");
        }

        // Clear order book when switching match
        orderBookTableModel.clear();
        orderBookEmptyLabel.setText("Select a market to see orders.");
        buyButton.setEnabled(false);
        sellButton.setEnabled(false);
        settleButton.setEnabled(false);
    }

    @Override
    public void showOrderBook(final OrderBookResponseModel responseModel) {
        if (responseModel.getOrderBook() != null) {
            orderBookTableModel.setOrderBook(responseModel.getOrderBook());
        }

        if (responseModel.isReconnecting()) {
            statusLabel.setText(responseModel.getMessage() != null
                ? responseModel.getMessage()
                : "Reconnecting...");
        }
        else if (responseModel.getMessage() != null) {
            statusLabel.setText(responseModel.getMessage());
        }
        else {
            statusLabel.setText(" ");
        }

        if (responseModel.isEmpty()) {
            orderBookEmptyLabel.setText(
                responseModel.getMessage() != null
                    ? responseModel.getMessage()
                    : "No orders yet");
        }
        else if (!responseModel.isReconnecting()) {
            orderBookEmptyLabel.setText(" ");
        }

        final boolean enableBuySell = currentlySelectedMarket != null
            && currentlySelectedMarket.isBuySellEnabled()
            && !responseModel.isReconnecting();

        buyButton.setEnabled(enableBuySell);
        sellButton.setEnabled(enableBuySell);
        settleButton.setEnabled(enableBuySell);
    }

    @Override
    public void showError(final String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ---- SettleMarketView (UC6) ----

    @Override
    public void showSettlementResult(final String message) {
        statusLabel.setText(message);
        String alice = "";
        String bob = "";
        String you = "";

        try {
            // FIXED: Use getter method instead of direct access
            alice = "Alice: " + StakeMateApp.getAccountRepo()
                .findByUsername("alice").getBalance();
            bob = "Bob:   " + StakeMateApp.getAccountRepo()
                .findByUsername("bob").getBalance();
            you = "You (ryth): " + StakeMateApp.getAccountRepo()
                .findByUsername("ryth").getBalance();
        }
        catch (final Exception ignored) {
        }

        JOptionPane.showMessageDialog(
            this,
            message + "\n\nAccount Balances:\n" + alice + "\n" + bob + "\n" + you,
            "Settlement Complete",
            JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void showSettlementError(final String errorMessage) {
        JOptionPane.showMessageDialog(this, errorMessage,
            "Settlement Error", JOptionPane.ERROR_MESSAGE);
    }

    public CommentsPanel getCommentsPanel() {
        return commentsPanel;
    }

    // ---- Order Book Table Model ----

    private static final class OrderBookTableModel extends AbstractTableModel {

        private static final String[] COLUMNS = {
            "Bid Size", "Bid Price", "Ask Price", "Ask Size"
        };

        private final List<Row> rows = new ArrayList<>();

        public void setOrderBook(final OrderBook orderBook) {
            rows.clear();

            final List<OrderBookEntry> bids = new ArrayList<>(orderBook.getBids());
            final List<OrderBookEntry> asks = new ArrayList<>(orderBook.getAsks());

            bids.sort(Comparator.comparingDouble(OrderBookEntry::getPrice).reversed());
            asks.sort(Comparator.comparingDouble(OrderBookEntry::getPrice));

            final int max = Math.max(bids.size(), asks.size());
            for (int i = 0; i < max; i++) {
                final OrderBookEntry bid = i < bids.size() ? bids.get(i) : null;
                final OrderBookEntry ask = i < asks.size() ? asks.get(i) : null;
                rows.add(new Row(bid, ask));
            }

            fireTableDataChanged();
        }

        public void clear() {
            rows.clear();
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public String getColumnName(final int column) {
            return COLUMNS[column];
        }

        @Override
        public Object getValueAt(final int rowIndex, final int columnIndex) {
            final Row row = rows.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return row.bidQty;
                case 1:
                    return row.bidPrice;
                case 2:
                    return row.askPrice;
                case 3:
                    return row.askQty;
                default:
                    return "";
            }
        }

        private static final class Row {
            final Double bidQty;
            final Double bidPrice;
            final Double askPrice;
            final Double askQty;

            Row(final OrderBookEntry bid, final OrderBookEntry ask) {
                this.bidQty = bid != null ? bid.getQuantity() : null;
                this.bidPrice = bid != null ? bid.getPrice() : null;
                this.askPrice = ask != null ? ask.getPrice() : null;
                this.askQty = ask != null ? ask.getQuantity() : null;
            }
        }
    }
}
