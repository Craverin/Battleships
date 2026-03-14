package gamestudio.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class Board
{
    private final CellState[][] cells;
    private final List<Ship> ships;
    private final int BOARD_SIZE = 10;

    public Board()
    {
        this.ships = new ArrayList<>();
        this.cells = new CellState[BOARD_SIZE][BOARD_SIZE];
        resetCellStates();
    }

    public void generateShips()
    {
        Random rand = new Random();
        boolean canLand = false;

        // TODO Test values. Should be changed when done

        for (int length = 4, j = 4; j > 0 ; j--)
        {
            for (int quantity = 6 - length; quantity > 0; quantity--)
            {
                for (int i = 0; i < 10000; i++)
                {
                    Coordinate start = new Coordinate(rand.nextInt(10), rand.nextInt(10));
                    Orientation orientation = (rand.nextInt(2) == 0)
                            ? Orientation.HORIZONTAL
                            : Orientation.VERTICAL;
                    Ship ship = new Ship(UUID.randomUUID(), start, orientation, length);
                    canLand = canLand(ship);

                    if (canLand)
                    {
                        ships.add(ship);
                        recalculateCellStates();
                        break;
                    }
                }
                //TODO
                if (!canLand) throw new RuntimeException();
            }
        }
    }

    public boolean canLand(Ship ship)
    {
        for (Coordinate cell : ship.getCells())
        {
            if (!inBounds(cell) || !cells[cell.row()][cell.col()].equals(CellState.EMPTY)) return false;
        }

        return true;
    }

    public boolean moveShip(Ship ship, Coordinate newStart, Orientation newOrientation)
    {
        Ship newShip = new Ship(ship.getId(), newStart, newOrientation, ship.getLength());

        ships.remove(ship);
        recalculateCellStates();
        ships.add(newShip);

        if (!canLand(newShip))
        {
            ships.remove(newShip);
            ships.add(ship);
            recalculateCellStates();
            System.out.println("Failed to move. Returning false");
            return false;
        }

      //  recalculateCellStates();
        System.out.println("Successfully moved. Returning true");
        return true;
    }

    private void recalculateCellStates()
    {
        resetCellStates();
        for (Ship ship : ships)
        {
            for (Coordinate c : ship.getCells())
                cells[c.row()][c.col()] = CellState.OCCUPIED;

            for (Coordinate c : ship.getBorderCells())
                cells[c.row()][c.col()] = CellState.INDIRECTLY_OCCUPIED;
        }
    }

    private void resetCellStates()
    {
        for (int i = 0; i < BOARD_SIZE; i++)
        {
            for (int j = 0; j < BOARD_SIZE; j++)
                cells[i][j] = CellState.EMPTY;
        }
    }

    public CellState[][] shoot(Coordinate coordinate)
    {
        blockNearbyCells(coordinate);
        for (Ship ship : ships)
        {
            if (ship.hit(coordinate))
            {
                cells[coordinate.row()][coordinate.col()] = CellState.HIT;
                if (ship.isSunk())
                {
                    for (Coordinate cell : ship.getCells())
                        cells[cell.row()][cell.col()] = CellState.SUNK;
                    ships.remove(ship);
                }
                return cells;
            }
        }

        cells[coordinate.row()][coordinate.col()] = CellState.MISS;
        return cells;
    }

    private void blockNearbyCells(Coordinate coordinate)
    {
        for (int r = -1; r <= 1; r += 2)
        {
            for (int c = -1; c <= 1; c += 2)
            {
                try
                {
                    Coordinate cell = new Coordinate(coordinate.row() + r, coordinate.col() + c);
                    if (inBounds(cell) && isEmpty(cell))
                        cells[cell.row()][cell.col()] = CellState.BLOCKED;
                }
                catch (IllegalArgumentException ignored) {  }
            }
        }
    }

    private boolean inBounds(Coordinate coordinate)
    {
        int row = coordinate.row(), col = coordinate.col();
        return row >= 0 && row < 10 && col >= 0 && col < 10;
    }

    private boolean isEmpty(Coordinate coordinate)
    {
        return cells[coordinate.row()][coordinate.col()] == CellState.EMPTY;
    }

    public CellState[][] getCells()
    {
        return cells;
    }

    public List<Ship> getShips()
    {
        return ships;
    }

    public Ship getShipById(UUID id)
    {
        for (Ship ship : ships) if (ship.getId().equals(id)) return ship;
        return null;
    }

}
