import styles from "./GameStatus.module.css";
import React, {useState} from "react";
import {setReady} from "../../api/gameApi.js";

export const GameStatus = ({opponentJoined,
                           opponentReady,
                           opponentDisconnected,
                           isReady,
                           setIsReady,
                           isYourTurn = false,
                           gameId,
                           playerToken,
                           score,
                           opponentName}) => {
    console.log(`GAMESTATUS RECEIVED ${opponentName}`);
    const isBattlePhase = opponentReady && isReady;
    const getOpponentStatus = () => {
        if (opponentDisconnected)
        {
            return {
                text: `${opponentName} disconnected`,
                className: styles.disconnectedPill
            };
        }

        if (opponentReady)
        {
            return {
                text: `${opponentName} is ready`,
                className: styles.opponentReadyPill
            };
        }

        if (opponentJoined)
        {
            return {
                text: `Opponent found: ${opponentName}`,
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
                currentTurn: isYourTurn ? "Your turn" : `${opponentName}'s turn`,
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

            {isBattlePhase && (
                <div className={styles.scoreBadge}>
                    <span className={styles.scoreLabel}>Score</span>
                    <strong className={styles.scoreValue}>{score}</strong>
                </div>
            )}

            <div className={styles.boardTopActions}>
                {!isBattlePhase && (
                    <span className={`${styles.topPill} ${opponentStatus.className}`}>
                        {opponentStatus.text}
                    </span>
                )}

                {/*{!isBattlePhase && opponentDisconnected && (*/}
                {/*    <span className={`${styles.topPill} ${styles.disconnectedPill}`}>*/}
                {/*        Opponent disconnected*/}
                {/*    </span>*/}
                {/*)}*/}

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

                {isBattlePhase && opponentDisconnected && (
                    <span className={`${styles.topPill} ${styles.disconnectedPill}`}>
                        Opponent disconnected
                    </span>
                )}

                {isBattlePhase && !opponentDisconnected && (
                    <span className={`${styles.topPill} ${battleStatus.className}`}>
                        {battleStatus.currentTurn}
                    </span>
                )}
            </div>
        </div>
    )
}
