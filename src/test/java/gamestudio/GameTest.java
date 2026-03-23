package gamestudio;

import gamestudio.domain.Board;
import gamestudio.domain.Game;
import gamestudio.domain.GamePhase;
import gamestudio.dto.ShotResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GameTest
{
    private Game game;
    private UUID hostToken;
    private UUID opponentToken;

    @BeforeEach
    void setUp()
    {
        game = new Game();
    }

    @Test
    void createGame_noExistingGame_initializesGameIdHostTokenBoardAndScore()
    {
        UUID gameId = game.createGame();
        UUID hostToken = game.getHostToken();

        assertNotNull(gameId);
        assertEquals(gameId, game.getGameId());

        assertNotNull(hostToken);
        assertNotNull(game.getBoard(hostToken));
        assertEquals(0, game.getScore(hostToken));
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
    void getOpponentBoard_hostToken_returnsOpponentBoard()
    {
        createGameWithTwoPlayers();
        Board opponentBoard = game.getBoard(opponentToken);

        assertSame(opponentBoard, game.getOpponentBoard(hostToken));
    }

    @Test
    void getOpponentBoard_opponentToken_returnsHostBoard()
    {
        createGameWithTwoPlayers();
        Board hostBoard = game.getBoard(hostToken);

        assertSame(hostBoard, game.getOpponentBoard(opponentToken));
    }

    @Test
    void getPhase_bothPlayersHaveShips_returnsCombat()
    {
        createGameWithTwoPlayers();

        assertEquals(GamePhase.PLACEMENT, game.getPhase());
    }

    @Test
    void getPhase_hostHasNoShips_returnsFinished()
    {
        createGameWithTwoPlayers();
        game.getBoard(hostToken).getShips().clear();

        assertEquals(GamePhase.FINISHED, game.getPhase());
    }

    @Test
    void getPhase_opponentHasNoShips_returnsFinished()
    {
        createGameWithTwoPlayers();
        game.getBoard(opponentToken).getShips().clear();

        assertEquals(GamePhase.FINISHED, game.getPhase());
    }

    @Test
    void getWinner_hostHasNoShips_returnsOpponentToken()
    {
        createGameWithTwoPlayers();
        game.getBoard(hostToken).getShips().clear();

        assertEquals(opponentToken, game.getWinner());
    }

    @Test
    void getWinner_opponentHasNoShips_returnsHostToken()
    {
        createGameWithTwoPlayers();
        game.getBoard(opponentToken).getShips().clear();

        assertEquals(hostToken, game.getWinner());
    }


    @Test
    void addScore_miss_doesNotIncreaseScore()
    {
        game.createGame();
        UUID createdHostToken = game.getHostToken();

        game.updateScore(ShotResult.MISS, createdHostToken, 10);
        assertEquals(0, game.getScore(createdHostToken));
    }

    @Test
    void addScore_firstHit_increasesScoreByBaseScore()
    {
        game.createGame();
        UUID createdHostToken = game.getHostToken();

        game.updateScore(ShotResult.HIT, createdHostToken, 10);
        assertEquals(10, game.getScore(createdHostToken));
    }

    @Test
    void addScore_consecutiveHits_appliesIncreasingMultiplier()
    {
        game.createGame();
        UUID createdHostToken = game.getHostToken();

        game.updateScore(ShotResult.HIT, createdHostToken, 10);
        game.updateScore(ShotResult.HIT, createdHostToken, 10);

        assertEquals(30, game.getScore(createdHostToken));
    }

    @Test
    void addScore_missAfterHit_resetsHitStreak()
    {
        game.createGame();
        UUID createdHostToken = game.getHostToken();

        game.updateScore(ShotResult.HIT, createdHostToken, 10);
        game.updateScore(ShotResult.HIT, createdHostToken, 10);
        game.updateScore(ShotResult.MISS, createdHostToken, 10);
        game.updateScore(ShotResult.HIT, createdHostToken, 10);

        assertEquals(40, game.getScore(createdHostToken));
    }

    @Test
    void addScore_sunk_appliesMultiplierAndAddsBonusPoints()
    {
        game.createGame();
        UUID createdHostToken = game.getHostToken();

        game.updateScore(ShotResult.HIT, createdHostToken, 10);
        game.updateScore(ShotResult.SUNK, createdHostToken, 10);

        assertEquals(45, game.getScore(createdHostToken));
    }

    private void createGameWithTwoPlayers()
    {
        game.createGame();
        hostToken = game.getHostToken();
        opponentToken = game.addPlayer();
    }
}