package gamestudio.controller;

import gamestudio.service.SseService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequestMapping("/games")

public class SseController
{
    private final SseService sseService;

    public SseController(SseService sseService)
    {
        this.sseService = sseService;
    }

    @GetMapping(path = "/{gameId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter events(@PathVariable UUID gameId, @RequestParam("token") UUID playerToken)
    {
        return sseService.subscribe(gameId, playerToken);
    }
}
