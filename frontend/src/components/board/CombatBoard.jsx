import React, {useState} from "react";
import boardStyles from "./Board.module.css";
import styles from "./CombatBoard.module.css";
import {BOARD_SIZE} from "../../pages/game/GamePage.jsx";
import {shoot} from "../../api/gameApi.js";

const getCellMarker = (cellState) => {
    if (cellState === "BLOCKED") return "×";
    if (cellState === "MISS") return "•";
    if (cellState === "HIT") return "✕";
    if (cellState === "SUNK") return "✦";

    return "";
}

const isOccupied = (row, col, cells, isHost) => {
    if (row === 0 || col === 0) return false;

    return isHost && cells[row - 1][col - 1] === "OCCUPIED" ||
           !isHost && cells[row - 1][col - 1] === "SHIP";
}

const getCellState = (row, col, cells) => {
    if (!cells) return;
    if (row === 0 || col === 0) return;

    return cells[row - 1][col - 1].toLowerCase();
}

const getCellLabel = (row, col, cells) => {
    if (row === 0 && col === 0) return "";
    if (row === 0) return String.fromCharCode('A'.charCodeAt(0) + (col - 1));
    if (col === 0) return row;

    if (!cells) return "";
    return getCellMarker(cells[row - 1][col - 1]);
};

export const CombatBoard = ({gameId, playerToken, cells, isHost = true}) => {
    const gridSize = BOARD_SIZE + 1;

    return (
        <div className={`card border-0 shadow-lg ${boardStyles.boardCard}`}>
            <div className="card-body p-3 p-md-4">
                <div className={boardStyles.boardViewport}>
                    <div
                        className={boardStyles.board}
                    >
                        <div className={boardStyles.grid}>
                            {Array.from({ length: gridSize * gridSize }).map((_, index) => {
                                const row = Math.floor(index / gridSize);
                                const col = index % gridSize;
                                const isMarkup = row === 0 || col === 0;
                                const isCorner = row === 0 && col === 0;

                                return (
                                    <button
                                        key={`${row}-${col}`}
                                        onClick={async () => {
                                            if (!isHost)
                                                await shoot(gameId, playerToken, {row: row - 1, col: col - 1});
                                        }}
                                        className={
                                            [
                                                styles.combatCell,
                                                isMarkup ? boardStyles.markupCell : boardStyles.cell,
                                                isCorner ? boardStyles.cornerCell : ""
                                            ]
                                                .filter(Boolean)
                                                .join(" ")
                                        }
                                        data-has-ship={isOccupied(row, col, cells, isHost)}
                                        data-state={getCellState(row, col, cells)}
                                        disabled={isHost}

                                    >
                                        <span className={(row === 0 || col === 0) ? "" : styles.combatCellMark}>
                                            {getCellLabel(row, col, cells)}
                                        </span>
                                    </button>
                                );
                            })}
                        </div>

                    </div>
                </div>
            </div>
        </div>
    );
}