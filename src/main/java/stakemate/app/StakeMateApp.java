package stakemate.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import javax.swing.SwingUtilities;

import stakemate.data_access.api.OddsApiGatewayImpl;
import stakemate.data_access.api.OddsApiResponseAdapter;
import stakemate.data_access.in_memory.FakeOrderBookGateway;
import stakemate.data_access.in_memory.InMemoryAccountRepository;
import stakemate.data_access.in_memory.InMemoryBetRepository;
import stakemate.data_access.in_memory.InMemoryCommentRepository;
import stakemate.data_access.in_memory.InMemoryMarketRepository;
import stakemate.data_access.in_memory.InMemoryMatchRepository;
import stakemate.data_access.in_memory.InMemorySettlementRecordRepository;
import stakemate.data_access.supabase.SupabaseClientFactory;
import stakemate.data_access.supabase.SupabaseGameRepository;
import stakemate.data_access.supabase.SupabaseUserDataAccess;
import stakemate.entity.Game;
import stakemate.entity.Side;
import stakemate.entity.User;
import stakemate.interface_adapter.controllers.LoginController;
import stakemate.interface_adapter.controllers.SettleMarketController;
import stakemate.interface_adapter.controllers.SignupController;
import stakemate.interface_adapter.view_comments.PostCommentController;
import stakemate.interface_adapter.view_comments.SwingPostCommentPresenter;
import stakemate.interface_adapter.view_comments.SwingViewCommentsPresenter;
import stakemate.interface_adapter.view_comments.ViewCommentsController;
import stakemate.interface_adapter.view_login.SwingLoginPresenter;
import stakemate.interface_adapter.view_market.SwingSettleMarketPresenter;
import stakemate.interface_adapter.view_market.SwingViewMarketsPresenter;
import stakemate.interface_adapter.view_market.ViewMarketController;
import stakemate.interface_adapter.view_signup.SwingSignupPresenter;
import stakemate.use_case.comments.post.PostCommentInteractor;
import stakemate.use_case.comments.view.ViewCommentsInteractor;
import stakemate.use_case.fetch_games.FetchGamesInteractor;
import stakemate.use_case.fetch_games.FetchGamesOutputBoundary;
import stakemate.use_case.fetch_games.FetchGamesResponseModel;
import stakemate.interface_adapter.view_live.LiveMatchesController;
import stakemate.interface_adapter.view_live.SwingLiveMatchesPresenter;
import stakemate.use_case.view_live.LiveMatchesInteractor;
import stakemate.view.LiveMatchesFrame;
import stakemate.use_case.login.LoginInteractor;
import stakemate.use_case.settle_market.Bet;
import stakemate.use_case.settle_market.SettleMarketInteractor;
import stakemate.use_case.signup.SignupInteractor;
import stakemate.use_case.view_market.ViewMarketInteractor;
import stakemate.use_case.view_market.facade.MarketDataFacade;
import stakemate.view.LoginFrame;
import stakemate.view.MarketsFrame;
import stakemate.view.SignupFrame;

/**
 * The Main Application Class for StakeMate.
 * Wires together the Clean Architecture layers (Entity, Use Case, Interface Adapter, View).
 */
// -@cs[ClassDataAbstractionCoupling] Main class wires the entire application together.
// -@cs[ClassFanOutComplexity] Main class depends on many components for dependency injection.
public final class StakeMateApp {

    private static final int INITIAL_BALANCE = 1000;
    private static final double BET_AMOUNT = 50.0;
    private static final double ODDS_WIN = 0.6;
    private static final double ODDS_LOSE = 0.4;

    private static InMemoryAccountRepository accountRepo;
    private static InMemoryBetRepository betRepo;

    private StakeMateApp() {
        // Private constructor to prevent instantiation
    }

    /**
     * Gets the Account Repository.
     *
     * @return The in-memory account repository.
     */
    public static InMemoryAccountRepository getAccountRepo() {
        return accountRepo;
    }

    /**
     * Gets the Bet Repository.
     *
     * @return The in-memory bet repository.
     */
    public static InMemoryBetRepository getBetRepo() {
        return betRepo;
    }

    /**
     * The main entry point of the application.
     *
     * @param args Command line arguments.
     */
    // -@cs[UncommentedMain] Main entry point is required for the application execution.
    public static void main(final String[] args) {
        // Load .env file if it exists
        loadEnvFile();

        SwingUtilities.invokeLater(StakeMateApp::runApp);
    }

