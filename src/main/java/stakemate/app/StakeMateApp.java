package stakemate.app;

import javax.swing.SwingUtilities;

import stakemate.data_access.csv.CsvUserDataAccess;

import stakemate.data_access.in_memory.*;

import stakemate.entity.Side;
import stakemate.entity.User;
import stakemate.interface_adapter.controllers.LoginController;
import stakemate.interface_adapter.controllers.SignupController;

import stakemate.interface_adapter.view_login.SwingLoginPresenter;
import stakemate.interface_adapter.view_signup.SwingSignupPresenter;

import stakemate.use_case.login.LoginInteractor;
import stakemate.use_case.settle_market.Bet;
import stakemate.use_case.settle_market.SettleMarketInteractor;
import stakemate.use_case.signup.SignupInteractor;

import stakemate.interface_adapter.view_market.SwingViewMarketsPresenter;
import stakemate.interface_adapter.view_market.ViewMarketController;
import stakemate.use_case.view_market.ViewMarketInteractor;

import stakemate.view.MarketsFrame;
import stakemate.view.LoginFrame;
import stakemate.view.SignupFrame;

// NEW for UC6 GUI wiring
import stakemate.interface_adapter.view_market.SwingSettleMarketPresenter;
import stakemate.interface_adapter.controllers.SettleMarketController;

public final class StakeMateApp {
    public static InMemoryAccountRepository accountRepo;
    public static InMemoryBetRepository betRepo;

    private StakeMateApp() {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // ==============================
            // Infrastructure for markets
            // ==============================


            InMemoryMatchRepository matchRepository = new InMemoryMatchRepository();
            InMemoryMarketRepository marketRepository = new InMemoryMarketRepository();
            FakeOrderBookGateway   orderBookGateway  = new FakeOrderBookGateway();

            betRepo    = new InMemoryBetRepository();
            InMemorySettlementRecordRepository recordRepo = new InMemorySettlementRecordRepository();
            accountRepo = new InMemoryAccountRepository();

            MarketsFrame marketsFrame = new MarketsFrame();

            SwingViewMarketsPresenter marketsPresenter =
                    new SwingViewMarketsPresenter(marketsFrame);

            ViewMarketInteractor marketInteractor =
                    new ViewMarketInteractor(
                            matchRepository,
                            marketRepository,
                            orderBookGateway,
                            marketsPresenter
                    );

            ViewMarketController marketController =
                    new ViewMarketController(marketInteractor);
            marketsFrame.setController(marketController);

            // ==============================
            // UC6 Settlement (using same repos)
            // ==============================

            // Demo data: two users and two bets on the same market
            accountRepo.addDemoUser(new User("alice", "password", 1000));
            accountRepo.addDemoUser(new User("bob",   "password", 1000));

            betRepo.addDemoBet(new Bet("alice", "M1-ML", Side.BUY, 50, 0.6));
            betRepo.addDemoBet(new Bet("bob",   "M1-ML", Side.SELL, 50, 0.4));


            SwingSettleMarketPresenter settlePresenter =
                    new SwingSettleMarketPresenter(marketsFrame);

            SettleMarketInteractor settleInteractor =
                    new SettleMarketInteractor(betRepo, accountRepo, recordRepo, settlePresenter);

            SettleMarketController settleController =
                    new SettleMarketController(settleInteractor);
            marketsFrame.setSettleMarketController(settleController);

            // ==============================
            // User repository (login + signup)
            // ==============================

            CsvUserDataAccess userRepo = new CsvUserDataAccess("users.csv");

            // ==============================
            // Login & Signup frames
            // ==============================

            LoginFrame loginFrame   = new LoginFrame(marketsFrame);
            SignupFrame signupFrame = new SignupFrame(loginFrame);

            // ----- Login wiring -----
            SwingLoginPresenter loginPresenter =
                    new SwingLoginPresenter(loginFrame);

            LoginInteractor loginInteractor =
                    new LoginInteractor(userRepo, loginPresenter);

            LoginController loginController =
                    new LoginController(loginInteractor);

            loginFrame.setController(loginController);
            loginFrame.setSignupFrame(signupFrame);

            // ----- Signup wiring -----
            SwingSignupPresenter signupPresenter =
                    new SwingSignupPresenter(signupFrame);

            SignupInteractor signupInteractor =
                    new SignupInteractor(userRepo, signupPresenter);

            SignupController signupController =
                    new SignupController(signupInteractor);

            signupFrame.setController(signupController);

            // ==============================
            // Launch app at login
            // ==============================

            loginFrame.setVisible(true);
            // Markets will be shown *after* login inside LoginFrame.onLoginSuccess()
        });
    }
}
