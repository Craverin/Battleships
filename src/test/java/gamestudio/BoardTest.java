package gamestudio;

import gamestudio.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest
{
    private Board board;
    private List<Ship> ships;

    @BeforeEach
    void setUp()
    {
         /*
                  A     B     C     D     E     F     G     H     I     J
               +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
             1 |     |     |     |     |     |     |     |     |     |     |
               +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
             2 |  ■  |  ■  |     |     |  ■  |  ■  |     |     |     |     |
               +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
             3 |     |     |     |     |     |     |     |     |     |     |
               +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
             4 |     |     |     |     |  ■  |  ■  |  ■  |  ■  |     |  ■  |
               +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
             5 |     |     |     |     |     |     |     |     |     |     |
               +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
             6 |     |     |  ■  |  ■  |     |     |  ■  |     |     |     |
               +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
             7 |  ■  |     |     |     |     |     |     |     |     |     |
               +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
             8 |     |     |     |  ■  |     |     |     |  ■  |  ■  |  ■  |
               +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
             9 |     |  ■  |     |  ■  |     |     |     |     |     |     |
               +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
            10 |     |     |     |  ■  |     |     |     |     |     |     |
               +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
        */

        CellState[][] cells = new CellState[][]
        {
            new CellState[]{CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.EMPTY, CellState.EMPTY, CellState.EMPTY},
            new CellState[]{CellState.OCCUPIED, CellState.OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.OCCUPIED, CellState.OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.EMPTY, CellState.EMPTY, CellState.EMPTY},
            new CellState[]{CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED},
            new CellState[]{CellState.EMPTY, CellState.EMPTY, CellState.EMPTY, CellState.INDIRECTLY_OCCUPIED, CellState.OCCUPIED, CellState.OCCUPIED, CellState.OCCUPIED, CellState.OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.OCCUPIED},
            new CellState[]{CellState.EMPTY, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED},
            new CellState[]{CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.OCCUPIED, CellState.OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.EMPTY, CellState.EMPTY},
            new CellState[]{CellState.OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED},
            new CellState[]{CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.EMPTY, CellState.INDIRECTLY_OCCUPIED, CellState.OCCUPIED, CellState.OCCUPIED, CellState.OCCUPIED},
            new CellState[]{CellState.INDIRECTLY_OCCUPIED, CellState.OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.EMPTY, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED},
            new CellState[]{CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.EMPTY, CellState.EMPTY, CellState.EMPTY, CellState.EMPTY, CellState.EMPTY},
        };

        ships = new ArrayList<>()
        {
            {
                add(new Ship(UUID.fromString("79cbb59d-bbea-45ca-8778-67ded08d7a32"), new Coordinate(3, 4), Orientation.HORIZONTAL, 4));
                add(new Ship(UUID.fromString("c84b1b89-e778-42d0-a81b-07e69f51d764"), new Coordinate(7, 7), Orientation.HORIZONTAL, 3));
                add(new Ship(UUID.fromString("f4ade990-c387-45ac-993b-2b523de42354"), new Coordinate(7, 3), Orientation.VERTICAL, 3));
                add(new Ship(UUID.fromString("45678861-d55d-4379-93da-e6f1bd18c2fb"), new Coordinate(1, 4), Orientation.HORIZONTAL, 2));
                add(new Ship(UUID.fromString("1cb87796-17fd-4d8a-8fc1-35505fb89f35"), new Coordinate(1, 0), Orientation.HORIZONTAL, 2));
                add(new Ship(UUID.fromString("9699b85e-33bb-4b59-84c6-593f7641e1fe"), new Coordinate(5, 2), Orientation.HORIZONTAL, 2));
                add(new Ship(UUID.fromString("f3d83b62-b550-4b2a-afbf-cea21330320a"), new Coordinate(5, 6), Orientation.HORIZONTAL, 1));
                add(new Ship(UUID.fromString("9be778ba-b76b-45c1-b772-46fd5c8de243"), new Coordinate(3, 9), Orientation.VERTICAL, 1));
                add(new Ship(UUID.fromString("801ca034-33d2-4896-9194-656beef0d7cd"), new Coordinate(6, 0), Orientation.VERTICAL, 1));
                add(new Ship(UUID.fromString("d379cbe1-004d-4c70-ac18-dc3d811c3e4c"), new Coordinate(8, 1), Orientation.VERTICAL, 1));
            }
        };

        board = new Board(cells, ships);
    }

	@Test
	public void canLand_validShips_returnsTrue()
    {
        Ship longHorizontalShip = new Ship(UUID.randomUUID(), new Coordinate(9, 5), Orientation.HORIZONTAL, 4);
        Ship longVerticalShip =  new Ship(UUID.randomUUID(), new Coordinate(7, 5), Orientation.VERTICAL, 3);

        Ship shortHorizontalShip = new Ship(UUID.randomUUID(), new Coordinate(1, 7), Orientation.HORIZONTAL, 2);
        Ship shortVerticalShip = new Ship(UUID.randomUUID(), new Coordinate(3, 0), Orientation.VERTICAL, 2);

        assertTrue(board.canLand(longHorizontalShip));
        assertTrue(board.canLand(longVerticalShip));
        assertTrue(board.canLand(shortHorizontalShip));
        assertTrue(board.canLand(shortVerticalShip));
	}

    @Test
    public void canLand_shipsOutOfBounds_returnsFalse()
    {
        Ship negativeCoordShip = new Ship(UUID.randomUUID(), new Coordinate(-1, 5), Orientation.HORIZONTAL, 4);
        Ship outOfBoundsShip = new Ship(UUID.randomUUID(), new Coordinate(10, 7), Orientation.VERTICAL, 2);
        Ship tooLongShip = new Ship(UUID.randomUUID(), new Coordinate(0, 7), Orientation.HORIZONTAL, 4);

        assertFalse(board.canLand(negativeCoordShip));
        assertFalse(board.canLand(outOfBoundsShip));
        assertFalse(board.canLand(tooLongShip));
    }

    @Test
    public void canLand_overlappingShips_returnsFalse()
    {
        Ship directlyOverlappingShip = new Ship(UUID.randomUUID(), new Coordinate(3, 2), Orientation.VERTICAL, 3);
        Ship indirectlyOverlappingShip = new Ship(UUID.randomUUID(), new Coordinate(6, 5), Orientation.VERTICAL, 4);

        assertFalse(board.canLand(directlyOverlappingShip));
        assertFalse(board.canLand(indirectlyOverlappingShip));
    }

    @Test
    public void moveShip_validCoordinates_returnsTrue()
    {
        Ship lengthFourHorizontal = ships.get(0);

        assertTrue(board.moveShip(lengthFourHorizontal, new Coordinate(9, 6), Orientation.HORIZONTAL));
    }

    @Test
    public void moveShip_validMoveWithOrientationChange_returnsTrue()
    {
        Ship lengthThreeVertical = ships.get(2);

        assertTrue(board.moveShip(lengthThreeVertical, new Coordinate(1, 7), Orientation.HORIZONTAL));
    }

    @Test
    public void moveShip_outOfBounds_returnsFalse()
    {
        Ship lengthThreeHorizontal = ships.get(1);

        assertFalse(board.moveShip(lengthThreeHorizontal, new Coordinate(3, -1), Orientation.HORIZONTAL));
        assertFalse(board.moveShip(lengthThreeHorizontal, new Coordinate(1, 10), Orientation.HORIZONTAL));
        assertFalse(board.moveShip(lengthThreeHorizontal, new Coordinate(9, 8), Orientation.HORIZONTAL));
    }

    @Test
    public void moveShip_directlyOverlapping_returnsFalse()
    {
        Ship lengthTwoHorizontal = ships.get(3);

        assertFalse(board.moveShip(lengthTwoHorizontal, new Coordinate(4, 3), Orientation.HORIZONTAL));
    }

    @Test
    public void moveShip_indirectlyOverlapping_returnsFalse()
    {
        Ship lengthOne = ships.get(7);

        assertFalse(board.moveShip(lengthOne, new Coordinate(4, 8), Orientation.VERTICAL));
    }

    @Test
    public void shoot_shipCell_marksCellAsHit()
    {
        board.shoot(new Coordinate(7, 3));

        assertEquals(CellState.HIT, board.getCells()[7][3]);
    }

    @Test
    public void shoot_allShipCells_marksCellsAsSunk()
    {
        board.shoot(new Coordinate(3, 4));
        board.shoot(new Coordinate(3, 5));
        board.shoot(new Coordinate(3, 6));
        board.shoot(new Coordinate(3, 7));

        CellState[][] afterShotBoard = board.getCells();
        assertEquals(CellState.SUNK, afterShotBoard[3][4]);
        assertEquals(CellState.SUNK, afterShotBoard[3][5]);
        assertEquals(CellState.SUNK, afterShotBoard[3][6]);
        assertEquals(CellState.SUNK, afterShotBoard[3][7]);
    }

    @Test
    public void shoot_emptyCell_marksCellAsMiss()
    {
        board.shoot(new Coordinate(2, 2));

        assertEquals(CellState.MISS, board.getCells()[2][2]);
    }

}
