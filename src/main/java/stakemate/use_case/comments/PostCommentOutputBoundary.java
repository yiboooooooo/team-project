package stakemate.use_case.comments;

import stakemate.use_case.comments.PostCommentOutputData;

public interface PostCommentOutputBoundary {
    void present(PostCommentOutputData outputData);
}
