import mainStyles from "./Board.module.css";
import styles from "./PlaceholderBoard.module.css";
import React from "react";
import {BOARD_SIZE} from "../../pages/GamePage.jsx";
import {getCellLabel} from "./PlacementBoard.jsx";


export const PlaceholderBoard = ({opponentJoined, opponentReady, opponentMode, status, isReady, isGameCreated}) => {
    const gridSize = BOARD_SIZE + 1;
    const getMessage = () => {
        if (opponentMode === 'RANDOM')
        {
            if (status === 'IDLE') return 'Find an opponent to start a game.';
            if (status === 'SEARCHING') return 'Searching for an opponent...';
            if (status === 'MATCHED')
            {
                if (!isReady) return 'Place your ships and press \"I\'m ready\" to start the battle.'
                return 'Waiting for opponent...';
            }
        }

        if (opponentMode === 'FRIEND')
        {
            if (!isGameCreated)
                return 'Create a private game to invite a friend.';

            if (!opponentJoined)
                return 'Share the invite link or game code.';

            if (!isReady)
                return "Place your ships and press \"I'm ready\" to start the battle.";

            if (!opponentReady)
                return "Waiting for opponent...";
        }
    }

    return (
        <div className={`${styles.placeholderBoardCard}`}>
            <div className={styles.placeholderBoardInner}>
                <div className={mainStyles.board}>
                        <div className={mainStyles.grid}>
                            {Array.from({ length: gridSize * gridSize }).map((_, index) => {
                                const row = Math.floor(index / gridSize);
                                const col = index % gridSize;
                                const isMarkup = row === 0 || col === 0;
                                const isCorner = row === 0 && col === 0;

                                return (
                                    <div
                                        key={`${row}-${col}`}
                                        className={
                                            [
                                                mainStyles.cell,
                                                isMarkup ? mainStyles.markupCell : mainStyles.playCell,
                                                isCorner ? mainStyles.cornerCell : ""
                                            ]
                                                .filter(Boolean)
                                                .join(" ")}
                                    >
                                        {getCellLabel(row, col)}
                                    </div>
                                    );
                                })}
                        </div>
                </div>
            </div>
            <div className={styles.placeholderOverlay}>
                <div className={styles.placeholderMessage}>
                    <p className={styles.placeholderText}>{getMessage()}</p>
                </div>
            </div>
        </div>
    );
}