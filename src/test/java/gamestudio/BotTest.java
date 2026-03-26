package gamestudio;

import gamestudio.domain.*;
import gamestudio.dto.CellStateView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BotTest
{
    private CellStateView[][] cells;
    private Bot bot;

    @BeforeEach
    void setUp()
    {
         /*
              A     B     C     D     E     F     G     H     I     J
           +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
         1 |  *  |     |  *  |  ■  |     |     |     |  *  |     |     |
           +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
         2 |     |     |     |  ■  |     |  *  |  ■  |  ■  |  ■  |  *  |
           +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
         3 |     |  ■  |     |     |     |     |     |  *  |     |     |
           +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
         4 |  *  |  ■  |  *  |     |     |     |     |     |     |     |
           +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
         5 |     |     |     |     |  *  |  ■  |     |     |     |     |
           +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
         6 |  ■  |  ■  |     |     |  *  |     |     |     |  ■  |     |
           +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
         7 |     |     |     |  *  |  ■  |  *  |     |  *  |  ■  |  *  |
           +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
         8 |  ■  |     |     |     |  ■  |     |     |     |  ■  |     |
           +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
         9 |     |     |     |     |  ■  |     |     |     |  ■  |     |
           +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
        10 |  ■  |     |     |     |     |     |  ■  |     |     |     |
           +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
        */

        cells = new CellStateView[][]
        {
            new CellStateView[]{CellStateView.MISS, CellStateView.UNKNOWN, CellStateView.MISS, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.MISS, CellStateView.UNKNOWN, CellStateView.UNKNOWN},
            new CellStateView[]{CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.MISS, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.MISS},
            new CellStateView[]{CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.MISS, CellStateView.UNKNOWN, CellStateView.UNKNOWN},
            new CellStateView[]{CellStateView.MISS, CellStateView.UNKNOWN, CellStateView.MISS, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN},
            new CellStateView[]{CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.MISS, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN},
            new CellStateView[]{CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.MISS, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN},
            new CellStateView[]{CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.MISS, CellStateView.UNKNOWN, CellStateView.MISS, CellStateView.UNKNOWN, CellStateView.MISS, CellStateView.UNKNOWN, CellStateView.MISS},
            new CellStateView[]{CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN},
            new CellStateView[]{CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN},
            new CellStateView[]{CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN},
        };

        bot = new Bot();
    }

    @Test
    public void getTargetCell_noOpenHitSeries_returnsUnknownCell()
    {
        Coordinate coordinate = bot.getTargetCell(cells, null);

        assertEquals(CellStateView.UNKNOWN, cells[coordinate.row()][coordinate.col()]);
    }

    @Test
    public void getTargetCell_firstHitProvidedAndOnlyOneDirectionIsValid_returnsThatCell()
    {
        Coordinate firstHitCell = new Coordinate(6, 4);
        Coordinate expectedHitCell = new Coordinate(7, 4);
        cells[6][4] = CellStateView.HIT;

        Coordinate hitCell = bot.getTargetCell(cells, firstHitCell);
        assertEquals(expectedHitCell, hitCell);
    }

    @Test
    public void getTargetCell_previousShotWasMiss_changesDirection()
    {
        Coordinate firstHitCell = new Coordinate(5, 8);
        cells[5][8] = CellStateView.HIT;

        List<Coordinate> expectedHitCells = new ArrayList<>()
        {
            {
                add(new Coordinate(4, 8));
                add(new Coordinate(5, 7));
                add(new Coordinate(5, 9));
                add(new Coordinate(6, 8));
            }
        };

        Coordinate secondHitCell = bot.getTargetCell(cells, firstHitCell);

        assertTrue(expectedHitCells.contains(secondHitCell));
        expectedHitCells.remove(secondHitCell);

        cells[secondHitCell.row()][secondHitCell.col()] = CellStateView.MISS;

        Coordinate newHitCell = bot.getTargetCell(cells, firstHitCell);
        assertTrue(expectedHitCells.contains(newHitCell));
    }

    @Test
    public void getTargetCell_previousShotWasHit_continuesInSameDirection()
    {
        Coordinate firstHitCell = new Coordinate(1, 6);
        cells[1][6] = CellStateView.HIT;

        Coordinate secondHitCell = bot.getTargetCell(cells, firstHitCell);
        cells[secondHitCell.row()][secondHitCell.col()] = CellStateView.HIT;

        Coordinate direction;
        if (firstHitCell.row() == secondHitCell.row())
            direction = new Coordinate(0, secondHitCell.col() - 6);
        else
            direction = new Coordinate(secondHitCell.row() - 1, 0);

        Coordinate expectedHitCell = Coordinate.addCoordinates(secondHitCell, direction);
        Coordinate hitCell = bot.getTargetCell(cells, firstHitCell);
        assertEquals(expectedHitCell, hitCell);
    }

    @Test
    public void getTargetCell_cellInSameDirectionIsNotUnknownInThreeHitSeries_changesDirectionToOpposite()
    {
        Coordinate firstHitCell = new Coordinate(1, 7);
        cells[1][7] = CellStateView.HIT;

        List<Coordinate> expectedHitCells = new ArrayList<>()
        {
            {
                add(new Coordinate(1, 6));
                add(new Coordinate(1, 8));
            }
        };

        Coordinate secondHitCell = bot.getTargetCell(cells, firstHitCell);

        assertTrue(expectedHitCells.contains(secondHitCell));
        expectedHitCells.remove(secondHitCell);

        cells[secondHitCell.row()][secondHitCell.col()] = CellStateView.HIT;

        Coordinate hitCell = bot.getTargetCell(cells, firstHitCell);
        assertEquals(expectedHitCells.get(0), hitCell);
    }

    @Test
    public void getTargetCell_previousShotWasMissInThreeHitSeries_changesDirectionToOpposite()
    {
        Coordinate firstHitCell = new Coordinate(6, 8);
        cells[6][8] = CellStateView.HIT;

        Coordinate secondHitCell = bot.getTargetCell(cells, firstHitCell);
        cells[secondHitCell.row()][secondHitCell.col()] = CellStateView.HIT;

        Coordinate oppositeDirection;
        if (secondHitCell.row() > firstHitCell.row())
            oppositeDirection = new Coordinate( -1, 0);
        else
            oppositeDirection = new Coordinate(1, 0);

        Coordinate missCell = bot.getTargetCell(cells, firstHitCell);
        cells[missCell.row()][missCell.col()] = CellStateView.MISS;

        Coordinate expectedHitCell = Coordinate.addCoordinates(firstHitCell, oppositeDirection);
        Coordinate hitCell = bot.getTargetCell(cells, firstHitCell);

        assertEquals(expectedHitCell, hitCell);
    }
}
