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

    private StakeMateApp() {
    }

    public static void main(String[] args) {
        // Load .env file if it exists
        loadEnvFile();

        SwingUtilities.invokeLater(() -> {
            // ==============================
            // ==============================
            // Infrastructure for markets
            // ==============================

            InMemoryMatchRepository matchRepository = new InMemoryMatchRepository();
            InMemoryMarketRepository marketRepository = new InMemoryMarketRepository();
            FakeOrderBookGateway orderBookGateway = new FakeOrderBookGateway();

            MarketsFrame marketsFrame = new MarketsFrame();

            SwingViewMarketsPresenter marketsPresenter = new SwingViewMarketsPresenter(marketsFrame);

            ViewMarketInteractor marketInteractor = new ViewMarketInteractor(
                    matchRepository,
                    marketRepository,
                    orderBookGateway,
                    marketsPresenter);

            ViewMarketController marketController = new ViewMarketController(marketInteractor);
            marketsFrame.setController(marketController);

            // ==============================
            // User data access
            // ==============================

            SupabaseClientFactory supabaseFactory = new SupabaseClientFactory();
            SupabaseUserDataAccess userRepo = new SupabaseUserDataAccess(supabaseFactory);

            // ==============================
            // Profile Frame
            // ==============================
            stakemate.view.ProfileFrame profileFrame = new stakemate.view.ProfileFrame();

            stakemate.interface_adapter.view_profile.ProfileViewModel profileViewModel = new stakemate.interface_adapter.view_profile.ProfileViewModel();
            profileFrame.setViewModel(profileViewModel);

            stakemate.use_case.view_profile.ViewProfileOutputBoundary profilePresenter = new stakemate.interface_adapter.view_profile.ViewProfilePresenter(
                    profileViewModel);

            stakemate.use_case.view_profile.ViewProfileInteractor profileInteractor = new stakemate.use_case.view_profile.ViewProfileInteractor(
                    userRepo, profilePresenter);

            stakemate.interface_adapter.view_profile.ViewProfileController profileController = new stakemate.interface_adapter.view_profile.ViewProfileController(
                    profileInteractor);

            marketsFrame.setProfileFrame(profileFrame);
            marketsFrame.setProfileController(profileController);

            // ==============================
            // Login & Signup frames
            // ==============================

            // Create frames first, so we can pass references:
            LoginFrame loginFrame = new LoginFrame(marketsFrame); // you might also pass signup later
            SignupFrame signupFrame = new SignupFrame(loginFrame);

            // ----- Login wiring -----
            SwingLoginPresenter loginPresenter = new SwingLoginPresenter(loginFrame);

            LoginInteractor loginInteractor = new LoginInteractor(userRepo, loginPresenter);

            LoginController loginController = new LoginController(loginInteractor);

            loginFrame.setController(loginController);
            // Make loginFrame aware of signupFrame (e.g. for a "Sign up" button)
            loginFrame.setSignupFrame(signupFrame); // Add this setter in LoginFrame

            // ----- Signup wiring -----
            SwingSignupPresenter signupPresenter = new SwingSignupPresenter(signupFrame);

            SignupInteractor signupInteractor = new SignupInteractor(userRepo, signupPresenter);

            SignupController signupController = new SignupController(signupInteractor);

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
                    new java.io.FileReader(envFile));

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
