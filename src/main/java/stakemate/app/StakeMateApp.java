package stakemate.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import javax.swing.SwingUtilities;

import stakemate.data_access.api.OddsApiGatewayImpl;
import stakemate.data_access.api.OddsApiResponseAdapter;
import stakemate.data_access.in_memory.InMemoryAccountRepository;
import stakemate.data_access.in_memory.InMemoryBetRepository;
import stakemate.data_access.in_memory.InMemoryMarketRepository;
import stakemate.data_access.in_memory.InMemoryMatchRepository;
import stakemate.data_access.in_memory.InMemorySettlementRecordRepository;
import stakemate.data_access.supabase.PostgresOrderBookGateway;
import stakemate.data_access.supabase.PostgresOrderRepository;
import stakemate.data_access.supabase.PostgresPositionRepository;
import stakemate.data_access.supabase.SupabaseAccountRepository;
import stakemate.data_access.supabase.SupabaseBetRepository;
import stakemate.data_access.supabase.SupabaseClientFactory;
import stakemate.data_access.supabase.SupabaseCommentRepository;
import stakemate.data_access.supabase.SupabaseGameRepository;
import stakemate.data_access.supabase.SupabaseUserDataAccess;
import stakemate.entity.Game;
import stakemate.interface_adapter.controllers.SettleMarketController;
import stakemate.interface_adapter.view_comments.PostCommentController;
import stakemate.interface_adapter.view_comments.SwingPostCommentPresenter;
import stakemate.interface_adapter.view_comments.SwingViewCommentsPresenter;
import stakemate.interface_adapter.view_comments.ViewCommentsController;
import stakemate.interface_adapter.view_live.LiveMatchesController;
import stakemate.interface_adapter.view_live.SwingLiveMatchesPresenter;
import stakemate.interface_adapter.view_login.LoginController;
import stakemate.interface_adapter.view_login.LoginViewModel;
import stakemate.interface_adapter.view_live.SwingLiveMatchesPresenter;
import stakemate.interface_adapter.view_login.SwingLoginPresenter;
import stakemate.interface_adapter.view_market.SwingSettleMarketPresenter;
import stakemate.interface_adapter.view_market.SwingViewMarketsPresenter;
import stakemate.interface_adapter.view_market.ViewMarketController;
import stakemate.interface_adapter.view_profile.ProfileViewModel;
import stakemate.interface_adapter.view_profile.ViewProfileController;
import stakemate.interface_adapter.view_profile.ViewProfilePresenter;
import stakemate.interface_adapter.view_signup.SignupController;
import stakemate.interface_adapter.view_signup.SignupViewModel;
import stakemate.interface_adapter.view_signup.SwingSignupPresenter;
import stakemate.use_case.PlaceOrderUseCase.PlaceOrderUseCase;
import stakemate.use_case.comments.post.PostCommentInteractor;
import stakemate.use_case.comments.view.ViewCommentsInteractor;
import stakemate.use_case.fetch_games.FetchGamesInteractor;
import stakemate.use_case.fetch_games.FetchGamesOutputBoundary;
import stakemate.use_case.fetch_games.FetchGamesResponseModel;
import stakemate.use_case.login.LoginInteractor;
import stakemate.use_case.settle_market.SettleMarketInteractor;
import stakemate.use_case.signup.SignupInteractor;
import stakemate.use_case.view_live.LiveMatchesInteractor;
import stakemate.use_case.view_market.ViewMarketInteractor;
import stakemate.use_case.view_market.facade.MarketDataFacade;
import stakemate.use_case.view_profile.ViewProfileInteractor;
import stakemate.use_case.view_profile.ViewProfileOutputBoundary;
import stakemate.view.LiveMatchesFrame;
import stakemate.view.LoginFrame;
import stakemate.view.MarketsFrame;
import stakemate.view.ProfileFrame;
import stakemate.view.SignupFrame;

/**
 * The Main Application Class for StakeMate.
 * Wires together the Clean Architecture layers (Entity, Use Case, Interface
 * Adapter, View).
 */
// -@cs[ClassDataAbstractionCoupling] Main class wires the entire application
// together.
// -@cs[ClassFanOutComplexity] Main class depends on many components for
// dependency injection.
public final class StakeMateApp {

    // -@cs[VisibilityModifier] Public field used by UI components directly for
    // simplicity in this iteration.
    public static SupabaseUserDataAccess userRepo;

