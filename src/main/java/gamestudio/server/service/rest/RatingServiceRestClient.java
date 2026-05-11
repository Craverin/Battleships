package gamestudio.server.service.rest;

import gamestudio.server.dto.RatingSummaryResponse;
import gamestudio.server.entity.Rating;
import gamestudio.server.service.RatingService;
import gamestudio.server.service.exception.RatingException;
import org.springframework.web.client.RestTemplate;

public class RatingServiceRestClient implements RatingService
{
    private final String url;
    private final RestTemplate restTemplate;

    public RatingServiceRestClient(RestTemplate restTemplate)
    {
        this.restTemplate = restTemplate;
        this.url = "http://localhost:8080/api/rating";
    }


    public void setRating(Rating rating)
    {
        restTemplate.postForEntity(url, rating, Void.class);
    }

    @Override
    public void setRating(String game, int rating) throws RatingException {

    }

    @Override
    public float getAverageRating(String game)
    {
        Float rating = restTemplate.getForObject(url + "/" + game, Float.class);

        return rating == null ? -1 : rating;
    }

    @Override
    public RatingSummaryResponse getRatingSummary(String game) throws RatingException {
        return null;
    }

    @Override
    public int getMyRating(String game)
    {
        Integer rating = restTemplate.getForObject(url + "/" + game, Integer.class);

        return rating == null ? -1 : rating;
    }

    @Override
    public int getRatingCount(String game)
    {
        Integer ratingCount = restTemplate.getForObject(url + "/" + game + "/count", Integer.class);

        return ratingCount == null ? -1 : ratingCount;
    }

    @Override
    public void reset()
    {
        throw new UnsupportedOperationException("Not supported via web service");
    }
}
