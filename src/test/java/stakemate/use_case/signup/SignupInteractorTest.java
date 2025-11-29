package stakemate.use_case.signup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import stakemate.entity.User;

/**
 * Tests for the Signup Interactor.
 */
class SignupInteractorTest {

    private StubUserDataAccess stubUserDataAccess;
    private SignupInteractor interactor;

    @BeforeEach
    void setUp() {
        stubUserDataAccess = new StubUserDataAccess();
    }

    @Test
    void testSuccess() {
        final SignupOutputBoundary successPresenter = new SignupOutputBoundary() {
            @Override
            public void prepareSuccessView(final SignupOutputData outputData) {
                assertEquals("newUser", outputData.getUsername());
                assertTrue(stubUserDataAccess.wasSaveCalled());
                assertEquals("newUser", stubUserDataAccess.getSavedUser().getUsername());
                assertEquals(10000, stubUserDataAccess.getSavedUser().getBalance());
            }

            @Override
            public void prepareFailView(final String error) {
                fail("Unexpected failure: " + error);
            }
        };

        interactor = new SignupInteractor(stubUserDataAccess, successPresenter);
        interactor.execute(new SignupInputData("newUser", "password"));
    }

    @Test
    void testUserAlreadyExists() {
        stubUserDataAccess.setExists(true);

        final SignupOutputBoundary failPresenter = new SignupOutputBoundary() {
            @Override
            public void prepareSuccessView(final SignupOutputData outputData) {
                fail("Unexpected success");
            }

            @Override
            public void prepareFailView(final String error) {
                assertEquals("Username already exists.", error);
            }
        };

        interactor = new SignupInteractor(stubUserDataAccess, failPresenter);
        interactor.execute(new SignupInputData("existingUser", "password"));
    }

    @Test
    void testEmptyUsername() {
        final SignupOutputBoundary failPresenter = new SignupOutputBoundary() {
            @Override
            public void prepareSuccessView(final SignupOutputData outputData) {
                fail("Unexpected success");
            }

            @Override
            public void prepareFailView(final String error) {
                assertEquals("Username and password cannot be empty.", error);
            }
        };

        interactor = new SignupInteractor(stubUserDataAccess, failPresenter);
        interactor.execute(new SignupInputData("", "password"));
    }

    @Test
    void testEmptyPassword() {
        final SignupOutputBoundary failPresenter = new SignupOutputBoundary() {
            @Override
            public void prepareSuccessView(final SignupOutputData outputData) {
                fail("Unexpected success");
            }

            @Override
            public void prepareFailView(final String error) {
                assertEquals("Username and password cannot be empty.", error);
            }
        };

        interactor = new SignupInteractor(stubUserDataAccess, failPresenter);
        interactor.execute(new SignupInputData("user", ""));
    }

    @Test
    void testNullInput() {
        final SignupOutputBoundary failPresenter = new SignupOutputBoundary() {
            @Override
            public void prepareSuccessView(final SignupOutputData outputData) {
                fail("Unexpected success");
            }

            @Override
            public void prepareFailView(final String error) {
                assertEquals("Username and password cannot be empty.", error);
            }
        };

        interactor = new SignupInteractor(stubUserDataAccess, failPresenter);
        interactor.execute(new SignupInputData(null, "password"));
    }

    @Test
    void testNullPassword() {
        final SignupOutputBoundary failPresenter = new SignupOutputBoundary() {
            @Override
            public void prepareSuccessView(final SignupOutputData outputData) {
                fail("Unexpected success");
            }

            @Override
            public void prepareFailView(final String error) {
                assertEquals("Username and password cannot be empty.", error);
            }
        };

        interactor = new SignupInteractor(stubUserDataAccess, failPresenter);
        interactor.execute(new SignupInputData("user", null));
    }

    private static class StubUserDataAccess implements SignupUserDataAccessInterface {
        private boolean exists = false;
        private User savedUser;
        private boolean saveCalled = false;

        void setExists(final boolean exists) {
            this.exists = exists;
        }

        boolean wasSaveCalled() {
            return saveCalled;
        }

        User getSavedUser() {
            return savedUser;
        }

        @Override
        public boolean existsByUsername(final String username) {
            return exists;
        }

        @Override
        public void save(final User user) {
            this.savedUser = user;
            this.saveCalled = true;
        }
    }
}
