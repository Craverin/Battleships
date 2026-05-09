package gamestudio.server.webservice;

import gamestudio.server.entity.Score;
import gamestudio.server.service.ScoreService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/score")
public class ScoreController
{
    private final ScoreService scoreService;

    public ScoreController(ScoreService scoreService)
    {
        this.scoreService = scoreService;
    }

    @GetMapping("/{game}")
    public List<Score> getTopScores(@PathVariable String game)
    {
        return scoreService.getTopScores(game);
    }

    @GetMapping("/{game}/players/{player}")
    public int getTopScore(@PathVariable String game, @PathVariable String player)
    {
        return scoreService.getTopScore(game, player);
    }

    @PostMapping
    public void addScore(@RequestBody Score score)
    {
        scoreService.addScore(score);
    }

}
