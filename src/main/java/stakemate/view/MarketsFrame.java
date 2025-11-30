package stakemate.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import stakemate.app.StakeMateApp;
import stakemate.interface_adapter.controllers.SettleMarketController;
import stakemate.interface_adapter.view_comments.PostCommentController;
import stakemate.interface_adapter.view_comments.ViewCommentsController;
import stakemate.interface_adapter.view_live.LiveMatchesController;
import stakemate.interface_adapter.view_market.MarketsView;
import stakemate.interface_adapter.view_market.SettleMarketView;
import stakemate.interface_adapter.view_market.ViewMarketController;
import stakemate.use_case.view_market.MarketSummary;
import stakemate.use_case.view_market.MarketsResponseModel;
import stakemate.use_case.view_market.MatchSummary;
import stakemate.use_case.view_market.OrderBookResponseModel;

// -@cs[ClassDataAbstractionCoupling] UI class requires many dependencies.
// -@cs[ClassFanOutComplexity] UI class requires many dependencies.
public class MarketsFrame extends JFrame implements MarketsView, SettleMarketView {

    private static final String EMPTY_TEXT = " ";
    private static final String ERROR_TITLE = "Error";
    private static final String SELECT_MARKET_MSG = "Select a market to see orders.";
    private static final int WINDOW_WIDTH = 1000;
    private static final int WINDOW_HEIGHT = 600;
    private static final int BORDER_PADDING = 8;
    private static final int DIVIDER_LOC = 280;
    private static final int REFRESH_INTERVAL = 5000;
    private static final int DARK_GREEN_G = 100;
    private static final int ROW_HEIGHT = 24;
    private static final int GAP = 4;

    private final DefaultListModel<MatchSummary> matchesListModel = new DefaultListModel<>();
    private final JList<MatchSummary> matchesList = new JList<>(matchesListModel);
    private final JLabel matchesEmptyLabel = new JLabel(EMPTY_TEXT);

    private final DefaultListModel<MarketSummary> marketsListModel = new DefaultListModel<>();
    private final JList<MarketSummary> marketsList = new JList<>(marketsListModel);
    private final JLabel marketsEmptyLabel = new JLabel(EMPTY_TEXT);

    private final OrderBookTableModel orderBookTableModel = new OrderBookTableModel();
    private final JTable orderBookTable = new JTable(orderBookTableModel);
    private final JLabel orderBookEmptyLabel = new JLabel(SELECT_MARKET_MSG);

    private final JLabel statusLabel = new JLabel(" ");

    // Buttons
    private final JButton buyButton = new JButton("Buy");
    private final JButton sellButton = new JButton("Sell");

    private final JButton refreshButton = new JButton("Refresh");
    private final JButton myProfileButton = new JButton("My Profile");
    private final JButton liveMatchesButton = new JButton("Live Matches");
    private final JButton settleButton = new JButton("Settle (Demo)");

    // Place Order Button
    private final JButton placeOrderButton = new JButton("Place Order");
    private final CommentsPanel commentsPanel = new CommentsPanel();

    private ViewMarketController controller;
    private SettleMarketController settleMarketController;
    private ViewCommentsController viewCommentsController;
    private PostCommentController postCommentController;
    private MarketSummary currentlySelectedMarket;
    private ProfileFrame profileFrame;
    private stakemate.interface_adapter.view_profile.ViewProfileController profileController;
    private String currentUser;
    private LiveMatchesFrame liveMatchesFrame;
    private LiveMatchesController liveMatchesController;

    private Timer autoRefreshTimer;

    // Listeners stored as fields to allow detachment during updates
    private ListSelectionListener matchesSelectionListener;
    private ListSelectionListener marketsSelectionListener;

    public MarketsFrame() {
        super("StakeMate - Markets & Order Book");
        initUi();
    }

    /**
     * Sets the ViewMarketController and initializes hooks.
     *
     * @param controllerArg The controller for market operations.
     */
    public void setController(final ViewMarketController controllerArg) {
        this.controller = controllerArg;
        hookEvents();
        if (autoRefreshTimer != null && !autoRefreshTimer.isRunning()) {
            autoRefreshTimer.start();
        }
        if (controller != null) {
            controller.refresh();
        }
    }

