package gamestudio;

import gamestudio.server.domain.*;
import gamestudio.server.dto.*;
import gamestudio.server.security.principal.AuthUser;
import gamestudio.server.service.GameService;
import gamestudio.server.service.PlayerStatsService;
import gamestudio.server.service.ScoreService;
import gamestudio.server.service.authentication.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest
{
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private PlayerStatsService playerStatsService;
    @Mock
    private ScoreService scoreService;

    @InjectMocks
    private GameService gameService;

    @Test
    void createGame_createsAndStoresPlacementGame()
    {
        CreateGameResponse response = gameService.createGame();

        assertNotNull(response.gameId());
        assertNotNull(response.playerToken());

        Game game = gameService.getGame(response.gameId());
        assertNotNull(game);
        assertEquals(response.gameId(), game.getGameId());
        assertEquals(response.playerToken(), game.getHostToken());
        assertEquals(GamePhase.PLACEMENT, game.getPhase());
    }

    @Test
    void createGame_authenticatedUser_setsHostUser()
    {
        AuthUser host = new AuthUser(11, "host");
        when(currentUserService.getCurrentAuthUserOptional()).thenReturn(Optional.of(host));

        CreateGameResponse response = gameService.createGame();

        Game game = gameService.getGame(response.gameId());
        assertSame(host, game.getUserByToken(response.playerToken()));
        assertEquals("host", game.getUsername(response.playerToken()));
    }

    @Test
    void joinGame_existingGame_addsOpponent()
    {
        CreateGameResponse created = gameService.createGame();

        UUID opponentToken = gameService.joinGame(created.gameId());

        assertNotNull(opponentToken);
        Game game = gameService.getGame(created.gameId());
        assertEquals(opponentToken, game.getOpponentToken());
        assertNotNull(game.getBoard(opponentToken));
    }

    @Test
    void joinGame_authenticatedUser_setsOpponentUser()
    {
        AuthUser opponent = new AuthUser(22, "opponent");
        when(currentUserService.getCurrentAuthUserOptional())
                .thenReturn(Optional.of(opponent));

        CreateGameResponse created = gameService.createGame();
        UUID opponentToken = gameService.joinGame(created.gameId());

        Game game = gameService.getGame(created.gameId());
        assertSame(opponent, game.getUserByToken(opponentToken));
        assertEquals("opponent", game.getUsername(opponentToken));
    }

    @Test
    void joinGame_gameAlreadyHasOpponent_returnsNull()
    {
        CreateGameResponse created = gameService.createGame();
        UUID firstOpponent = gameService.joinGame(created.gameId());

        UUID secondOpponent = gameService.joinGame(created.gameId());

        assertNotNull(firstOpponent);
        assertNull(secondOpponent);
        assertEquals(firstOpponent, gameService.getGame(created.gameId()).getOpponentToken());
    }

    @Test
    void createPrivateGame_generatesResolvableSixCharacterInviteCode()
    {
        CreatePrivateGameResponse response = gameService.createPrivateGame();

        assertNotNull(response.gameId());
        assertNotNull(response.hostToken());
        assertNotNull(response.inviteCode());
        assertEquals(6, response.inviteCode().length());
        assertEquals(response.gameId(), gameService.getGameIdByInviteCode(response.inviteCode()));
    }

    @Test
    void getGameIdByInviteCode_invalidCode_throwsNotFound()
    {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> gameService.getGameIdByInviteCode("BAD")
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void removeGame_privateGame_removesGameAndInviteCode()
    {
        CreatePrivateGameResponse response = gameService.createPrivateGame();

        gameService.removeGame(response.gameId());

        assertNull(gameService.getGame(response.gameId()));
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> gameService.getGameIdByInviteCode(response.inviteCode())
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void moveShip_validMove_movesShipAndUpdatesShipResponse()
    {
        TestBoard host = board(new Ship(UUID.randomUUID(), new Coordinate(0, 0), Orientation.HORIZONTAL, 2));
        CreatePrivateGameResponse created = gameService.createPrivateGame(host.cells(), host.ships());
        UUID shipId = host.ships().get(0).getId();

        boolean moved = gameService.moveShip(
                created.gameId(), shipId, created.hostToken(),
                new Coordinate(5, 5), Orientation.HORIZONTAL
        );

        assertTrue(moved);
        assertEquals(
                new ShipResponse(shipId, 5, 5, Orientation.HORIZONTAL, 2),
                gameService.getShips(created.gameId(), created.hostToken()).get(0)
        );
    }

    @Test
    void moveShip_outOfBounds_returnsFalseAndKeepsOriginalShip()
    {
        TestBoard host = board(new Ship(UUID.randomUUID(), new Coordinate(0, 0), Orientation.HORIZONTAL, 2));
        CreatePrivateGameResponse created = gameService.createPrivateGame(host.cells(), host.ships());
        UUID shipId = host.ships().get(0).getId();

        boolean moved = gameService.moveShip(
                created.gameId(), shipId, created.hostToken(),
                new Coordinate(9, 9), Orientation.HORIZONTAL
        );

        assertFalse(moved);
        assertEquals(
                new ShipResponse(shipId, 0, 0, Orientation.HORIZONTAL, 2),
                gameService.getShips(created.gameId(), created.hostToken()).get(0)
        );
    }

    @Test
    void getShips_fixedBoard_returnsCurrentShipResponses()
    {
        Ship first = new Ship(UUID.randomUUID(), new Coordinate(0, 0), Orientation.HORIZONTAL, 2);
        Ship second = new Ship(UUID.randomUUID(), new Coordinate(5, 5), Orientation.VERTICAL, 1);
        TestBoard host = board(first, second);
        CreatePrivateGameResponse created = gameService.createPrivateGame(host.cells(), host.ships());

        List<ShipResponse> ships = gameService.getShips(created.gameId(), created.hostToken());

        assertEquals(2, ships.size());
        assertEquals(new ShipResponse(first.getId(), 0, 0, Orientation.HORIZONTAL, 2), ships.get(0));
        assertEquals(new ShipResponse(second.getId(), 5, 5, Orientation.VERTICAL, 1), ships.get(1));
    }

    @Test
    void getCombatView_duringPlacement_returnsNull()
    {
        CreateGameResponse created = gameService.createGame();
        gameService.joinGame(created.gameId());

        assertNull(gameService.getCombatView(created.gameId(), created.playerToken()));
    }

    @Test
    void getCombatView_duringCombat_returnsOwnBoardAndMasksUnseenOpponentCells()
    {
        TestBoard host = board(new Ship(UUID.randomUUID(), new Coordinate(0, 0), Orientation.HORIZONTAL, 2));
        TestBoard opponent = board(new Ship(UUID.randomUUID(), new Coordinate(5, 5), Orientation.HORIZONTAL, 2));
        CreatePrivateGameResponse created = gameService.createPrivateGame(host.cells(), host.ships());
        gameService.joinGame(created.gameId(), opponent.cells(), opponent.ships());
        Game game = gameService.getGame(created.gameId());
        game.changeToCombatPhase();

        CombatViewResponse response = gameService.getCombatView(created.gameId(), created.hostToken());

        assertNotNull(response);
        assertEquals(GamePhase.COMBAT, response.phase());
        assertSame(host.cells(), response.hostBoard());
        assertEquals(CellStateView.UNKNOWN, response.opponentBoard()[5][5]);
        assertEquals(CellStateView.UNKNOWN, response.opponentBoard()[4][4]);
        assertEquals(ShotResult.NONE, response.shotResult());
        assertEquals(0, response.score());
    }

    @Test
    void shoot_unknownGame_throwsNotFound()
    {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> gameService.shoot(UUID.randomUUID(), UUID.randomUUID(), null)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void shoot_wrongTurn_throwsConflict()
    {
        TestBoard host = board(new Ship(UUID.randomUUID(), new Coordinate(0, 0), Orientation.HORIZONTAL, 2));
        TestBoard opponent = board(new Ship(UUID.randomUUID(), new Coordinate(0, 0), Orientation.HORIZONTAL, 2));
        CreatePrivateGameResponse created = gameService.createPrivateGame(host.cells(), host.ships());
        UUID opponentToken = gameService.joinGame(created.gameId(), opponent.cells(), opponent.ships());
        Game game = gameService.getGame(created.gameId());
        game.changeToCombatPhase();
        UUID wrongPlayer = game.getCurrentTurn().equals(created.hostToken()) ? opponentToken : created.hostToken();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> gameService.shoot(created.gameId(), wrongPlayer, new Coordinate(0, 0))
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void shoot_hit_updatesBoardLastShotAndScoreWithoutChangingTurn()
    {
        TestBoard host = board(new Ship(UUID.randomUUID(), new Coordinate(8, 3), Orientation.VERTICAL, 2));
        TestBoard opponent = board(new Ship(UUID.randomUUID(), new Coordinate(9, 3), Orientation.HORIZONTAL, 2));
        CreatePrivateGameResponse created = gameService.createPrivateGame(host.cells(), host.ships());
        gameService.joinGame(created.gameId(), opponent.cells(), opponent.ships());
        Game game = gameService.getGame(created.gameId());
        game.changeToCombatPhase();
        UUID shooter = game.getCurrentTurn();

        CombatViewResponse response = gameService.shoot(created.gameId(), shooter, new Coordinate(9, 3));

        assertEquals(ShotResult.HIT, response.shotResult());
        assertEquals(CellStateView.HIT, response.opponentBoard()[9][3]);
        assertEquals(10, response.score());
        assertTrue(response.yourTurn());
        assertEquals(shooter, game.getCurrentTurn());
        assertEquals(ShotResult.HIT, game.getBoard(shooter).getLastShotResult());
    }

    @Test
    void shoot_miss_marksCellAndSwitchesTurn()
    {
        TestBoard host = board(new Ship(UUID.randomUUID(), new Coordinate(0, 0), Orientation.HORIZONTAL, 2));
        TestBoard opponent = board(new Ship(UUID.randomUUID(), new Coordinate(0, 0), Orientation.HORIZONTAL, 2));
        CreatePrivateGameResponse created = gameService.createPrivateGame(host.cells(), host.ships());
        gameService.joinGame(created.gameId(), opponent.cells(), opponent.ships());
        Game game = gameService.getGame(created.gameId());
        game.changeToCombatPhase();
        UUID shooter = game.getCurrentTurn();

        CombatViewResponse response = gameService.shoot(created.gameId(), shooter, new Coordinate(9, 9));

        assertEquals(ShotResult.MISS, response.shotResult());
        assertEquals(CellStateView.MISS, response.opponentBoard()[9][9]);
        assertEquals(0, response.score());
        assertFalse(response.yourTurn());
        assertNotEquals(shooter, game.getCurrentTurn());
        assertEquals(ShotResult.MISS, game.getBoard(shooter).getLastShotResult());
    }

    @Test
    void shoot_lastRemainingShip_finishesGameAndMakesShooterWinner()
    {
        TestBoard host = board(new Ship(UUID.randomUUID(), new Coordinate(0, 0), Orientation.HORIZONTAL, 1));
        TestBoard opponent = board(new Ship(UUID.randomUUID(), new Coordinate(0, 0), Orientation.HORIZONTAL, 1));
        CreatePrivateGameResponse created = gameService.createPrivateGame(host.cells(), host.ships());
        gameService.joinGame(created.gameId(), opponent.cells(), opponent.ships());
        Game game = gameService.getGame(created.gameId());
        game.changeToCombatPhase();
        UUID shooter = game.getCurrentTurn();

        CombatViewResponse response = gameService.shoot(created.gameId(), shooter, new Coordinate(0, 0));

        assertEquals(ShotResult.SUNK, response.shotResult());
        assertEquals(CellStateView.SUNK, response.opponentBoard()[0][0]);
        assertEquals(GamePhase.FINISHED, response.phase());
        assertEquals(25, response.score());
        assertEquals(shooter, game.getWinner());
    }

    @Test
    void shoot_finishedAuthenticatedGame_recordsStatsAndScoresForBothPlayers()
    {
        TestBoard hostBoard = board(new Ship(UUID.randomUUID(), new Coordinate(0, 0), Orientation.HORIZONTAL, 1));
        TestBoard opponentBoard = board(new Ship(UUID.randomUUID(), new Coordinate(0, 0), Orientation.HORIZONTAL, 1));
        CreatePrivateGameResponse created = gameService.createPrivateGame(hostBoard.cells(), hostBoard.ships());
        UUID opponentToken = gameService.joinGame(created.gameId(), opponentBoard.cells(), opponentBoard.ships());
        Game game = gameService.getGame(created.gameId());
        AuthUser host = new AuthUser(1, "host");
        AuthUser opponent = new AuthUser(2, "opponent");
        game.setHostUser(host);
        game.setOpponentUser(opponent);
        game.changeToCombatPhase();
        UUID shooter = game.getCurrentTurn();

        gameService.shoot(created.gameId(), shooter, new Coordinate(0, 0));

        boolean hostWon = shooter.equals(created.hostToken());
        int hostScore = hostWon ? 25 : 0;
        int opponentScore = hostWon ? 0 : 25;

        verify(playerStatsService).recordGameResult(
                "battleships", host.userId(), host.username(), hostScore, hostWon
        );
        verify(playerStatsService).recordGameResult(
                "battleships", opponent.userId(), opponent.username(), opponentScore, !hostWon
        );
        verify(scoreService).addScore("battleships", host.userId(), host.username(), hostScore);
        verify(scoreService).addScore("battleships", opponent.userId(), opponent.username(), opponentScore);
        verifyNoMoreInteractions(playerStatsService, scoreService);
        assertTrue(game.isStatsRecorded());
        assertEquals(opponentToken, game.getOpponentToken());
    }

    private TestBoard board(Ship... ships)
    {
        CellState[][] cells = new CellState[Board.SIZE][Board.SIZE];
        for (int row = 0; row < Board.SIZE; row++)
        {
            for (int col = 0; col < Board.SIZE; col++)
                cells[row][col] = CellState.EMPTY;
        }

        List<Ship> shipList = new ArrayList<>(List.of(ships));
        for (Ship ship : shipList)
        {
            for (Coordinate border : ship.getBorderCells())
            {
                if (cells[border.row()][border.col()] == CellState.EMPTY)
                    cells[border.row()][border.col()] = CellState.INDIRECTLY_OCCUPIED;
            }
            for (Coordinate occupied : ship.getCells())
                cells[occupied.row()][occupied.col()] = CellState.OCCUPIED;
        }
        return new TestBoard(cells, shipList);
    }

    private record TestBoard(CellState[][] cells, List<Ship> ships) { }
}
