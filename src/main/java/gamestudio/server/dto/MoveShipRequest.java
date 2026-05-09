package gamestudio.server.dto;

import gamestudio.server.domain.Orientation;

public record MoveShipRequest(int row, int col, Orientation orientation) { }