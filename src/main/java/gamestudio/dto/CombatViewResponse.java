package gamestudio.dto;

import gamestudio.domain.CellState;
import gamestudio.domain.GamePhase;

public record CombatViewResponse(GamePhase phase,
                                 ShotResult shotResult,
                                 int score,
                                 CellState[][] hostBoard,
                                 CellStateView[][] opponentBoard) { }