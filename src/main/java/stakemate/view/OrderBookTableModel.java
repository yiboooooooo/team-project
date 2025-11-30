package stakemate.view;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import javax.swing.table.AbstractTableModel;

import stakemate.entity.OrderBook;
import stakemate.entity.OrderBookEntry;

/**
 * Table model for displaying the Order Book.
 */
public class OrderBookTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {
        "Bid Size", "Bid Price", "Ask Price", "Ask Size",
    };

    private static final int COL_BID_QTY = 0;
    private static final int COL_BID_PRICE = 1;
    private static final int COL_ASK_PRICE = 2;
    private static final int COL_ASK_QTY = 3;

    private final List<Row> rows = new ArrayList<>();

    /**
     * Updates the table with a new OrderBook snapshot.
     *
     * @param orderBook The new order book data.
     */
    public void setOrderBook(final OrderBook orderBook) {
        if (orderBook == null) {
            clear();
        }
        else {
            final List<OrderBookEntry> bids = new ArrayList<>(orderBook.getBids());
            final List<OrderBookEntry> asks = new ArrayList<>(orderBook.getAsks());

            bids.sort(Comparator.comparingDouble(OrderBookEntry::getPrice).reversed());
            asks.sort(Comparator.comparingDouble(OrderBookEntry::getPrice));

            final List<Row> newRows = new ArrayList<>();
            final int max = Math.max(bids.size(), asks.size());
            for (int i = 0; i < max; i++) {
                final OrderBookEntry bid;
                if (i < bids.size()) {
                    bid = bids.get(i);
                }
                else {
                    bid = null;
                }

                final OrderBookEntry ask;
                if (i < asks.size()) {
                    ask = asks.get(i);
                }
                else {
                    ask = null;
                }
                newRows.add(new Row(bid, ask));
            }

            if (!rows.equals(newRows)) {
                rows.clear();
                rows.addAll(newRows);
                fireTableDataChanged();
            }
        }
    }

    /**
     * Clears all data from the table.
     */
    public void clear() {
        if (!rows.isEmpty()) {
            rows.clear();
            fireTableDataChanged();
        }
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
        final Object result;
        final Row row = rows.get(rowIndex);
        switch (columnIndex) {
            case COL_BID_QTY:
                result = formatQty(row.bidQty);
                break;
            case COL_BID_PRICE:
                result = formatVal(row.bidPrice);
                break;
            case COL_ASK_PRICE:
                result = formatVal(row.askPrice);
                break;
            case COL_ASK_QTY:
                result = formatQty(row.askQty);
                break;
            default:
                result = "";
                break;
        }
        return result;
    }

    private String formatVal(final Double val) {
        final String res;
        if (val == null || val == 0.0) {
            res = "-";
        }
        else {
            res = String.format("$%.2f", val);
        }
        return res;
    }

    private String formatQty(final Double val) {
        final String res;
        if (val == null) {
            res = "-";
        }
        else {
            res = String.valueOf(val);
        }
        return res;
    }

    /**
     * Internal row representation.
     */
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

        @Override
        public boolean equals(final Object o) {
            boolean result = false;
            if (this == o) {
                result = true;
            }
            else if (o != null && getClass() == o.getClass()) {
                final Row row = (Row) o;
                result = Objects.equals(bidQty, row.bidQty)
                    && Objects.equals(bidPrice, row.bidPrice)
                    && Objects.equals(askPrice, row.askPrice)
                    && Objects.equals(askQty, row.askQty);
            }
            return result;
        }

        @Override
        public int hashCode() {
            return Objects.hash(bidQty, bidPrice, askPrice, askQty);
        }
    }
}