    private static InMemoryAccountRepository accountRepo;
    private static InMemoryBetRepository betRepo;

    private static PlaceOrderUseCase placeOrderUseCase;

    // We need access to the OrderRepo globally or created earlier for the facade
    private static PostgresOrderRepository sharedOrderRepo;

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
     * Initializes the trading system components including DB repositories and
     * matching engine.
     */
    public static void initTradingSystem() {
        // For orders & positions (same DS we already use)
        final javax.sql.DataSource ds = stakemate.use_case.PlaceOrderUseCase.DataSourceFactory.create();

        // Real DB repositories
        sharedOrderRepo = new PostgresOrderRepository(ds);
        final var positionRepo = new PostgresPositionRepository(ds);

        // DbAccountService uses Supabase profiles table
        final var accountService = new stakemate.service.DbAccountService(new SupabaseClientFactory());

        // MatchingEngine
        final var engine = new stakemate.engine.MatchingEngine(sharedOrderRepo, positionRepo, accountService);

        // Create use-case
        placeOrderUseCase = new PlaceOrderUseCase(
                engine,
                accountService,
                sharedOrderRepo,
                positionRepo);
    }

    public static PlaceOrderUseCase getPlaceOrderUseCase() {
        return placeOrderUseCase;
    }

    /**
     * The main entry point of the application.
     *
     * @param args Command line arguments.
     */
    // -@cs[UncommentedMain] Main entry point is required for the application
    // execution.
    public static void main(final String[] args) {
        // Load .env file if it exists
        loadEnvFile();

        SwingUtilities.invokeLater(StakeMateApp::runApp);
    }

    private static void runApp() {
        // Initialize our real order-book trading system FIRST to set up sharedOrderRepo
        initTradingSystem();

        final SupabaseClientFactory gamesSupabaseFactory = new SupabaseClientFactory();
        final SupabaseGameRepository gameRepository = new SupabaseGameRepository(gamesSupabaseFactory);
        final FetchGamesInteractor fetchGamesInteractor = createFetchGamesInteractor(gameRepository);

        final InMemoryMatchRepository matchRepository = new InMemoryMatchRepository(gameRepository,
                fetchGamesInteractor);

        // Use the REAL Database Gateway for Order Book data
        final PostgresOrderBookGateway dbOrderBookGateway = new PostgresOrderBookGateway(sharedOrderRepo);

        // Wired to Postgres
        final MarketDataFacade marketFacade = new MarketDataFacade(
                matchRepository,
                new InMemoryMarketRepository(gameRepository),
                dbOrderBookGateway);

        final SupabaseClientFactory dbFactory = new SupabaseClientFactory();

        // Use the Supabase repositories so Settle can see the DB bets
        final stakemate.use_case.settle_market.BetRepository realBetRepo = new SupabaseBetRepository(dbFactory);

        final stakemate.use_case.settle_market.AccountRepository realAccountRepo = new SupabaseAccountRepository(
                dbFactory);

        final MarketsFrame marketsFrame = new MarketsFrame();
        setupMarketView(marketsFrame, marketFacade);

        setupCommentSystem(marketsFrame);

        // Pass the REAL repositories to the settlement setup
        setupSettlementUseCase(
                marketsFrame,
                realBetRepo,
                realAccountRepo,
                new InMemorySettlementRecordRepository());

        setupLiveMatchesView(marketsFrame, fetchGamesInteractor, gameRepository);

        final SupabaseClientFactory supabaseFactory = new SupabaseClientFactory();
        userRepo = new SupabaseUserDataAccess(supabaseFactory);

        setupProfileUseCase(marketsFrame, userRepo);
        setupAuth(marketsFrame, userRepo);
    }

    private static FetchGamesInteractor createFetchGamesInteractor(final SupabaseGameRepository gameRepo) {
        final String apiKey = getEnvVar("ODDS_API_KEY");
        final FetchGamesOutputBoundary presenter = new ConsoleFetchGamesPresenter();
        FetchGamesInteractor interactor = null;

        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("WARNING: ODDS_API_KEY not set. Creating interactor with null gateway.");
            interactor = new FetchGamesInteractor(null, new OddsApiResponseAdapter(), gameRepo, presenter);
        }
        else {
            final OddsApiGatewayImpl apiGateway = new OddsApiGatewayImpl(apiKey);
            final OddsApiResponseAdapter responseAdapter = new OddsApiResponseAdapter();
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
        final SupabaseCommentRepository commentRepo = new SupabaseCommentRepository();

        final SwingViewCommentsPresenter viewPresenter = new SwingViewCommentsPresenter(
                marketsFrame.getCommentsPanel());

        final ViewCommentsController viewController = new ViewCommentsController(
                new ViewCommentsInteractor(commentRepo, viewPresenter));

        final SwingPostCommentPresenter postPresenter = new SwingPostCommentPresenter(marketsFrame.getCommentsPanel(),
                viewController);

        final PostCommentInteractor postInteractor = new PostCommentInteractor(commentRepo, postPresenter);

        final PostCommentController postController = new PostCommentController(postInteractor);

        marketsFrame.setPostCommentController(postController);
        marketsFrame.setViewCommentsController(viewController);
        marketsFrame.wireCommentsPanel();
    }

