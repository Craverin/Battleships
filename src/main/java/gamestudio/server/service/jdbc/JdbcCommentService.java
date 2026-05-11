package gamestudio.server.service.jdbc;

import gamestudio.server.dto.AddCommentRequest;
import gamestudio.server.entity.Comment;
import gamestudio.server.service.CommentService;
import gamestudio.server.service.exception.CommentException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcCommentService implements CommentService
{
    private final DataSource dataSource;

    private static final String INSERT_COMMENT = "INSERT INTO comment (player, game, comment, commentedOn) VALUES (?, ?, ?, ?)";
    private static final String SELECT_GAME_COMMENTS = "SELECT player, game, comment, commentedOn from comment WHERE game = ? ORDER BY commentedOn DESC";
    private static final String SELECT_PLAYER_COMMENTS = "SELECT player, game, comment, commentedOn from comment WHERE game = ? AND player = ? ORDER BY commentedOn DESC";

    public JdbcCommentService(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }


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
    public void addComment(String game, AddCommentRequest comment) throws CommentException {

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
    public List<Comment> getMyComments(String game) throws CommentException {
        return List.of();
    }


    public List<Comment> getMyComments(String player, String game)
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
        return List.of();
//        try (ResultSet rs = statement.executeQuery())
//        {
//            List<Comment> comments = new ArrayList<>();
//            while (rs.next())
//            {
//                comments.add(new Comment(
//                       rs.getString(1),
//                       rs.getString(2),
//                       rs.getString(3),
//                       rs.getTimestamp(4))
//                );
//            }
//
//            return comments;
//        }
//        catch (SQLException e) { throw new CommentException("Failed to select comments", e); }
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
