package gamestudio;

import gamestudio.server.domain.Game;
import gamestudio.server.domain.GamePhase;
import gamestudio.server.dto.ShotResult;
import gamestudio.server.security.principal.AuthUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GameTest
{
    private Game game;

    @BeforeEach
    void setUp()
    {
        game = new Game();
    }

    @Test
    void createGame_initializesGameIdHostTokenBoardAndScore()
    {
        UUID gameId = game.createGame();
        UUID hostToken = game.getHostToken();

        assertNotNull(gameId);
        assertEquals(gameId, game.getGameId());
        assertNotNull(hostToken);
        assertNotNull(game.getBoard(hostToken));
        assertEquals(0, game.getScore(hostToken));
        assertEquals(GamePhase.PLACEMENT, game.getPhase());
    }

    @Test
    void addPlayer_afterCreateGame_initializesOpponentTokenBoardAndScore()
    {
        game.createGame();

        UUID opponentToken = game.addPlayer();

        assertNotNull(opponentToken);
        assertEquals(opponentToken, game.getOpponentToken());
        assertNotNull(game.getBoard(opponentToken));
        assertEquals(0, game.getScore(opponentToken));
    }

    @Test
    void getOpponentBoard_forHostAndOpponent_returnsOtherPlayersBoard()
    {
        createGameWithTwoPlayers();

        assertSame(game.getBoard(game.getOpponentToken()), game.getOpponentBoard(game.getHostToken()));
        assertSame(game.getBoard(game.getHostToken()), game.getOpponentBoard(game.getOpponentToken()));
    }

    @Test
    void getPhase_afterBothPlayersJoin_staysPlacementUntilBothReady()
    {
        createGameWithTwoPlayers();

        assertEquals(GamePhase.PLACEMENT, game.getPhase());

        assertTrue(game.setReady(game.getHostToken()));
        assertEquals(GamePhase.PLACEMENT, game.getPhase());

        assertTrue(game.setReady(game.getOpponentToken()));
        assertEquals(GamePhase.COMBAT, game.getPhase());
        assertNotNull(game.getCurrentTurn());
    }

    @Test
    void setReady_unknownToken_returnsFalseAndDoesNotChangePhase()
    {
        createGameWithTwoPlayers();

        assertFalse(game.setReady(UUID.randomUUID()));
        assertEquals(GamePhase.PLACEMENT, game.getPhase());
    }

    @Test
    void getWinner_beforeGameIsFinished_returnsNull()
    {
        createGameWithTwoPlayers();
        game.getBoard(game.getHostToken()).getShips().clear();

        assertNull(game.getWinner());
    }

    @Test
    void getWinner_finishedGameWithNoHostShips_returnsOpponentToken()
    {
        createGameWithTwoPlayers();
        game.getBoard(game.getHostToken()).getShips().clear();
        game.markGameAsFinished();

        assertEquals(game.getOpponentToken(), game.getWinner());
    }

    @Test
    void getWinner_finishedGameWithNoOpponentShips_returnsHostToken()
    {
        createGameWithTwoPlayers();
        game.getBoard(game.getOpponentToken()).getShips().clear();
        game.markGameAsFinished();

        assertEquals(game.getHostToken(), game.getWinner());
    }

    @Test
    void changeCurrentTurn_hit_keepsCurrentPlayer()
    {
        createCombatGame();
        UUID currentPlayer = game.getCurrentTurn();

        game.changeCurrentTurn(ShotResult.HIT);

        assertEquals(currentPlayer, game.getCurrentTurn());
    }

    @Test
    void changeCurrentTurn_sunk_keepsCurrentPlayer()
    {
        createCombatGame();
        UUID currentPlayer = game.getCurrentTurn();

        game.changeCurrentTurn(ShotResult.SUNK);

        assertEquals(currentPlayer, game.getCurrentTurn());
    }

    @Test
    void changeCurrentTurn_miss_switchesPlayer()
    {
        createCombatGame();
        UUID currentPlayer = game.getCurrentTurn();

        game.changeCurrentTurn(ShotResult.MISS);

        assertNotEquals(currentPlayer, game.getCurrentTurn());
    }

    @Test
    void updateScore_firstHit_addsBaseScore()
    {
        createGameWithTwoPlayers();
        UUID hostToken = game.getHostToken();

        game.updateScore(ShotResult.HIT, hostToken, Game.BASE_SCORE_PER_HIT);

        assertEquals(10, game.getScore(hostToken));
    }

    @Test
    void updateScore_consecutiveHits_increaseMultiplier()
    {
        createGameWithTwoPlayers();
        UUID hostToken = game.getHostToken();

        game.updateScore(ShotResult.HIT, hostToken, Game.BASE_SCORE_PER_HIT);
        game.updateScore(ShotResult.HIT, hostToken, Game.BASE_SCORE_PER_HIT);
        game.updateScore(ShotResult.HIT, hostToken, Game.BASE_SCORE_PER_HIT);

        assertEquals(60, game.getScore(hostToken));
    }

    @Test
    void updateScore_miss_resetsHitStreak()
    {
        createGameWithTwoPlayers();
        UUID hostToken = game.getHostToken();

        game.updateScore(ShotResult.HIT, hostToken, Game.BASE_SCORE_PER_HIT);
        game.updateScore(ShotResult.HIT, hostToken, Game.BASE_SCORE_PER_HIT);
        game.updateScore(ShotResult.MISS, hostToken, Game.BASE_SCORE_PER_HIT);
        game.updateScore(ShotResult.HIT, hostToken, Game.BASE_SCORE_PER_HIT);

        assertEquals(40, game.getScore(hostToken));
    }

    @Test
    void updateScore_sunk_addsStreakScoreAndSunkBonus()
    {
        createGameWithTwoPlayers();
        UUID hostToken = game.getHostToken();

        game.updateScore(ShotResult.HIT, hostToken, Game.BASE_SCORE_PER_HIT);
        game.updateScore(ShotResult.SUNK, hostToken, Game.BASE_SCORE_PER_HIT);

        assertEquals(45, game.getScore(hostToken));
    }

    @Test
    void getUsername_guestAndAuthenticatedPlayers_returnsExpectedNames()
    {
        createGameWithTwoPlayers();
        UUID hostToken = game.getHostToken();
        UUID opponentToken = game.getOpponentToken();

        assertEquals("Guest", game.getUsername(hostToken));
        assertEquals("Guest", game.getUsername(opponentToken));

        game.setHostUser(new AuthUser(1, "alice"));
        game.setOpponentUser(new AuthUser(2, "bob"));

        assertEquals("alice", game.getUsername(hostToken));
        assertEquals("bob", game.getUsername(opponentToken));
        assertNull(game.getUsername(UUID.randomUUID()));
    }

    @Test
    void statsRecorded_isFalseByDefaultAndCanBeMarkedRecorded()
    {
        game.createGame();

        assertFalse(game.isStatsRecorded());

        game.markStatsRecorded();

        assertTrue(game.isStatsRecorded());
    }

    private void createGameWithTwoPlayers()
    {
        game.createGame();
        game.addPlayer();
    }

    private void createCombatGame()
    {
        createGameWithTwoPlayers();
        game.setReady(game.getHostToken());
        game.setReady(game.getOpponentToken());
    }
}
