package gamestudio.domain;

import gamestudio.dto.ShotResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Game
{
    private UUID gameId, hostToken, opponentToken;
    private Map<UUID, Board> boards;
    private Map<UUID, Integer> scores;
    private Map<UUID, Integer> hitStreaks;
    private GamePhase gamePhase;

    public UUID createGame()
    {
        gameId = UUID.randomUUID();
        hostToken = UUID.randomUUID();
        gamePhase = GamePhase.PLACEMENT;
        boards = new HashMap<>();
        scores = new HashMap<>();
        hitStreaks = new HashMap<>();

        Board board = new Board();
        board.generateShips();
        boards.put(hostToken, board);
        scores.put(hostToken, 0);
        hitStreaks.put(hostToken, 0);

        return gameId;
    }


    public Board getBoard(UUID playerToken) { return boards.get(playerToken); }
    public Board getOpponentBoard(UUID playerToken)
    {
        if (playerToken.equals(hostToken)) return boards.get(opponentToken);
        return boards.get(hostToken);
    }

    public UUID getGameId()
    {
        return gameId;
    }

    public UUID addPlayer()
    {
        this.opponentToken = UUID.randomUUID();

        Board board = new Board();
        board.generateShips();
        boards.put(opponentToken, board);
        scores.put(opponentToken, 0);
        hitStreaks.put(opponentToken, 0);

        return opponentToken;
    }

    public UUID getHostToken()
    {
        return hostToken;
    }

    public UUID getOpponentToken()
    {
        return opponentToken;
    }

    public GamePhase getPhase()
    {
        if (opponentToken != null && (getBoard(hostToken).getShips().isEmpty() || getBoard(opponentToken).getShips().isEmpty()))
            gamePhase = GamePhase.FINISHED;

        return gamePhase;
    }

    public UUID getWinner()
    {
        if (getPhase() != GamePhase.FINISHED) return null;

        return getBoard(hostToken).getShips().isEmpty() ? opponentToken : hostToken;
    }

    public int getScore(UUID playerToken)
    {
        return scores.get(playerToken);
    }

    public void addScore(ShotResult shotResult, UUID playerToken, int score)
    {
        int multiplier;

        if (!shotResult.equals(ShotResult.MISS)) multiplier = hitStreaks.get(playerToken) + 1;
        else
        {
            hitStreaks.put(playerToken, 0);
            return;
        }

        scores.put(playerToken, scores.get(playerToken) + score * multiplier);
        hitStreaks.put(playerToken, multiplier);

        if (shotResult.equals(ShotResult.SUNK)) scores.put(playerToken, scores.get(playerToken) + 15);
    }

    public void changeToCombatPhase()
    {
        gamePhase = GamePhase.COMBAT;
    }
}
