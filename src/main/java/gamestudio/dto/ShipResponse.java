package gamestudio.dto;

import gamestudio.domain.Orientation;

import java.util.UUID;

public record ShipResponse(UUID shipId, int row, int col, Orientation orientation, int length) { }