    private static void setupSettlementUseCase(final MarketsFrame marketsFrame,
            final stakemate.use_case.settle_market.BetRepository settlementBetRepo,
            final stakemate.use_case.settle_market.AccountRepository settlementAccountRepo,
            final InMemorySettlementRecordRepository recordRepo) {

        final SwingSettleMarketPresenter settlePresenter = new SwingSettleMarketPresenter(marketsFrame);

        final SettleMarketInteractor settleInteractor = new SettleMarketInteractor(
                settlementBetRepo,
                settlementAccountRepo,
                recordRepo,
                settlePresenter);

        final SettleMarketController settleController = new SettleMarketController(settleInteractor);
        marketsFrame.setSettleMarketController(settleController);
    }

    private static void setupLiveMatchesView(final MarketsFrame marketsFrame,
            final FetchGamesInteractor fetchGamesInteractor,
            final SupabaseGameRepository gameRepository) {
        final LiveMatchesFrame liveMatchesFrame = new LiveMatchesFrame();
        final SwingLiveMatchesPresenter livePresenter = new SwingLiveMatchesPresenter(liveMatchesFrame);
        final LiveMatchesInteractor liveInteractor = new LiveMatchesInteractor(fetchGamesInteractor, gameRepository,
                livePresenter);
        final LiveMatchesController liveController = new LiveMatchesController(liveInteractor);

        liveMatchesFrame.setController(liveController);
        marketsFrame.setLiveMatchesFrame(liveMatchesFrame);
        marketsFrame.setLiveMatchesController(liveController);
    }

    private static void setupProfileUseCase(final MarketsFrame marketsFrame,
            final SupabaseUserDataAccess userDataAccess) {
        final ProfileFrame profileFrame = new ProfileFrame();
        final ProfileViewModel profileViewModel = new ProfileViewModel();
        profileFrame.setViewModel(profileViewModel);

        final ViewProfileOutputBoundary profilePresenter = new ViewProfilePresenter(profileViewModel);

        final ViewProfileInteractor profileInteractor = new ViewProfileInteractor(
                userDataAccess, profilePresenter);

        final ViewProfileController profileController = new ViewProfileController(profileInteractor);

        marketsFrame.setProfileFrame(profileFrame);
        marketsFrame.setProfileController(profileController);
        profileFrame.setController(profileController);
    }

    private static void setupAuth(final MarketsFrame marketsFrame, final SupabaseUserDataAccess userDataAccess) {
        final LoginFrame loginFrame = new LoginFrame(marketsFrame);
        final SignupFrame signupFrame = new SignupFrame(loginFrame);

        final LoginViewModel loginViewModel = new LoginViewModel();
        loginFrame.setViewModel(loginViewModel);
        final SwingLoginPresenter loginPresenter = new SwingLoginPresenter(loginViewModel);
        final LoginInteractor loginInteractor = new LoginInteractor(userDataAccess, loginPresenter);
        final LoginController loginController = new LoginController(loginInteractor);
        loginFrame.setController(loginController);

        final SignupViewModel signupViewModel = new SignupViewModel();
        signupFrame.setViewModel(signupViewModel);
        final SwingSignupPresenter signupPresenter = new SwingSignupPresenter(signupViewModel);
        final SignupInteractor signupInteractor = new SignupInteractor(userDataAccess, signupPresenter);
        final SignupController signupController = new SignupController(signupInteractor);
        signupFrame.setController(signupController);

        loginFrame.setSignupFrame(signupFrame);
        loginFrame.setVisible(true);
    }

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

    private static String getEnvVar(final String key) {
        String value = System.getenv(key);
        if (value == null) {
            value = System.getProperty(key);
        }
        return value;
    }

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
