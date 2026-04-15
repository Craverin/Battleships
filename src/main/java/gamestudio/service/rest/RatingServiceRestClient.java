package gamestudio.service.rest;

import gamestudio.entity.Rating;
import gamestudio.service.RatingService;
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

    @Override
    public void setRating(Rating rating)
    {
        restTemplate.postForEntity(url, rating, Void.class);
    }

    @Override
    public int getAverageRating(String game)
    {
        Integer rating = restTemplate.getForObject(url + "/" + game, Integer.class);

        return rating == null ? -1 : rating;
    }

    @Override
    public int getRating(String game, String player)
    {
        Integer rating = restTemplate.getForObject(url + "/" + game + "/players/" + player, Integer.class);

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
