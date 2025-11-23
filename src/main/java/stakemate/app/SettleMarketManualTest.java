package stakemate.app;

import stakemate.data_access.supabase.SupabaseBetRepository;
import stakemate.data_access.supabase.SupabaseAccountDataAccess;
import stakemate.data_access.supabase.SupabaseClientFactory;
import stakemate.data_access.in_memory.InMemorySettlementRecordRepository;
import stakemate.use_case.settle_market.AccountRepository;
import stakemate.use_case.settle_market.BetRepository;
import stakemate.use_case.settle_market.SettlementRecordRepository;
import stakemate.use_case.settle_market.SettleMarketInputBoundary;
import stakemate.use_case.settle_market.SettleMarketInteractor;
import stakemate.use_case.settle_market.SettleMarketOutputBoundary;
import stakemate.use_case.settle_market.SettleMarketRequestModel;
import stakemate.use_case.settle_market.SettleMarketResponseModel;
// thhis is a tmep class
public class SettleMarketManualTest {

    public static void main(String[] args) {
        // 1) Set up Supabase factory (uses your existing config / env)
        SupabaseClientFactory factory = new SupabaseClientFactory();

        // 2) Repositories for the use case
        BetRepository betRepository = new SupabaseBetRepository(factory);
        AccountRepository accountRepository = new SupabaseAccountDataAccess(factory);
        SettlementRecordRepository settlementRecordRepository =
            new InMemorySettlementRecordRepository();

        // 3) Simple presenter that just prints to console
        SettleMarketOutputBoundary presenter = new SettleMarketOutputBoundary() {
            @Override
            public void presentSuccess(SettleMarketResponseModel response) {
                System.out.println("=== Settlement SUCCESS ===");
                System.out.println("Market ID: " + response.getMarketId());
                System.out.println("Bets settled: " + response.getBetsSettled());
                System.out.println("Total net payout to winners: " + response.getTotalPayout());
            }

            @Override
            public void presentFailure(String errorMessage) {
                System.out.println("=== Settlement FAILED ===");
                System.out.println(errorMessage);
            }
        };

        // 4) Build the interactor
        SettleMarketInputBoundary interactor =
            new SettleMarketInteractor(
                betRepository,
                accountRepository,
                settlementRecordRepository,
                presenter
            );

        // 5) The marketId you manually put into positions
        String marketId = "cd627332-49c2-4ec0-abcf-12e2c245cd03";

        // NOTE: second arg is homeTeamWon, but your interactor doesn't use it anymore.
        SettleMarketRequestModel request =
            new SettleMarketRequestModel(marketId);

        // 6) Execute the use case
        interactor.execute(request);

        System.out.println("Done. Now check the 'profiles' table balances for bob & alice.");
    }
}
