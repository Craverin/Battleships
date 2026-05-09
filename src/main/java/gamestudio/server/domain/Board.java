package gamestudio.server.domain;

import gamestudio.server.dto.ShotResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class Board
{
    private final CellState[][] cells;
    private final List<Ship> ships;
    private ShotResult lastShotResult;
    public static final int SIZE = 10;

    public Board()
    {
        this.ships = new ArrayList<>();
        this.cells = new CellState[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++)
        {
            for (int j = 0; j < SIZE; j++)
                cells[i][j] = CellState.EMPTY;
        }
        lastShotResult = ShotResult.NONE;
    }

    public Board(CellState[][] cells, List<Ship> ships)
    {
        this.cells = cells;
        this.ships = ships;
        lastShotResult = ShotResult.NONE;
    }

    public void generateShips()
    {
        // One of the ship placement methods, which randomly places the standard fleet:
        // 1 ship of length 4, 2 ships of length 3, 3 ships of length 2, and 4 ships of length 1
        for (int length = 4; length > 0; length--)
        {
            for (int quantity = 5 - length; quantity > 0; quantity--)
                generateShip(length);
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

    public boolean moveShip(Ship oldShip, Coordinate newStart, Orientation newOrientation)
    {
        Ship newShip = new Ship(oldShip.getId(), newStart, newOrientation, oldShip.getLength());
        removeShip(oldShip);
        recalculateBorderCells();

        if (!canLand(newShip))
        {
            addShip(oldShip);
            return false;
        }

        addShip(newShip);
        return true;
    }

    private void recalculateBorderCells()
    {
        for (Ship ship : ships)
        {
            for (Coordinate cell : ship.getBorderCells())
                cells[cell.row()][cell.col()] = CellState.INDIRECTLY_OCCUPIED;
        }
    }

    private void generateShip(int length)
    {
        Random rnd = new Random();
        for (int i = 0; i < 10000; i++)
        {
            Coordinate start = new Coordinate(rnd.nextInt(SIZE), rnd.nextInt(SIZE));
            Orientation orientation = Orientation.values()[rnd.nextInt(Orientation.values().length)];

            Ship ship = new Ship(UUID.randomUUID(), start, orientation, length);

            if (canLand(ship))
            {
                addShip(ship);
                return;
            }
        }

        throw new RuntimeException();
    }

    private void removeShip(Ship ship)
    {
        ships.remove(ship);

        for (Coordinate cell : ship.getCells()) cells[cell.row()][cell.col()] = CellState.EMPTY;
        for (Coordinate cell : ship.getBorderCells()) cells[cell.row()][cell.col()] = CellState.EMPTY;
    }

    private void addShip(Ship ship)
    {
        ships.add(ship);

        for (Coordinate cell : ship.getCells()) cells[cell.row()][cell.col()] = CellState.OCCUPIED;
        for (Coordinate cell : ship.getBorderCells()) cells[cell.row()][cell.col()] = CellState.INDIRECTLY_OCCUPIED;
    }

    private boolean isAlreadyShotOrBlocked(Coordinate coordinate)
    {
        CellState cellState = cells[coordinate.row()][coordinate.col()];
        return cellState.equals(CellState.MISS) ||
               cellState.equals(CellState.HIT)  ||
               cellState.equals(CellState.SUNK) ||
               cellState.equals(CellState.BLOCKED);
    }

    public ShotResult shoot(Coordinate coordinate)
    {
        if (isAlreadyShotOrBlocked(coordinate)) return ShotResult.NONE;
        for (Ship ship : ships)
        {
            if (ship.hit(coordinate))
            {
                cells[coordinate.row()][coordinate.col()] = CellState.HIT;
                if (ship.isSunk())
                {
                    removeShip(ship);

                    // If the ship is sunk, block all surrounding border cells, including diagonal ones
                    markNearbyCellsAsBlocked(coordinate, ship.getBorderCells());
                    for (Coordinate cell : ship.getCells())
                        cells[cell.row()][cell.col()] = CellState.SUNK;
                    return ShotResult.SUNK;
                }

                // If the ship is only hit, block only diagonal cells to avoid revealing the rest of the ship
                markNearbyCellsAsBlocked(coordinate, null);
                return ShotResult.HIT;
            }
        }

        cells[coordinate.row()][coordinate.col()] = CellState.MISS;
        return ShotResult.MISS;
    }

    public void setLastShotResult(ShotResult shotResult) { this.lastShotResult = shotResult; }
    public ShotResult getLastShotResult() { return lastShotResult; }

    private void markNearbyCellsAsBlocked(Coordinate coordinate, List<Coordinate> sunkShipBorderCells)
    {
        for (int r = -1; r <= 1; r += 2)
        {
            for (int c = -1; c <= 1; c += 2)
            {
                int row = coordinate.row() + r, col = coordinate.col() + c;
                if (!Coordinate.isValid(row, col)) continue;

                CellState currCellState = cells[row][col];
                if (currCellState.equals(CellState.EMPTY) || currCellState.equals(CellState.INDIRECTLY_OCCUPIED))
                    cells[row][col] = CellState.BLOCKED;
            }
        }

        if (sunkShipBorderCells != null)
        {
            for (Coordinate cell : sunkShipBorderCells) cells[cell.row()][cell.col()] = CellState.BLOCKED;
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
