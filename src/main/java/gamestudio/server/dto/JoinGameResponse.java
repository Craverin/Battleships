package gamestudio.server.dto;

import java.util.UUID;

public record JoinGameResponse(UUID gameId, UUID playerToken, String opponentUsername) { }