    private static void runApp() {
        final SupabaseClientFactory gamesSupabaseFactory = new SupabaseClientFactory();
        final SupabaseGameRepository gameRepository = new SupabaseGameRepository(gamesSupabaseFactory);
        final FetchGamesInteractor fetchGamesInteractor = createFetchGamesInteractor(gameRepository);

        final InMemoryMatchRepository matchRepository =
            new InMemoryMatchRepository(gameRepository, fetchGamesInteractor);
        final MarketDataFacade marketFacade = new MarketDataFacade(
            matchRepository,
            new InMemoryMarketRepository(),
            new FakeOrderBookGateway()
        );

        betRepo = new InMemoryBetRepository();
        accountRepo = new InMemoryAccountRepository();
        setupDemoData();

        final MarketsFrame marketsFrame = new MarketsFrame();
        setupMarketView(marketsFrame, marketFacade);
        setupCommentSystem(marketsFrame);
        setupSettlementUseCase(marketsFrame, new InMemorySettlementRecordRepository());
        setupLiveMatchesView(marketsFrame, fetchGamesInteractor, gameRepository);

        final SupabaseClientFactory supabaseFactory = new SupabaseClientFactory();
        final SupabaseUserDataAccess userRepo = new SupabaseUserDataAccess(supabaseFactory);

        setupProfileUseCase(marketsFrame, userRepo);
        setupAuth(marketsFrame, userRepo);
    }

