package gamestudio.domain;

import gamestudio.dto.CellStateView;

import java.util.*;

public class Bot
{
    private Coordinate lastHitCell, firstHitCell;
    private HitDirection hitDirection;
    private EnumSet<HitDirection> possibleDirections = EnumSet.allOf(HitDirection.class);

    public Coordinate getTargetCell(CellStateView[][] cells, Coordinate firstHitCell)
    {
        if (firstHitCell != null) return getPossibleCell(cells, firstHitCell);
        else return findEmptyCell(cells);
    }

    private Coordinate getPossibleCell(CellStateView[][] cells, Coordinate cell)
    {
        if (lastHitCell != null)
        {
            if (cells[lastHitCell.row()][lastHitCell.col()].equals(CellStateView.MISS))
            {
              //  System.out.println("MISS: (" + lastHitCell.row() + ", " + lastHitCell.col() + ")");
                lastHitCell = firstHitCell;
              //  System.out.println("First hit cell: (" + lastHitCell.row() + ", " + lastHitCell.col() + ")");

               // System.out.println("CURRENT POSSIBLE DIRECTIONS:");
               // for (HitDirection direction : possibleDirections) System.out.println("d: " + direction.name());

                hitDirection = pickHitDirection(cells);

              //  System.out.println("\nPicked new hit direction: " + hitDirection.name());
                possibleDirections.remove(hitDirection);
            }

            else
                possibleDirections.removeIf(dir -> dir != hitDirection && dir != hitDirection.getOpposite());

            Coordinate shift = hitDirection.coordinate;
            if (!isHitCellValid(cells, lastHitCell.row() + shift.row(), lastHitCell.col() + shift.col()))
            {
                lastHitCell = firstHitCell;
                hitDirection = pickHitDirection(cells);
                shift = hitDirection.coordinate;
            }

            lastHitCell = new Coordinate(lastHitCell.row() + shift.row(), lastHitCell.col() + shift.col());
            return lastHitCell;
        }

        firstHitCell = new Coordinate(cell.row(), cell.col());

        for (HitDirection shift : possibleDirections)
        {
            int row = cell.row() + shift.coordinate.row();
            int col = cell.col() + shift.coordinate.col();

            if (isHitCellValid(cells, row, col) && cells[row][col].equals(CellStateView.UNKNOWN))
            {
                lastHitCell = new Coordinate(row, col);
                hitDirection = shift;

             //   for (HitDirection h : possibleDirections) System.out.println(h.name());
                possibleDirections.remove(hitDirection);

                //System.out.println("Picked (" + lastHitCell.row() +  ", " + lastHitCell.col() + ") as target cell. Direction " + hitDirection.name());

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
            firstHitCell = null;
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

    private boolean isHitCellValid(CellStateView[][] cells, int row, int col)
    {
        if (!Coordinate.isValid(row, col) || !cells[row][col].equals(CellStateView.UNKNOWN)) return false;

        int startRow = row > 0 ? row - 1 : row, endRow = row < Board.SIZE - 1 ? row + 1 : row;
        int startCol = col > 0 ? col - 1 : col, endCol = col < Board.SIZE - 1 ? col + 1 : col;

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
     //   System.out.println("TRYING TO FIND HIT DIRECTION");
        var hitDirs = possibleDirections.toArray(new HitDirection[0]);
        hitDirection = hitDirs[new Random().nextInt(hitDirs.length)];
      //  System.out.println("LENGTH OF POSSIBLE DIRECTIONS ARRAY: " + hitDirs.length);
      //  System.out.println("PICKING " + hitDirection.name());

        Coordinate hitCell = Coordinate.addCoordinates(lastHitCell, hitDirection.coordinate);

        while (!isHitCellValid(cells, hitCell.row(), hitCell.col()))
        {
            // System.out.print(hitDirection.name() + " wasn't valid. Trying ");
            possibleDirections.remove(hitDirection);

            hitDirs = possibleDirections.toArray(new HitDirection[0]);
            hitDirection = hitDirs[new Random().nextInt(hitDirs.length)];
        //    System.out.println(hitDirection.name());
            hitCell = Coordinate.addCoordinates(firstHitCell, hitDirection.coordinate);
        }

        return hitDirection;
    }
}
