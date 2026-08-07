package gamestudio.server.controller;

import gamestudio.server.domain.Game;
import gamestudio.server.dto.MatchmakingResponse;
import gamestudio.server.dto.MatchmakingStatus;
import gamestudio.server.security.principal.ApplicationPrincipal;
import gamestudio.server.service.GameService;
import gamestudio.server.service.MatchmakingService;
import gamestudio.server.service.SseService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/games")
public class MatchmakingController
{
    private final GameService gameService;
    private final MatchmakingService matchmakingService;
    private final SseService sseService;

    public MatchmakingController(GameService gameService, SseService sseService, MatchmakingService matchmakingService)
    {
        this.gameService = gameService;
        this.matchmakingService = matchmakingService;
        this.sseService = sseService;
    }

    @PostMapping("/find")
    public MatchmakingResponse findGame()
    {
        MatchmakingResponse resp = matchmakingService.findGame();
        UUID gameId = resp.gameId();

        if (resp.status().equals(MatchmakingStatus.MATCHED))
        {
            Game game = gameService.getGame(gameId);
            UUID hostToken = game.getHostToken();

            String opponentUsername = game.getUsername(game.getOpponentToken());
            sseService.sendToPlayer(gameId, hostToken, "opponent-joined", opponentUsername);
        }

        return resp;
    }

    @PostMapping("/{gameId}/cancel")
    public void cancelSearch(@PathVariable UUID gameId,
                             @RequestHeader("Player-Token") UUID playerToken)
    {
        matchmakingService.cancelSearch(gameId, playerToken);
    }
}
