package gamestudio.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Ship
{
    private final UUID id;
    private Coordinate start;
    private Orientation orientation;
    private int length;
    private boolean[] hit;

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
            {
                cells.add(new Coordinate(start.row(), start.col() + i));
            }
            else cells.add(new Coordinate(start.row() + i, start.col()));
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

//    public boolean bordersWith(Coordinate start, Orientation orientation, int length)
//    {
//        int dRow, dCol;
//
//        if (orientation == Orientation.HORIZONTAL)
//        {
//            dRow = start.getRow() - this.start.getRow();
//            for (int i = 0; i < length; i++)
//            {
//                dCol = start.getCol() + i - this.start.getCol();
//                if (Math.abs(dRow) <= 1 && dCol >= -1 && dCol <= this.length) return true;
//            }
//        }
//
//        else
//        {
//            dCol = start.getCol() - this.start.getCol();
//            for (int i = 0; i < length; i++)
//            {
//                dRow = start.getRow() + i - this.start.getRow();
//                if (Math.abs(dCol) <= 1 && dRow >= -1 && dRow <= this.length) return true;
//            }
//        }
//
//        return false;
//    }

    public List<Coordinate> getBorderCells()
    {
        List<Coordinate> borderCells = new ArrayList<>();
        Coordinate startCell;
        Coordinate endCell;

        if (orientation.equals(Orientation.HORIZONTAL))
        {
            int endCol = start.col() + length - 1;

            startCell = new Coordinate(
                    start.row() > 0 ? start.row() - 1 : 0,
                    start.col() > 0 ? start.col() - 1 : 0
            );

            endCell = new Coordinate(
                    start.row() < 9 ? start.row() + 1 : 9,
                    endCol < 9 ? endCol + 1 : endCol
            );
        }

        else
        {
            int endRow = start.row() + length - 1;

            startCell = new Coordinate(
                    start.row() > 0 ? start.row() - 1 : 0,
                    start.col() > 0 ? start.col() - 1 : 0
            );

            endCell = new Coordinate(
                    endRow < 9 ? endRow + 1 : endRow,
                    start.col() < 9 ? start.col() + 1 : 9
            );
        }

        for (int i = startCell.row(); i <= endCell.row(); i++)
        {
            for (int j = startCell.col(); j <= endCell.col(); j++) borderCells.add(new Coordinate(i, j));
        }

        return borderCells;
    }

    private boolean inBounds(int row, int col)
    {
        return row >= 0 && row < 10 && col >= 0 && col < 10;
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
