package gamestudio.server.service;

import gamestudio.server.domain.Game;
import gamestudio.server.dto.CreateGameResponse;
import gamestudio.server.dto.MatchmakingResponse;
import gamestudio.server.dto.MatchmakingStatus;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.UUID;

@Service
public class MatchmakingService
{
    private final Queue<UUID> waitingGames = new ArrayDeque<>();
    private final Object matchmakingMonitor = new Object();
    private final GameService gameService;

    public MatchmakingService(GameService gameService)
    {
        this.gameService = gameService;
    }

    public MatchmakingResponse findGame()
    {
        synchronized (matchmakingMonitor)
        {
            UUID gameId;
            while ((gameId = waitingGames.poll()) != null)
            {
                UUID playerToken = gameService.joinGame(gameId);
                if (playerToken == null) continue;

                return new MatchmakingResponse(gameId, playerToken, MatchmakingStatus.MATCHED);
            }

            CreateGameResponse game = gameService.createGame();
            waitingGames.offer(game.gameId());

            return new MatchmakingResponse(game.gameId(), game.playerToken(), MatchmakingStatus.SEARCHING);
        }
    }

    public void cancelSearch(UUID gameId, UUID playerToken)
    {
        if (gameId == null) return;

        synchronized (matchmakingMonitor)
        {
            Game game = gameService.getGame(gameId);

            if (game == null)
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find game with specified userId");

            if (!game.getHostToken().equals(playerToken))
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Wrong player token");

            boolean removed = waitingGames.remove(gameId);
            if (!removed)
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Game's already been started");

            gameService.removeGame(gameId);
        }

    }
}
