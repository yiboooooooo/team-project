package stakemate.use_case.settle_market;

/**
 * Input Boundary for the Settle Market use case.
 */
public interface SettleMarketInputBoundary {

    /**
     * Executes the settlement logic for a specific market.
     *
     * @param requestModel the request data containing the market ID to settle.
     */
    void execute(SettleMarketRequestModel requestModel);
}
