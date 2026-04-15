package gamestudio.server.webservice;

import gamestudio.entity.Score;
import gamestudio.service.ScoreService;
import gamestudio.service.jpa.ScoreServiceJPA;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/score")
@Import(ScoreServiceJPA.class)
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
