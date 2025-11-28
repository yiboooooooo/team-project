package stakemate.use_case.settle_market;

/**
 * Output Boundary for the Settle Market use case.
 * Defines methods to present the result of the settlement operation.
 */
public interface SettleMarketOutputBoundary {

    /**
     * Presents a successful settlement response.
     *
     * @param responseModel the data containing settlement details.
     */
    void presentSuccess(SettleMarketResponseModel responseModel);

    /**
     * Presents a failure message when settlement cannot be completed.
     *
     * @param errorMessage the error message explaining the failure.
     */
    void presentFailure(String errorMessage);
}
