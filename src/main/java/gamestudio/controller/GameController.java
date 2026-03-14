package gamestudio.controller;

import gamestudio.dto.*;
import gamestudio.domain.*;
import gamestudio.dto.*;
import gamestudio.domain.Coordinate;
import gamestudio.domain.Game;
import gamestudio.service.GameService;
import gamestudio.service.SseService;
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

    @PostMapping
    public CreateGameResponse createGame()
    {
        return gameService.createGame();
    }

    @PutMapping("/{gameId}/shoot")
    public CombatViewResponse shoot(@PathVariable UUID gameId,
                                    @RequestHeader("Player-Token") UUID playerToken,
                                    @RequestBody Coordinate cell)
    {
        Game game = gameService.getGame(gameId);
        CombatViewResponse resp = gameService.shoot(gameId, playerToken, cell);

        sseService.sendToPlayer(gameId, game.getHostToken(), "shot", resp);
        sseService.sendToPlayer(gameId, game.getOpponentToken(), "shot", resp);

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

        if (canMove) sseService.sendToPlayer(gameId, playerToken, "ship_moved", resp);

        return new ResponseEntity<>(resp, canMove ? HttpStatus.OK : HttpStatus.CONFLICT);
    }

    @PostMapping("/{gameId}/join")
    public String joinGame(@PathVariable UUID gameId)
    {
        UUID opponentToken = gameService.joinGame(gameId);
        UUID hostToken = gameService.getGame(gameId).getHostToken();

        sseService.sendToPlayer(gameId, hostToken, "opponent_joined", "");

        return opponentToken.toString();
    }

    @GetMapping("/{gameId}/ships")
    public List<ShipResponse> getShips(@PathVariable UUID gameId,
                                       @RequestHeader("Player-Token") UUID playerToken)
    {
        return gameService.getShips(gameId, playerToken);
    }


}