    private static FetchGamesInteractor createFetchGamesInteractor(final SupabaseGameRepository gameRepo) {
        final String apiKey = getEnvVar("ODDS_API_KEY");
        FetchGamesInteractor interactor = null;

        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("WARNING: ODDS_API_KEY not set. Using default hardcoded matches.");
        }
        else {
            final OddsApiGatewayImpl apiGateway = new OddsApiGatewayImpl(apiKey);
            final OddsApiResponseAdapter responseAdapter = new OddsApiResponseAdapter();
            final FetchGamesOutputBoundary presenter = new ConsoleFetchGamesPresenter();
            interactor = new FetchGamesInteractor(apiGateway, responseAdapter, gameRepo, presenter);
        }
        return interactor;
    }

    private static void setupMarketView(final MarketsFrame marketsFrame, final MarketDataFacade marketFacade) {
        final SwingViewMarketsPresenter marketsPresenter = new SwingViewMarketsPresenter(marketsFrame);
        final ViewMarketInteractor marketInteractor = new ViewMarketInteractor(marketFacade, marketsPresenter);
        final ViewMarketController marketController = new ViewMarketController(marketInteractor);
        marketsFrame.setController(marketController);
    }

    private static void setupCommentSystem(final MarketsFrame marketsFrame) {
        // COMMENTS SYSTEM WIRING BELOW
        final InMemoryCommentRepository commentRepo = new InMemoryCommentRepository();

        final SwingPostCommentPresenter postPresenter =
            new SwingPostCommentPresenter(marketsFrame.getCommentsPanel());
        final SwingViewCommentsPresenter viewPresenter =
            new SwingViewCommentsPresenter(marketsFrame.getCommentsPanel());

        final PostCommentInteractor postInteractor = new PostCommentInteractor(commentRepo, postPresenter);
        final ViewCommentsInteractor viewInteractor = new ViewCommentsInteractor(commentRepo, viewPresenter);

        final PostCommentController postController = new PostCommentController(postInteractor);
        final ViewCommentsController viewController = new ViewCommentsController(viewInteractor);

        marketsFrame.getCommentsPanel().setControllers(postController, viewController);
    }

    private static void setupDemoData() {
        accountRepo.addDemoUser(new User("alice", "password", INITIAL_BALANCE));
        accountRepo.addDemoUser(new User("bob", "password", INITIAL_BALANCE));

        // Added null (outcome unknown) and false (not settled) to match the new 7-arg constructor
        betRepo.addDemoBet(new Bet("alice", "M1-ML", Side.BUY, BET_AMOUNT, ODDS_WIN, null, false));
        betRepo.addDemoBet(new Bet("bob", "M1-ML", Side.SELL, BET_AMOUNT, ODDS_LOSE, null, false));
    }

    private static void setupSettlementUseCase(final MarketsFrame marketsFrame,
                                               final InMemorySettlementRecordRepository recordRepo) {
        final SwingSettleMarketPresenter settlePresenter =
            new SwingSettleMarketPresenter(marketsFrame);

        final SettleMarketInteractor settleInteractor =
            new SettleMarketInteractor(betRepo, accountRepo, recordRepo, settlePresenter);

        final SettleMarketController settleController =
            new SettleMarketController(settleInteractor);
        marketsFrame.setSettleMarketController(settleController);
    }

    private static void setupLiveMatchesView(final MarketsFrame marketsFrame,
                                            final FetchGamesInteractor fetchGamesInteractor,
                                            final SupabaseGameRepository gameRepository) {
        final LiveMatchesFrame liveMatchesFrame = new LiveMatchesFrame();
        final SwingLiveMatchesPresenter livePresenter = new SwingLiveMatchesPresenter(liveMatchesFrame);
        final LiveMatchesInteractor liveInteractor =
            new LiveMatchesInteractor(fetchGamesInteractor, gameRepository, livePresenter);
        final LiveMatchesController liveController = new LiveMatchesController(liveInteractor);

        liveMatchesFrame.setController(liveController);
        marketsFrame.setLiveMatchesFrame(liveMatchesFrame);
        marketsFrame.setLiveMatchesController(liveController);
    }

    private static void setupProfileUseCase(final MarketsFrame marketsFrame,
                                            final SupabaseUserDataAccess userRepo) {
        final stakemate.view.ProfileFrame profileFrame = new stakemate.view.ProfileFrame();
        final stakemate.interface_adapter.view_profile.ProfileViewModel profileViewModel =
            new stakemate.interface_adapter.view_profile.ProfileViewModel();
        profileFrame.setViewModel(profileViewModel);

        final stakemate.use_case.view_profile.ViewProfileOutputBoundary profilePresenter =
            new stakemate.interface_adapter.view_profile.ViewProfilePresenter(profileViewModel);

        final stakemate.use_case.view_profile.ViewProfileInteractor profileInteractor =
            new stakemate.use_case.view_profile.ViewProfileInteractor(userRepo, profilePresenter);

        final stakemate.interface_adapter.view_profile.ViewProfileController profileController =
            new stakemate.interface_adapter.view_profile.ViewProfileController(profileInteractor);

        marketsFrame.setProfileFrame(profileFrame);
        marketsFrame.setProfileController(profileController);
    }

    private static void setupAuth(final MarketsFrame marketsFrame, final SupabaseUserDataAccess userRepo) {
        final LoginFrame loginFrame = new LoginFrame(marketsFrame);
        final SignupFrame signupFrame = new SignupFrame(loginFrame);
        final SwingLoginPresenter loginPresenter = new SwingLoginPresenter(loginFrame);
        final LoginInteractor loginInteractor = new LoginInteractor(userRepo, loginPresenter);
        final LoginController loginController = new LoginController(loginInteractor);

        loginFrame.setController(loginController);
        loginFrame.setSignupFrame(signupFrame);
        final SwingSignupPresenter signupPresenter = new SwingSignupPresenter(signupFrame);
        final SignupInteractor signupInteractor = new SignupInteractor(userRepo, signupPresenter);
        final SignupController signupController = new SignupController(signupInteractor);

        signupFrame.setController(signupController);

        loginFrame.setVisible(true);
    }

    /**
     * Loads environment variables from .env file in project root.
     * Format: KEY=value (one per line, no quotes needed)
     */
    private static void loadEnvFile() {
        try {
            final File envFile = new File(".env");
            if (envFile.exists()) {
                final BufferedReader reader = new BufferedReader(new FileReader(envFile));

                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }

                    final int equalIndex = line.indexOf('=');
                    if (equalIndex > 0) {
                        final String key = line.substring(0, equalIndex).trim();
                        final String value = line.substring(equalIndex + 1).trim();

                        if (System.getenv(key) == null) {
                            System.setProperty(key, value);
                        }
                    }
                }
                reader.close();
            }
        }
        catch (final IOException ex) {
            System.err.println("Warning: Could not read .env file: " + ex.getMessage());
        }
    }

    /**
     * Gets environment variable from system env or system properties (.env file).
     *
     * @param key The environment variable key.
     * @return The value of the environment variable, or null if not found.
     */
    private static String getEnvVar(final String key) {
        String value = System.getenv(key);
        if (value == null) {
            value = System.getProperty(key);
        }
        return value;
    }

    /**
     * Private inner class to handle console output for game fetching.
     * Replaces the anonymous inner class to satisfy Checkstyle length rules.
     */
    private static final class ConsoleFetchGamesPresenter implements FetchGamesOutputBoundary {
        @Override
        public void presentFetchInProgress() {
            System.out.println("Fetching games from API...");
        }

        @Override
        public void presentFetchSuccess(final FetchGamesResponseModel response) {
            System.out.println("API fetch completed: " + response.getMessage());
        }

        @Override
        public void presentFetchError(final String error) {
            System.err.println("API fetch error: " + error);
        }

        @Override
        public void presentSearchResults(final List<Game> games, final String query) {
            System.out.println("Search found " + games.size() + " games for: " + query);
        }
    }
}
