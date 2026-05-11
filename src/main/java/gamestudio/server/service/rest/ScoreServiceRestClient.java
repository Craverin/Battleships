package gamestudio.server.service.rest;

import gamestudio.server.entity.Score;
import gamestudio.server.service.ScoreService;
import gamestudio.server.service.exception.ScoreException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

public class ScoreServiceRestClient implements ScoreService
{
    private final String url;
    private final RestTemplate restTemplate;

    public ScoreServiceRestClient(RestTemplate restTemplate)
    {
        this.restTemplate = restTemplate;
        this.url = "http://localhost:8080/api/score";
    }


    public void addScore(Score score)
    {
        restTemplate.postForEntity(url, score, Void.class);
    }

    @Override
    public void addScore(String game, int userId, String username, int score) throws ScoreException {

    }

    @Override
    public List<Score> getTopScores(String game)
    {
        Score[] scores = restTemplate.getForObject(url + "/" + game, Score[].class);

        return scores == null ? List.of() : Arrays.asList(scores);
    }

    @Override
    public int getMyTopScore(String game)
    {
        Integer score = restTemplate.getForObject(url + "/" + game, Integer.class);

        return score == null ? -1 : score;
    }

    @Override
    public void reset()
    {
        throw new UnsupportedOperationException("Not supported via web service");
    }
}
