package stakemate.view;

import stakemate.interface_adapter.view_market.MarketsView;
import stakemate.interface_adapter.view_market.ViewMarketController;
import stakemate.use_case.view_market.MatchSummary;
import stakemate.use_case.view_market.MarketSummary;
import stakemate.use_case.view_market.MarketsResponseModel;
import stakemate.use_case.view_market.OrderBookResponseModel;
import stakemate.entity.OrderBook;
import stakemate.entity.OrderBookEntry;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MarketsFrame extends JFrame implements MarketsView {

    private ViewMarketController controller;

    private final DefaultListModel<MatchSummary> matchesListModel =
            new DefaultListModel<>();
    private final JList<MatchSummary> matchesList =
            new JList<>(matchesListModel);
    private final JLabel matchesEmptyLabel = new JLabel(" ");

    private final DefaultListModel<MarketSummary> marketsListModel =
            new DefaultListModel<>();
    private final JList<MarketSummary> marketsList =
            new JList<>(marketsListModel);
    private final JLabel marketsEmptyLabel = new JLabel(" ");

    private final OrderBookTableModel orderBookTableModel = new OrderBookTableModel();
    private final JTable orderBookTable = new JTable(orderBookTableModel);
    private final JLabel orderBookEmptyLabel = new JLabel("Select a market to see orders.");

    private final JLabel statusLabel = new JLabel(" ");
    private final JButton buyButton = new JButton("Buy");
    private final JButton sellButton = new JButton("Sell");
    private final JButton refreshButton = new JButton("Refresh");

    private MarketSummary currentlySelectedMarket;

    public MarketsFrame() {
        super("StakeMate - Markets & Order Book");
        initUi();
    }

    public void setController(ViewMarketController controller) {
        this.controller = controller;
        hookEvents();
    }

    private void initUi() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 600);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topBar.add(refreshButton);

        matchesEmptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        marketsEmptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        orderBookEmptyLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Left: matches
        JPanel leftPanel = new JPanel(new BorderLayout(4, 4));
        JLabel matchesLabel = new JLabel("Matches");
        leftPanel.add(matchesLabel, BorderLayout.NORTH);
        matchesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        leftPanel.add(new JScrollPane(matchesList), BorderLayout.CENTER);
        leftPanel.add(matchesEmptyLabel, BorderLayout.SOUTH);

        // Right top: markets
        JPanel marketsPanel = new JPanel(new BorderLayout(4, 4));
        JLabel marketsLabel = new JLabel("Markets");
        marketsPanel.add(marketsLabel, BorderLayout.NORTH);
        marketsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        marketsPanel.add(new JScrollPane(marketsList), BorderLayout.CENTER);
        marketsPanel.add(marketsEmptyLabel, BorderLayout.SOUTH);

        // Right bottom: order book + controls
        JPanel orderBookPanel = new JPanel(new BorderLayout(4, 4));
        JLabel orderBookLabel = new JLabel("Order Book");
        orderBookPanel.add(orderBookLabel, BorderLayout.NORTH);
        orderBookTable.setFillsViewportHeight(true);
        orderBookPanel.add(new JScrollPane(orderBookTable), BorderLayout.CENTER);
        orderBookPanel.add(orderBookEmptyLabel, BorderLayout.SOUTH);

        JPanel buySellPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buyButton.setEnabled(false);
        sellButton.setEnabled(false);
        buySellPanel.add(buyButton);
        buySellPanel.add(sellButton);

        JPanel rightPanel = new JPanel(new BorderLayout(4, 4));
        rightPanel.add(marketsPanel, BorderLayout.NORTH);
        rightPanel.add(orderBookPanel, BorderLayout.CENTER);
        rightPanel.add(buySellPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                leftPanel,
                rightPanel
        );
        splitPane.setDividerLocation(280);

        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);

        root.add(topBar, BorderLayout.NORTH);
        root.add(splitPane, BorderLayout.CENTER);
        root.add(statusLabel, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void hookEvents() {
        refreshButton.addActionListener(e -> {
            if (controller != null) {
                controller.refresh();
            }
        });

        matchesList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && controller != null) {
                MatchSummary selected = matchesList.getSelectedValue();
                currentlySelectedMarket = null;
                controller.onMatchSelected(selected);
            }
        });

        marketsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && controller != null) {
                MarketSummary selected = marketsList.getSelectedValue();
                currentlySelectedMarket = selected;
                controller.onMarketSelected(selected);
            }
        });

        buyButton.addActionListener(e ->
                JOptionPane.showMessageDialog(
                        MarketsFrame.this,
                        "Buy clicked. (Use Case 4 will handle order placement.)"
                )
        );

        sellButton.addActionListener(e ->
                JOptionPane.showMessageDialog(
                        MarketsFrame.this,
                        "Sell clicked. (Use Case 4 will handle order placement.)"
                )
        );
    }

    // ---- MarketsView implementation ----

    @Override
    public void showMatches(List<MatchSummary> matches, String emptyStateMessage) {
        matchesListModel.clear();
        if (matches == null || matches.isEmpty()) {
            matchesEmptyLabel.setText(
                    emptyStateMessage != null ? emptyStateMessage : "No matches."
            );
        } else {
            for (MatchSummary m : matches) {
                matchesListModel.addElement(m);
            }
            matchesEmptyLabel.setText(" ");
        }
    }

    @Override
    public void showMarketsForMatch(MarketsResponseModel responseModel) {
        setTitle("StakeMate - " + responseModel.getMatchTitle());

        marketsListModel.clear();
        currentlySelectedMarket = null;
        if (responseModel.getMarkets() == null || responseModel.getMarkets().isEmpty()) {
            marketsEmptyLabel.setText(
                    responseModel.getEmptyStateMessage() != null
                            ? responseModel.getEmptyStateMessage()
                            : "No markets for this match."
            );
        } else {
            for (MarketSummary m : responseModel.getMarkets()) {
                marketsListModel.addElement(m);
            }
            marketsEmptyLabel.setText(" ");
        }

        // Clear order book when switching match
        orderBookTableModel.clear();
        orderBookEmptyLabel.setText("Select a market to see orders.");
        buyButton.setEnabled(false);
        sellButton.setEnabled(false);
    }

    @Override
    public void showOrderBook(OrderBookResponseModel responseModel) {
        if (responseModel.getOrderBook() != null) {
            orderBookTableModel.setOrderBook(responseModel.getOrderBook());
        }

        if (responseModel.isReconnecting()) {
            statusLabel.setText(responseModel.getMessage() != null
                    ? responseModel.getMessage()
                    : "Reconnecting...");
        } else if (responseModel.getMessage() != null) {
            statusLabel.setText(responseModel.getMessage());
        } else {
            statusLabel.setText(" ");
        }

        if (responseModel.isEmpty()) {
            orderBookEmptyLabel.setText(
                    responseModel.getMessage() != null
                            ? responseModel.getMessage()
                            : "No orders yet"
            );
        } else if (!responseModel.isReconnecting()) {
            orderBookEmptyLabel.setText(" ");
        }

        boolean enableBuySell = false;
        if (currentlySelectedMarket != null
                && currentlySelectedMarket.isBuySellEnabled()
                && !responseModel.isReconnecting()) {
            enableBuySell = true;
        }

        buyButton.setEnabled(enableBuySell);
        sellButton.setEnabled(enableBuySell);
    }

    @Override
    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ---- Order Book Table Model ----

    private static final class OrderBookTableModel extends AbstractTableModel {

        private static final String[] COLUMNS = {
                "Bid Size", "Bid Price", "Ask Price", "Ask Size"
        };

        private final List<Row> rows = new ArrayList<>();

        public void setOrderBook(OrderBook orderBook) {
            rows.clear();

            List<OrderBookEntry> bids = new ArrayList<>(orderBook.getBids());
            List<OrderBookEntry> asks = new ArrayList<>(orderBook.getAsks());

            bids.sort(Comparator.comparingDouble(OrderBookEntry::getPrice).reversed());
            asks.sort(Comparator.comparingDouble(OrderBookEntry::getPrice));

            int max = Math.max(bids.size(), asks.size());
            for (int i = 0; i < max; i++) {
                OrderBookEntry bid = i < bids.size() ? bids.get(i) : null;
                OrderBookEntry ask = i < asks.size() ? asks.get(i) : null;
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
        public String getColumnName(int column) {
            return COLUMNS[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Row row = rows.get(rowIndex);
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

            Row(OrderBookEntry bid, OrderBookEntry ask) {
                this.bidQty = bid != null ? bid.getQuantity() : null;
                this.bidPrice = bid != null ? bid.getPrice() : null;
                this.askPrice = ask != null ? ask.getPrice() : null;
                this.askQty = ask != null ? ask.getQuantity() : null;
            }
        }
    }
}
