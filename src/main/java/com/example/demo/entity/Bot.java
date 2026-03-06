package com.example.demo.entity;

import com.example.demo.dto.CellStateView;

import java.util.*;

public class Bot
{
    private Coordinate lastHitCell;
    private HitDirection hitDirection;
    private static final List<HitDirection> possibleDirections = new ArrayList<>();

    public Coordinate attack(CellStateView[][] cells)
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

                hitDirection = possibleDirections.get(new Random().nextInt(possibleDirections.size()));
                while (!isValidDirection(lastHitCell, hitDirection))
                {
                    possibleDirections.remove(hitDirection);
                    hitDirection = possibleDirections.get(new Random().nextInt(possibleDirections.size()));
                }

                System.out.println("\nPicked new hit direction: " + hitDirection.name());
                possibleDirections.remove(hitDirection);
            }

            Coordinate shift = hitDirection.coordinate;
            lastHitCell = new Coordinate(lastHitCell.row() + shift.row(), lastHitCell.col() + shift.col());
            return lastHitCell;
        }

        List<HitDirection> shifts = new ArrayList<>(){
            {
                add(HitDirection.UP);
                add(HitDirection.DOWN);
                add(HitDirection.RIGHT);
                add(HitDirection.LEFT);
            }
        };
        Collections.shuffle(shifts);

        for (HitDirection shift : shifts)
        {
            int row = cell.row() + shift.coordinate.row();
            int col = cell.col() + shift.coordinate.col();

            if (isValid(row, col) && cells[row][col].equals(CellStateView.UNKNOWN))
            {
                lastHitCell = new Coordinate(row, col);
                hitDirection = shift;

                possibleDirections.addAll(EnumSet.allOf(HitDirection.class));
                possibleDirections.remove(hitDirection);

                System.out.println("Picked (" + lastHitCell.row() +  ", " + lastHitCell.col() + ") as target cell. Direction " + hitDirection.name());

                return lastHitCell;
            }
        }


        return null;
    }

    private Coordinate findEmptyCell(CellStateView[][] cells)
    {
        // TODO needs to check if there aren't any SUNK cells nearby
        Random rnd = new Random();

        if (lastHitCell != null)
        {
            System.out.println("ADDDING ALL POSSIBLE DIRECTIONS");
            possibleDirections.removeAll(EnumSet.allOf(HitDirection.class));
            possibleDirections.addAll(EnumSet.allOf(HitDirection.class));
            lastHitCell = null;
        }

        int row, col;

        for (int i = 0; i < 500; i++)
        {
            row = rnd.nextInt(cells.length);
            col = rnd.nextInt(cells[0].length);

            if (cells[row][col].equals(CellStateView.UNKNOWN))
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

    private boolean isValidDirection(Coordinate cell, HitDirection hitDirection)
    {
        Coordinate shift = hitDirection.coordinate;
        return isValid(cell.row() + shift.row(), cell.col() + shift.col());
    }
}
