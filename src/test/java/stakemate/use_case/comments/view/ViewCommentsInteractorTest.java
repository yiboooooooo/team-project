package stakemate.use_case.comments.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import stakemate.entity.Comment;
import stakemate.use_case.comments.CommentRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ViewCommentsInteractorTest {

    private FakeCommentRepository repository;
    private SpyPresenter presenter;
    private ViewCommentsInteractor interactor;

    @BeforeEach
    void setUp() {
        repository = new FakeCommentRepository();
        presenter = new SpyPresenter();
        interactor = new ViewCommentsInteractor(repository, presenter);
    }

    @Test
    void testExecute_ReturnsCommentsForMarket() {
        // Arrange
        String marketId = "m123";

        Comment c1 = new Comment("1", marketId, "alice", "Nice market", LocalDateTime.now());
        Comment c2 = new Comment("2", marketId, "bob", "Interesting odds", LocalDateTime.now());
        Comment cOther = new Comment("3", "otherMarket", "eve", "irrelevant", LocalDateTime.now());

        repository.savedComments.add(c1);
        repository.savedComments.add(c2);
        repository.savedComments.add(cOther);

        ViewCommentsInputData input = new ViewCommentsInputData(marketId);

        // Act
        interactor.execute(input);

        // Assert presenter called
        assertTrue(presenter.presentCalled);

        ViewCommentsOutputData output = presenter.receivedOutput;
        assertNotNull(output);

        List<Comment> comments = output.getComments();
        assertEquals(2, comments.size());
        assertTrue(comments.contains(c1));
        assertTrue(comments.contains(c2));
        assertFalse(comments.contains(cOther));
    }

    // =========================================================================
    // Fakes / Spies
    // =========================================================================

    private static class FakeCommentRepository implements CommentRepository {
        List<Comment> savedComments = new ArrayList<>();

        @Override
        public void saveComment(Comment comment) {
            savedComments.add(comment);
        }

        @Override
        public List<Comment> getCommentsForMarket(String marketId) {
            List<Comment> result = new ArrayList<>();
            for (Comment c : savedComments) {
                if (c.getMarketId().equals(marketId)) {
                    result.add(c);
                }
            }
            return result;
        }
    }

    private static class SpyPresenter implements ViewCommentsOutputBoundary {
        boolean presentCalled = false;
        ViewCommentsOutputData receivedOutput;

        @Override
        public void present(ViewCommentsOutputData data) {
            presentCalled = true;
            receivedOutput = data;
        }
    }
}
