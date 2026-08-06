package gamestudio.server.dto;

import java.util.UUID;

public record CreateGameResponse(UUID gameId, UUID playerToken) { }
