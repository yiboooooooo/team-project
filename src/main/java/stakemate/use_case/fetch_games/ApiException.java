package stakemate.use_case.fetch_games;

public class ApiException extends Exception {
    public ApiException(final String message) {
        super(message);
    }

    public ApiException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

