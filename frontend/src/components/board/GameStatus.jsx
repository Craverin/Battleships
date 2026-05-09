import styles from "./Board.module.css";
import React, {useState} from "react";
import {setReady} from "../../api/gameApi.js";

export const GameStatus = ({opponentJoined,
                           opponentReady,
                           isReady,
                           setIsReady,
                           isYourTurn = false,
                           gameId,
                           playerToken}) => {
    const isBattlePhase = opponentReady && isReady;

    const getOpponentStatus = () => {
        if (opponentReady)
        {
            return {
                text: "Opponent ready",
                className: styles.opponentReadyPill
            };
        }

        if (opponentJoined)
        {
            return {
                text: "Opponent joined",
                className: styles.opponentJoinedPill
            };
        }

        return {
            text: "Waiting for opponent",
            className: styles.waitingPill
        };
    }

    const getBattleStatus = () => {
        if (isBattlePhase)
        {
            return {
                currentTurn: isYourTurn ? "Your turn" : "Opponent's turn",
                className: isYourTurn ? styles.yourTurnPill : styles.opponentTurnPill
            };
        }
    }

    const opponentStatus = getOpponentStatus();
    const battleStatus = getBattleStatus();

    return (
        <div className={styles.boardTopBar}>
            <span
                className={`
                     ${styles.topPill}
                     ${isBattlePhase ? styles.battlePhasePill : styles.setupPhasePill}`
                }
            >
                {isBattlePhase ? "Battle phase" : "Setup phase"}
            </span>

            <div className={styles.boardTopActions}>
                {!isBattlePhase && (
                    <span className={`${styles.topPill} ${opponentStatus.className}`}>
                        {opponentStatus.text}
                    </span>
                )}

                {!isBattlePhase && opponentJoined && (
                    <button
                        type="button"
                        className={styles.readyButton}
                        disabled={isReady}
                        onClick={async () => {
                            if (!gameId || !playerToken) return;

                            try
                            {
                                await setReady(gameId, playerToken);
                                setIsReady(true);
                            }
                            catch (err) { }
                        }}
                    >
                        {isReady ? "You are ready" : "I'm ready"}
                    </button>
                )}

                {isBattlePhase && (
                    <span className={`${styles.topPill} ${battleStatus.className}`}>
                        {battleStatus.currentTurn}
                    </span>
                )}
            </div>
        </div>
    )
}
