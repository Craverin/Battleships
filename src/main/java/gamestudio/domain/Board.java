package gamestudio.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class Board
{
    private final CellState[][] cells;
    private final List<Ship> ships;
    public static final int SIZE = 10;

    public Board()
    {
        this.ships = new ArrayList<>();
        this.cells = new CellState[SIZE][SIZE];
        resetCellStates();
    }

    public Board(CellState[][] cells, List<Ship> ships)
    {
        this.cells = cells;
        this.ships = ships;
    }

    public void generateShips()
    {
        Random rand = new Random();
        boolean canLand = false;

        // TODO Test values. Should be changed when done

        for (int length = 4; length > 0; length--)
        {
            for (int quantity = 5 - length; quantity > 0; quantity--)
            {
                for (int i = 0; i < 10000; i++)
                {
                    Coordinate start = new Coordinate(rand.nextInt(SIZE), rand.nextInt(SIZE));
                    Orientation orientation = Orientation.values()[rand.nextInt(Orientation.values().length)];

                    Ship ship = new Ship(UUID.randomUUID(), start, orientation, length);
                    canLand = canLand(ship);

                    if (canLand)
                    {
                        ships.add(ship);
                        recalculateCellStates();
                        break;
                    }
                }
                if (!canLand) throw new RuntimeException();
            }
        }
    }

    public boolean canLand(Ship ship)
    {
        for (Coordinate cell : ship.getCells())
        {
            if (!Coordinate.isValid(cell) || !cells[cell.row()][cell.col()].equals(CellState.EMPTY)) return false;
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
        for (int i = 0; i < SIZE; i++)
        {
            for (int j = 0; j < SIZE; j++)
                cells[i][j] = CellState.EMPTY;
        }
    }

    public void shoot(Coordinate coordinate)
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
                return;
            }
        }

        cells[coordinate.row()][coordinate.col()] = CellState.MISS;
    }

    private void blockNearbyCells(Coordinate coordinate)
    {
        for (int r = -1; r <= 1; r += 2)
        {
            for (int c = -1; c <= 1; c += 2)
            {
                int row  = coordinate.row() + r, col = coordinate.col() + c;
                if (Coordinate.isValid(row, col) && cells[row][col].equals(CellState.EMPTY))
                    cells[row][col] = CellState.BLOCKED;
            }
        }
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
