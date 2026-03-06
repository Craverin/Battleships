package com.example.demo.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Game
{
    private UUID gameId, hostToken, opponentToken;
    private Map<UUID, Board> boards;

    public UUID createGame()
    {
        gameId = UUID.randomUUID();
        hostToken = UUID.randomUUID();
        boards = new HashMap<>();

        Board board = new Board();
        board.generateShips();
        boards.put(hostToken, board);

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
}
