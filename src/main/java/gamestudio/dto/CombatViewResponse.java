package gamestudio.dto;

import gamestudio.domain.GamePhase;

public record CombatViewResponse(GamePhase phase,
                                 ShotResult shotResult,
                                 int score,
                                 CellStateView[][] hostBoard,
                                 CellStateView[][] opponentBoard) { }