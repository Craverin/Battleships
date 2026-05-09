package gamestudio.server.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Ship
{
    private final UUID id;
    private final Coordinate start;
    private final Orientation orientation;
    private final int length;
    private final boolean[] hit;

    public Ship(UUID id, Coordinate start, Orientation orientation, int length)
    {
        this.start = start;
        this.orientation = orientation;
        this.length = length;
        this.id = id;
        this.hit = new boolean[length];
    }

    public List<Coordinate> getCells()
    {
        List<Coordinate> cells = new ArrayList<>();
        cells.add(start);

        for (int i = 1; i < length; i++)
        {
            if (orientation.equals(Orientation.HORIZONTAL))
                cells.add(new Coordinate(start.row(), start.col() + i));
            else
                cells.add(new Coordinate(start.row() + i, start.col()));
        }

        return cells;
    }

    public boolean occupies(Coordinate coordinate)
    {
        List<Coordinate> cells = getCells();
        for (Coordinate c : cells)
        {
            if (c.row() == coordinate.row() && c.col() == coordinate.col()) return true;
        }

        return false;
    }

    public List<Coordinate> getBorderCells()
    {
        // Returns all cells around the ship, excluding its own cells

        List<Coordinate> shipCells = getCells();
        List<Coordinate> borderCells = new ArrayList<>();
        Coordinate rightBorderCell;

        Coordinate leftBorderCell = new Coordinate(
                start.row() > 0 ? start.row() - 1 : 0,
                start.col() > 0 ? start.col() - 1 : 0
        );

        if (orientation.equals(Orientation.HORIZONTAL))
        {
            int endCol = start.col() + length - 1;
            rightBorderCell = new Coordinate(
                    start.row() < Board.SIZE - 1 ? start.row() + 1 : start.row(),
                    endCol < Board.SIZE - 1 ? endCol + 1 : endCol
            );
        }

        else
        {
            int endRow = start.row() + length - 1;
            rightBorderCell = new Coordinate(
                    endRow < Board.SIZE - 1 ? endRow + 1 : endRow,
                    start.col() < Board.SIZE - 1 ? start.col() + 1 : start.col()
            );
        }

        for (int i = leftBorderCell.row(); i <= rightBorderCell.row(); i++)
        {
            for (int j = leftBorderCell.col(); j <= rightBorderCell.col(); j++)
            {
                Coordinate cell = new Coordinate(i, j);
                if (shipCells.contains(cell)) continue;

                borderCells.add(new Coordinate(i, j));
            }
        }

        return borderCells;
    }

    public boolean hit(Coordinate coordinate)
    {
        if (!this.occupies(coordinate)) return false;
        int index = (orientation == Orientation.HORIZONTAL)
                    ? coordinate.col() - start.col()
                    : coordinate.row() - start.row();
        hit[index] = true;
        return true;
    }

    public boolean isSunk()
    {
        for (boolean b : hit) if (!b) return false;
        return true;
    }

    public UUID getId()
    {
        return id;
    }

    public Coordinate getStart()
    {
        return start;
    }

    public int getLength()
    {
        return length;
    }

    public Orientation getOrientation()
    {
        return orientation;
    }
}
