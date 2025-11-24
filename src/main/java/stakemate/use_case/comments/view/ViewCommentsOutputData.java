package stakemate.use_case.comments.view;

import stakemate.entity.Comment;

import java.util.List;

public class ViewCommentsOutputData {

    private final List<Comment> comments;

    public ViewCommentsOutputData(List<Comment> comments) {
        this.comments = comments;
    }

    public List<Comment> getComments() {
        return comments;
    }
}
