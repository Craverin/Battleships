package gamestudio.server.service.jdbc;

import gamestudio.server.entity.Score;
import gamestudio.server.service.ScoreService;
import gamestudio.server.service.exception.ScoreException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcScoreService implements ScoreService
{
    private final DataSource dataSource;

    private static final String SELECT_TOP10_SCORES = "SELECT player, game, points, playedOn FROM score WHERE game = ? ORDER BY points DESC LIMIT 10";
    private static final String SELECT_TOP_SCORE = "SELECT MAX(points) FROM score WHERE game = ? AND player = ?";
    private static final String INSERT = "INSERT INTO score (player, game, points, playedOn) VALUES (?, ?, ?, ?)";

    public JdbcScoreService(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    public void addScore(Score score)
    {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(INSERT))
        {
            statement.setString(1, score.getPlayer());
            statement.setString(2, score.getGame());
            statement.setInt(3, score.getPoints());
            statement.setTimestamp(4, new Timestamp(score.getPlayedOn().getTime()));
            statement.executeUpdate();
        }
        catch (SQLException e) { throw new ScoreException("Failed to insert score", e); }
        finally { DataSourceUtils.releaseConnection(connection, dataSource); }
    }


    @Override
    public void addScore(String game, int userId, String username, int score) throws ScoreException {

    }

    @Override
    public List<Score> getTopScores(String game)
    {
        return List.of();
//        Connection connection = DataSourceUtils.getConnection(dataSource);
//        try (PreparedStatement statement = connection.prepareStatement(SELECT_TOP10_SCORES))
//        {
//            statement.setString(1, game);
//            try (ResultSet rs = statement.executeQuery())
//            {
//                List<Score> scores = new ArrayList<>();
//                while (rs.next())
//                {
//                    scores.add(new Score(rs.getString(1),
//                               rs.getString(2),
//                               rs.getInt(3),
//                               rs.getTimestamp(4)));
//                }
//                return scores;
//            }
//        }
//        catch (SQLException e) { throw new ScoreException("Failed to select score", e); }
//        finally { DataSourceUtils.releaseConnection(connection, dataSource); }
    }

    @Override
    public int getMyTopScore(String game) throws ScoreException {
        return 0;
    }

    public int getMyTopScore(String game, String player)
    {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(SELECT_TOP_SCORE))
        {
            statement.setString(1, game);
            statement.setString(2, player);

            try (ResultSet rs = statement.executeQuery())
            {
               if (rs.next())
               {
                   int score = rs.getInt(1);
                   if (rs.wasNull()) return -1;

                   return score;
               }
               return -1;
            }

        }
        catch (SQLException e) { throw new ScoreException("Failed to select score", e); }
        finally { DataSourceUtils.releaseConnection(connection, dataSource); }
    }

    @Override
    public void reset()
    {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM score"))
        {
            statement.executeUpdate();
        }
        catch (SQLException e) { throw new ScoreException("Failed to delete score", e); }
        finally { DataSourceUtils.releaseConnection(connection, dataSource); }
    }
}
