package gamestudio.cli;

import gamestudio.server.domain.Board;
import gamestudio.server.domain.CellState;
import gamestudio.server.domain.Coordinate;
import gamestudio.server.domain.Orientation;
import gamestudio.server.dto.CellStateView;
import gamestudio.server.dto.CombatViewResponse;
import gamestudio.server.dto.ShipResponse;

import java.util.*;

import static gamestudio.cli.Color.*;

public class BoardRenderer
{
    private final boolean[][] occupiedCells = new boolean[Board.SIZE][Board.SIZE];
    private final Map<CellState, String> hostCellMarkers = new HashMap<>()
    {
        {
            put(CellState.EMPTY, " ");
            put(CellState.OCCUPIED, ANSI_CYAN.unicode + "■" + ANSI_RESET.unicode);
            put(CellState.INDIRECTLY_OCCUPIED, " ");
            put(CellState.BLOCKED, " ");
            put(CellState.HIT, ANSI_RED.unicode + "X" + ANSI_RESET.unicode);
            put(CellState.MISS, ANSI_YELLOW.unicode + "*" + ANSI_RESET.unicode);
            put(CellState.SUNK, ANSI_PURPLE.unicode + "#" + ANSI_RESET.unicode);
        }
    };

    private final Map<CellStateView, String> opponentCellMarkers = new HashMap<>()
    {
        {
            put(CellStateView.UNKNOWN, " ");
            put(CellStateView.SHIP, ANSI_CYAN.unicode + "■" + ANSI_RESET.unicode);
            put(CellStateView.BLOCKED, ANSI_WHITE.unicode + "." + ANSI_RESET.unicode);
            put(CellStateView.HIT, ANSI_RED.unicode + "X" + ANSI_RESET.unicode);
            put(CellStateView.MISS, ANSI_YELLOW.unicode + "*" + ANSI_RESET.unicode);
            put(CellStateView.SUNK, ANSI_PURPLE.unicode + "#" + ANSI_RESET.unicode);
        }
    };

    public void drawPlacementBoard(List<ShipResponse> ships)
    {
        BoardMessages.printColumns();
        fillOccupiedCells(ships);

        for (int i = 0; i < Board.SIZE; i++)
        {
            System.out.println("  ");
            BoardMessages.printBorderLine();

            if (i < Board.SIZE - 1) System.out.print(" ");
            System.out.print(i + 1 + " ");

            for (int k = 0; k < Board.SIZE; k++)
                System.out.print("|  " + (occupiedCells[i][k] ? ANSI_CYAN.unicode + "■" + ANSI_RESET.unicode : " ") + "  ");

            System.out.print("|");
        }

        System.out.println();
        BoardMessages.printBorderLine();
    }

    public void drawCombatBoard(CombatViewResponse combatView, UUID playerToken, UUID hostToken, int score)
    {
        boolean isHostBoard = playerToken.equals(hostToken);
        if (!isHostBoard)
            System.out.println(ANSI_BLUE.unicode + "Score: " + score + ANSI_RESET.unicode);

        BoardMessages.printColumns();
        for (int i = 0; i < Board.SIZE; i++)
        {
            System.out.println("  ");
            BoardMessages.printBorderLine();

            if (i < Board.SIZE - 1) System.out.print(" ");
            System.out.print(i + 1 + " ");

            for (int k = 0; k < Board.SIZE; k++)
            {
                System.out.print("|  ");

                if (isHostBoard)
                    System.out.print(hostCellMarkers.get(combatView.hostBoard()[i][k]));
                else
                    System.out.print(opponentCellMarkers.get(combatView.opponentBoard()[i][k]));

                System.out.print("  ");
            }
            System.out.print("|");

        }

        System.out.println();
        BoardMessages.printBorderLine();
    }

    public void fillOccupiedCells(List<ShipResponse> ships)
    {
        resetOccupiedCells();
        for (ShipResponse ship : ships)
        {
            for (Coordinate cell : getShipCoordinates(ship))
                occupiedCells[cell.row()][cell.col()] = true;
        }
    }

    public void resetOccupiedCells()
    {
        for (int i = 0; i < Board.SIZE; i++)
        {
            for (int j = 0; j < Board.SIZE; j++)
                occupiedCells[i][j] = false;
        }
    }

    public List<Coordinate> getShipCoordinates(ShipResponse ship)
    {
        List<Coordinate> coordinates = new ArrayList<>();
        coordinates.add(new Coordinate(ship.row(), ship.col()));

        for (int i = 1; i < ship.length(); i++)
        {
            if (ship.orientation().equals(Orientation.VERTICAL))
                coordinates.add(new Coordinate(ship.row() + i, ship.col()));
            else
                coordinates.add(new Coordinate(ship.row(), ship.col() + i));
        }

        return coordinates;
    }
}
