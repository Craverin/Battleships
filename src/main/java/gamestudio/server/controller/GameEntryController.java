package gamestudio.server.controller;

import gamestudio.server.domain.Game;
import gamestudio.server.dto.CreatePrivateGameResponse;
import gamestudio.server.dto.JoinGameResponse;
import gamestudio.server.security.principal.ApplicationPrincipal;
import gamestudio.server.service.GameService;
import gamestudio.server.service.SseService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/games")
public class GameEntryController
{
    private final GameService gameService;
    private final SseService sseService;

    public GameEntryController(GameService gameService, SseService sseService)
    {
        this.gameService = gameService;
        this.sseService = sseService;
    }


    @PostMapping
    public CreatePrivateGameResponse createGame()
    {
        return gameService.createPrivateGame();
    }

    @PostMapping("/{inviteCode}/join")
    public JoinGameResponse joinPrivateGame(@PathVariable String inviteCode)
    {
        UUID gameId = gameService.getGameIdByInviteCode(inviteCode);
        Game game = gameService.getGame(gameId);

        UUID opponentToken = gameService.joinGame(gameId);
        UUID hostToken = game.getHostToken();

        String hostUsername = game.getUsername(hostToken);
        String opponentUsername = game.getUsername(opponentToken);

        sseService.sendToPlayer(gameId, hostToken, "opponent-joined", opponentUsername);

        return new JoinGameResponse(gameId, opponentToken, hostUsername);
    }
}