    public void setSettleMarketController(final SettleMarketController controllerArg) {
        this.settleMarketController = controllerArg;
    }

    public void setViewCommentsController(final ViewCommentsController controllerArg) {
        this.viewCommentsController = controllerArg;
    }

    public void setPostCommentController(final PostCommentController controllerArg) {
        this.postCommentController = controllerArg;
    }

    /**
     * Wires up the comments panel with controllers.
     */
    public void wireCommentsPanel() {
        if (postCommentController != null && viewCommentsController != null) {
            commentsPanel.setMarketsFrame(this);
            commentsPanel.setControllers(postCommentController, viewCommentsController);
        }
    }

    public void setProfileFrame(final ProfileFrame profileFrame) {
        this.profileFrame = profileFrame;
    }

    public void setProfileController(
        final stakemate.interface_adapter.view_profile.ViewProfileController profileController) {
        this.profileController = profileController;
    }

    public void setLiveMatchesFrame(final LiveMatchesFrame liveMatchesFrame) {
        this.liveMatchesFrame = liveMatchesFrame;
    }

    public void setLiveMatchesController(final LiveMatchesController liveMatchesController) {
        this.liveMatchesController = liveMatchesController;
    }

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

        // Create Top Panel (inlined)
        final JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topBar.add(myProfileButton);

        final JSplitPane splitPane = createMainSplitPane();

        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        statusLabel.setForeground(Color.GRAY);

        root.add(topBar, BorderLayout.NORTH);
        root.add(splitPane, BorderLayout.CENTER);
        root.add(statusLabel, BorderLayout.SOUTH);
        root.add(commentsPanel, BorderLayout.EAST);

