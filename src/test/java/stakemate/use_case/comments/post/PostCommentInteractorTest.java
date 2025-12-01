package stakemate.use_case.comments.post;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import stakemate.entity.Comment;
import stakemate.use_case.comments.CommentRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PostCommentInteractorTest {

    private FakeCommentRepository repository;
    private SpyPresenter presenter;
    private PostCommentInteractor interactor;

    @BeforeEach
    void setUp() {
        repository = new FakeCommentRepository();
        presenter = new SpyPresenter();
        interactor = new PostCommentInteractor(repository, presenter);
    }

    @Test
    void testExecute_SavesComment_AndCallsPresenter() {
        // Arrange
        PostCommentInputData request = new PostCommentInputData(
            "market123",
            "erwin",
            "hello world"
        );

        // Act
        interactor.execute(request);

        // ----------------------------
        // Assertions: repository side
        // ----------------------------
        assertEquals(1, repository.savedComments.size());
        Comment saved = repository.savedComments.get(0);

        assertNotNull(saved.getId());
        assertEquals("market123", saved.getMarketId());
        assertEquals("erwin", saved.getUsername());
        assertEquals("hello world", saved.getMessage());
        assertNotNull(saved.getTimestamp());

        // ----------------------------
        // Assertions: presenter side
        // ----------------------------
        assertTrue(presenter.presentCalled);

        PostCommentOutputData output = presenter.receivedOutput;
        assertNotNull(output);

        assertTrue(output.isSuccess());
        assertEquals("Comment posted.", output.getMessage());
        assertEquals("market123", output.getMarketId());
    }

    // ===========================================================
    // Fakes / Spies
    // ===========================================================

    private static class FakeCommentRepository implements CommentRepository {

        List<Comment> savedComments = new ArrayList<>();

        @Override
        public void saveComment(Comment comment) {
            savedComments.add(comment);
        }

        @Override
        public List<Comment> getCommentsForMarket(String marketId) {
            // Return all saved comments matching this market
            List<Comment> result = new ArrayList<>();
            for (Comment c : savedComments) {
                if (c.getMarketId().equals(marketId)) {
                    result.add(c);
                }
            }
            return result;
        }
    }

    private static class SpyPresenter implements PostCommentOutputBoundary {
        boolean presentCalled = false;
        PostCommentOutputData receivedOutput;

        @Override
        public void present(PostCommentOutputData data) {
            presentCalled = true;
            receivedOutput = data;
        }
    }
}
