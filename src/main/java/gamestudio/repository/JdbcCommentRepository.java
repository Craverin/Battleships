package gamestudio.repository;

import gamestudio.entity.Comment;
import gamestudio.repository.exception.CommentException;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class JdbcCommentRepository implements CommentRepository
{
    private final DataSource dataSource;

    private static final String INSERT_COMMENT = "INSERT INTO comment (game, player, comment, commentedOn) VALUES (?, ?, ?, ?)";
    private static final String SELECT_COMMENTS = "SELECT comment from comment WHERE game = ?";

    public JdbcCommentRepository(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    @Override
    public void addComment(Comment comment)
    {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_COMMENT))
        {
            statement.setString(1, comment.getGame());
            statement.setString(2, comment.getPlayer());
            statement.setString(3, comment.getComment());
            statement.setTimestamp(4, new Timestamp(comment.getCommentedOn().getTime()));

            statement.executeUpdate();
        }
        catch (SQLException e) { throw new CommentException("Failed to insert comment'", e); }
    }

    @Override
    public List<Comment> getComments(String game)
    {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_COMMENTS))
        {
            statement.setString(1, game);
            try (ResultSet rs = statement.executeQuery())
            {
                List<Comment> comments = new ArrayList<>();
                while (rs.next())
                {
                    comments.add(new Comment(rs.getString(1),
                                 rs.getString(2),
                                 rs.getString(3),
                                 rs.getTimestamp(4)));
                }

                return comments;
            }
        }
        catch (SQLException e) { throw new CommentException("Failed to select comments", e); }
    }

    @Override
    public void reset()
    {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM comment"))
        {
            statement.executeUpdate();
        }
        catch (SQLException e) { throw new CommentException("Failed to delete score", e); }
    }
}
