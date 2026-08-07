package gamestudio.server.dto;

import java.util.UUID;

public record MatchmakingResponse(UUID gameId, UUID playerToken, String opponentUsername, MatchmakingStatus status) { }
