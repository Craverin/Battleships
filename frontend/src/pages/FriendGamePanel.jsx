import styles from "./GamePage.module.css"
import {useState} from "react";

export const FriendGamePanel = ({inviteCode, opponentJoined, isHost, initializeGame}) => {
    const [joinCode, setJoinCode] = useState("");
    const [isInviteCopied, setIsInviteCopied] = useState(false);
    const inviteLink = inviteCode ? `${window.location.origin}/games/${inviteCode}/join` : "";

    const copyInviteLink = async () => {
        if (!inviteLink) return;

        await navigator.clipboard.writeText(inviteLink);

        setIsInviteCopied(true);

        setTimeout(() => {
            setIsInviteCopied(false);
        }, 1400);
    };

    return (
        <div className={styles.inviteBlock}>
            <div className={styles.inviteHeader}>
                <p className={styles.blockLabel}>Invite</p>
                <span className={styles.inviteBadge}>Private game</span>
            </div>

            <div className={styles.inviteLinkBox}>
                <span className={styles.inviteLinkText}>
                {
                    !inviteCode
                    ? `Create game to generate invite link`
                    : inviteLink
                }
                </span>

                <button
                    type="button"
                    className={`
                        ${styles.copyButton}
                        ${isInviteCopied ? styles.copyButtonCopied : ""}
                    `}
                    onClick={copyInviteLink}
                    disabled={!inviteCode}
                >
                    {isInviteCopied ? "✓ Copied" : "Copy"}
                </button>
            </div>

            <div className={styles.codeBox}>
                <span className={styles.codeLabel}>Game code</span>
                <strong className={styles.codeValue}>{inviteCode}</strong>
            </div>

            <p className={styles.hintText}>
                Share the link or send the code to your opponent.
            </p>

            <div className={styles.menuActions}>
                <button
                    type="button"
                    className={styles.primaryButton}
                    onClick={() => initializeGame()}
                    disabled={opponentJoined || !isHost}
                >
                    Create game
                </button>
            </div>

            <div className={styles.menuActions}>
                <div className={styles.joinBlock}>
                    <span className={styles.codeLabel}>Join game</span>

                    <input
                        type="text"
                        value={joinCode}
                        maxLength={6}
                        onChange={(event) => setJoinCode(event.target.value)}
                        placeholder="Enter invite code"
                        className={styles.joinCodeInput}
                    />

                    <button
                        type="button"
                        className={styles.secondaryButton}
                        disabled={joinCode.trim().length !== 6}
                        onClick={() => navigate(`/games/${joinCode.trim()}/join`)}
                    >
                        Join by code
                    </button>
                </div>
            </div>
        </div>
    )
}