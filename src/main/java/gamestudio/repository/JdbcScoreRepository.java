package gamestudio.repository;

import gamestudio.entity.Score;
import gamestudio.repository.exception.ScoreException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class JdbcScoreRepository implements ScoreRepository
{
    private final DataSource dataSource;

    private static final String SELECT_TOP10_SCORES = "SELECT player, game, points, playedOn FROM score WHERE game = ? ORDER BY points DESC LIMIT 10";
    private static final String SELECT_TOP_SCORE = "SELECT MAX(points) FROM score WHERE game = ? AND player = ?";
    private static final String INSERT = "INSERT INTO score (player, game, points, playedOn) VALUES (?, ?, ?, ?)";

    public JdbcScoreRepository(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    @Override
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
    public List<Score> getTopScores(String game)
    {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(SELECT_TOP10_SCORES))
        {
            statement.setString(1, game);
            try (ResultSet rs = statement.executeQuery())
            {
                List<Score> scores = new ArrayList<>();
                while (rs.next())
                {
                    scores.add(new Score(rs.getString(1),
                               rs.getString(2),
                               rs.getInt(3),
                               rs.getTimestamp(4)));
                }
                return scores;
            }
        }
        catch (SQLException e) { throw new ScoreException("Failed to select score", e); }
        finally { DataSourceUtils.releaseConnection(connection, dataSource); }
    }

    public int getTopScore(String game, String player)
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
