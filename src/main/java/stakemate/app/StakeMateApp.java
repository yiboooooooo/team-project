package stakemate.app;

import stakemate.data_access.api.OddsApiGatewayImpl;
import stakemate.data_access.api.OddsApiResponseAdapter;
import stakemate.data_access.in_memory.*;
import stakemate.data_access.supabase.SupabaseClientFactory;
import stakemate.data_access.supabase.SupabaseGameRepository;
import stakemate.data_access.supabase.SupabaseUserDataAccess;
import stakemate.entity.Game;
import stakemate.entity.Side;
import stakemate.entity.User;
import stakemate.interface_adapter.controllers.LoginController;
import stakemate.interface_adapter.controllers.SettleMarketController;
import stakemate.interface_adapter.controllers.SignupController;
import stakemate.interface_adapter.view_login.SwingLoginPresenter;
import stakemate.interface_adapter.view_market.SwingSettleMarketPresenter;
import stakemate.interface_adapter.view_market.SwingViewMarketsPresenter;
import stakemate.interface_adapter.view_market.ViewMarketController;
import stakemate.interface_adapter.view_signup.SwingSignupPresenter;
import stakemate.use_case.fetch_games.FetchGamesInteractor;
import stakemate.use_case.fetch_games.FetchGamesOutputBoundary;
import stakemate.use_case.fetch_games.FetchGamesResponseModel;
import stakemate.use_case.login.LoginInteractor;
import stakemate.use_case.settle_market.Bet;
import stakemate.use_case.settle_market.SettleMarketInteractor;
import stakemate.use_case.signup.SignupInteractor;
import stakemate.use_case.view_market.ViewMarketInteractor;
import stakemate.view.LoginFrame;
import stakemate.view.MarketsFrame;
import stakemate.view.SignupFrame;

import javax.swing.*;

public final class StakeMateApp {
    public static InMemoryAccountRepository accountRepo;
    public static InMemoryBetRepository betRepo;

    private StakeMateApp() {
    }

    public static void main(String[] args) {
        // Load .env file if it exists
        loadEnvFile();

        SwingUtilities.invokeLater(() -> {
            // ==============================
            // Infrastructure for markets
            // ==============================

            // Create Supabase factory for games
            SupabaseClientFactory gamesSupabaseFactory = new SupabaseClientFactory();
            SupabaseGameRepository gameRepository = new SupabaseGameRepository(gamesSupabaseFactory);

            // Create API fetching components
            String apiKey = getEnvVar("ODDS_API_KEY");
            if (apiKey == null || apiKey.isEmpty()) {
                System.err.println("WARNING: ODDS_API_KEY not set. Using default hardcoded matches.");
            }

            FetchGamesInteractor fetchGamesInteractor = null;
            if (apiKey != null && !apiKey.isEmpty()) {
                OddsApiGatewayImpl apiGateway = new OddsApiGatewayImpl(apiKey);
                OddsApiResponseAdapter responseAdapter = new OddsApiResponseAdapter();

                // Simple presenter that logs results
                FetchGamesOutputBoundary presenter = new FetchGamesOutputBoundary() {
                    @Override
                    public void presentFetchInProgress() {
                        System.out.println("Fetching games from API...");
                    }

                    @Override
                    public void presentFetchSuccess(FetchGamesResponseModel response) {
                        System.out.println("API fetch completed: " + response.getMessage());
                    }

                    @Override
                    public void presentFetchError(String error) {
                        System.err.println("API fetch error: " + error);
                    }

                    @Override
                    public void presentSearchResults(java.util.List<Game> games, String query) {
                        System.out.println("Search found " + games.size() + " games for: " + query);
                    }
                };

                fetchGamesInteractor = new FetchGamesInteractor(
                        apiGateway, responseAdapter, gameRepository, presenter
                );
            }

            InMemoryMatchRepository matchRepository = new InMemoryMatchRepository(gameRepository, fetchGamesInteractor);
            InMemoryMarketRepository marketRepository = new InMemoryMarketRepository();
            FakeOrderBookGateway orderBookGateway = new FakeOrderBookGateway();

            betRepo = new InMemoryBetRepository();
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
            accountRepo.addDemoUser(new User("bob", "password", 1000));

            betRepo.addDemoBet(new Bet("alice", "M1-ML", Side.BUY, 50, 0.6));
            betRepo.addDemoBet(new Bet("bob", "M1-ML", Side.SELL, 50, 0.4));

            SwingSettleMarketPresenter settlePresenter =
                    new SwingSettleMarketPresenter(marketsFrame);

            SettleMarketInteractor settleInteractor =
                    new SettleMarketInteractor(betRepo, accountRepo, recordRepo, settlePresenter);

            SettleMarketController settleController =
                    new SettleMarketController(settleInteractor);
            marketsFrame.setSettleMarketController(settleController);

            // ==============================
            // User data access 
            // ==============================

            SupabaseClientFactory supabaseFactory = new SupabaseClientFactory();
            SupabaseUserDataAccess userRepo = new SupabaseUserDataAccess(supabaseFactory);

            // ==============================
            // Login & Signup frames
            // ==============================

            LoginFrame loginFrame = new LoginFrame(marketsFrame);
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

    /**
     * Loads environment variables from .env file in project root.
     * Format: KEY=value (one per line, no quotes needed)
     */
    private static void loadEnvFile() {
        try {
            java.io.File envFile = new java.io.File(".env");
            if (!envFile.exists()) {
                return; // No .env file, use system env vars only
            }

            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.FileReader(envFile)
            );

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue; // Skip empty lines and comments
                }

                int equalIndex = line.indexOf('=');
                if (equalIndex > 0) {
                    String key = line.substring(0, equalIndex).trim();
                    String value = line.substring(equalIndex + 1).trim();

                    // Only set if not already in system env
                    if (System.getenv(key) == null) {
                        System.setProperty(key, value);
                    }
                }
            }
            reader.close();
        } catch (java.io.IOException e) {
            System.err.println("Warning: Could not read .env file: " + e.getMessage());
        }
    }

    /**
     * Gets environment variable from system env or system properties (.env file).
     */
    private static String getEnvVar(String key) {
        String value = System.getenv(key);
        if (value == null) {
            value = System.getProperty(key);
        }
        return value;
    }
}
