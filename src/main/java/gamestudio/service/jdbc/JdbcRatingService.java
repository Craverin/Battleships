package gamestudio.service.jdbc;

import gamestudio.entity.Rating;
import gamestudio.service.RatingService;
import gamestudio.service.exception.RatingException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.*;

public class JdbcRatingService implements RatingService
{
    private final DataSource dataSource;

    private static final String SELECT_ALL_RATINGS = "SELECT rating FROM rating WHERE game = ?";
    private static final String SELECT_RATING = "SELECT rating FROM rating WHERE player = ? AND game = ?";
    private static final String INSERT = "INSERT INTO rating (player, game, rating, ratedOn) VALUES (?, ?, ?, ?) ON CONFLICT (player, game) DO UPDATE SET rating = EXCLUDED.rating, ratedOn = EXCLUDED.ratedOn";
    private static final String RATING_COUNT = "SELECT COUNT(*) FROM rating WHERE game = ?";

    public JdbcRatingService(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    @Override
    public void setRating(Rating rating)
    {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(INSERT))
        {
            statement.setString(1, rating.getPlayer());
            statement.setString(2, rating.getGame());
            statement.setInt(3, rating.getRating());
            statement.setTimestamp(4, new Timestamp(rating.getRatedOn().getTime()));

            statement.executeUpdate();
        }
        catch (SQLException e) { throw new RatingException("Failed to insert rating", e); }
        finally { DataSourceUtils.releaseConnection(connection, dataSource); }
    }

    @Override
    public int getAverageRating(String game)
    {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(SELECT_ALL_RATINGS))
        {
            statement.setString(1, game);
            try (ResultSet result = statement.executeQuery())
            {
                int counter = 0, totalScore = 0;
                while (result.next())
                {
                    totalScore += result.getInt("rating");
                    counter++;
                }

                if (counter == 0) return -1;
                return totalScore / counter;
            }
        }
        catch (SQLException e) { throw new RatingException("Failed to select ratings", e); }
        finally { DataSourceUtils.releaseConnection(connection, dataSource); }
    }

    @Override
    public int getRating(String game, String player)
    {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(SELECT_RATING))
        {
            statement.setString(1, player);
            statement.setString(2, game);

            try (ResultSet result = statement.executeQuery())
            {
                if (result.next()) return result.getInt("rating");
                return -1;
            }
        }
        catch (SQLException e) { throw new RatingException("Failed to select rating", e); }
        finally { DataSourceUtils.releaseConnection(connection, dataSource); }
    }

    @Override
    public int getRatingCount(String game) throws RatingException
    {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(RATING_COUNT))
        {
            statement.setString(1, game);

            try (ResultSet result = statement.executeQuery())
            {
                int ratingCount = 0;

                if (result.next()) ratingCount = result.getInt(1);
                return ratingCount;
            }
        }
        catch (SQLException e) { throw new RatingException("Failed to select rating", e); }
        finally { DataSourceUtils.releaseConnection(connection, dataSource); }
    }

    @Override
    public void reset() throws RatingException
    {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM rating"))
        {
            statement.executeUpdate();
        }
        catch (SQLException e) { throw new RatingException("Failed to reset rating", e); }
        finally { DataSourceUtils.releaseConnection(connection, dataSource); }
    }
}
