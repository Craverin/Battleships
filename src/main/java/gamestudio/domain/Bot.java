package gamestudio.domain;

import gamestudio.dto.CellStateView;

import java.util.*;

public class Bot
{
    private Coordinate lastHitCell;
    private HitDirection hitDirection;
    private EnumSet<HitDirection> possibleDirections = EnumSet.allOf(HitDirection.class);

    public Coordinate getTargetCell(CellStateView[][] cells)
    {
        for (int i = 0; i < cells.length; i++)
        {
            for (int j = 0; j < cells[i].length; j++)
            {
                if (cells[i][j].equals(CellStateView.HIT))
                {
                    System.out.println("Found HIT cell (" + i + ", " + j + ")");
                    Coordinate cell = getPossibleCell(cells, new Coordinate(i, j));
                    if (cell != null) return cell;
                }
            }
        }

        return findEmptyCell(cells);
    }

    private Coordinate getPossibleCell(CellStateView[][] cells, Coordinate cell)
    {
        if (lastHitCell != null)
        {
            if (cells[lastHitCell.row()][lastHitCell.col()].equals(CellStateView.MISS))
            {
                System.out.println("MISS: (" + lastHitCell.row() + ", " + lastHitCell.col() + ")");
                lastHitCell = findFirstHitCell(cells, cell, hitDirection);
                System.out.println("First hit cell: (" + lastHitCell.row() + ", " + lastHitCell.col() + ")");

                System.out.println("CURRENT POSSIBLE DIRECTIONS:");
                for (HitDirection direction : possibleDirections) System.out.println("d: " + direction.name());

                hitDirection = pickHitDirection(cells);

                System.out.println("\nPicked new hit direction: " + hitDirection.name());
                possibleDirections.remove(hitDirection);
            }

            else
                possibleDirections.removeIf(dir -> dir != hitDirection && dir != hitDirection.getOpposite());

            Coordinate shift = hitDirection.coordinate;
            if (!isHitCellValid(cells, lastHitCell.row() + shift.row(), lastHitCell.col() + shift.col()))
                hitDirection = pickHitDirection(cells);

            lastHitCell = new Coordinate(lastHitCell.row() + shift.row(), lastHitCell.col() + shift.col());
            return lastHitCell;
        }

        for (HitDirection shift : possibleDirections)
        {
            int row = cell.row() + shift.coordinate.row();
            int col = cell.col() + shift.coordinate.col();

            if (isValid(row, col) && cells[row][col].equals(CellStateView.UNKNOWN))
            {
                lastHitCell = new Coordinate(row, col);
                hitDirection = shift;

                for (HitDirection h : possibleDirections) System.out.println(h.name());
                possibleDirections.remove(hitDirection);

                System.out.println("Picked (" + lastHitCell.row() +  ", " + lastHitCell.col() + ") as target cell. Direction " + hitDirection.name());

                return lastHitCell;
            }
        }

        return null;
    }

    private Coordinate findEmptyCell(CellStateView[][] cells)
    {
        Random rnd = new Random();

        if (lastHitCell != null)
        {
            possibleDirections = EnumSet.allOf(HitDirection.class);
            lastHitCell = null;
        }

        int row, col;

        for (int i = 0; i < 500; i++)
        {
            row = rnd.nextInt(cells.length);
            col = rnd.nextInt(cells[0].length);

            if (isHitCellValid(cells, row, col))
               return new Coordinate(row, col);
        }

       return null;
    }

    private Coordinate findFirstHitCell(CellStateView[][] cells, Coordinate cell, HitDirection hitDirection)
    {
        Coordinate shift = hitDirection.coordinate;
        int row = cell.row(), col = cell.col();

        while (isValid(row - shift.row(), col - shift.col()) && cells[row - shift.row()][col - shift.col()].equals(CellStateView.HIT))
        {
            row -= shift.row();
            col -= shift.col();
        }

        return new Coordinate(row, col);
    }

    private boolean isValid(int row, int col)
    {
        return row >= 0 && row <= 9 && col >= 0 && col <= 9;
    }

    private boolean isHitCellValid(CellStateView[][] cells, int row, int col)
    {
        if (!isValid(row, col) || !cells[row][col].equals(CellStateView.UNKNOWN)) return false;

        int startRow = row > 0 ? row - 1 : row, endRow = row < 9 ? row + 1 : row;
        int startCol = col > 0 ? col - 1 : col, endCol = col < 9 ? col + 1 : col;

        for (int i = startRow; i <= endRow; i++)
        {
            for (int j = startCol; j <= endCol; j++)
            {
                if (cells[i][j].equals(CellStateView.SUNK)) return false;
            }
        }

        return true;
    }

    private HitDirection pickHitDirection(CellStateView[][] cells)
    {
        var hitDirs = possibleDirections.toArray(new HitDirection[0]);
        hitDirection = hitDirs[new Random().nextInt(hitDirs.length)];

        Coordinate firstHitCell, hitCell = Coordinate.addCoordinates(lastHitCell, hitDirection.coordinate);

        while (!isHitCellValid(cells, hitCell.row(), hitCell.col()))
        {
            possibleDirections.remove(hitDirection);
            firstHitCell = findFirstHitCell(cells, lastHitCell, hitDirection);

            hitDirection = hitDirs[new Random().nextInt(hitDirs.length)];
            hitCell = Coordinate.addCoordinates(firstHitCell, hitDirection.coordinate);
        }

        return hitDirection;
    }
}
