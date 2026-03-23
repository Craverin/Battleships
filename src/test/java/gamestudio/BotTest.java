package gamestudio;

import gamestudio.domain.*;
import gamestudio.dto.CellStateView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
         1 |  *  |     |  *  |  ■  |     |     |  *  |  *  |     |     |
           +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
         2 |     |     |     |  ■  |     |  *  |  ■  |  ■  |  ■  |     |
           +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
         3 |     |  ■  |     |     |     |     |     |     |     |     |
           +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
         4 |  *  |  ■  |  *  |     |     |     |     |     |     |     |
           +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
         5 |     |     |     |     |  *  |  ■  |     |     |     |     |
           +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
         6 |  ■  |  ■  |     |     |  *  |     |     |  *  |  ■  |  *  |
           +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
         7 |     |     |     |  *  |  ■  |  *  |     |     |  ■  |     |
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
            new CellStateView[]{CellStateView.MISS, CellStateView.UNKNOWN, CellStateView.MISS, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.MISS, CellStateView.MISS, CellStateView.UNKNOWN, CellStateView.UNKNOWN},
            new CellStateView[]{CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.MISS, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN},
            new CellStateView[]{CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN},
            new CellStateView[]{CellStateView.MISS, CellStateView.UNKNOWN, CellStateView.MISS, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN},
            new CellStateView[]{CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.MISS, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN},
            new CellStateView[]{CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.MISS, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.MISS, CellStateView.UNKNOWN, CellStateView.MISS},
            new CellStateView[]{CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.MISS, CellStateView.UNKNOWN, CellStateView.MISS, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN, CellStateView.UNKNOWN},
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
        Coordinate expectedHitCell = new Coordinate(6, 8);
        cells[5][8] = CellStateView.HIT;

        bot.getTargetCell(cells, firstHitCell); // returns (4, 8)
        cells[4][8] = CellStateView.MISS;

        Coordinate newHitCell = bot.getTargetCell(cells, firstHitCell);
        assertEquals(expectedHitCell, newHitCell);
    }

    @Test
    public void getTargetCell_previousShotWasHit_continuesInSameDirection()
    {
        Coordinate firstHitCell = new Coordinate(1, 6);
        Coordinate expectedHitCell = new Coordinate(1, 8);
        cells[1][6] = CellStateView.HIT;

        bot.getTargetCell(cells, firstHitCell); // returns (1, 7);
        cells[1][7] = CellStateView.HIT;

        Coordinate hitCell = bot.getTargetCell(cells, firstHitCell);
        assertEquals(expectedHitCell, hitCell);
    }

    @Test
    public void getTargetCell_cellInSameDirectionIsNotUnknownInThreeHitSeries_changesDirectionToOpposite()
    {
        Coordinate firstHitCell = new Coordinate(1, 7);
        Coordinate expectedHitCell = new Coordinate(1, 8);
        cells[1][7] = CellStateView.HIT;

        bot.getTargetCell(cells, firstHitCell); // returns (1, 6);
        cells[1][6] = CellStateView.HIT;

        Coordinate hitCell = bot.getTargetCell(cells, firstHitCell);
        assertEquals(expectedHitCell, hitCell);
    }

    @Test
    public void getTargetCell_previousShotWasMissInThreeHitSeries_changesDirectionToOpposite()
    {
        Coordinate firstHitCell = new Coordinate(6, 8);
        Coordinate expectedHitCell = new Coordinate(7, 8);
        cells[6][8] = CellStateView.HIT;

        bot.getTargetCell(cells, firstHitCell); // returns (5, 8);
        cells[5][8] = CellStateView.HIT;

        bot.getTargetCell(cells, firstHitCell); // returns (4, 8);
        cells[4][8] = CellStateView.MISS;

        Coordinate hitCell = bot.getTargetCell(cells, firstHitCell);
        assertEquals(expectedHitCell, hitCell);
    }
}
