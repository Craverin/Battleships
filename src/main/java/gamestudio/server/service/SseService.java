package gamestudio.server.service;

import gamestudio.server.domain.Game;
import gamestudio.server.domain.GamePhase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SseService
{
    private final Map<UUID, Map<UUID, SseEmitter>> emitters = new ConcurrentHashMap<>();
    private final GameService gameService;

    public SseService(GameService gameService)
    {
        this.gameService = gameService;
    }

    public SseEmitter subscribe(UUID gameId, UUID playerToken)
    {
        SseEmitter emitter = new SseEmitter(0L);

        emitters.computeIfAbsent(gameId, id -> new ConcurrentHashMap<>()).put(playerToken, emitter);

        Runnable cleanup = () ->
        {
            remove(gameId, playerToken);
            notifyOpponentDisconnected(gameId, playerToken);
        };

        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(error -> cleanup.run());

        return emitter;
    }

    public void sendToPlayer(UUID gameId, UUID playerToken, String eventName, Object data)
    {
        SseEmitter emitter = get(gameId, playerToken);
        if (emitter == null) return;

        try { emitter.send(SseEmitter.event().name(eventName).data(data)); }
        catch (IOException e)
        {
            notifyOpponentDisconnected(gameId, playerToken);
            remove(gameId, playerToken);
        }
    }

    private SseEmitter get(UUID gameId, UUID playerToken)
    {
        Map<UUID, SseEmitter> map = emitters.get(gameId);
        if (map == null) return null;

        return map.get(playerToken);
    }

    private void remove(UUID gameId, UUID playerToken)
    {
        Map<UUID, SseEmitter> map = emitters.get(gameId);
        if (map == null) return;

        map.remove(playerToken);
        if (map.isEmpty()) emitters.remove(gameId);
    }

    private void notifyOpponentDisconnected(UUID gameId, UUID disconnectedPlayerToken) {
        Game game = gameService.getGame(gameId);
        if (game == null || game.getPhase() == GamePhase.FINISHED) return;

        UUID opponentToken = disconnectedPlayerToken.equals(game.getOpponentToken())
                ? game.getHostToken()
                : game.getOpponentToken();

        sendToPlayer(gameId, opponentToken, "opponent-disconnected", "");
        
    }

    @Scheduled(fixedRate = 5_000)
    public void ping()
    {
       for (UUID gameId : emitters.keySet())
       {
           for (UUID playerToken : emitters.get(gameId).keySet())
           {
               sendToPlayer(gameId, playerToken, "ping", "");
           }
       }
    }
}
