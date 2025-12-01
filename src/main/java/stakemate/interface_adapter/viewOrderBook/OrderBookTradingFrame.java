package stakemate.interface_adapter.viewOrderBook;

import stakemate.entity.OrderBook;
import stakemate.entity.OrderBookEntry;
import stakemate.entity.Side;
import stakemate.engine.BookOrder;
import stakemate.engine.Trade;
import stakemate.use_case.PlaceOrderUseCase.PlaceOrderRequest;
import stakemate.use_case.PlaceOrderUseCase.PlaceOrderResponse;
import stakemate.use_case.PlaceOrderUseCase.PlaceOrderUseCase;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

/**
 * Clean Swing UI for viewing an order book and placing orders.
 * Depends only on the PlaceOrderUseCase (application layer).
 */
public class OrderBookTradingFrame extends JFrame {

    private final PlaceOrderUseCase placeOrderUseCase;
    private final String currentUserId;
    private final String currentUsername;
    private String currentMarketId;
    private final String teamAName;
    private final String teamBName;

    private final JLabel statusLabel = new JLabel(" ");
    private final JLabel lastTradeLabel = new JLabel("Last: -   Spread: -");

    private final DefaultTableModel bidsModel;
    private final DefaultTableModel asksModel;
    private final DefaultTableModel openOrdersModel;

    private final JTextField marketField = new JTextField(10);
    private final JComboBox<String> teamCombo;
    private final JComboBox<Side> sideCombo = new JComboBox<>(Side.values());
    private final JComboBox<String> orderTypeCombo = new JComboBox<>(new String[] { "Limit", "Market" });
    private final JTextField priceField = new JTextField(6);
    private final JTextField qtyField = new JTextField(6);

