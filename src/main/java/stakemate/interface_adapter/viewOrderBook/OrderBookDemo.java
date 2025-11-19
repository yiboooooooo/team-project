package stakemate.interface_adapter.viewOrderBook;

import stakemate.engine.MatchingEngine;
import stakemate.engine.Trade;
import stakemate.entity.OrderBook;
import stakemate.entity.OrderBookEntry;
import stakemate.entity.Side;
import stakemate.service.AccountService;
import stakemate.service.InMemoryAccountService;
import stakemate.use_case.PlaceOrderUseCase.PlaceOrderRequest;
import stakemate.use_case.PlaceOrderUseCase.PlaceOrderResponse;
import stakemate.use_case.PlaceOrderUseCase.PlaceOrderUseCase;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

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

    public OrderBookDemo() {
        this.engine = new MatchingEngine();
        this.accountService = new InMemoryAccountService();
        this.useCase = new PlaceOrderUseCase(engine, accountService);

        JPanel controls = new JPanel();

        JComboBox<Side> sideBox = new JComboBox<>(Side.values());
        JCheckBox marketCheck = new JCheckBox("Market");
        JTextField priceField = new JTextField("1.5", 6);
        JTextField qtyField = new JTextField("10", 6);
        JButton place = new JButton("Place Order");

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

        JTable bidsTable = new JTable(bidsModel);
        JTable asksTable = new JTable(asksModel);

        JPanel bookPanel = new JPanel(new GridLayout(1, 2));
        bookPanel.add(new JScrollPane(bidsTable));
        bookPanel.add(new JScrollPane(asksTable));

        frame.setLayout(new BorderLayout());
        frame.add(controls, BorderLayout.NORTH);
        frame.add(bookPanel, BorderLayout.CENTER);

        tradesLog.setEditable(false);
        frame.add(new JScrollPane(tradesLog), BorderLayout.SOUTH);

        place.addActionListener(e -> {
            try {
                String user = userField.getText().trim();
                Side side = (Side) sideBox.getSelectedItem();
                boolean isMarket = marketCheck.isSelected();
                Double price = isMarket ? null : Double.parseDouble(priceField.getText().trim());
                double qty = Double.parseDouble(qtyField.getText().trim());

                PlaceOrderRequest req = new PlaceOrderRequest(user, "demo-market", side, price, qty);
                PlaceOrderResponse res = useCase.place(req);
                JOptionPane.showMessageDialog(frame, res.message);

                // show trades
                List<Trade> trades = useCase.recentTrades();
                tradesLog.setText("");
                for (Trade t : trades) tradesLog.append(t.toString() + "\n");

                refreshBook();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Invalid input: " + ex.getMessage());
            }
        });

        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
    }

    public OrderBookDemo(PlaceOrderUseCase useCase, MatchingEngine engine, AccountService accountService) {
        this.useCase = useCase;
        this.engine = engine;
        this.accountService = accountService;
    }

    public static void main(String[] args) {
        // ensure we run on the EDT
        SwingUtilities.invokeLater(() -> {
            OrderBookDemo demo = new OrderBookDemo();
            demo.show();
        });
    }

    private void refreshBook() {
        // engine.snapshotOrderBook("demo-market")
        OrderBook ob = useCase.snapshot("demo-market");
        bidsModel.setRowCount(0);
        asksModel.setRowCount(0);

        if (ob != null) {
            for (OrderBookEntry be : ob.getBids()) {
                bidsModel.addRow(new Object[]{String.format("%.2f", be.getPrice()), String.format("%.2f", be.getQuantity())});
            }
            for (OrderBookEntry ae : ob.getAsks()) {
                asksModel.addRow(new Object[]{String.format("%.2f", ae.getPrice()), String.format("%.2f", ae.getQuantity())});
            }
        }
    }

    public void show() {
        frame.setVisible(true);
    }
}

