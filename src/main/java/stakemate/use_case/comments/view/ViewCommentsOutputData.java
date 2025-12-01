package stakemate.use_case.comments.view;

import java.util.List;

import stakemate.entity.Comment;

public class ViewCommentsOutputData {

    private final List<Comment> comments;

    public ViewCommentsOutputData(List<Comment> comments) {
        this.comments = comments;
    }

    public List<Comment> getComments() {
        return comments;
    }
}
