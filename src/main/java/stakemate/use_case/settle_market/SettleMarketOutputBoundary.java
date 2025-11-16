package stakemate.use_case.settle_market;

public interface SettleMarketOutputBoundary {

    void presentSuccess(SettleMarketResponseModel responseModel);

    void presentFailure(String errorMessage);
}