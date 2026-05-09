package gamestudio;

import gamestudio.server.domain.*;
import gamestudio.server.dto.*;
import gamestudio.server.service.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest
{
    private final GameService gameService = new GameService();
    private CreateGameResponse randomGameResponse;
    private CreateGameResponse fixedGameResponse;
    private List<ShipResponse> shipResponses;
    private CellState[][] hostCells;
    private CellState[][] opponentCells;
    private List<Ship> opponentShips;

    @BeforeEach
    void setUp()
    {
         /*                           HOST BOARD
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

        hostCells = new CellState[][]
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

        List<Ship> ships = new ArrayList<>() {
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

        shipResponses = new ArrayList<>()
        {
            {
                add(new ShipResponse(UUID.fromString("79cbb59d-bbea-45ca-8778-67ded08d7a32"), 3, 4, Orientation.HORIZONTAL, 4));
                add(new ShipResponse(UUID.fromString("c84b1b89-e778-42d0-a81b-07e69f51d764"), 7, 7, Orientation.HORIZONTAL, 3));
                add(new ShipResponse(UUID.fromString("f4ade990-c387-45ac-993b-2b523de42354"), 7, 3, Orientation.VERTICAL, 3));
                add(new ShipResponse(UUID.fromString("45678861-d55d-4379-93da-e6f1bd18c2fb"), 1, 4, Orientation.HORIZONTAL, 2));
                add(new ShipResponse(UUID.fromString("1cb87796-17fd-4d8a-8fc1-35505fb89f35"), 1, 0, Orientation.HORIZONTAL, 2));
                add(new ShipResponse(UUID.fromString("9699b85e-33bb-4b59-84c6-593f7641e1fe"), 5, 2, Orientation.HORIZONTAL, 2));
                add(new ShipResponse(UUID.fromString("f3d83b62-b550-4b2a-afbf-cea21330320a"), 5, 6, Orientation.HORIZONTAL, 1));
                add(new ShipResponse(UUID.fromString("9be778ba-b76b-45c1-b772-46fd5c8de243"), 3, 9, Orientation.VERTICAL, 1));
                add(new ShipResponse(UUID.fromString("801ca034-33d2-4896-9194-656beef0d7cd"), 6, 0, Orientation.VERTICAL, 1));
                add(new ShipResponse(UUID.fromString("d379cbe1-004d-4c70-ac18-dc3d811c3e4c"), 8, 1, Orientation.VERTICAL, 1));
            }
        };

        /*                           OPPONENT BOARD
                  A     B     C     D     E     F     G     H     I     J
               +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
             1 |     |     |     |  ■  |  ■  |  ■  |     |  ■  |  ■  |     |
               +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
             2 |  ■  |     |     |     |     |     |     |     |     |     |
               +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
             3 |     |     |  ■  |     |     |     |  ■  |     |     |     |
               +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
             4 |     |     |     |     |     |     |     |     |  ■  |     |
               +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
             5 |     |  ■  |     |     |     |     |     |     |  ■  |     |
               +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
             6 |     |  ■  |     |     |     |  ■  |     |     |  ■  |     |
               +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
             7 |     |     |     |     |     |     |     |     |  ■  |     |
               +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
             8 |     |     |     |     |     |     |     |     |     |     |
               +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
             9 |     |     |     |     |     |     |     |  ■  |  ■  |     |
               +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
            10 |     |     |     |  ■  |  ■  |  ■  |     |     |     |     |
               +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
        */

        opponentCells = new CellState[][]
        {
                new CellState[]{CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.OCCUPIED, CellState.OCCUPIED, CellState.OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.OCCUPIED, CellState.OCCUPIED, CellState.INDIRECTLY_OCCUPIED},
                new CellState[]{CellState.OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED},
                new CellState[]{CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.EMPTY, CellState.INDIRECTLY_OCCUPIED, CellState.OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED},
                new CellState[]{CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.EMPTY, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.OCCUPIED, CellState.INDIRECTLY_OCCUPIED},
                new CellState[]{CellState.INDIRECTLY_OCCUPIED, CellState.OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.EMPTY, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.OCCUPIED, CellState.INDIRECTLY_OCCUPIED},
                new CellState[]{CellState.INDIRECTLY_OCCUPIED, CellState.OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.EMPTY, CellState.INDIRECTLY_OCCUPIED, CellState.OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.OCCUPIED, CellState.INDIRECTLY_OCCUPIED},
                new CellState[]{CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.EMPTY, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.OCCUPIED, CellState.INDIRECTLY_OCCUPIED},
                new CellState[]{CellState.EMPTY, CellState.EMPTY, CellState.EMPTY, CellState.EMPTY, CellState.EMPTY, CellState.EMPTY, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED},
                new CellState[]{CellState.EMPTY, CellState.EMPTY, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.OCCUPIED, CellState.OCCUPIED, CellState.INDIRECTLY_OCCUPIED},
                new CellState[]{CellState.EMPTY, CellState.EMPTY, CellState.INDIRECTLY_OCCUPIED, CellState.OCCUPIED, CellState.OCCUPIED, CellState.OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED, CellState.INDIRECTLY_OCCUPIED}
        };

        opponentShips = new ArrayList<>()
        {
            {
                add(new Ship(UUID.fromString("50904f66-409b-4931-8250-11c0322d0785"), new Coordinate(3, 8), Orientation.VERTICAL, 4));
                add(new Ship(UUID.fromString("9a843e66-89a3-40d8-a9ee-01af4e8cfd3b"), new Coordinate(9, 3), Orientation.HORIZONTAL, 3));
                add(new Ship(UUID.fromString("ed75869b-69fd-469d-9aa2-40875f07d907"), new Coordinate(0, 3), Orientation.HORIZONTAL, 3));
                add(new Ship(UUID.fromString("02539df1-ff8b-4f7c-9ff7-14663b4c668f"), new Coordinate(0, 7), Orientation.HORIZONTAL, 2));
                add(new Ship(UUID.fromString("8922b3f7-7481-4105-824c-3928fd1243b3"), new Coordinate(4, 1), Orientation.VERTICAL, 2));
                add(new Ship(UUID.fromString("530616ca-f592-40eb-b319-8ba5ded67119"), new Coordinate(8, 7), Orientation.HORIZONTAL, 2));
                add(new Ship(UUID.fromString("a50bb1dc-7254-4374-a96d-12a176b2a312"), new Coordinate(5, 5), Orientation.VERTICAL, 1));
                add(new Ship(UUID.fromString("1de9fe66-732e-4d17-b501-e5e0e097a0a6"), new Coordinate(2, 2), Orientation.HORIZONTAL, 1));
                add(new Ship(UUID.fromString("195dad6d-a84e-41c9-9c4c-3f27968e5c12"), new Coordinate(1, 0), Orientation.HORIZONTAL, 1));
                add(new Ship(UUID.fromString("e995573f-ee2c-48be-a5d0-753d1557c033"), new Coordinate(2, 6), Orientation.VERTICAL, 1));
            }
        };

        randomGameResponse = gameService.createGame();
        fixedGameResponse = gameService.createGame(hostCells, ships);
    }

    @Test
    public void createGame_creatingGame_returnsGameIdAndHostToken()
    {
        UUID gameId = randomGameResponse.gameId(), hostToken = randomGameResponse.hostToken();

        assertNotNull(gameId);
        assertNotNull(hostToken);

        assertEquals(gameId, gameService.getGame(gameId).getGameId());
        assertEquals(hostToken, gameService.getGame(gameId).getHostToken());
    }

    @Test
    public void joinGame_opponentJoinsGame_returnsOpponentToken()
    {
        UUID opponentToken = gameService.joinGame(randomGameResponse.gameId());

        assertNotNull(opponentToken);
        assertNotNull(gameService.getGame(randomGameResponse.gameId()).getOpponentToken());
        assertEquals(opponentToken, gameService.getGame(randomGameResponse.gameId()).getOpponentToken());
    }

    @Test
    public void moveShip_validMove_returnsTrue()
    {
        UUID gameId = fixedGameResponse.gameId(), hostToken = fixedGameResponse.hostToken();
        Ship ship = gameService.getGame(gameId).getBoard(hostToken).getShips().get(0);

        boolean isMoved = gameService.moveShip(gameId, ship.getId(), hostToken, new Coordinate(0, 7), Orientation.VERTICAL);
        assertTrue(isMoved);
    }

    @Test
    public void moveShip_invalidMove_returnsFalse()
    {
        UUID gameId = fixedGameResponse.gameId(), hostToken = fixedGameResponse.hostToken();
        Ship ship = gameService.getGame(gameId).getBoard(hostToken).getShips().get(0);

        boolean isMoved = gameService.moveShip(gameId, ship.getId(), hostToken, new Coordinate(0, 8), Orientation.VERTICAL);
        assertFalse(isMoved);
    }

    @Test
    public void getShips_gameCreated_returnsShipResponsesList()
    {
        List<ShipResponse> ships = gameService.getShips(fixedGameResponse.gameId(), fixedGameResponse.hostToken());

        for (int i = 0; i < ships.size(); i++)
            assertEquals(shipResponses.get(i), ships.get(i));
    }

    @Test
    public void shoot_hitShipCell_marksCellAsHitAndUpdatesScore()
    {
        gameService.joinGame(fixedGameResponse.gameId(), opponentCells, opponentShips);

        Game game = gameService.getGame(fixedGameResponse.gameId());
        UUID hostToken = game.getHostToken();
        CombatViewResponse resp = gameService.shoot(fixedGameResponse.gameId(), hostToken, new Coordinate(9, 3));

        assertEquals(CellStateView.HIT, resp.opponentBoard()[9][3]);
        assertEquals(ShotResult.HIT, resp.shotResult());

        assertEquals(10, game.getScore(hostToken));
        assertEquals(ShotResult.HIT, game.getBoard(hostToken).getLastShotResult());
    }

    @Test
    public void shoot_lastShipCell_marksAllShipCellsAsSunkAndRemovesShip()
    {
        gameService.joinGame(fixedGameResponse.gameId(), opponentCells, opponentShips);

        Game game = gameService.getGame(fixedGameResponse.gameId());
        UUID hostToken = game.getHostToken();

        CombatViewResponse resp = null;
        Ship ship = opponentShips.get(1);

        for (Coordinate cell : ship.getCells())
            resp = gameService.shoot(fixedGameResponse.gameId(), hostToken, cell);

        assertNotNull(resp);
        for (Coordinate cell : ship.getCells())
            assertEquals(CellStateView.SUNK, resp.opponentBoard()[cell.row()][cell.col()]);

        assertEquals(ShotResult.SUNK, resp.shotResult());
        assertEquals(75, game.getScore(hostToken));
        assertEquals(ShotResult.SUNK, game.getBoard(hostToken).getLastShotResult());

        assertNull(game.getOpponentBoard(hostToken).getShipById(ship.getId()));
    }

    @Test
    public void shoot_afterMiss_marksCellAsMiss()
    {
        gameService.joinGame(fixedGameResponse.gameId(), opponentCells, opponentShips);

        Game game = gameService.getGame(fixedGameResponse.gameId());
        UUID hostToken = game.getHostToken();
        CombatViewResponse resp = gameService.shoot(fixedGameResponse.gameId(), hostToken, new Coordinate(4, 0));

        assertEquals(CellStateView.MISS, resp.opponentBoard()[4][0]);
        assertEquals(ShotResult.MISS, resp.shotResult());

        assertEquals(0, game.getScore(hostToken));
        assertEquals(ShotResult.MISS, game.getBoard(hostToken).getLastShotResult());
    }

    @Test
    public void getCombatView_placementPhase_returnsNull()
    {
        assertNull(gameService.getCombatView(randomGameResponse.gameId(), randomGameResponse.hostToken()));
    }

    @Test
    public void getCombatView_combatPhase_returnsCorrectCombatView()
    {
        UUID gameId = fixedGameResponse.gameId();
        Game game = gameService.getGame(gameId);

        gameService.joinGame(gameId);
        game.changeToCombatPhase();

        CombatViewResponse resp = gameService.getCombatView(gameId, fixedGameResponse.hostToken());

        assertEquals(GamePhase.COMBAT, resp.phase());
        assertEquals(hostCells, resp.hostBoard());

        for (int i = 0; i < Board.SIZE; i++)
        {
            for (int j = 0; j < Board.SIZE; j++)
                assertEquals(CellStateView.UNKNOWN, resp.opponentBoard()[i][j]);
        }

        assertEquals(ShotResult.NONE, resp.shotResult());
        assertEquals(0, resp.score());
    }
}
