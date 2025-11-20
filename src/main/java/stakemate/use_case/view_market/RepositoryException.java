package stakemate.use_case.view_market;

public class RepositoryException extends Exception {
    public RepositoryException(final String message) {
        super(message);
    }

    public RepositoryException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
