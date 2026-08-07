import {joinGame} from "../../api/gameEntryApi.js";
import {GamePage} from "../game/GamePage.jsx";
import {useEffect, useState} from "react";
import {useNavigate} from "react-router";
import styles from "./JoinGamePage.module.css"

export const JoinGamePage = () => {
    const inviteCode = window.location.pathname.match("\/games\/(.{6})\/join")?.[1];
    const navigate = useNavigate();
    const [gameData, setGameData] = useState(null);
    const [error, setError] = useState(null);

    useEffect(() => {
        const parseGameData = async () => {
            try
            {
                const data = await joinGame(inviteCode);
                setGameData(data);
            }

            catch (error)
            {
                setError('We couldn’t find a game with this invite code.');
            }
        }

        parseGameData();
    }, [inviteCode]);

    const JoinStateCard = ({badge,
                           badgeClassName,
                           title,
                           text,
                           showLoader = false,
                           inviteCode,
                           backToMenu,
                                     }) => {
        return (
            <div className={styles.statePage}>
                <div className={styles.backgroundGlow} />

                <main className={styles.stateShell}>
                    <section className={styles.stateCard}>
                    <span className={`${styles.stateBadge} ${badgeClassName}`}>
                        {badge}
                    </span>

                        {showLoader && <div className={styles.loader} />}

                        <h1 className={styles.stateTitle}>{title}</h1>

                        <p className={styles.stateText}>{text}</p>

                        {inviteCode && (
                            <div className={styles.codeBox}>
                                <span className={styles.codeLabel}>Invite code</span>
                                <span className={styles.codeValue}>{inviteCode}</span>
                            </div>
                        )}

                        {backToMenu && (
                            <div className={styles.stateActions}>
                                {backToMenu}
                            </div>
                        )}
                    </section>
                </main>
            </div>
        );
    };

    if (error)
    {
        return (
            <JoinStateCard
                badge="Join failed"
                badgeClassName={styles.errorBadge}
                title="Unable to join game"
                text={error}
                backToMenu={
                    <button
                        type="button"
                        className={styles.primaryButton}
                        onClick={() => navigate("/")}
                    >
                        Back to menu
                    </button>
                }
            />
        );
    }

    if (!gameData)
    {
        return (
            <JoinStateCard
                badge="Connecting"
                badgeClassName={styles.loadingBadge}
                title="Joining game..."
                text="Please wait while we connect you to the lobby."
                showLoader={true}
                inviteCode={inviteCode}
            />
        );
    }

    return (
        <GamePage
            gameId={gameData.gameId}
            playerToken={gameData.playerToken}
            inviteCode={inviteCode}
            hostUsername={gameData.opponentUsername}
        />
    );



}