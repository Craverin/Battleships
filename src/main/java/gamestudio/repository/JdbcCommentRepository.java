package gamestudio.repository;

import gamestudio.entity.Comment;
import gamestudio.repository.exception.CommentException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class JdbcCommentRepository implements CommentRepository
{
    private final DataSource dataSource;

    private static final String INSERT_COMMENT = "INSERT INTO comment (player, game, comment, commentedOn) VALUES (?, ?, ?, ?)";
    private static final String SELECT_GAME_COMMENTS = "SELECT player, game, comment, commentedOn from comment WHERE game = ? ORDER BY commentedOn DESC";
    private static final String SELECT_PLAYER_COMMENTS = "SELECT player, game, comment, commentedOn from comment WHERE game = ? AND player = ? ORDER BY commentedOn DESC";

    public JdbcCommentRepository(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    @Override
    public void addComment(Comment comment)
    {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(INSERT_COMMENT))
        {
            statement.setString(1, comment.getPlayer());
            statement.setString(2, comment.getGame());
            statement.setString(3, comment.getComment());
            statement.setTimestamp(4, new Timestamp(comment.getCommentedOn().getTime()));

            statement.executeUpdate();
        }
        catch (SQLException e) { throw new CommentException("Failed to insert comment'", e); }
        finally { DataSourceUtils.releaseConnection(connection, dataSource); }
    }

    @Override
    public List<Comment> getComments(String game)
    {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(SELECT_GAME_COMMENTS))
        {
            statement.setString(1, game);
            return getComments(statement);
        }
        catch (SQLException e) { throw new CommentException("Failed to select comments", e); }
        finally { DataSourceUtils.releaseConnection(connection, dataSource); }
    }

    @Override
    public List<Comment> getPlayerComments(String game, String player)
    {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(SELECT_PLAYER_COMMENTS))
        {
            statement.setString(1, game);
            statement.setString(2, player);

            return getComments(statement);
        }
        catch (SQLException e) { throw new CommentException("Failed to select comments", e); }
        finally { DataSourceUtils.releaseConnection(connection, dataSource); }
    }

    private List<Comment> getComments(PreparedStatement statement)
    {
        try (ResultSet rs = statement.executeQuery())
        {
            List<Comment> comments = new ArrayList<>();
            while (rs.next())
            {
                comments.add(new Comment(
                       rs.getString(1),
                       rs.getString(2),
                       rs.getString(3),
                       rs.getTimestamp(4))
                );
            }

            return comments;
        }
        catch (SQLException e) { throw new CommentException("Failed to select comments", e); }
    }

    @Override
    public void reset()
    {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM comment"))
        {
            statement.executeUpdate();
        }
        catch (SQLException e) { throw new CommentException("Failed to delete score", e); }
        finally { DataSourceUtils.releaseConnection(connection, dataSource); }
    }
}
