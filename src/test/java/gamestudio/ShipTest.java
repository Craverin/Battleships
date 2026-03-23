package gamestudio;

import gamestudio.domain.Coordinate;
import gamestudio.domain.Orientation;
import gamestudio.domain.Ship;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ShipTest
{
    private Ship horizontalShip;
    private Ship verticalShip;
    private List<Coordinate> horizontalShipCells;
    private List<Coordinate> verticalShipCells;

    @BeforeEach
    void setUp()
    {
        horizontalShip = new Ship(UUID.randomUUID(), new Coordinate(4, 3), Orientation.HORIZONTAL, 4);
        verticalShip = new Ship(UUID.randomUUID(), new Coordinate(2, 8), Orientation.VERTICAL, 4);

        horizontalShipCells = new ArrayList<>()
        {
            {
                add(new Coordinate(4, 3));
                add(new Coordinate(4, 4));
                add(new Coordinate(4, 5));
                add(new Coordinate(4, 6));
            }
        };

        verticalShipCells = new ArrayList<>()
        {
            {
                add(new Coordinate(2, 8));
                add(new Coordinate(3, 8));
                add(new Coordinate(4, 8));
                add(new Coordinate(5, 8));
            }
        };
    }

    @Test
    public void getCells_ships_returnsOccupiedCells()
    {
        assertEquals(horizontalShipCells, horizontalShip.getCells());
        assertEquals(verticalShipCells, verticalShip.getCells());
    }

    @Test
    public void occupies_shipsOccupyCells_returnsTrue()
    {
        for (Coordinate c : horizontalShipCells)
            assertTrue(horizontalShip.occupies(c));

        for (Coordinate c : verticalShipCells)
            assertTrue(verticalShip.occupies(c));
    }

    @Test
    public void occupies_shipsDoNotOccupyCells_returnsFalse()
    {
        assertFalse(horizontalShip.occupies(new Coordinate(4, 2)));
        assertFalse(horizontalShip.occupies(new Coordinate(4, 7)));
        assertFalse(horizontalShip.occupies(new Coordinate(3, 4)));
        assertFalse(horizontalShip.occupies(new Coordinate(3, 6)));

        assertFalse(verticalShip.occupies(new Coordinate(2, 9)));
        assertFalse(verticalShip.occupies(new Coordinate(6, 8)));
        assertFalse(verticalShip.occupies(new Coordinate(3, 7)));
        assertFalse(verticalShip.occupies(new Coordinate(3, 9)));
    }

    @Test
    public void getBorderCells_ships_returnsBorderCells()
    {
        Ship borderLineShip = new Ship(UUID.randomUUID(), new Coordinate(9, 8), Orientation.HORIZONTAL, 2);

        List<Coordinate> horizontalShipBorderCells = new ArrayList<>()
        {
            {
                add(new Coordinate(3, 2));
                add(new Coordinate(3, 3));
                add(new Coordinate(3, 4));
                add(new Coordinate(3, 5));
                add(new Coordinate(3, 6));
                add(new Coordinate(3, 7));
                add(new Coordinate(4, 2));
                add(new Coordinate(4, 7));
                add(new Coordinate(5, 2));
                add(new Coordinate(5, 3));
                add(new Coordinate(5, 4));
                add(new Coordinate(5, 5));
                add(new Coordinate(5, 6));
                add(new Coordinate(5, 7));
            }
        };

        List<Coordinate> verticalShipBorderCells = new ArrayList<>()
        {
            {
                add(new Coordinate(1, 7));
                add(new Coordinate(1, 8));
                add(new Coordinate(1, 9));
                add(new Coordinate(2, 7));
                add(new Coordinate(2, 9));
                add(new Coordinate(3, 7));
                add(new Coordinate(3, 9));
                add(new Coordinate(4, 7));
                add(new Coordinate(4, 9));
                add(new Coordinate(5, 7));
                add(new Coordinate(5, 9));
                add(new Coordinate(6, 7));
                add(new Coordinate(6, 8));
                add(new Coordinate(6, 9));
            }
        };

        List<Coordinate> borderLineShipBorderCells = new ArrayList<>()
        {
            {
                add(new Coordinate(8, 7));
                add(new Coordinate(8, 8));
                add(new Coordinate(8, 9));
                add(new Coordinate(9, 7));
            }
        };

        assertEquals(horizontalShipBorderCells, horizontalShip.getBorderCells());
        assertEquals(verticalShipBorderCells, verticalShip.getBorderCells());
        assertEquals(borderLineShipBorderCells, borderLineShip.getBorderCells());
    }

    @Test
    public void hit_shipsOccupyCell_returnsTrue()
    {
        for (Coordinate c : horizontalShipCells)
            assertTrue(horizontalShip.hit(c));

        for (Coordinate c : verticalShipCells)
            assertTrue(verticalShip.hit(c));
    }

    @Test
    public void hit_shipsDoNotOccupyCell_returnFalse()
    {
        assertFalse(horizontalShip.hit(new Coordinate(3, 2)));
        assertFalse(horizontalShip.hit(new Coordinate(3, 7)));

        assertFalse(verticalShip.hit(new Coordinate(1, 7)));
        assertFalse(verticalShip.hit(new Coordinate(6, 9)));
    }

    @Test
    public void isSunk_shipsSank_returnsTrue()
    {
        assertFalse(horizontalShip.isSunk());
        assertFalse(verticalShip.isSunk());

        for (Coordinate c : horizontalShipCells)
            horizontalShip.hit(c);
        for (Coordinate c : verticalShipCells)
            verticalShip.hit(c);

        assertTrue(horizontalShip.isSunk());
        assertTrue(verticalShip.isSunk());
    }

    @Test
    public void isSunk_shipsDidNotSank_returnsFalse()
    {
        assertFalse(horizontalShip.isSunk());
        assertFalse(verticalShip.isSunk());

        for (int i = 0; i < horizontalShipCells.size() - 1; i++)
            horizontalShip.hit(horizontalShipCells.get(i));

        for (int j = 0; j < verticalShipCells.size() - 1; j++)
            verticalShip.hit(verticalShipCells.get(j));

        assertFalse(horizontalShip.isSunk());
        assertFalse(verticalShip.isSunk());
    }
}
