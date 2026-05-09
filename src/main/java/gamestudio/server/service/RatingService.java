package gamestudio.server.service;

import gamestudio.server.dto.RatingSummaryResponse;
import gamestudio.server.service.exception.RatingException;
import gamestudio.server.entity.Rating;

public interface RatingService
{
    void setRating(Rating rating) throws RatingException;
    float getAverageRating(String game) throws RatingException;
    RatingSummaryResponse getRatingSummary(String game) throws RatingException;
    int getRating(String game, String player) throws RatingException;
    int getRatingCount(String game) throws RatingException;
    void reset() throws RatingException;
}