    public OrderBookTradingFrame(PlaceOrderUseCase placeOrderUseCase,
            String currentUserId,
            String currentUsername,
            String initialMarketId,
            String teamAName,
            String teamBName) {
        super("StakeMate – Order Book");

        this.placeOrderUseCase = placeOrderUseCase;
        this.currentUserId = currentUserId;
        this.currentUsername = currentUsername;
        this.currentMarketId = initialMarketId;
        this.teamAName = teamAName != null ? teamAName : "Team A";
        this.teamBName = teamBName != null ? teamBName : "Team B";

        // Initialize team combo with actual team names
        this.teamCombo = new JComboBox<>(new String[] { this.teamAName, this.teamBName });

        // ---- window basics ----
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(950, 600);
        setLocationRelativeTo(null);

        // ---- layout root ----
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(root);

        // ================== TOP BAR ==================
        JPanel topBar = new JPanel(new BorderLayout());
        JLabel userLabel = new JLabel(" User: " + currentUsername);
        userLabel.setFont(userLabel.getFont().deriveFont(Font.BOLD, 14f));

        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        topRight.add(new JLabel("Market:"));
        marketField.setText(currentMarketId);
        marketField.setEditable(false); // Enforce valid ID from games repo
        topRight.add(marketField);
        JButton changeMarketBtn = new JButton("Load");
        topRight.add(changeMarketBtn);

        topBar.add(userLabel, BorderLayout.WEST);
        topBar.add(topRight, BorderLayout.EAST);
        root.add(topBar, BorderLayout.NORTH);

        // ================== CENTER: ORDER BOOK ==================
        String[] obColumns = { "Price", "Size" };
        bidsModel = new DefaultTableModel(obColumns, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        asksModel = new DefaultTableModel(obColumns, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable bidsTable = new JTable(bidsModel);
        JTable asksTable = new JTable(asksModel);

        // simple cell coloring: green bids, red asks
        DefaultTableCellRenderer greenRenderer = new DefaultTableCellRenderer();
        greenRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        greenRenderer.setForeground(new Color(0, 128, 0));
        DefaultTableCellRenderer redRenderer = new DefaultTableCellRenderer();
        redRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        redRenderer.setForeground(new Color(180, 0, 0));

        bidsTable.getColumnModel().getColumn(0).setCellRenderer(greenRenderer);
        bidsTable.getColumnModel().getColumn(1).setCellRenderer(greenRenderer);
        asksTable.getColumnModel().getColumn(0).setCellRenderer(redRenderer);
        asksTable.getColumnModel().getColumn(1).setCellRenderer(redRenderer);

        // Wrap asks table with a red header "Asks (Sell)"
        JPanel asksContainer = new JPanel(new BorderLayout());
        JLabel asksLabel = new JLabel("Asks (Sell)");
        asksLabel.setForeground(new Color(180, 0, 0));
        asksLabel.setHorizontalAlignment(SwingConstants.CENTER);
        asksLabel.setBorder(new EmptyBorder(2, 0, 2, 0));
        asksContainer.add(asksLabel, BorderLayout.NORTH);
        asksContainer.add(new JScrollPane(asksTable), BorderLayout.CENTER);

        // Wrap bids table with a green header "Bids (Buy)"
        JPanel bidsContainer = new JPanel(new BorderLayout());
        JLabel bidsLabel = new JLabel("Bids (Buy)");
        bidsLabel.setForeground(new Color(0, 128, 0));
        bidsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        bidsLabel.setBorder(new EmptyBorder(2, 0, 2, 0));
        bidsContainer.add(bidsLabel, BorderLayout.NORTH);
        bidsContainer.add(new JScrollPane(bidsTable), BorderLayout.CENTER);

        JPanel orderBookPanel = new JPanel(new BorderLayout());
        JLabel obTitle = new JLabel("Order Book");
        obTitle.setFont(obTitle.getFont().deriveFont(Font.BOLD, 15f));
        orderBookPanel.add(obTitle, BorderLayout.NORTH);

        JSplitPane obSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                asksContainer,
                bidsContainer);
        obSplit.setResizeWeight(0.5);
        orderBookPanel.add(obSplit, BorderLayout.CENTER);

        lastTradeLabel.setBorder(new EmptyBorder(5, 0, 0, 0));
        orderBookPanel.add(lastTradeLabel, BorderLayout.SOUTH);

        // ================== RIGHT: ORDER ENTRY ==================
        JPanel orderEntry = new JPanel();
        orderEntry.setLayout(new BoxLayout(orderEntry, BoxLayout.Y_AXIS));
        orderEntry.setBorder(BorderFactory.createTitledBorder("Place Order"));

        // Row 1: order type + team + side
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 3));
        row1.add(new JLabel("Type:"));
        orderTypeCombo.setPreferredSize(new Dimension(90, 24));
        row1.add(orderTypeCombo);
        row1.add(Box.createHorizontalStrut(10));
        row1.add(new JLabel("Team:"));
        teamCombo.setPreferredSize(new Dimension(90, 24));
        row1.add(teamCombo);
        row1.add(Box.createHorizontalStrut(10));
        row1.add(new JLabel("Side:"));
        sideCombo.setPreferredSize(new Dimension(90, 24));
        row1.add(sideCombo);

