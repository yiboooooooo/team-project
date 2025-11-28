package stakemate.view;

import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.Timer;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import stakemate.app.StakeMateApp;
import stakemate.entity.OrderBook;
import stakemate.entity.OrderBookEntry;
import stakemate.interface_adapter.controllers.SettleMarketController;
import stakemate.interface_adapter.view_comments.PostCommentController;
import stakemate.interface_adapter.view_market.MarketsView;
import stakemate.interface_adapter.view_market.SettleMarketView;
import stakemate.interface_adapter.view_market.ViewMarketController;
import stakemate.use_case.view_market.MarketSummary;
import stakemate.use_case.view_market.MarketsResponseModel;
import stakemate.use_case.view_market.MatchSummary;
import stakemate.use_case.view_market.OrderBookResponseModel;

/**
 * The main frame for viewing markets and order books.
 */
// -@cs[ClassDataAbstractionCoupling] Swing UI classes require many dependencies to function.
// -@cs[ClassFanOutComplexity] View layer inevitably depends on many Swing and App components.
public class MarketsFrame extends JFrame implements MarketsView, SettleMarketView {

    private static final String EMPTY_TEXT = " ";
    private static final String ERROR_TITLE = "Error";
    private static final int WINDOW_WIDTH = 1000;
    private static final int WINDOW_HEIGHT = 600;
    private static final int BORDER_PADDING = 8;
    private static final int GAP = 4;
    private static final int DIVIDER_LOC = 280;
    private static final int REFRESH_INTERVAL = 5000;
    private static final int DARK_GREEN_G = 100;

    // Table Column Constants
    private static final int COL_BID_QTY = 0;
    private static final int COL_BID_PRICE = 1;
    private static final int COL_ASK_PRICE = 2;
    private static final int COL_ASK_QTY = 3;

    private final DefaultListModel<MatchSummary> matchesListModel = new DefaultListModel<>();
    private final JList<MatchSummary> matchesList = new JList<>(matchesListModel);
    private final JLabel matchesEmptyLabel = new JLabel(EMPTY_TEXT);

    private final DefaultListModel<MarketSummary> marketsListModel = new DefaultListModel<>();
    private final JList<MarketSummary> marketsList = new JList<>(marketsListModel);
    private final JLabel marketsEmptyLabel = new JLabel(EMPTY_TEXT);

    private final OrderBookTableModel orderBookTableModel = new OrderBookTableModel();
    private final JTable orderBookTable = new JTable(orderBookTableModel);
    private final JLabel orderBookEmptyLabel = new JLabel("Select a market to see orders.");

    private final JLabel statusLabel = new JLabel(EMPTY_TEXT);
    private final JButton buyButton = new JButton("Buy");
    private final JButton sellButton = new JButton("Sell");
    private final JButton myProfileButton = new JButton("My Profile");
    private final JButton settleButton = new JButton("Settle (Demo)");
    private final CommentsPanel commentsPanel = new CommentsPanel();

    private ViewMarketController viewController;
    private SettleMarketController settleMarketController;
    private stakemate.interface_adapter.view_comments.ViewCommentsController viewCommentsController;
    private stakemate.interface_adapter.view_comments.PostCommentController postCommentController;
    private MarketSummary currentlySelectedMarket;
    private ProfileFrame profileFrame;
    private stakemate.interface_adapter.view_profile.ViewProfileController profileController;
    private String currentUser;

    private Timer autoRefreshTimer;

    public MarketsFrame() {
        super("StakeMate - Markets & Order Book");
        initUi();
    }

    /**
     * Sets the ViewMarketController.
     *
     * @param controller The controller for market operations.
     */
    public void setController(final ViewMarketController controller) {
        this.viewController = controller;
        hookEvents();
        startAutoRefresh();
    }

    /**
     * Sets the SettleMarketController.
     *
     * @param controller The controller for settlement operations.
     */
    public void setSettleMarketController(final SettleMarketController controller) {
        this.settleMarketController = controller;
    }

    /**
     * Sets the ViewComment Controller.
     *
     * @param controller The controller for comments
     */
    public void setViewCommentsController(stakemate.interface_adapter.view_comments.ViewCommentsController controller) {
        this.viewCommentsController = controller;
    }

    /**
     * Sets the PostCommentController.
     *
     * @param controller The controller for posting comments
     */
    public void setPostCommentController(PostCommentController controller) {
        this.postCommentController = controller;
    }

    /**
     * Sets up the comments panel.
     *
     */
    public void wireCommentsPanel() {
        if (postCommentController != null && viewCommentsController != null) {
            commentsPanel.setMarketsFrame(this);
            commentsPanel.setControllers(postCommentController, viewCommentsController);
        }
    }

