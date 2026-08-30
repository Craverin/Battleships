import styles from "./AccountPanel.module.css";

export const AccountPanel = ({user, opponentJoined, onLogin, onLogout, onSignUp, onShowSettings}) => {
    return (
        <div className={styles.accountBlock}>
            <p className={styles.blockLabel}>Account</p>

            {user && (
                <div
                    onClick={() => onShowSettings()}
                    className={styles.accountUserCard}
                >
                    <div className={styles.accountAvatar}>
                        {user.username.toUpperCase().charAt(0)}
                    </div>

                    <div className={styles.accountUserInfo}>
                        <span className={styles.accountUserLabel}>Signed in as</span>
                        <strong className={styles.accountUsername}>
                            {user.username}
                        </strong>
                    </div>

                    <button
                        type="button"
                        className={styles.accountLogoutButton}
                        onClick={event => {
                            event.stopPropagation();
                            onLogout();
                        }}
                    >
                        Logout
                    </button>
                </div>
            )}

            {!user && (
                <div className={styles.accountActions}>
                    <button
                        type="button"
                        disabled={opponentJoined}
                        className={`btn ${styles.accountButton}`}
                        onClick={() => onLogin()}
                    >
                        Log in
                    </button>

                    <button
                        type="button"
                        disabled={opponentJoined}
                        className={`btn ${styles.accountButton} ${styles.accountButtonPrimary}`}
                        onClick={() => onSignUp()}
                    >
                        Sign up
                    </button>
                </div>
            )}
        </div>
    )
}