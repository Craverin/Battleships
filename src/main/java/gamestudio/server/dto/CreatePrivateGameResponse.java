package gamestudio.server.dto;

import java.util.UUID;

public record CreatePrivateGameResponse(UUID gameId, UUID hostToken, String inviteCode) { }