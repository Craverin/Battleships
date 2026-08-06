package gamestudio.server.dto;

import java.util.UUID;

public record MatchmakingResponse(UUID gameId, UUID playerToken, MatchmakingStatus status) { }
