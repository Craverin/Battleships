package gamestudio.server.dto;

import gamestudio.server.domain.Orientation;

import java.util.UUID;

public record ShipResponse(UUID shipId, int row, int col, Orientation orientation, int length) { }
