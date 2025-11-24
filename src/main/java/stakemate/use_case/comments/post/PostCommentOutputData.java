package stakemate.use_case.comments.post;

public class PostCommentOutputData {

    private final boolean success;
    private final String message;

    public PostCommentOutputData(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}
