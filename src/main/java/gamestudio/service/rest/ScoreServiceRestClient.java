package gamestudio.service.rest;

import gamestudio.entity.Score;
import gamestudio.service.ScoreService;
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

    @Override
    public void addScore(Score score)
    {
        restTemplate.postForEntity(url, score, Void.class);
    }

    @Override
    public List<Score> getTopScores(String game)
    {
        Score[] scores = restTemplate.getForObject(url + "/" + game, Score[].class);

        return scores == null ? List.of() : Arrays.asList(scores);
    }

    @Override
    public int getTopScore(String game, String player)
    {
        Integer score = restTemplate.getForObject(url + "/" + game + "/players/" + player, Integer.class);

        return score == null ? -1 : score;
    }

    @Override
    public void reset()
    {
        throw new UnsupportedOperationException("Not supported via web service");
    }
}
