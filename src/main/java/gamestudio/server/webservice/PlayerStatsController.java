package gamestudio.server.webservice;

import gamestudio.server.dto.PlayerStatResponse;
import gamestudio.server.entity.PlayerStats;
import gamestudio.server.service.PlayerStatsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stats")
public class PlayerStatsController
{
    private final PlayerStatsService playerStatsService;

    public PlayerStatsController(PlayerStatsService playerStatsService) { this.playerStatsService = playerStatsService; }

    @GetMapping("/{game}/me")
    public PlayerStatResponse getMyStats(@PathVariable String game)
    {
        return playerStatsService.getMyStats(game);
    }

    @GetMapping("/{game}")
    public List<PlayerStats> getTopPlayers(@PathVariable String game,
                                           @RequestParam String sortBy,
                                           @RequestParam String sortType)
    {
        return playerStatsService.getTopPlayers(game, sortBy, sortType);
    }
}
