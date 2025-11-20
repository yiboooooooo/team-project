package stakemate.data_access.in_memory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import stakemate.use_case.settle_market.SettlementRecord;
import stakemate.use_case.settle_market.SettlementRecordRepository;

public class InMemorySettlementRecordRepository implements SettlementRecordRepository {

    private final List<SettlementRecord> records = new ArrayList<>();

    @Override
    public Optional<SettlementRecord> findByMarketId(final String marketId) {
        for (final SettlementRecord r : records) {
            if (r.getMarketId().equals(marketId)) {
                return Optional.of(r);
            }
        }
        return Optional.empty();
    }

    @Override
    public void save(final SettlementRecord record) {
        records.add(record);
    }

    // Optional: for debugging/demo
    public List<SettlementRecord> findAll() {
        return new ArrayList<>(records);
    }
}
