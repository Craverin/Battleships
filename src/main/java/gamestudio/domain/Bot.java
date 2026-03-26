package gamestudio.domain;

import gamestudio.dto.CellStateView;

import java.util.*;

public class Bot
{
    private Coordinate lastHitCell, firstHitCell;
    private HitDirection hitDirection;
    private EnumSet<HitDirection> possibleDirections = EnumSet.allOf(HitDirection.class);
    private final Random rnd;

    public Bot()
    {
        this.rnd = new Random();
    }

    public Coordinate getTargetCell(CellStateView[][] cells, Coordinate firstHitCell)
    {
        if (firstHitCell != null) return getPossibleCell(cells, firstHitCell);
        else return findEmptyCell(cells);
    }

    private Coordinate getPossibleCell(CellStateView[][] cells, Coordinate cell)
    {
        // Continuing to target a ship after the first hit
        if (lastHitCell != null)
        {
            if (cells[lastHitCell.row()][lastHitCell.col()].equals(CellStateView.MISS))
            {
                // If previous shot was a miss, go back to the first hit cell, pick another available direction
                // and remove it from the set of possible directions to avoid picking it for the 2-nd time
                lastHitCell = firstHitCell;
                hitDirection = pickHitDirection(cells);

                possibleDirections.remove(hitDirection);
            }

            // If previous shot was a hit, the ship orientation is now known (due to the rules of ships' placement)
            // Remove all directions except the current and the opposite one
            else
            {
                possibleDirections.removeIf(dir -> dir != hitDirection &&
                        dir != hitDirection.getOpposite());
            }

            Coordinate shift = hitDirection.coordinate;

            // If the next cell in the current direction is not valid,
            // go back to the first hit cell and try another direction.
            if (!isHitCellValid(cells, lastHitCell.row() + shift.row(), lastHitCell.col() + shift.col()))
            {
                lastHitCell = firstHitCell;
                hitDirection = pickHitDirection(cells);
                shift = hitDirection.coordinate;
            }

            lastHitCell = new Coordinate(lastHitCell.row() + shift.row(), lastHitCell.col() + shift.col());
            return lastHitCell;
        }

        // First shot after the initial hit. Setting firstHitCell to initial hit cell
        firstHitCell = new Coordinate(cell.row(), cell.col());

        for (HitDirection shift : getShuffledDirections())
        {
            int row = cell.row() + shift.coordinate.row();
            int col = cell.col() + shift.coordinate.col();

            // Pick the first valid unknown cell
            // Store it as the lastHitCell and save its direction
            if (isHitCellValid(cells, row, col) && cells[row][col].equals(CellStateView.UNKNOWN))
            {
                lastHitCell = new Coordinate(row, col);
                hitDirection = shift;

                possibleDirections.remove(hitDirection);

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
            // Reset all related variables as the previous hit series was finished
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
        // Target cell is valid only if it is inside the board,
        // still unknown and not adjacent to any sunk ship cell.

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
        for (HitDirection direction : getShuffledDirections())
        {
            Coordinate hitCell = Coordinate.addCoordinates(firstHitCell, direction.coordinate);

            if (isHitCellValid(cells, hitCell.row(), hitCell.col()))
                return direction;

            possibleDirections.remove(hitDirection);
        }

        throw new IllegalStateException("No valid hit direction found");
    }

    private List<HitDirection> getShuffledDirections()
    {
        List<HitDirection> shuffledDirections = new ArrayList<>(possibleDirections);
        Collections.shuffle(shuffledDirections, rnd);
        return shuffledDirections;
    }
}