        setContentPane(root);
    }

    private JSplitPane createMainSplitPane() {
        matchesEmptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        marketsEmptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        orderBookEmptyLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Create Left Panel (inlined)
        final JPanel leftPanel = new JPanel(new BorderLayout(GAP, GAP));
        final JLabel matchesLabel = new JLabel("Matches");
        leftPanel.add(matchesLabel, BorderLayout.NORTH);
        matchesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        leftPanel.add(new JScrollPane(matchesList), BorderLayout.CENTER);
        final JPanel matchesSouth = new JPanel(new BorderLayout());
        matchesSouth.add(matchesEmptyLabel, BorderLayout.NORTH);
        matchesSouth.add(liveMatchesButton, BorderLayout.SOUTH);
        leftPanel.add(matchesSouth, BorderLayout.SOUTH);

        final JPanel rightPanel = createRightPanel();

        final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(DIVIDER_LOC);
        return splitPane;
    }

    private JPanel createRightPanel() {
        final JPanel marketsPanel = new JPanel(new BorderLayout(GAP, GAP));
        final JLabel marketsLabel = new JLabel("Markets");
        marketsPanel.add(marketsLabel, BorderLayout.NORTH);
        marketsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        marketsPanel.add(new JScrollPane(marketsList), BorderLayout.CENTER);
        marketsPanel.add(marketsEmptyLabel, BorderLayout.SOUTH);

        final JPanel buySellPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buyButton.setEnabled(false);
        sellButton.setEnabled(false);
        settleButton.setEnabled(false);
        buySellPanel.add(placeOrderButton);
        buySellPanel.add(settleButton);

        final JPanel rightPanel = new JPanel(new BorderLayout(GAP, GAP));
        rightPanel.add(marketsPanel, BorderLayout.NORTH);
        rightPanel.add(createOrderBookPanel(), BorderLayout.CENTER);
        rightPanel.add(buySellPanel, BorderLayout.SOUTH);
        return rightPanel;
    }

    private JPanel createOrderBookPanel() {
        final JPanel orderBookPanel = new JPanel(new BorderLayout(GAP, GAP));
        final JLabel orderBookLabel = new JLabel("Order Book");
        orderBookLabel.setHorizontalAlignment(SwingConstants.CENTER);
        orderBookPanel.add(orderBookLabel, BorderLayout.NORTH);

        orderBookTable.setFillsViewportHeight(true);
        orderBookTable.setRowHeight(ROW_HEIGHT);

        final DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < orderBookTable.getColumnCount(); i++) {
            orderBookTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        final TableCellRenderer headerRenderer = orderBookTable.getTableHeader().getDefaultRenderer();
        if (headerRenderer instanceof DefaultTableCellRenderer) {
            ((DefaultTableCellRenderer) headerRenderer).setHorizontalAlignment(JLabel.CENTER);
        }

        orderBookPanel.add(new JScrollPane(orderBookTable), BorderLayout.CENTER);
        orderBookPanel.add(orderBookEmptyLabel, BorderLayout.SOUTH);
        return orderBookPanel;
    }

    private void hookEvents() {
        autoRefreshTimer = new Timer(REFRESH_INTERVAL, evt -> {
            if (controller != null) {
                controller.refresh();
            }
        });

        refreshButton.addActionListener(evt -> {
            if (controller != null) {
                controller.refreshWithApi();
            }
        });

        myProfileButton.addActionListener(evt -> openProfile());

        liveMatchesButton.addActionListener(evt -> {
            if (liveMatchesFrame != null && liveMatchesController != null) {
                liveMatchesFrame.setVisible(true);
                liveMatchesController.startTracking();
            }
            else {
                JOptionPane.showMessageDialog(this, "Live Matches view not connected.");
            }
        });

        matchesSelectionListener = this::handleMatchSelection;
        matchesList.addListSelectionListener(matchesSelectionListener);

        marketsSelectionListener = this::handleMarketSelection;
        marketsList.addListSelectionListener(marketsSelectionListener);

        placeOrderButton.addActionListener(evt -> openOrderBookPopup());

        settleButton.addActionListener(evt -> {
            if (settleMarketController != null && currentlySelectedMarket != null) {
                performSettlement();
            }
        });
    }

        marketsList.addListSelectionListener(evt -> handleMarketSelection(evt));

        hookButtons();
    }

    private void openLiveMatches() {
        if (liveMatchesFrame != null && liveMatchesController != null) {
            liveMatchesFrame.setVisible(true);
            liveMatchesController.startTracking();
        }
        else {
            JOptionPane.showMessageDialog(this, "Live Matches view not connected.");
        }
    }

    private void handleMarketSelection(final javax.swing.event.ListSelectionEvent evt) {
    private void handleMatchSelection(final ListSelectionEvent evt) {
        if (!evt.getValueIsAdjusting() && controller != null) {
            final MatchSummary selected = matchesList.getSelectedValue();
            currentlySelectedMarket = null;

            orderBookTableModel.clear();
            orderBookEmptyLabel.setText(SELECT_MARKET_MSG);
            updateButtonStates(false);

            controller.onMatchSelected(selected);
        }
    }

    private void handleMarketSelection(final ListSelectionEvent evt) {
        if (!evt.getValueIsAdjusting()) {
            final MarketSummary selected = marketsList.getSelectedValue();
            currentlySelectedMarket = selected;

            if (selected != null) {
                orderBookTableModel.clear();
                orderBookEmptyLabel.setText("Loading...");
            }

            if (controller != null && selected != null) {
                controller.onMarketSelected(selected);
            }

            if (viewCommentsController != null && selected != null) {
                viewCommentsController.fetchComments(selected.getId());
            }
        }
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

    private void performSettlement() {
        final int choice = JOptionPane.showConfirmDialog(
            MarketsFrame.this,
            "Demo Tool: Did the HOME team win this market?",
            "Simulate Settlement",
            JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            settleMarketController.settleMarket(currentlySelectedMarket.getId(), true);
        }
        else if (choice == JOptionPane.NO_OPTION) {
            settleMarketController.settleMarket(currentlySelectedMarket.getId(), false);
        }
    }

    // ---- MarketsView implementation ----

    @Override
    public void showMatches(final List<MatchSummary> matches, final String emptyStateMessage) {
        String selectedId = null;
        final MatchSummary current = matchesList.getSelectedValue();
        if (current != null) {
            selectedId = current.getId();
        }

        matchesList.removeListSelectionListener(matchesSelectionListener);

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

        restoreListSelection(matchesList, selectedId);
        matchesList.addListSelectionListener(matchesSelectionListener);
    }

    @Override
    public void showMarketsForMatch(final MarketsResponseModel responseModel) {
        setTitle("StakeMate - " + responseModel.getMatchTitle());

        String selectedId = null;
        final MarketSummary current = marketsList.getSelectedValue();
        if (current != null) {
            selectedId = current.getId();
        }

        marketsList.removeListSelectionListener(marketsSelectionListener);

        marketsListModel.clear();

        if (responseModel.getMarkets() == null || responseModel.getMarkets().isEmpty()) {
            currentlySelectedMarket = null;
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

        restoreListSelection(marketsList, selectedId);

        // Update current selection based on list state
        if (marketsList.getSelectedIndex() != -1) {
            currentlySelectedMarket = marketsList.getSelectedValue();
        }
        else {
            currentlySelectedMarket = null;
        }

        if (currentlySelectedMarket == null) {
            orderBookTableModel.clear();
            orderBookEmptyLabel.setText(SELECT_MARKET_MSG);
            updateButtonStates(false);
        }
        else {
            updateButtonStates(currentlySelectedMarket.isBuySellEnabled());
        }

        marketsList.addListSelectionListener(marketsSelectionListener);
    }

    private <T> void restoreListSelection(final JList<T> list, final String selectedId) {
        if (selectedId != null) {
            final javax.swing.ListModel<T> model = list.getModel();
            for (int i = 0; i < model.getSize(); i++) {
                final T item = model.getElementAt(i);
                String itemId = null;
                if (item instanceof MatchSummary) {
                    itemId = ((MatchSummary) item).getId();
                }
                else if (item instanceof MarketSummary) {
                    itemId = ((MarketSummary) item).getId();
                }

                if (selectedId.equals(itemId)) {
                    list.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    @Override
    public void showOrderBook(final OrderBookResponseModel responseModel) {
        if (responseModel.getOrderBook() != null) {
            orderBookTableModel.setOrderBook(responseModel.getOrderBook());
        }

        updateStatusLabels(responseModel);

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

        final boolean enableBuySell = currentlySelectedMarket != null
            && currentlySelectedMarket.isBuySellEnabled()
            && !responseModel.isReconnecting();

        updateButtonStates(enableBuySell);
    }

    private void updateStatusLabels(final OrderBookResponseModel responseModel) {
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
    }

    private void updateButtonStates(final boolean enabled) {
        buyButton.setEnabled(enabled);
        sellButton.setEnabled(enabled);
        settleButton.setEnabled(enabled);
        placeOrderButton.setEnabled(enabled);
    }

    @Override
    public void showError(final String message) {
        JOptionPane.showMessageDialog(this, message, ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void showSettlementResult(final String message) {
        statusLabel.setText(message);
        JOptionPane.showMessageDialog(this, message, "Settlement Complete", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void showSettlementError(final String errorMessage) {
        JOptionPane.showMessageDialog(this, errorMessage, "Settlement Error", JOptionPane.ERROR_MESSAGE);
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

    /**
     * Enables the order book popup.
     * Kept for API compatibility with StakeMateApp.
     */
    public void enableOrderBookPopup() {
        // No-op
    }

    private void openOrderBookPopup() {
        if (currentlySelectedMarket == null) {
            JOptionPane.showMessageDialog(this, "Select a market first.");
        }
        else if (currentUser == null) {
            JOptionPane.showMessageDialog(this, "No logged-in user.");
        }
        else {
            final String userId = StakeMateApp.userRepo.getUserIdByUsername(currentUser);
            if (userId == null) {
                JOptionPane.showMessageDialog(this, "Cannot find profile for: " + currentUser);
            }
            else {
                final var uc = StakeMateApp.getPlaceOrderUseCase();
                final var f = new stakemate.interface_adapter.viewOrderBook.OrderBookTradingFrame(
                    uc, userId, currentlySelectedMarket.getId());
                f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                f.setVisible(true);
            }
        }
    }
}
