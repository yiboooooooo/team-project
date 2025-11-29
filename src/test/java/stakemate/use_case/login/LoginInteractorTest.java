package stakemate.use_case.login;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import stakemate.entity.User;

/**
 * Tests for the Login Interactor.
 */
class LoginInteractorTest {

    private StubUserDataAccess stubUserDataAccess;
    private LoginInteractor interactor;

    @BeforeEach
    void setUp() {
        stubUserDataAccess = new StubUserDataAccess();
    }

    @Test
    void testSuccess() {
        final User user = new User("validUser", "password123", 1000);
        stubUserDataAccess.setUserToReturn(user);

        final LoginOutputBoundary successPresenter = new LoginOutputBoundary() {
            @Override
            public void prepareSuccessView(final LoginOutputData outputData) {
                assertEquals("validUser", outputData.getUsername());
            }

            @Override
            public void prepareFailView(final String error) {
                fail("Unexpected failure: " + error);
            }
        };

        interactor = new LoginInteractor(stubUserDataAccess, successPresenter);
        interactor.execute(new LoginInputData("validUser", "password123"));
    }

    @Test
    void testUserDoesNotExist() {
        stubUserDataAccess.setUserToReturn(null);

        final LoginOutputBoundary failPresenter = new LoginOutputBoundary() {
            @Override
            public void prepareSuccessView(final LoginOutputData outputData) {
                fail("Unexpected success");
            }

            @Override
            public void prepareFailView(final String error) {
                assertEquals("User does not exist.", error);
            }
        };

        interactor = new LoginInteractor(stubUserDataAccess, failPresenter);
        interactor.execute(new LoginInputData("nonExistent", "pass"));
    }

    @Test
    void testIncorrectPassword() {
        final User user = new User("validUser", "password123", 1000);
        stubUserDataAccess.setUserToReturn(user);

        final LoginOutputBoundary failPresenter = new LoginOutputBoundary() {
            @Override
            public void prepareSuccessView(final LoginOutputData outputData) {
                fail("Unexpected success");
            }

            @Override
            public void prepareFailView(final String error) {
                assertEquals("Incorrect password.", error);
            }
        };

        interactor = new LoginInteractor(stubUserDataAccess, failPresenter);
        interactor.execute(new LoginInputData("validUser", "wrongPass"));
    }

    private static class StubUserDataAccess implements LoginUserDataAccessInterface {
        private User userToReturn;

        void setUserToReturn(final User user) {
            this.userToReturn = user;
        }

        @Override
        public User getByUsername(final String username) {
            return userToReturn;
        }
    }
}
