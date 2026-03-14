package gamestudio.service;

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

    public SseEmitter subscribe(UUID gameId, UUID playerToken)
    {
        SseEmitter emitter = new SseEmitter(0L);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));

        emitters.computeIfAbsent(gameId, m -> new ConcurrentHashMap<>()).put(playerToken, emitter);

        sendToPlayer(gameId, playerToken, "hello_msg", "hello");

        return emitter;
    }

    public void sendToPlayer(UUID gameId, UUID playerToken, String eventName, Object data)
    {
        SseEmitter emitter = get(gameId, playerToken);
        if (emitter == null) return;

        try { emitter.send(SseEmitter.event().name(eventName).data(data)); }
        catch (IOException e) { remove(gameId, playerToken); }
    }

    private SseEmitter get(UUID gameId, UUID playerToken)
    {
        Map<UUID, SseEmitter> map = emitters.get(gameId);
        if (map == null) return null;

        for (var entry : map.entrySet())
        {
            if (entry.getKey().equals(playerToken)) return entry.getValue();
        }

        return null;
    }

    private void remove(UUID gameId, UUID playerToken)
    {
        Map<UUID, SseEmitter> map = emitters.get(gameId);
        if (map == null) return;

        map.remove(playerToken);
        if (map.isEmpty()) emitters.remove(gameId);
    }
}
