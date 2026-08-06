import styles from "./MatchmakingPanel.module.css";

const STATUS_LABELS = {
    IDLE: {
        title: "Ready for battle?",
        description: "We'll match you with the first available opponent.",
        hint: "No invite code needed.",
        button: "Find opponent"
    },

    REQUESTING: {
        title: "Starting matchmaking",
        description: "Creating your public game.",
        hint: "Please wait a moment.",
        button: "Starting search..."
    },

    SEARCHING: {
        title: "Searching for an opponent",
        description: "First available player will join your game.",
        hint: "You can arrange your fleet while you wait.",
        button: "Cancel search"
    },

    CANCELLING: {
        title: "Cancelling search",
        description: "Removing your game from the matchmaking queue.",
        hint: "Please wait a moment.",
        button: "Cancelling..."
    },

    MATCHED: {
        title: "Opponent connected",
        description: "Your match is now active.",
        hint: "Good luck, Captain.",
        button: "Match active"
    },

    ERROR: {
        title: "Matchmaking unavailable",
        description: "We couldn't start the search.",
        hint: "Check your connection and try again.",
        button: "Try again"
    }
};

export const MatchmakingPanel = ({status = "ERROR", onFind, onCancel}) => {
    const isSonarActive = status === "REQUESTING" || status === "SEARCHING" || status === "CANCELLING";
    const isSearchButtonEnabled = status === "IDLE" || status === "SEARCHING" || status === "ERROR";

    const isMatched = status === "MATCHED";
    const isError = status === "ERROR";

    const label = STATUS_LABELS[status];

    const getButtonClassName = () => {
        const baseClassName = styles.actionButton;

        if (status === "MATCHED")
            return `${baseClassName} ${styles.matchFoundButton}`;

        if (status === "SEARCHING" || status === "CANCELLING")
            return `${baseClassName} ${styles.cancelSearchButton}`;

        return `${baseClassName} ${styles.findGameButton}`;
    }

    return (
        <div className={styles.matchmakingPanel}>
            <div className={styles.matchmakingHeader}>
                <p className={styles.blockLabel}>
                    Matchmaking
                </p>

                <span className={styles.publicGameBadge}>
                    <span
                        className={styles.publicGameDot}
                        aria-hidden="true"
                    />
                    Public game
                </span>
            </div>

            <div
                className={`
                    ${styles.statusCard}
                    ${isMatched ? styles.statusCardMatched : ""}
                    ${isError ? styles.statusCardError : ""}
                `}
                role="status"
                aria-live="polite"
            >
                <div
                    className={`
                        ${styles.sonar}
                        ${isSonarActive ? styles.sonarActive : ""}
                        ${isMatched ? styles.sonarMatched : ""}
                    `}
                    aria-hidden="true"
                >
                    <span className={styles.sonarSweep} />
                    <span className={styles.sonarRings} />
                    <span className={styles.sonarBlip} />
                    <span className={styles.sonarCenter} />
                    <span className={styles.matchFoundMark}>✓</span>
                </div>

                <div className={styles.statusCopy}>
                    <strong className={styles.statusTitle}>
                        {label.title}
                    </strong>

                    <span className={styles.statusDescription}>
                        {label.description}
                    </span>
                </div>
            </div>

            <p className={styles.statusHint}>
                {label.hint}
            </p>

            <button
                type="button"
                className={getButtonClassName()}
                onClick={() => {
                    if (status === "SEARCHING")
                        onCancel();

                    if (status === "IDLE" || status === "ERROR")
                        onFind();
                }}
                disabled={!isSearchButtonEnabled}
            >
                {label.button}
            </button>
        </div>
    );
};