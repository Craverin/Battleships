package gamestudio.dto;

import gamestudio.domain.Orientation;

public record MoveShipRequest(int row, int col, Orientation orientation) { }