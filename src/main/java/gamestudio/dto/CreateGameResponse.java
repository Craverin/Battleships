package gamestudio.dto;

import java.util.UUID;

public record CreateGameResponse(UUID gameId, UUID hostToken) { }