    /**
     * Sets the ProfileFrame for navigation.
     *
     * @param profileFrame The user profile window.
     */
    public void setProfileFrame(final ProfileFrame profileFrame) {
        this.profileFrame = profileFrame;
    }

    /**
     * Sets the ProfileController.
     *
     * @param profileController The controller for profile operations.
     */
    public void setProfileController(
        final stakemate.interface_adapter.view_profile.ViewProfileController profileController) {
        this.profileController = profileController;
    }

    /**
     * Sets the currently logged-in user.
     *
     * @param username The username.
     */
    public void setLoggedInUser(final String username) {
        this.currentUser = username;
    }

    private void initUi() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);

        final JPanel root = new JPanel(new BorderLayout(BORDER_PADDING, BORDER_PADDING));
        root.setBorder(BorderFactory.createEmptyBorder(
            BORDER_PADDING, BORDER_PADDING, BORDER_PADDING, BORDER_PADDING));

        final JPanel topBar = createTopPanel();
        final JSplitPane splitPane = createMainSplitPane();

        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        statusLabel.setForeground(Color.GRAY);

        root.add(topBar, BorderLayout.NORTH);
        root.add(splitPane, BorderLayout.CENTER);
        root.add(statusLabel, BorderLayout.SOUTH);
        root.add(commentsPanel, BorderLayout.EAST);

        setContentPane(root);
    }

    private JPanel createTopPanel() {
        final JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topBar.add(myProfileButton);
        return topBar;
    }

    private JSplitPane createMainSplitPane() {
        matchesEmptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        marketsEmptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        orderBookEmptyLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // --- Left Panel (Matches) ---
        final JPanel leftPanel = new JPanel(new BorderLayout(GAP, GAP));
        final JLabel matchesLabel = new JLabel("Active Matches");
        leftPanel.add(matchesLabel, BorderLayout.NORTH);
        matchesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        leftPanel.add(new JScrollPane(matchesList), BorderLayout.CENTER);
        leftPanel.add(matchesEmptyLabel, BorderLayout.SOUTH);

        // --- Right Panel (Markets + OrderBook + Controls) ---
        final JPanel rightPanel = createRightPanel();

        final JSplitPane splitPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            leftPanel,
            rightPanel);
        splitPane.setDividerLocation(DIVIDER_LOC);
        return splitPane;
    }

    private JPanel createRightPanel() {
        // --- Middle Top (Markets) ---
        final JPanel marketsPanel = new JPanel(new BorderLayout(GAP, GAP));
        final JLabel marketsLabel = new JLabel("Markets");
        marketsPanel.add(marketsLabel, BorderLayout.NORTH);
        marketsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        marketsPanel.add(new JScrollPane(marketsList), BorderLayout.CENTER);
        marketsPanel.add(marketsEmptyLabel, BorderLayout.SOUTH);

        // --- Middle Bottom (Order Book) ---
        final JPanel orderBookPanel = new JPanel(new BorderLayout(GAP, GAP));
        final JLabel orderBookLabel = new JLabel("Live Order Book");
        orderBookPanel.add(orderBookLabel, BorderLayout.NORTH);
        orderBookTable.setFillsViewportHeight(true);

        // Center align the DATA
        final DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < orderBookTable.getColumnCount(); i++) {
            orderBookTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Center align the HEADERS
        final TableCellRenderer headerRenderer =
            orderBookTable.getTableHeader().getDefaultRenderer();
        if (headerRenderer instanceof DefaultTableCellRenderer) {
            ((DefaultTableCellRenderer) headerRenderer)
                .setHorizontalAlignment(JLabel.CENTER);
        }

        orderBookPanel.add(new JScrollPane(orderBookTable), BorderLayout.CENTER);
        orderBookPanel.add(orderBookEmptyLabel, BorderLayout.SOUTH);

        // --- Controls ---
        final JPanel buySellPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buyButton.setEnabled(false);
        sellButton.setEnabled(false);
        settleButton.setEnabled(false);

        buySellPanel.add(buyButton);
        buySellPanel.add(sellButton);
        buySellPanel.add(settleButton);

        final JPanel rightPanel = new JPanel(new BorderLayout(GAP, GAP));
        rightPanel.add(marketsPanel, BorderLayout.NORTH);
        rightPanel.add(orderBookPanel, BorderLayout.CENTER);
        rightPanel.add(buySellPanel, BorderLayout.SOUTH);

        return rightPanel;
    }

    private void hookEvents() {
        autoRefreshTimer = new Timer(REFRESH_INTERVAL, evt -> {
            if (viewController != null) {
                viewController.refresh();
            }
        });

        myProfileButton.addActionListener(evt -> openProfile());

        matchesList.addListSelectionListener(evt -> {
            if (!evt.getValueIsAdjusting() && viewController != null) {
                final MatchSummary selected = matchesList.getSelectedValue();
                currentlySelectedMarket = null;
                viewController.onMatchSelected(selected);
            }
        });

        marketsList.addListSelectionListener(evt -> {
            if (!evt.getValueIsAdjusting()) {
                final MarketSummary selected = marketsList.getSelectedValue();
                currentlySelectedMarket = selected;

                if (viewController != null && selected != null) {
                    viewController.onMarketSelected(selected);
                }

                // >>> ADD THIS: automatically load comments
                if (viewCommentsController != null && selected != null) {
                    viewCommentsController.fetchComments(selected.getId());
                }
            }
        });

        hookButtons();
    }

    private void openProfile() {
        if (profileFrame != null) {
            if (currentUser != null && profileController != null) {
                profileController.execute(currentUser);
            }
            profileFrame.setVisible(true);
        }
        else {
            JOptionPane.showMessageDialog(this, "Profile frame not connected.");
        }
    }

    private void hookButtons() {
        buyButton.addActionListener(evt -> {
            JOptionPane.showMessageDialog(
                MarketsFrame.this,
                "Buy clicked. (Handled by PlaceOrderUseCase)");
        });

        sellButton.addActionListener(evt -> {
            JOptionPane.showMessageDialog(
                MarketsFrame.this,
                "Sell clicked. (Handled by PlaceOrderUseCase)");
        });

        settleButton.addActionListener(evt -> performSettlement());
    }

    private void performSettlement() {
        if (settleMarketController != null && currentlySelectedMarket != null) {
            final int choice = JOptionPane.showConfirmDialog(
                MarketsFrame.this,
                "Demo Tool: Did the HOME team win this market?",
                "Simulate Settlement",
                JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION || choice == JOptionPane.NO_OPTION) {
                settleMarketController.settleMarket(currentlySelectedMarket.getId());
            }
        }
    }

    private void startAutoRefresh() {
        if (autoRefreshTimer != null && !autoRefreshTimer.isRunning()) {
            autoRefreshTimer.start();
        }
        if (viewController != null) {
            viewController.refresh();
        }
    }

    // ---- MarketsView implementation ----

    @Override
    public void showMatches(final List<MatchSummary> matches, final String emptyStateMessage) {
        final int selectedIndex = matchesList.getSelectedIndex();

        matchesListModel.clear();
        if (matches == null || matches.isEmpty()) {
            if (emptyStateMessage != null) {
                matchesEmptyLabel.setText(emptyStateMessage);
            }
            else {
                matchesEmptyLabel.setText("No matches.");
            }
        }
        else {
            for (final MatchSummary m : matches) {
                matchesListModel.addElement(m);
            }
            matchesEmptyLabel.setText(EMPTY_TEXT);
        }

        if (selectedIndex >= 0 && selectedIndex < matchesListModel.size()) {
            matchesList.setSelectedIndex(selectedIndex);
        }
    }

    @Override
    public void showMarketsForMatch(final MarketsResponseModel responseModel) {
        setTitle("StakeMate - " + responseModel.getMatchTitle());

        final int selectedIndex = marketsList.getSelectedIndex();

        marketsListModel.clear();
        currentlySelectedMarket = null;
        if (responseModel.getMarkets() == null || responseModel.getMarkets().isEmpty()) {
            if (responseModel.getEmptyStateMessage() != null) {
                marketsEmptyLabel.setText(responseModel.getEmptyStateMessage());
            }
            else {
                marketsEmptyLabel.setText("No markets for this match.");
            }
        }
        else {
            for (final MarketSummary m : responseModel.getMarkets()) {
                marketsListModel.addElement(m);
            }
            marketsEmptyLabel.setText(EMPTY_TEXT);
        }

        if (selectedIndex >= 0 && selectedIndex < marketsListModel.size()) {
            marketsList.setSelectedIndex(selectedIndex);
            currentlySelectedMarket = marketsList.getSelectedValue();
        }
        else {
            orderBookTableModel.clear();
            orderBookEmptyLabel.setText("Select a market to see orders.");
            updateButtonStates(false);
        }
    }

    @Override
    public void showOrderBook(final OrderBookResponseModel responseModel) {
        if (responseModel.getOrderBook() != null) {
            orderBookTableModel.setOrderBook(responseModel.getOrderBook());
        }

        if (responseModel.isReconnecting()) {
            if (responseModel.getMessage() != null) {
                statusLabel.setText(responseModel.getMessage());
            }
            else {
                statusLabel.setText("Reconnecting...");
            }
            statusLabel.setForeground(Color.RED);
        }
        else if (responseModel.getMessage() != null) {
            statusLabel.setText(responseModel.getMessage());
            statusLabel.setForeground(Color.BLACK);
        }
        else {
            statusLabel.setText("System: Live");
            statusLabel.setForeground(new Color(0, DARK_GREEN_G, 0));
        }

        handleOrderBookEmptyState(responseModel);

        final boolean enableBuySell = currentlySelectedMarket != null
            && currentlySelectedMarket.isBuySellEnabled()
            && !responseModel.isReconnecting();

        updateButtonStates(enableBuySell);
    }

    private void handleOrderBookEmptyState(final OrderBookResponseModel responseModel) {
        if (responseModel.isEmpty()) {
            if (responseModel.getMessage() != null) {
                orderBookEmptyLabel.setText(responseModel.getMessage());
            }
            else {
                orderBookEmptyLabel.setText("No orders yet");
            }
        }
        else if (!responseModel.isReconnecting()) {
            orderBookEmptyLabel.setText(EMPTY_TEXT);
        }
    }

    private void updateButtonStates(final boolean enabled) {
        buyButton.setEnabled(enabled);
        sellButton.setEnabled(enabled);
        settleButton.setEnabled(enabled);

        if (!enabled && currentlySelectedMarket != null && !currentlySelectedMarket.isBuySellEnabled()) {
            statusLabel.setText("Market Closed");
            statusLabel.setForeground(Color.GRAY);
        }
    }

    @Override
    public void showError(final String message) {
        JOptionPane.showMessageDialog(this, message, ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
    }

    // ---- SettleMarketView (UC6) ----

    @Override
    public void showSettlementResult(final String message) {
        statusLabel.setText(message);
        String alice = "";
        String bob = "";
        String you = "";

        try {
            alice = "Alice: $" + StakeMateApp.getAccountRepo()
                .findByUsername("alice").getBalance();
            bob = "Bob:   $" + StakeMateApp.getAccountRepo()
                .findByUsername("bob").getBalance();

            final String userToFind;
            if (currentUser != null) {
                userToFind = currentUser;
            }
            else {
                userToFind = "user";
            }

            you = "You:   $" + StakeMateApp.getAccountRepo()
                .findByUsername(userToFind).getBalance();
        }
        // -@cs[IllegalCatch] Demo code needs to robustly handle missing data during presentation
        catch (final Exception ignored) {
            // Suppressed: It is acceptable for demo balances to fail if repo is not ready
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

    public String getCurrentUser() {
        return currentUser;
    }

    public MarketSummary getCurrentlySelectedMarket() {
        return currentlySelectedMarket;
    }

    // ---- Order Book Table Model ----

    private static final class OrderBookTableModel extends AbstractTableModel {

        private static final String[] COLUMNS = {
            "Bid Size", "Bid Price", "Ask Price", "Ask Size",
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
                OrderBookEntry bid = null;
                if (i < bids.size()) {
                    bid = bids.get(i);
                }

                OrderBookEntry ask = null;
                if (i < asks.size()) {
                    ask = asks.get(i);
                }

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
            Object result = "";
            switch (columnIndex) {
                case COL_BID_QTY:
                    result = formatVal(row.getBidQty());
                    break;
                case COL_BID_PRICE:
                    result = formatVal(row.getBidPrice());
                    break;
                case COL_ASK_PRICE:
                    result = formatVal(row.getAskPrice());
                    break;
                case COL_ASK_QTY:
                    result = formatVal(row.getAskQty());
                    break;
                default:
                    result = "";
                    break;
            }
            return result;
        }

        private String formatVal(final Double val) {
            final String result;
            if (val == null) {
                result = "";
            }
            else {
                result = "$" + String.format("%.2f", val);
            }
            return result;
        }

        private static final class Row {
            private final Double bidQty;
            private final Double bidPrice;
            private final Double askPrice;
            private final Double askQty;

            Row(final OrderBookEntry bid, final OrderBookEntry ask) {
                if (bid != null) {
                    this.bidQty = bid.getQuantity();
                    this.bidPrice = bid.getPrice();
                }
                else {
                    this.bidQty = null;
                    this.bidPrice = null;
                }

                if (ask != null) {
                    this.askPrice = ask.getPrice();
                    this.askQty = ask.getQuantity();
                }
                else {
                    this.askPrice = null;
                    this.askQty = null;
                }
            }

            public Double getBidQty() {
                return bidQty;
            }

            public Double getBidPrice() {
                return bidPrice;
            }

            public Double getAskPrice() {
                return askPrice;
            }

            public Double getAskQty() {
                return askQty;
            }
        }
    }
}
