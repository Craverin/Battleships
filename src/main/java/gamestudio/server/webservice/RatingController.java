package gamestudio.server.webservice;

import gamestudio.dto.RatingSummaryResponse;
import gamestudio.entity.Rating;
import gamestudio.service.RatingService;
import gamestudio.service.jpa.RatingServiceJPA;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rating")
@Import(RatingServiceJPA.class)
public class RatingController
{
    private final RatingService ratingService;

    public RatingController(RatingService ratingService)
    {
        this.ratingService = ratingService;
    }

    @GetMapping("/{game}")
    public int getAverageRating(@PathVariable String game)
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
        return new RatingSummaryResponse(ratingService.getAverageRating(game),
                                         ratingService.getRatingCount(game));
    }

    @GetMapping("/{game}/players/{player}")
    public int getRating(@PathVariable String game, @PathVariable String player)
    {
        return ratingService.getRating(game, player);
    }

    @PostMapping
    public void setRating(@RequestBody Rating rating)
    {
        ratingService.setRating(rating);
    }
}
