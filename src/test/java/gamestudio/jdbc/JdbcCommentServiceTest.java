package gamestudio.jdbc;

import gamestudio.server.entity.Comment;
import gamestudio.server.service.jdbc.JdbcCommentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest(properties = {
        "spring.sql.init.schema-locations=classpath:jdbc-schema.sql",
        "spring.sql.init.mode=always"
})
@Testcontainers
@ContextConfiguration(classes = JdbcCommentServiceTest.TestConfig.class)
@Import(JdbcCommentService.class)
public class JdbcCommentServiceTest
{
    @Autowired
    private JdbcCommentService repository;

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));

    @SpringBootConfiguration
    static class TestConfig { }

    @Test
    void getComments_threeCommentsAdded_returnsAllGameCommentsTrimmed()
    {
        Date firstDate = new Date();
        Date secondDate = new Date();
        Date thirdDate = new Date();

        repository.addComment(new Comment("p1", "battleships", "Comment one", firstDate));
        repository.addComment(new Comment("p2", "battleships", "Comment two  ", secondDate));
        repository.addComment(new Comment("p3", "battleships", "   Comment three   ", thirdDate));

        List<Comment> comments = repository.getComments("battleships");
        assertEquals(3, comments.size());

        Comment firstComment = comments.get(0);
        Comment secondComment = comments.get(1);
        Comment thirdComment = comments.get(2);

        assertEquals("p1", firstComment.getPlayer());
        assertEquals("battleships", firstComment.getGame());
        assertEquals("Comment one", firstComment.getComment());
        assertEquals(firstDate, firstComment.getCommentedOn());

        assertEquals("p2", secondComment.getPlayer());
        assertEquals("battleships", secondComment.getGame());
        assertEquals("Comment two", secondComment.getComment());
        assertEquals(secondDate, secondComment.getCommentedOn());

        assertEquals("p3", thirdComment.getPlayer());
        assertEquals("battleships", thirdComment.getGame());
        assertEquals("Comment three", thirdComment.getComment());
        assertEquals(thirdDate, thirdComment.getCommentedOn());
    }

    @Test
    void getComments_noCommentsAdded_returnsEmptyList()
    {
        List<Comment> comments = repository.getComments("battleships");

        assertNotNull(comments);
        assertTrue(comments.isEmpty());
    }

    @Test
    void getPlayerComments_twoCommentsAdded_returnsAllPlayerCommentsTrimmed()
    {
        Date firstDate = new Date();
        Date secondDate = new Date();

        repository.addComment(new Comment("p1", "battleships", "Comment one", firstDate));
        repository.addComment(new Comment("p1", "battleships", " Comment two  ", secondDate));

        List<Comment> comments = repository.getPlayerComments("battleships", "p1");
        assertEquals(2, comments.size());

        Comment firstComment = comments.get(0);
        Comment secondComment = comments.get(1);

        assertEquals("p1", firstComment.getPlayer());
        assertEquals("battleships", firstComment.getGame());
        assertEquals("Comment one", firstComment.getComment());
        assertEquals(firstDate, firstComment.getCommentedOn());

        assertEquals("p1", secondComment.getPlayer());
        assertEquals("battleships", secondComment.getGame());
        assertEquals("Comment two", secondComment.getComment());
        assertEquals(secondDate, secondComment.getCommentedOn());

    }

    @Test
    void getPlayerComments_noCommentsAdded_returnsEmptyList()
    {
        List<Comment> comments = repository.getPlayerComments("battleships", "p1");

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