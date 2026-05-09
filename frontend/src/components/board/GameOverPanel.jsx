import styles from "./GameOverPanel.module.css"
import {useNavigate} from "react-router";

export const GameOverPanel = ({isWinner = false, startNewGame}) => {
    const navigate = useNavigate();
    const getLabelsInfo = () => {
        if (isWinner)
        {
            return {
                title: "Victory",
                titleClassName: styles.gameOverTitleVictory,
                text: "Enemy fleet destroyed."
            };
        }

        return {
            title: "Defeat",
            titleClassName: styles.gameOverTitleDefeat,
            text: "Your fleet has been sunk."
        }
    }
    const labelsInfo = getLabelsInfo();

    return (
        <div className={styles.gameOverOverlay}>
            <div className={styles.gameOverCard}>
                <span className={styles.gameOverLabel}>Game over</span>

                <h3 className={`${styles.gameOverTitle} ${labelsInfo.titleClassName}`}>
                    {labelsInfo.title}
                </h3>

                <p className={styles.gameOverText}>
                    {labelsInfo.text}
                </p>

                <div className="d-flex justify-content-center gap-2 mt-4">
                    <button
                        type="button"
                        className={`btn ${styles.gameOverPrimaryButton}`}
                        onClick={() => {
                            if (location.pathname === '/')
                            {
                                startNewGame();
                                return;
                            }

                            navigate('/', {
                                replace: true,
                                state: { autoCreateGame: true }
                            });
                        }}
                    >
                            New game
                    </button>
                </div>
            </div>
        </div>
    )
}