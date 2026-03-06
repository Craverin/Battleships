package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.*;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class GameService
{
    private final Map<UUID, Game> games;
    private static Map<CellState, CellStateView> hostCellStateMap = new HashMap<>()
    {
        {
            put(CellState.EMPTY, CellStateView.EMPTY);
            put(CellState.OCCUPIED, CellStateView.SHIP);
            put(CellState.INDIRECTLY_OCCUPIED, CellStateView.EMPTY);
            put(CellState.MISS,  CellStateView.MISS);
            put(CellState.HIT,  CellStateView.HIT);
            put(CellState.SUNK,  CellStateView.SUNK);
            put(CellState.BLOCKED, CellStateView.BLOCKED);
        }
    };

    private static Map<CellState, CellStateView> opponentCellStateMap = new HashMap<>()
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

    public UUID joinGame(UUID gameId)
    {
        return games.get(gameId).addPlayer();
    }

    public boolean moveShip(UUID gameId,
                            UUID shipId,
                            UUID playerToken,
                            Coordinate newStart,
                            Orientation newOrientation)
    {
        Game game = games.get(gameId);
        Board board = game.getBoard(playerToken);

        Orientation orientation = newOrientation == null
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
        CellState[][] cells = getGame(gameId).getOpponentBoard(playerToken).shoot(cell);

        ShotResult shotResult = getShotResult(cells, cell);
        CellStateView[][] hostBoard = new CellStateView[10][10];
        CellStateView[][] opponentBoard = new CellStateView[10][10];

       for (int i = 0; i < cells.length; i++)
       {
           for (int j = 0; j < cells[i].length; j++)
           {
               hostBoard[i][j] = hostCellStateMap.get(cells[i][j]);
               opponentBoard[i][j] = opponentCellStateMap.get(cells[i][j]);
           }
       }

      return new CombatViewResponse(shotResult, hostBoard, opponentBoard);
    }

    private ShotResult getShotResult(CellState[][] cells, Coordinate cell)
    {
        if (cells[cell.row()][cell.col()].equals(CellState.MISS)) return ShotResult.MISS;
        return ShotResult.HIT;
    }
}
