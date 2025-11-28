package stakemate.use_case.settle_market;

import java.util.Optional;

/**
 * Repository interface for accessing and persisting settlement records.
 */
public interface SettlementRecordRepository {

    /**
     * Finds a settlement record by the associated market ID.
     *
     * @param marketId the ID of the market to search for.
     * @return an Optional containing the settlement record if found, or empty otherwise.
     */
    Optional<SettlementRecord> findByMarketId(String marketId);

    /**
     * Saves a settlement record to the repository.
     *
     * @param record the SettlementRecord entity to save.
     */
    void save(SettlementRecord record);
}
