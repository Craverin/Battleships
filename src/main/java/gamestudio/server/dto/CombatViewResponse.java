package gamestudio.server.dto;

import gamestudio.server.domain.CellState;
import gamestudio.server.domain.GamePhase;

public record CombatViewResponse(GamePhase phase,
                                 ShotResult shotResult,
                                 boolean yourTurn,
                                 int score,
                                 CellState[][] hostBoard,
                                 CellStateView[][] opponentBoard) { }
