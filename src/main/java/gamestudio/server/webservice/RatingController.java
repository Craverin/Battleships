package gamestudio.server.webservice;

import gamestudio.server.dto.RatingSummaryResponse;
import gamestudio.server.entity.Rating;
import gamestudio.server.service.RatingService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rating")
public class RatingController
{
    private final RatingService ratingService;

    public RatingController(RatingService ratingService)
    {
        this.ratingService = ratingService;
    }

    @GetMapping("/{game}")
    public float getAverageRating(@PathVariable String game)
    {
        return ratingService.getAverageRating(game);
    }

    @GetMapping("/{game}/count")
    public int getRatingCount(@PathVariable String game)
    {
        return ratingService.getRatingCount(game);
    }

    @GetMapping("/{game}/summary")
    public RatingSummaryResponse getRatingSummary(@PathVariable String game)
    {
        return ratingService.getRatingSummary(game);
    }

    @GetMapping("/{game}/me")
    public int getMyRating(@PathVariable String game)
    {
        return ratingService.getMyRating(game);
    }

    @PostMapping("/{game}")
    public void setRating(@PathVariable String game, @RequestBody int rating)
    {
        ratingService.setRating(game, rating);
    }
}
