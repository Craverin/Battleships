package gamestudio.service;

import gamestudio.dto.*;
import gamestudio.domain.*;
import gamestudio.domain.*;
import gamestudio.dto.*;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class GameService
{
    private final Map<UUID, Game> games;

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

    public GameService()
    {
        this.games = new HashMap<>();
    }

    public CreateGameResponse createGame()
    {
        Game game = new Game();
        UUID gameId = game.createGame();

        games.put(gameId, game);
        return new CreateGameResponse(gameId, game.getHostToken());
    }

    public CreateGameResponse createGame(CellState[][] cells, List<Ship> ships)
    {
        Game game = new Game();
        UUID gameId = game.createGame(cells, ships);

        games.put(gameId, game);
        return new CreateGameResponse(gameId, game.getHostToken());
    }

    public UUID joinGame(UUID gameId)
    {
        return games.get(gameId).addPlayer();
    }

    public UUID joinGame(UUID gameId, CellState[][] cells, List<Ship> ships)
    {
        return games.get(gameId).addPlayer(cells, ships);
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
        Game game = getGame(gameId);
        Board hostBoard = game.getBoard(playerToken);
        Board opponentBoard = game.getOpponentBoard(playerToken);
        ShotResult shotResult = opponentBoard.shoot(cell);

        CellState[][] hostCells = game.getBoard(playerToken).getCells();
        CellStateView[][] opponentCells = transform(opponentBoard.getCells());

        hostBoard.setLastShotResult(shotResult);
        game.updateScore(shotResult, playerToken, Game.BASE_SCORE_PER_HIT);

        return new CombatViewResponse(game.getPhase(), shotResult, game.getScore(playerToken), hostCells, opponentCells);
    }


    public CombatViewResponse getCombatView(UUID gameId, UUID playerToken)
    {
        Game game = getGame(gameId);
        if (game.getPhase().equals(GamePhase.PLACEMENT)) return null;

        Board hostBoard = game.getBoard(playerToken);

        CellState[][] hostCells = hostBoard.getCells();
        CellStateView[][] opponentCells = transform(game.getOpponentBoard(playerToken).getCells());

        return new CombatViewResponse(game.getPhase(), hostBoard.getLastShotResult(), game.getScore(playerToken), hostCells, opponentCells);
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
}
