package gamestudio.repository;

import gamestudio.entity.Rating;
import gamestudio.repository.exception.RatingException;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;

@Repository
public class JdbcRatingRepository implements RatingRepository
{
    private final DataSource dataSource;

    private static final String SELECT_ALL_RATINGS = "SELECT rating FROM rating WHERE game = ?";
    private static final String SELECT_RATING = "SELECT rating FROM rating WHERE game = ? AND player = ?";
    private static final String INSERT = "INSERT INTO rating (player, game, rating, ratedOn) VALUES (?, ?, ?, ?) ON CONFLICT (player, game) DO UPDATE SET rating = EXCLUDED.rating, ratedOn = EXCLUDED.ratedOn";

    public JdbcRatingRepository(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    @Override
    public void setRating(Rating rating)
    {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT))
        {
            System.out.println("Setting rating in database... (" + rating.getRating() + ")");
            statement.setString(1, rating.getPlayer());
            statement.setString(2, rating.getGame());
            statement.setInt(3, rating.getRating());
            statement.setTimestamp(4, new Timestamp(rating.getRatedOn().getTime()));

            statement.executeUpdate();
        }
        catch (SQLException e) { throw new RatingException("Failed to insert rating", e); }
    }

    @Override
    public int getAverageRating(String game)
    {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL_RATINGS))
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

                return totalScore / counter;
            }
        }
        catch (SQLException e) { throw new RatingException("Failed to select ratings", e); }
    }

    @Override
    public int getRating(String game, String player)
    {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_RATING))
        {
            statement.setString(1, game);
            statement.setString(2, player);

            try (ResultSet result = statement.executeQuery())
            {
                if (result.next()) return result.getInt("rating");
                throw new RatingException("Rating not found");
            }
        }
        catch (SQLException e) { throw new RatingException("Failed to select rating", e); }
    }

    @Override
    public void reset() throws RatingException
    {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM score"))
        {
            statement.executeUpdate();
        }
        catch (SQLException e) { throw new RatingException("Failed to reset rating", e); }
    }
}
