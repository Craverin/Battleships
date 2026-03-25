package gamestudio.repository;

import gamestudio.repository.exception.RatingException;
import gamestudio.entity.Rating;

public interface RatingRepository
{
    void setRating(Rating rating) throws RatingException;
    int getAverageRating(String game) throws RatingException;
    int getRating(String game, String player) throws RatingException;
    int getRatingCount(String game);
    void reset() throws RatingException;
}
