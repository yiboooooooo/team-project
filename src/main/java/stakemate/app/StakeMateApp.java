package stakemate.app;

import javax.swing.SwingUtilities;

import stakemate.data_access.supabase.SupabaseClientFactory;
import stakemate.data_access.supabase.SupabaseUserDataAccess;

import stakemate.data_access.in_memory.FakeOrderBookGateway;
import stakemate.data_access.in_memory.InMemoryMarketRepository;
import stakemate.data_access.in_memory.InMemoryMatchRepository;

import stakemate.interface_adapter.controllers.LoginController;
import stakemate.interface_adapter.controllers.SignupController;

import stakemate.interface_adapter.view_login.SwingLoginPresenter;
import stakemate.interface_adapter.view_signup.SwingSignupPresenter;

import stakemate.use_case.login.LoginInteractor;
import stakemate.use_case.signup.SignupInteractor;

import stakemate.interface_adapter.view_market.SwingViewMarketsPresenter;
import stakemate.interface_adapter.view_market.ViewMarketController;
import stakemate.use_case.view_market.ViewMarketInteractor;

import stakemate.view.MarketsFrame;
import stakemate.view.LoginFrame;
import stakemate.view.SignupFrame;


public final class StakeMateApp {

    private StakeMateApp() {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // ==============================
            // Infrastructure for markets
            // ==============================

            InMemoryMatchRepository matchRepository = new InMemoryMatchRepository();
            InMemoryMarketRepository marketRepository = new InMemoryMarketRepository();
            FakeOrderBookGateway orderBookGateway = new FakeOrderBookGateway();

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
            // DB initialization and user stuff
            // ==============================

            SupabaseClientFactory supabaseFactory = new SupabaseClientFactory();
            SupabaseUserDataAccess userRepo = new SupabaseUserDataAccess(supabaseFactory);

            // ==============================
            // Login & Signup frames
            // ==============================

            // Create frames first, so we can pass references:
            LoginFrame loginFrame   = new LoginFrame(marketsFrame); // you might also pass signup later
            SignupFrame signupFrame = new SignupFrame(loginFrame);

            // ----- Login wiring -----
            SwingLoginPresenter loginPresenter =
                    new SwingLoginPresenter(loginFrame);

            LoginInteractor loginInteractor =
                    new LoginInteractor(userRepo, loginPresenter);

            LoginController loginController =
                    new LoginController(loginInteractor);

            loginFrame.setController(loginController);
            // Make loginFrame aware of signupFrame (e.g. for a "Sign up" button)
            loginFrame.setSignupFrame(signupFrame);   // Add this setter in LoginFrame

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
