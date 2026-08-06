package gamestudio.server.controller;

import gamestudio.server.domain.Coordinate;
import gamestudio.server.domain.Game;
import gamestudio.server.domain.GamePhase;
import gamestudio.server.dto.*;
import gamestudio.server.service.GameService;
import gamestudio.server.service.MatchmakingService;
import gamestudio.server.service.SseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/games")

public class GameController
{
    private final GameService gameService;
    private final SseService sseService;

    public GameController(GameService gameService, SseService sseService)
    {
        this.gameService = gameService;
        this.sseService = sseService;
    }

    @PostMapping("/{gameId}/ready")
    public void setReady(@PathVariable UUID gameId,
                         @RequestHeader("Player-Token") UUID playerToken)
    {
        Game game = gameService.getGame(gameId);
        if (game == null) return;

        if (game.setReady(playerToken))
        {
            UUID opponentToken = game.getHostToken().equals(playerToken)
                                 ? game.getOpponentToken()
                                 : game.getHostToken();

            sseService.sendToPlayer(gameId, opponentToken, "opponent-ready", "");
            if (game.getPhase().equals(GamePhase.COMBAT))
            {
                UUID hostToken = game.getHostToken(), oppToken = game.getOpponentToken();

                CombatViewResponse hostResp = gameService.getCombatView(gameId, hostToken);
                CombatViewResponse oppResp = gameService.getCombatView(gameId, oppToken);

                sseService.sendToPlayer(gameId, hostToken, "battle-started", hostResp);
                sseService.sendToPlayer(gameId, oppToken, "battle-started", oppResp);
            }
        }
    }

    @PutMapping("/{gameId}/shoot")
    public CombatViewResponse shoot(@PathVariable UUID gameId,
                                    @RequestHeader("Player-Token") UUID playerToken,
                                    @RequestBody Coordinate cell)
    {
        Game game = gameService.getGame(gameId);
        CombatViewResponse resp = gameService.shoot(gameId, playerToken, cell);

        UUID opponentToken = game.getHostToken().equals(playerToken) ? game.getOpponentToken() : game.getHostToken();
        CombatViewResponse opponentResp = gameService.getCombatView(gameId, opponentToken);

        sseService.sendToPlayer(gameId, playerToken, "opponent-shoot", resp);
        sseService.sendToPlayer(gameId, opponentToken, "opponent-shoot", opponentResp);

        if (resp.phase().equals(GamePhase.FINISHED))
        {
            sseService.sendToPlayer(gameId, playerToken, "game-over",
                                    new GameOverResponse(game.getWinner().equals(playerToken),
                                                         game.getScore(playerToken)));
            sseService.sendToPlayer(gameId, opponentToken, "game-over",
                                    new GameOverResponse(game.getWinner().equals(opponentToken),
                                                         game.getScore(opponentToken)));
            gameService.removeGame(gameId);
        }

        return resp;
    }

    @PutMapping("/{gameId}/ships/move/{shipId}")
    public ResponseEntity<PlacementViewResponse> moveShip(@PathVariable UUID gameId,
                                                          @PathVariable UUID shipId,
                                                          @RequestHeader("Player-Token") UUID playerToken,
                                                          @RequestBody MoveShipRequest pos)
    {
        boolean canMove = gameService.moveShip(gameId, shipId, playerToken,
                                               new Coordinate(pos.row(), pos.col()),
                                               pos.orientation());
        List<ShipResponse> ships = gameService.getShips(gameId, playerToken);
        PlacementViewResponse resp = new PlacementViewResponse(ships);

        if (canMove) sseService.sendToPlayer(gameId, playerToken, "ship-moved", resp);

        return new ResponseEntity<>(resp, canMove ? HttpStatus.OK : HttpStatus.CONFLICT);
    }

    @GetMapping("/{gameId}/ships")
    public List<ShipResponse> getShips(@PathVariable UUID gameId,
                                       @RequestHeader("Player-Token") UUID playerToken)
    {
        return gameService.getShips(gameId, playerToken);
    }

}
