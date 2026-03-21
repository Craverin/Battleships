package gamestudio.repository;

import gamestudio.entity.Rating;
import gamestudio.repository.exception.RatingException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;

@Repository
public class JdbcRatingRepository implements RatingRepository
{
    private final DataSource dataSource;

    private static final String SELECT_ALL_RATINGS = "SELECT rating FROM rating WHERE game = ?";
    private static final String SELECT_RATING = "SELECT rating FROM rating WHERE player = ? AND game = ?";
    private static final String INSERT = "INSERT INTO rating (player, game, rating, ratedOn) VALUES (?, ?, ?, ?) ON CONFLICT (player, game) DO UPDATE SET rating = EXCLUDED.rating, ratedOn = EXCLUDED.ratedOn";

    public JdbcRatingRepository(DataSource dataSource)
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

                if (counter == 0) return 0;
                return totalScore / counter;
            }
        }
        catch (SQLException e) { throw new RatingException("Failed to select ratings", e); }
        finally { DataSourceUtils.releaseConnection(connection, dataSource); }
    }

    @Override
    public int getRating(String player, String game)
    {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(SELECT_RATING))
        {
            statement.setString(1, player);
            statement.setString(2, game);

            try (ResultSet result = statement.executeQuery())
            {
                if (result.next()) return result.getInt("rating");
                throw new RatingException("Rating not found");
            }
        }
        catch (SQLException e) { throw new RatingException("Failed to select rating", e); }
        finally { DataSourceUtils.releaseConnection(connection, dataSource); }
    }

    @Override
    public void reset() throws RatingException
    {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM score"))
        {
            statement.executeUpdate();
        }
        catch (SQLException e) { throw new RatingException("Failed to reset rating", e); }
        finally { DataSourceUtils.releaseConnection(connection, dataSource); }
    }
}
