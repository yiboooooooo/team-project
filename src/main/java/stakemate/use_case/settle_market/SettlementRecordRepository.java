package stakemate.use_case.settle_market;


import java.util.Optional;

public interface SettlementRecordRepository {

    Optional<SettlementRecord> findByMarketId(String marketId);

    void save(SettlementRecord record);
}