        // Row 2: price
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 3));
        row2.add(new JLabel("Price:"));
        priceField.setColumns(8);
        row2.add(priceField);
        JLabel priceHint = new JLabel("(ignored for Market)");
        priceHint.setFont(priceHint.getFont().deriveFont(Font.ITALIC, 11f));
        row2.add(priceHint);

        // Row 3: quantity
        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 3));
        row3.add(new JLabel("Quantity:"));
        qtyField.setColumns(8);
        row3.add(qtyField);

        // Row 4: button
        JButton placeBtn = new JButton("Place Order");
        JPanel row4 = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 10));
        row4.add(placeBtn);

        orderEntry.add(row1);
        orderEntry.add(row2);
        orderEntry.add(row3);
        orderEntry.add(row4);

        // ================== BOTTOM: OPEN ORDERS ==================
        String[] ooColumns = { "Side", "Market", "Price", "Remaining", "Original" };
        openOrdersModel = new DefaultTableModel(ooColumns, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable openOrdersTable = new JTable(openOrdersModel);
        JPanel openOrdersPanel = new JPanel(new BorderLayout());
        JLabel ooTitle = new JLabel("Open Orders");
        ooTitle.setFont(ooTitle.getFont().deriveFont(Font.BOLD, 14f));
        openOrdersPanel.add(ooTitle, BorderLayout.NORTH);
        openOrdersPanel.add(new JScrollPane(openOrdersTable), BorderLayout.CENTER);

        // ================== LAYOUT CENTER + RIGHT + BOTTOM ==================
        JSplitPane centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                orderBookPanel,
                orderEntry);
        centerSplit.setResizeWeight(0.72);

        root.add(centerSplit, BorderLayout.CENTER);
        root.add(openOrdersPanel, BorderLayout.SOUTH);

        // status line at very bottom
        statusLabel.setBorder(new EmptyBorder(4, 0, 0, 0));
        root.add(statusLabel, BorderLayout.PAGE_END);

        // ================== EVENT HANDLERS ==================

        // Change market
        changeMarketBtn.addActionListener(e -> {
            currentMarketId = marketField.getText().trim();
            refreshAll();
        });

        // Toggle Limit / Market: disable price for Market
        orderTypeCombo.addActionListener(e -> {
            boolean isLimit = "Limit".equals(orderTypeCombo.getSelectedItem());
            priceField.setEnabled(isLimit);
            if (!isLimit) {
                priceField.setText("");
            }
        });

        placeBtn.addActionListener(e -> placeOrder());

        // Team selector change - refresh order book to swap perspective
        teamCombo.addActionListener(e -> refreshOrderBook());

        // initial load
        refreshAll();
    }

    private void placeOrder() {
        try {
            String marketId = marketField.getText().trim();
            if (marketId.isEmpty()) {
                setStatus("Please enter a market id.");
                return;
            }

            // Determine actual side based on team selection and UI side selector
            Side uiSide = (Side) sideCombo.getSelectedItem();
            String team = (String) teamCombo.getSelectedItem();
            Side side = getActualSide(uiSide, team);

            String qtyText = qtyField.getText().trim();
            if (qtyText.isEmpty()) {
                setStatus("Quantity is required.");
                return;
            }
            double qty = Double.parseDouble(qtyText);

            // handle order type (Limit vs Market)
            boolean isLimit = "Limit".equals(orderTypeCombo.getSelectedItem());
            Double price = null;
            if (isLimit) {
                String priceText = priceField.getText().trim();
                if (priceText.isEmpty()) {
                    setStatus("Price is required for limit orders.");
                    return;
                }
                double uiPrice = Double.parseDouble(priceText);

                // Convert Team B price to Team A price using 1 - price
                boolean isTeamB = teamBName.equals(team);
                price = isTeamB ? (1.0 - uiPrice) : uiPrice;
            } else {
                // Market order -> price left null; matching engine treats null as market
                price = null;
            }

            PlaceOrderRequest req = new PlaceOrderRequest(currentUserId, marketId, side, price, qty);

            // This will save the order via OrderRepository and match it via MatchingEngine.
            PlaceOrderResponse resp = placeOrderUseCase.place(req);
            setStatus(resp.message);

            // Optional: update "last trade" text from recent trades
            List<Trade> trades = placeOrderUseCase.recentTrades();
            if (!trades.isEmpty()) {
                Trade last = trades.get(trades.size() - 1);
                lastTradeLabel.setText(
                        String.format("Last: %.2f   Size: %.2f", last.getPrice(), last.getSize()));
            }

            // refresh to show open orders + new book state
            refreshAll();
        } catch (NumberFormatException ex) {
            setStatus("Invalid number in price or quantity.");
        } catch (Exception ex) {
            ex.printStackTrace();
            setStatus("Error placing order: " + ex.getMessage());
        }
    }

    private void refreshAll() {
        refreshOrderBook();
        refreshOpenOrders();
    }

    private void refreshOrderBook() {
        if (currentMarketId == null || currentMarketId.isEmpty()) {
            return;
        }
        OrderBook ob = placeOrderUseCase.snapshot(currentMarketId);

        bidsModel.setRowCount(0);
        asksModel.setRowCount(0);

        String team = (String) teamCombo.getSelectedItem();
        boolean isTeamB = teamBName.equals(team);

        // For Team B, swap the display and transform prices using 1 - price
        if (isTeamB) {
            // Team B perspective: Team A's bids become Team B's asks with transformed price
            // Best bid for Team B = 1 - best ask for Team A
            for (OrderBookEntry e : ob.getBids()) {
                Object priceDisplay;
                if (e.getPrice() < 0) {
                    priceDisplay = "MARKET";
                } else {
                    // Transform price: 1 - Team A price
                    priceDisplay = 1.0 - e.getPrice();
                }
                asksModel.addRow(new Object[] { priceDisplay, e.getQuantity() });
            }
            // Team B perspective: Team A's asks become Team B's bids with transformed price
            // Best ask for Team B = 1 - best bid for Team A
            for (OrderBookEntry e : ob.getAsks()) {
                Object priceDisplay;
                if (e.getPrice() < 0) {
                    priceDisplay = "MARKET";
                } else {
                    // Transform price: 1 - Team A price
                    priceDisplay = 1.0 - e.getPrice();
                }
                bidsModel.addRow(new Object[] { priceDisplay, e.getQuantity() });
            }
        } else {
            // Team A perspective: normal view
            for (OrderBookEntry e : ob.getAsks()) {
                Object priceDisplay = (e.getPrice() < 0) ? "MARKET" : e.getPrice();
                asksModel.addRow(new Object[] { priceDisplay, e.getQuantity() });
            }
            for (OrderBookEntry e : ob.getBids()) {
                Object priceDisplay = (e.getPrice() < 0) ? "MARKET" : e.getPrice();
                bidsModel.addRow(new Object[] { priceDisplay, e.getQuantity() });
            }
        }
    }

    /**
     * Maps the team selection and UI side selector to the actual order side.
     * Team A: UI side matches actual side (BUY -> BUY, SELL -> SELL)
     * Team B: UI side is opposite of actual side (BUY -> SELL, SELL -> BUY)
     */
    private Side getActualSide(Side uiSide, String team) {
        boolean isTeamB = teamBName.equals(team);
        if (isTeamB) {
            // Swap for Team B
            return uiSide == Side.BUY ? Side.SELL : Side.BUY;
        } else {
            // No swap for Team A
            return uiSide;
        }
    }

    private void refreshOpenOrders() {
        openOrdersModel.setRowCount(0);
        List<BookOrder> orders = placeOrderUseCase.openOrdersForUser(currentUserId);
        for (BookOrder o : orders) {
            openOrdersModel.addRow(new Object[] {
                    o.getSide().name(),
                    o.getMarketId(),
                    o.getPrice(),
                    o.getRemainingQty(),
                    o.getOriginalQty()
            });
        }
    }

    private void setStatus(String msg) {
        statusLabel.setText(msg);
    }
}
// // ================== RUNNABLE MAIN (for demo/testing) ==================
// public static void main(String[] args) {
//
// // 1. DataSource (Supabase)
// javax.sql.DataSource ds =
// stakemate.use_case.PlaceOrderUseCase.DataSourceFactory.create();
//
// // 2. Repositories
// stakemate.data_access.supabase.PostgresOrderRepository orderRepo =
// new stakemate.data_access.supabase.PostgresOrderRepository(ds);
// stakemate.data_access.supabase.PostgresPositionRepository positionRepo =
// new stakemate.data_access.supabase.PostgresPositionRepository(ds);
//
// // 3. Engine + account service
// stakemate.engine.MatchingEngine engine = new
// stakemate.engine.MatchingEngine();
// stakemate.service.InMemoryAccountService accountService =
// new stakemate.service.InMemoryAccountService();
//
// // Fake UUID users (because DB expects UUID for user_id)
// String user1 = "11111111-1111-1111-1111-111111111111";
// String user2 = "22222222-2222-2222-2222-222222222222";
// accountService.deposit(user1, 1000.0);
// accountService.deposit(user2, 1000.0);
//
// // 4. Use case
// PlaceOrderUseCase uc = new PlaceOrderUseCase(
// engine,
// accountService,
// orderRepo,
// positionRepo
// );
//
// // 5. Launch UI for user1
// SwingUtilities.invokeLater(() -> {
// OrderBookTradingFrame frame =
// new OrderBookTradingFrame(uc, user1, "MARKET-1");
// frame.setVisible(true);
// });
// }
// }
