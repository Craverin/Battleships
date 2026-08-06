package gamestudio.server.service;

import gamestudio.server.domain.*;
import gamestudio.server.dto.*;
import gamestudio.server.security.principal.ApplicationPrincipal;
import gamestudio.server.service.authentication.CurrentUserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService
{
    private final Map<String, UUID> inviteCodes;
    private final Map<UUID, Game> games;
    private final CurrentUserService currentUserService;
    private final PlayerStatsService playerStatsService;
    private final ScoreService scoreService;

    private static final Map<CellState, CellStateView> opponentCellStateMap = new HashMap<>()
    {
        {
            put(CellState.EMPTY, CellStateView.UNKNOWN);
            put(CellState.OCCUPIED, CellStateView.UNKNOWN);
            put(CellState.INDIRECTLY_OCCUPIED, CellStateView.UNKNOWN);
            put(CellState.MISS,  CellStateView.MISS);
            put(CellState.HIT,  CellStateView.HIT);
            put(CellState.SUNK,  CellStateView.SUNK);
            put(CellState.BLOCKED, CellStateView.BLOCKED);
        }
    };


    public GameService(CurrentUserService currentUserService, PlayerStatsService playerStatsService, ScoreService scoreService)
    {
        this.games = new ConcurrentHashMap<>();
        this.inviteCodes = new ConcurrentHashMap<>();
        this.currentUserService = currentUserService;
        this.playerStatsService = playerStatsService;
        this.scoreService = scoreService;
    }

    public CreateGameResponse createGame()
    {
        Game game = new Game();
        UUID gameId = game.createGame();

        games.put(gameId, game);
        currentUserService.getCurrentAuthUserOptional().ifPresent(game::setHostUser);

        return new CreateGameResponse(gameId, game.getHostToken());
    }

    public CreatePrivateGameResponse createPrivateGame()
    {
        Game game = new Game();
        UUID gameId = game.createGame();
        String inviteCode = generateInviteCode();

        games.put(gameId, game);
        inviteCodes.put(inviteCode, gameId);

        currentUserService.getCurrentAuthUserOptional().ifPresent(game::setHostUser);

        return new CreatePrivateGameResponse(gameId, game.getHostToken(), inviteCode);
    }


    public CreatePrivateGameResponse createPrivateGame(CellState[][] cells, List<Ship> ships)
    {
        Game game = new Game();
        UUID gameId = game.createGame(cells, ships);
        String inviteCode = generateInviteCode();

        games.put(gameId, game);
        inviteCodes.put(inviteCode, gameId);
        return new CreatePrivateGameResponse(gameId, game.getHostToken(), inviteCode);
    }

    public UUID joinGame(UUID gameId)
    {
        // todo атомарность
        Game game = games.get(gameId);
        if (game.getOpponentToken() != null) return null;

        UUID opponentToken = game.addPlayer();

        currentUserService.getCurrentAuthUserOptional().ifPresent(game::setOpponentUser);

        return opponentToken;
    }

    public UUID joinGame(UUID gameId, CellState[][] cells, List<Ship> ships)
    {
        return games.get(gameId).addPlayer(cells, ships);
    }

    public UUID getGameIdByInviteCode(String inviteCode)
    {
        if (inviteCode != null && inviteCode.length() == 6)
        {
            UUID gameId = inviteCodes.get(inviteCode);
            if (gameId != null) return gameId;
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game with this invite code was not found");
    }

    public boolean moveShip(UUID gameId,
                            UUID shipId,
                            UUID playerToken,
                            Coordinate newStart,
                            Orientation newOrientation)
    {
        Game game = games.get(gameId);
        Board board = game.getBoard(playerToken);

        Orientation orientation = (newOrientation == null)
                                  ? board.getShipById(shipId).getOrientation()
                                  : newOrientation;

        return board.moveShip(board.getShipById(shipId), newStart, orientation);
    }

    public Game getGame(UUID gameId) { return games.get(gameId); }

    public List<ShipResponse> getShips(UUID gameId, UUID playerToken)
    {
        return toShipResponses(games.get(gameId).getBoard(playerToken).getShips());
    }

    private List<ShipResponse> toShipResponses(List<Ship> ships)
    {
        List<ShipResponse> shipResponses = new ArrayList<>();

        for (Ship s : ships)
        {
            Coordinate start = s.getStart();
            shipResponses.add(new ShipResponse(s.getId(), start.row(), start.col(),
                    s.getOrientation(), s.getLength()));
        }

        return shipResponses;
    }

    public CombatViewResponse shoot(UUID gameId, UUID playerToken, Coordinate cell)
    {
        Game game = games.get(gameId);
        if (game == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game was not found");

        if (!game.getCurrentTurn().equals(playerToken))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "It is not your turn");

        Board hostBoard = game.getBoard(playerToken);
        Board opponentBoard = game.getOpponentBoard(playerToken);
        ShotResult shotResult = opponentBoard.shoot(cell);

        CellState[][] hostCells = hostBoard.getCells();
        CellStateView[][] opponentCells = transform(opponentBoard.getCells());

        hostBoard.setLastShotResult(shotResult);
        game.changeCurrentTurn(shotResult);
        game.updateScore(shotResult, playerToken, Game.BASE_SCORE_PER_HIT);

        if (hostBoard.getShips().isEmpty() || opponentBoard.getShips().isEmpty())
        {
            game.markGameAsFinished();
            recordGameStats(game);
        }

        return new CombatViewResponse(game.getPhase(),
                                      shotResult,
                                      game.getCurrentTurn().equals(playerToken),
                                      game.getScore(playerToken),
                                      hostCells,
                                      opponentCells);
    }

    public CombatViewResponse getCombatView(UUID gameId, UUID playerToken)
    {
        Game game = getGame(gameId);
        if (game == null || game.getPhase().equals(GamePhase.PLACEMENT)) return null;

        Board hostBoard = game.getBoard(playerToken);

        CellState[][] hostCells = hostBoard.getCells();
        CellStateView[][] opponentCells = transform(game.getOpponentBoard(playerToken).getCells());

        return new CombatViewResponse(game.getPhase(),
                                      hostBoard.getLastShotResult(),
                                      game.getCurrentTurn().equals(playerToken),
                                      game.getScore(playerToken),
                                      hostCells,
                                      opponentCells);
    }

    private CellStateView[][] transform(CellState[][] cells)
    {
        CellStateView[][] cellsView = new CellStateView[Board.SIZE][Board.SIZE];
        for (int i = 0; i < cells.length; i++)
        {
            for (int j = 0; j < cells[i].length; j++)
                cellsView[i][j] = opponentCellStateMap.get(cells[i][j]);
        }

        return cellsView;
    }

    private String generateInviteCode()
    {
        char[] chars = "ABCDEFGHIJKLMNPQRSTUVWXYZ123456789".toCharArray();
        String code = "";

        do
        {
            SecureRandom rnd = new SecureRandom();
            for (int i = 0; i < 6; i++)
                code = code.concat(String.valueOf(chars[rnd.nextInt(chars.length)]));
        } while(inviteCodes.containsKey(code));

        return code;
    }

    public void removeGame(UUID gameId)
    {
        inviteCodes.values().remove(gameId);
        games.remove(gameId);
    }

    private void recordGameStats(Game game)
    {
        if (game.isStatsRecorded()) return;

        recordStatsForPlayer(game, game.getHostToken());
        recordStatsForPlayer(game, game.getOpponentToken());

        game.markStatsRecorded();
    }

    private void recordStatsForPlayer(Game game, UUID playerToken)
    {
        ApplicationPrincipal applicationPrincipal = game.getUserByToken(playerToken);
        if (applicationPrincipal == null) return;


        playerStatsService.recordGameResult("battleships",
                                                  applicationPrincipal.userId(),
                                                  applicationPrincipal.username(),
                                                  game.getScore(playerToken),
                                                  playerToken.equals(game.getWinner()));

        scoreService.addScore("battleships", applicationPrincipal.userId(), applicationPrincipal.username(), game.getScore(playerToken));
    }

}
