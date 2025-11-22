package stakemate.use_case.fetch_games;

public class RepositoryException extends Exception {
    public RepositoryException(final String message) {
        super(message);
    }

    public RepositoryException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

