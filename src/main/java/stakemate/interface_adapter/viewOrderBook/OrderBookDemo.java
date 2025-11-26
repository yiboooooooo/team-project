package stakemate.interface_adapter.viewOrderBook;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;

import javax.sql.DataSource;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import stakemate.data_access.supabase.PostgresOrderRepository;
import stakemate.engine.MatchingEngine;
import stakemate.engine.Trade;
import stakemate.entity.OrderBook;
import stakemate.entity.OrderBookEntry;
import stakemate.entity.Side;
import stakemate.service.AccountService;
import stakemate.service.InMemoryAccountService;
import stakemate.use_case.PlaceOrderUseCase.*;
import stakemate.use_case.PlaceOrderUseCase.DataSourceFactory;
import stakemate.data_access.supabase.PostgresPositionRepository;
import stakemate.data_access.supabase.PostgresOrderRepository;

/**
 * Self-contained Swing demo UI for the matching engine.
 * <p>
 * Put this file at: src/main/java/stakemate/ui/OrderBookSwingDemo.java
 */
public class OrderBookDemo {

    private final PlaceOrderUseCase useCase;
    private final MatchingEngine engine;
    private final AccountService accountService;

    private final JFrame frame = new JFrame("Order Book Demo");
    private final DefaultTableModel bidsModel = new DefaultTableModel(new String[]{"Price", "Qty"}, 0);
    private final DefaultTableModel asksModel = new DefaultTableModel(new String[]{"Price", "Qty"}, 0);
    private final JTextArea tradesLog = new JTextArea(10, 50);
    private final JTextField userField = new JTextField("alice", 8);
    DataSource ds = DataSourceFactory.create();
    OrderRepository repository = new PostgresOrderRepository(ds);
    PositionRepository positionRepo = new PostgresPositionRepository(ds);


    public OrderBookDemo() {
        this.engine = new MatchingEngine();
        this.accountService = new InMemoryAccountService();
        this.useCase = new PlaceOrderUseCase(engine, accountService, repository, positionRepo);

        final JPanel controls = new JPanel();

        final JComboBox<Side> sideBox = new JComboBox<>(Side.values());
        final JCheckBox marketCheck = new JCheckBox("Market");
        final JTextField priceField = new JTextField("1.5", 6);
        final JTextField qtyField = new JTextField("10", 6);
        final JButton place = new JButton("Place Order");

        controls.add(new JLabel("User:"));
        controls.add(userField);
        controls.add(new JLabel("Side:"));
        controls.add(sideBox);
        controls.add(new JLabel("Price:"));
        controls.add(priceField);
        controls.add(new JLabel("Qty:"));
        controls.add(qtyField);
        controls.add(marketCheck);
        controls.add(place);

        final JTable bidsTable = new JTable(bidsModel);
        final JTable asksTable = new JTable(asksModel);

        final JPanel bookPanel = new JPanel(new GridLayout(1, 2));
        bookPanel.add(new JScrollPane(bidsTable));
        bookPanel.add(new JScrollPane(asksTable));

        frame.setLayout(new BorderLayout());
        frame.add(controls, BorderLayout.NORTH);
        frame.add(bookPanel, BorderLayout.CENTER);

        tradesLog.setEditable(false);
        frame.add(new JScrollPane(tradesLog), BorderLayout.SOUTH);

        place.addActionListener(e -> {
            try {
                final String user = userField.getText().trim();
                final Side side = (Side) sideBox.getSelectedItem();
                final boolean isMarket = marketCheck.isSelected();
                final Double price = isMarket ? null : Double.parseDouble(priceField.getText().trim());
                final double qty = Double.parseDouble(qtyField.getText().trim());

                final PlaceOrderRequest req = new PlaceOrderRequest(user, "demo-market", side, price, qty);
                final PlaceOrderResponse res = useCase.place(req);
                JOptionPane.showMessageDialog(frame, res.message);
                final List<Trade> trades = useCase.recentTrades();
                tradesLog.setText("");
                for (final Trade t : trades) {
                    tradesLog.append(t.toString() + "\n");
                }

                refreshBook();
            }
            catch (final Exception ex) {
                JOptionPane.showMessageDialog(frame, "Invalid input: " + ex.getMessage());
            }
        });

        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
    }

    public OrderBookDemo(final PlaceOrderUseCase useCase, final MatchingEngine engine, final AccountService accountService) {
        this.useCase = useCase;
        this.engine = engine;
        this.accountService = accountService;
    }

    public static void main(final String[] args) {
        // ensure we run on the EDT
        SwingUtilities.invokeLater(() -> {
            final OrderBookDemo demo = new OrderBookDemo();
            demo.show();
        });
    }

    private void refreshBook() {
        // engine.snapshotOrderBook("demo-market")
        final OrderBook ob = useCase.snapshot("demo-market");
        bidsModel.setRowCount(0);
        asksModel.setRowCount(0);

        if (ob != null) {
            for (final OrderBookEntry be : ob.getBids()) {
                bidsModel.addRow(new Object[]{String.format("%.2f", be.getPrice()), String.format("%.2f", be.getQuantity())});
            }
            for (final OrderBookEntry ae : ob.getAsks()) {
                asksModel.addRow(new Object[]{String.format("%.2f", ae.getPrice()), String.format("%.2f", ae.getQuantity())});
            }
        }
    }

    public void show() {
        frame.setVisible(true);
    }
}

