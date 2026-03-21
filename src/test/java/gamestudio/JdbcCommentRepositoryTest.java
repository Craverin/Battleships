package gamestudio;

import gamestudio.entity.Comment;
import gamestudio.repository.JdbcCommentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@Testcontainers
@Import(JdbcCommentRepository.class)
public class JdbcCommentRepositoryTest
{
    @Autowired
    private JdbcCommentRepository repository;

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));

    @Test
    void getComments_threeComments_returnsAllGameCommentsTrimmed()
    {
        Date firstDate = new Date();
        Date secondDate = new Date();
        Date thirdDate = new Date();

        repository.addComment(new Comment("first player", "battleships", "Comment one", firstDate));
        repository.addComment(new Comment("second player", "battleships", "Comment two  ", secondDate));
        repository.addComment(new Comment("third player", "battleships", "   Comment three   ", thirdDate));

        List<Comment> comments = repository.getComments("battleships");
        assertEquals(3, comments.size());

        Comment firstComment = comments.get(0);
        Comment secondComment = comments.get(1);
        Comment thirdComment = comments.get(2);

        assertEquals("first player", firstComment.getPlayer());
        assertEquals("battleships", firstComment.getGame());
        assertEquals("Comment one", firstComment.getComment());
        assertEquals(firstDate, firstComment.getCommentedOn());

        assertEquals("second player", secondComment.getPlayer());
        assertEquals("battleships", secondComment.getGame());
        assertEquals("Comment two", secondComment.getComment());
        assertEquals(secondDate, secondComment.getCommentedOn());

        assertEquals("third player", thirdComment.getPlayer());
        assertEquals("battleships", thirdComment.getGame());
        assertEquals("Comment three", thirdComment.getComment());
        assertEquals(thirdDate, thirdComment.getCommentedOn());
    }

    @Test
    void getComments_noComments_returnsEmptyList()
    {
        List<Comment> comments = repository.getComments("battleships");

        assertNotNull(comments);
        assertTrue(comments.isEmpty());
    }

    @Test
    void addComment_validComment_savesComment()
    {
        Date date = new Date();
        repository.addComment(new Comment("some player", "battleships", "Good game + a comment", date));

        List<Comment> comments = repository.getComments("battleships");
        Comment comment = comments.get(0);

        assertEquals(1, comments.size());
        assertEquals("some player", comment.getPlayer());
        assertEquals("battleships", comment.getGame());
        assertEquals("Good game + a comment", comment.getComment());
        assertEquals(date, comment.getCommentedOn());
    }

    @Test
    void reset_afterAddingComments_deletesAllComments()
    {
        repository.addComment(new Comment("p1", "battleships", " commentary ", new Date()));
        repository.addComment(new Comment("p2", "battleships", " 2-nd comment", new Date()));

        assertFalse(repository.getComments("battleships").isEmpty());
        repository.reset();
        assertTrue(repository.getComments("battleships").isEmpty());
    }
}