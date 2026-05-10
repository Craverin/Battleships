import {PlacementBoard} from "../components/board/PlacementBoard.jsx";
import {GameStatus} from "../components/board/GameStatus.jsx";
import {createGame, getShips} from "../api/gameApi.js";
import {useEffect, useState} from "react";
import styles from "./GamePage.module.css"
import {subscribeToSse} from "../api/sseApi.js";
import {useLocation, useNavigate} from "react-router";
import {CombatBoard} from "../components/board/CombatBoard.jsx";
import {PlaceholderBoard} from "../components/board/PlaceholderBoard.jsx";
import {GameOverPanel} from "../components/board/GameOverPanel.jsx";
import {LeaderboardPanel} from "../components/community/LeaderboardPanel.jsx";
import {getPlayerStats, getTopPlayers} from "../api/leaderboardApi.js";
import {ReviewsPanel} from "../components/community/ReviewsPanel.jsx";
import {getComments, getPlayerComments, getRating, getRatingSummary} from "../api/reviewsApi.js";
import {getCurrentUser} from "../api/authApi.js";
import {AuthPanel} from "../components/authentication/AuthPanel.jsx";

export const BOARD_SIZE = 10;

export const GamePage = ({
                        gameId: gameUUID,
                        playerToken: token,
                        inviteCode: invCode}) => {
    const [isHost, setIsHost] = useState(!invCode);

    const location = useLocation();
    const navigate = useNavigate();

    const [isYourTurn, setIsYourTurn] = useState();
    const [isReady, setIsReady] = useState(false);
    const [opponentJoined, setOpponentJoined] = useState(!isHost);
    const [opponentReady, setOpponentReady] = useState(false);
    const [friendButtonActive, setFriendButtonActive] = useState(true);
    const [joinCode, setJoinCode] = useState("");

    const [gameId, setGameId] = useState(gameUUID);
    const [gamePhase, setGamePhase] = useState('');
    const [playerToken, setPlayerToken] = useState(token);
    const [inviteCode, setInviteCode] = useState(invCode);
    const [isInviteCopied, setIsInviteCopied] = useState(false);
    const [ships, setShips] = useState([]);

    const [hostCells, setHostCells] = useState();
    const [opponentCells, setOpponentCells] = useState();
    const [score, setScore] = useState(0);
    const [isWinner, setIsWinner] = useState();

    const [activeTab, setActiveTab] = useState("Play");

    const [leaderboard, setLeaderboard] = useState();
    const [playerStats, setPlayerStats] = useState();

    const [playerComments, setPlayerComments] = useState();
    const [comments, setComments] = useState();
    const [playerRating, setPlayerRating] = useState();
    const [ratingSummary, setRatingSummary] = useState();

    const [user, setUser] = useState();

    const inviteLink = inviteCode ? `${window.location.origin}/games/${inviteCode}/join` : "";

    const initializeGame = async () => {
        const {gameId: id, hostToken: token, inviteCode: invCode} = await createGame();

        setGamePhase('PLACEMENT');
        setGameId(id);
        setPlayerToken(token);
        setInviteCode(invCode);
    }

    const startNewGame = async () => {
        setShips([]);
        setGamePhase(undefined);
        setIsWinner(undefined);
        setIsYourTurn(undefined);
        setIsReady(false);
        setOpponentJoined(false);
        setOpponentReady(false);
        setHostCells(undefined);
        setOpponentCells(undefined);

        await initializeGame();
    };

    useEffect(() => {
        if (location.state?.autoCreateGame)
        {
            initializeGame();
            navigate('/', {
                replace: true,
                state: null
            });
        }

        const tryGetCurrentUser = async () => {
            try {
                const usr = await getCurrentUser();
                console.log(usr);
                setUser(usr);
            } catch (error) { }
        }
        tryGetCurrentUser()
    }, []);

    const copyInviteLink = async () => {
        if (!inviteLink) return;

        await navigator.clipboard.writeText(inviteLink);

        setIsInviteCopied(true);

        setTimeout(() => {
            setIsInviteCopied(false);
        }, 1400);
    };

    useEffect(  () => {
        if (!gameId || !playerToken) return;

        const sseHandler = subscribeToSse(gameId, playerToken);
        sseHandler.addEventListener("opponent-joined", () => setOpponentJoined(true));
        sseHandler.addEventListener("opponent-ready", () => setOpponentReady(true));

        sseHandler.addEventListener("battle-started", event => {
            const data = JSON.parse(event.data);

            console.log(data);
            setGamePhase(data.phase)
            setHostCells(data.hostBoard);
            setOpponentCells(data.opponentBoard);
            setIsYourTurn(data.yourTurn);
        });

        sseHandler.addEventListener("opponent-shoot", event => {
            const data = JSON.parse(event.data);

            console.log(data);
            setGamePhase(data.phase);
            setHostCells(data.hostBoard);
            setOpponentCells(data.opponentBoard);
            setIsYourTurn(data.yourTurn);
        });

        sseHandler.addEventListener("game-over", event => {
            const data = JSON.parse(event.data);

            console.log(data);
            setGamePhase("FINISHED");
            setIsWinner(data.isWinner);
            setScore(data.score);
        });

        return () => sseHandler.close();
    },[gameId, playerToken]);

    useEffect(() => {
        if (!gameId || !playerToken) return;
        const getBoardShips = async () => {
            const ships = await getShips(gameId, playerToken);
            setShips(ships);
        }

        getBoardShips();
    }, [gameId, playerToken]);



    return (
        <main className={styles.gamePage}>
            <section className={styles.gameShell}>
                <aside className={styles.gameMenu}>
                    <div className={styles.menuHeader}>
                        <p className={styles.eyebrow}>Battleship</p>
                    </div>
                    <div className="d-flex gap-2">
                        <button
                            type="button"
                            className={`
                                btn
                                ${styles.menuTabButton}
                                ${activeTab === "Play" ? styles.menuTabButtonActive : ""}
                           `}
                            onClick={() => setActiveTab("Play")}
                        >
                            Play
                        </button>
                        <button
                            type="button"
                            className={`
                                btn
                                ${styles.menuTabButton}
                                ${activeTab === "Top" ? styles.menuTabButtonActive : ""}
                           `}
                            onClick={async () => {
                                const leaderboard = await getTopPlayers({sortBy: "winRatio"});
                                const playerStats = await getPlayerStats("ThatsIt")

                                console.log(playerStats);
                                setLeaderboard(leaderboard);
                                setPlayerStats(playerStats);
                                setActiveTab("Top");
                            }}
                        >
                            Top
                        </button>
                        <button
                            type="button"
                            className={`
                                btn
                                ${styles.menuTabButton}
                                ${activeTab === "Reviews" ? styles.menuTabButtonActive : ""}
                           `}
                            onClick={async () => {
                                const playerComments = await getPlayerComments("NavalAce_054");
                                const comments = await getComments();
                                const playerRating = await getRating("NavalAce_054");
                                const ratingSummary = await getRatingSummary();

                                setPlayerComments(playerComments);
                                setComments(comments);
                                setPlayerRating(playerRating);
                                setRatingSummary(ratingSummary);
                                setActiveTab("Reviews")
                            }}
                        >
                            Reviews
                        </button>
                    </div>

                    <div className={styles.accountBlock}>
                        <p className={styles.blockLabel}>Account</p>

                        <div className={styles.accountActions}>
                            <button
                                type="button"
                                className={`btn ${styles.accountButton}`}
                                onClick={() => setActiveTab("Login")}
                            >
                                Log in
                            </button>

                            <button
                                type="button"
                                className={`btn ${styles.accountButton} ${styles.accountButtonPrimary}`}
                                onClick={() => setActiveTab("Register")}
                            >
                                Sign up
                            </button>
                        </div>
                    </div>

                    <div className={styles.modeBlock}>
                        <p className={styles.blockLabel}>Opponent</p>
                        <div className={styles.modeGrid}>
                            <button
                                type="button"
                                onClick={() => setFriendButtonActive(!friendButtonActive)}
                                className={`
                                    ${styles.modeCard}
                                    ${friendButtonActive ? styles.modeCardActive : ""}
                                `}
                            >
                                <span className={styles.modeTitle}>Friend</span>
                                <span className={styles.modeDescription}>Invite another player</span>
                            </button>

                            <button
                                type="button"
                                disabled={!isHost || opponentJoined}
                                onClick={() => setFriendButtonActive(!friendButtonActive)}
                                className={`
                                    ${styles.modeCard}
                                    ${friendButtonActive ? "" : styles.modeCardActive}
                                `}
                            >
                                <span className={styles.modeTitle}>Bot</span>
                                <span className={styles.modeDescription}>Practice alone</span>
                            </button>
                        </div>
                    </div>

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
                    </div>

                    <div className={styles.menuActions}>
                        <button
                            type="button"
                            className={styles.primaryButton}
                            onClick={() => initializeGame()}
                            disabled={opponentJoined || !isHost}
                        >
                            Create game
                        </button>

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
                </aside>


                <section
                    className={`
                        ${styles.boardCard}
                        ${activeTab === 'Login' || activeTab === 'Register' ?  styles.boardCardAuth : ""}
                        `}
                    >
                        {activeTab === 'Top' &&
                            <LeaderboardPanel leaderboard={leaderboard} playerStats={playerStats}/>
                        }

                        {activeTab === 'Play' && (
                            <div>
                                <GameStatus opponentJoined={opponentJoined}
                                            opponentReady={opponentReady}
                                            isReady={isReady}
                                            setIsReady={setIsReady}
                                            isYourTurn={isYourTurn}
                                            gameId={gameId}
                                            playerToken={playerToken}
                                />

                                {(gamePhase === '' || gamePhase === "PLACEMENT") && (
                                    <div className={styles.boardsRow}>
                                        <PlacementBoard gameId={gameId} playerToken={playerToken} ships={ships} isLocked={isReady} />
                                        <PlaceholderBoard opponentJoined={opponentJoined} opponentReady={opponentReady} isReady={isReady} />
                                    </div>
                                )}

                                {(gamePhase === "COMBAT" || gamePhase === "FINISHED") && (
                                    <div className={styles.boardsViewport}>
                                        <div className={styles.boardsRow}>
                                            <CombatBoard gameId={gameId} playerToken={playerToken} cells={hostCells} isHost={true}/>
                                            <CombatBoard gameId={gameId} playerToken={playerToken} cells={opponentCells} isHost={false}/>
                                        </div>
                                    </div>
                                )}

                                {gamePhase === "FINISHED" &&
                                    <GameOverPanel isWinner={isWinner} startNewGame={startNewGame} />
                                }
                            </div>
                        )}

                        {activeTab === 'Reviews' && (
                            <ReviewsPanel
                                playerComments={playerComments}
                                comments={comments}
                                playerRating={playerRating}
                                ratingSummary={ratingSummary}
                            />
                        )}

                        {activeTab === 'Login' &&
                            <AuthPanel
                                signingUp={false}
                                setUser={setUser}
                                onAuthSuccess={() => setActiveTab("Play")}
                            />
                        }

                        {activeTab === 'Register' &&
                            <AuthPanel
                                signingUp={true}
                                setUser={setUser}
                                onAuthSuccess={() => setActiveTab("Play")}
                            />
                        }
                </section>
            </section>
        </main>
    )
}


