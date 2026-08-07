package gamestudio.server.domain;

import gamestudio.server.dto.ShotResult;
import gamestudio.server.security.principal.ApplicationPrincipal;

import java.util.*;

public class Game
{
    private UUID gameId, hostToken, opponentToken;
    private ApplicationPrincipal hostUser, opponentUser;
    private boolean hostReady, opponentReady, isHostTurn, statsRecorded;
    private Map<UUID, Board> boards;
    private Map<UUID, Integer> scores;
    private Map<UUID, Integer> hitStreaks;
    private GamePhase gamePhase;
    public static final int BASE_SCORE_PER_HIT = 10;

    public UUID createGame()
    {
        Board board = new Board();
        board.generateShips();

        return initializeGame(board);
    }

    public UUID createGame(CellState[][] cells, List<Ship> ships)
    {
        Board board = new Board(cells, ships);
        return initializeGame(board);
    }

    private UUID initializeGame(Board board)
    {
        gameId = UUID.randomUUID();
        hostToken = UUID.randomUUID();
        gamePhase = GamePhase.PLACEMENT;
        boards = new HashMap<>();
        scores = new HashMap<>();
        hitStreaks = new HashMap<>();

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

    public UUID addPlayer(CellState[][] cells, List<Ship> ships)
    {
        this.opponentToken = UUID.randomUUID();

        Board board = new Board(cells, ships);
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

    public void updateScore(ShotResult shotResult, UUID playerToken, int baseScore)
    {
        // Consecutive hits increase the score multiplier by one, while sinking a ship gives an extra points
        // A miss resets the streak
        int multiplier;

        if (shotResult.equals(ShotResult.HIT) || shotResult.equals(ShotResult.SUNK))
            multiplier = hitStreaks.get(playerToken) + 1;
        else
        {
            hitStreaks.put(playerToken, 0);
            return;
        }

        scores.put(playerToken, scores.get(playerToken) + baseScore * multiplier);
        hitStreaks.put(playerToken, multiplier);

        if (shotResult.equals(ShotResult.SUNK)) scores.put(playerToken, scores.get(playerToken) + 15);
    }

    public void changeToCombatPhase()
    {
        gamePhase = GamePhase.COMBAT;
        isHostTurn = new Random().nextBoolean();
    }

    public UUID getCurrentTurn()
    {
//        if (!gamePhase.equals(GamePhase.COMBAT)) return null;
        System.out.println(gamePhase);
        return isHostTurn ? hostToken : opponentToken;
    }

    public void changeCurrentTurn(ShotResult shotResult)
    {
        if (shotResult.equals(ShotResult.MISS))
            isHostTurn = !isHostTurn;
    }

    public boolean setReady(UUID playerToken)
    {
        if (playerToken.equals(hostToken)) hostReady = true;
        if (playerToken.equals(opponentToken))  opponentReady = true;

        if (hostReady && opponentReady) changeToCombatPhase();

        return playerToken.equals(hostToken) || playerToken.equals(opponentToken);
    }

    public void markGameAsFinished() { gamePhase = GamePhase.FINISHED; }

    public void setHostUser(ApplicationPrincipal hostUser)
    {
        this.hostUser = hostUser;
    }

    public void setOpponentUser(ApplicationPrincipal opponentUser)
    {
        this.opponentUser = opponentUser;
    }

    public ApplicationPrincipal getUserByToken(UUID playerToken)
    {
        if (playerToken.equals(hostToken)) return hostUser;
        if (playerToken.equals(opponentToken)) return opponentUser;

        return null;
    }

    public String getUsername(UUID playerToken)
    {
        if (!playerToken.equals(hostToken) && !playerToken.equals(opponentToken))
            return null;

        String username = "Guest";
        ApplicationPrincipal user = getUserByToken(playerToken);
        if (user != null) username = user.username();

        return username;
    }

    public boolean isStatsRecorded() { return statsRecorded; }

    public void markStatsRecorded()
    {
        statsRecorded = true;
    }
}
