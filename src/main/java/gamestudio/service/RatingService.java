package gamestudio.service;

import gamestudio.service.exception.RatingException;
import gamestudio.entity.Rating;

public interface RatingService
{
    void setRating(Rating rating) throws RatingException;
    int getAverageRating(String game) throws RatingException;
    int getRating(String game, String player) throws RatingException;
    int getRatingCount(String game) throws RatingException;
    void reset() throws RatingException;
}
