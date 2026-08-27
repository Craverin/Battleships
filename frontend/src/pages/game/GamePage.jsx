import {PlacementBoard} from "../../components/board/PlacementBoard.jsx";
import {GameStatus} from "../../components/board/GameStatus.jsx";
import {createGame} from "../../api/gameEntryApi.js";
import {getShips} from "../../api/gameApi.js";
import {useEffect, useState} from "react";
import styles from "./GamePage.module.css"
import {subscribeToSse} from "../../api/sseApi.js";
import {useLocation, useNavigate} from "react-router";
import {CombatBoard} from "../../components/board/CombatBoard.jsx";
import {PlaceholderBoard} from "../../components/board/PlaceholderBoard.jsx";
import {GameOverPanel} from "../../components/board/GameOverPanel.jsx";
import {LeaderboardPanel} from "../../components/community/LeaderboardPanel.jsx";
import {getMyStats, getTopPlayers} from "../../api/leaderboardApi.js";
import {ReviewsPanel} from "../../components/community/ReviewsPanel.jsx";
import {getComments, getMyComments, getMyRating, getRatingSummary} from "../../api/reviewsApi.js";
import {getCurrentUser, logout} from "../../api/authApi.js";
import {AuthPanel} from "../../components/authentication/AuthPanel.jsx";
import {FriendGamePanel} from "./components/FriendGamePanel.jsx";
import {MatchmakingPanel} from "./components/MatchmakingPanel.jsx";
import {cancelSearch, findGame} from "../../api/matchmakingApi.js";
import {AccountPanel} from "./components/AccountPanel.jsx";
import {AccountSettingsPanel} from "./components/AccountSettingsPanel.jsx";

export const BOARD_SIZE = 10;

export const GamePage = ({
                        gameId: gameUUID,
                        playerToken: token,
                        inviteCode: invCode}) => {
    const location = useLocation();
    const navigate = useNavigate();

    const [isHost, setIsHost] = useState(!invCode);
    const [isYourTurn, setIsYourTurn] = useState();
    const [isReady, setIsReady] = useState(false);
    const [opponentJoined, setOpponentJoined] = useState(invCode);
    const [opponentReady, setOpponentReady] = useState(false);
    const [opponentDisconnected, setOpponentDisconnected] = useState(false);
    const [opponentMode, setOpponentMode] = useState(invCode ? "FRIEND" : "RANDOM");
    const [matchmakingStatus, setMatchmakingStatus] = useState('IDLE');

    const [gameId, setGameId] = useState(gameUUID);
    const [gamePhase, setGamePhase] = useState('');
    const [playerToken, setPlayerToken] = useState(token);
    const [inviteCode, setInviteCode] = useState(invCode);
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
    const [hostUsername, setHostUsername] = useState("Guest");
    const [opponentUsername, setOpponentUsername] = useState("Guest");

    const initializeGame = async () => {
        const {gameId: id, hostToken: token, inviteCode: invCode} = await createGame();

        setGamePhase('PLACEMENT');
        setGameId(id);
        setPlayerToken(token);
        setInviteCode(invCode);
        setOpponentDisconnected(false);
    }

    const startNewGame = async () => {
        setShips([]);
        setGamePhase(undefined);
        setIsWinner(undefined);
        setIsYourTurn(undefined);
        setIsReady(false);
        setMatchmakingStatus('IDLE');
        setOpponentJoined(false);
        setOpponentReady(false);
        setOpponentDisconnected(false);
        setHostCells(undefined);
        setOpponentCells(undefined);

        await initializeGame();
    };

    const handleFindGame = async () => {
        setActiveTab('Play');
        setMatchmakingStatus('SEARCHING');
        const {gameId: id, playerToken: token, opponentUsername, status} = await findGame();

        if (status === 'MATCHED')
        {
            setGameId(id);
            setPlayerToken(token);

            setIsHost(false);
            setHostUsername(opponentUsername);
            setOpponentJoined(true);
            setMatchmakingStatus('MATCHED');

            return;
        }

        setIsHost(true);
        setGameId(id);
        setPlayerToken(token);
        setMatchmakingStatus(status);
    }

    const handleCancelSearch = async () => {
        setActiveTab('Play');
        setMatchmakingStatus('CANCELLING');
        await cancelSearch(gameId, playerToken);

        setMatchmakingStatus('IDLE');
        setGameId(undefined);
        setPlayerToken(undefined);
        setShips([]);
        setGamePhase('');
    }

    const handleLogout = async () => {
        await logout();
        setUser(undefined);
        setActiveTab("Play");
    }

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

    useEffect(  () => {
        if (!gameId || !playerToken) return;

        const sseHandler = subscribeToSse(gameId, playerToken);
        sseHandler.addEventListener("opponent-joined", event => {
            setOpponentJoined(true);
            setOpponentUsername(event.data);

            if (opponentMode === 'RANDOM') setMatchmakingStatus('MATCHED');
        });
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
            setScore(data.score);
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

        sseHandler.addEventListener("opponent-disconnected", () => {
            setOpponentDisconnected(true);
        });

        return () => sseHandler.close();
    },[gameId, playerToken]);

    useEffect(() => {
        if (!gameId || !playerToken) return;
        const getBoardShips = async () => {
            const ships = await getShips(gameId, playerToken);
            console.log(ships);
            setShips(ships);
        }

        getBoardShips();
    }, [gameId, playerToken]);


    const getSectionClassName = () => {
        const className = styles.boardCard;

        if (activeTab === 'Settings')
            return className.concat(" m-0 ", styles.boardCardCentered);

        if (activeTab === 'Login' || activeTab === 'Register')
            return className.concat(" ", styles.boardCardCentered);

        if (activeTab === 'Top' || activeTab === 'Reviews')
            return className.concat(" m-0");

        return className;
    }

    return (
        <main className={styles.gamePage}>
            <section className={styles.gameShell}>
                <aside className={styles.gameMenu}>
                    <div className={styles.menuHeader}>
                        <p className={styles.eyebrow}>Battleship</p>
                    </div>
                    <div className="d-flex gap-2 flex-wrap">
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
                            disabled={opponentJoined}
                            className={`
                                btn
                                ${styles.menuTabButton}
                                ${activeTab === "Top" ? styles.menuTabButtonActive : ""}
                           `}
                            onClick={async () => {
                                const leaderboard = await getTopPlayers({});
                                if (user)
                                {
                                    const playerStats = await getMyStats();
                                    console.log(playerStats);
                                    setPlayerStats(playerStats);
                                }

                                setLeaderboard(leaderboard);
                                setActiveTab("Top");
                            }}
                        >
                            Top
                        </button>
                        <button
                            type="button"
                            disabled={opponentJoined}
                            className={`
                                btn
                                ${styles.menuTabButton}
                                ${activeTab === "Reviews" ? styles.menuTabButtonActive : ""}
                           `}
                            onClick={async () => {
                                if (user)
                                {
                                    const playerComments = await getMyComments();
                                    const playerRating = await getMyRating();
                                    setPlayerComments(playerComments);
                                    setPlayerRating(playerRating);
                                }

                                const comments = await getComments();
                                const ratingSummary = await getRatingSummary();

                                setComments(comments);
                                setRatingSummary(ratingSummary);
                                setActiveTab("Reviews")
                            }}
                        >
                            Reviews
                        </button>
                    </div>

                    <AccountPanel
                        user={user}
                        opponentJoined={opponentJoined}
                        onLogin={() => setActiveTab('Login')}
                        onLogout={handleLogout}
                        onSignUp={() => setActiveTab('Register')}
                        onShowSettings={() => setActiveTab('Settings')}
                    />

                    <div className={styles.modeBlock}>
                        <p className={styles.blockLabel}>Opponent</p>
                        <div className={styles.modeGrid}>
                            <button
                                type="button"
                                disabled={invCode || opponentJoined}
                                onClick={() => setOpponentMode("RANDOM")}
                                className={`
                                    ${styles.modeCard}
                                    ${opponentMode === "RANDOM" ? styles.modeCardActive : ""}
                                `}
                            >
                                <span className={styles.modeTitle}>Random</span>
                                <span className={styles.modeDescription}>Play with other people</span>
                            </button>

                            <button
                                type="button"
                                disabled={invCode || opponentJoined}
                                onClick={() => setOpponentMode("FRIEND")}
                                className={`
                                    ${styles.modeCard}
                                    ${opponentMode === "FRIEND" ? styles.modeCardActive : ""}
                                `}
                            >
                                <span className={styles.modeTitle}>Friend</span>
                                <span className={styles.modeDescription}>Invite another player</span>
                            </button>
                        </div>
                    </div>

                    {opponentMode === "RANDOM"
                        ? <MatchmakingPanel
                            status={matchmakingStatus}
                            onFind={handleFindGame}
                            onCancel={handleCancelSearch}
                           />
                        : <FriendGamePanel
                            inviteCode={inviteCode}
                            opponentJoined={opponentJoined}
                            isHost={isHost}
                            initializeGame={initializeGame}
                          />
                    }
                </aside>


                <section
                    className={getSectionClassName()}
                >
                        {activeTab === 'Top' &&
                            <LeaderboardPanel leaderboard={leaderboard} playerStats={playerStats}/>
                        }

                        {activeTab === 'Play' && (
                            <div>
                                <GameStatus opponentJoined={opponentJoined}
                                            opponentReady={opponentReady}
                                            opponentDisconnected={opponentDisconnected}
                                            isReady={isReady}
                                            setIsReady={setIsReady}
                                            isYourTurn={isYourTurn}
                                            gameId={gameId}
                                            playerToken={playerToken}
                                            score={score}
                                            opponentName={isHost ? opponentUsername : hostUsername}
                                />

                                {(gamePhase === '' || gamePhase === "PLACEMENT") && (
                                    <div className={styles.boardsRow}>
                                        <PlacementBoard gameId={gameId} playerToken={playerToken} ships={ships} isLocked={isReady} />
                                        <PlaceholderBoard
                                            opponentJoined={opponentJoined}
                                            opponentReady={opponentReady}
                                            opponentMode={opponentMode}
                                            status={matchmakingStatus}
                                            isReady={isReady}
                                            isGameCreated={!!gameId}
                                        />
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

                                {(gamePhase === "FINISHED" || opponentDisconnected) &&
                                    <GameOverPanel isWinner={isWinner} opponentDisconnected={opponentDisconnected} startNewGame={startNewGame} />
                                }
                            </div>
                        )}

                        {activeTab === 'Reviews' && (
                            <ReviewsPanel
                                playerComments={playerComments ?? []}
                                comments={comments ?? []}
                                playerRating={playerRating ?? -1}
                                ratingSummary={ratingSummary}
                                isAuthorized={!!user}
                                onLoginClick={() => setActiveTab("Login")}
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
                                onAuthSuccess={() =>  setActiveTab("Play")}
                            />
                        }

                        {activeTab === 'Settings' &&
                            <AccountSettingsPanel
                                currentUsername={user.username}
                                setUser={setUser}
                                redirectToHomePage={() => setActiveTab('Play')}
                            />
                        }
                </section>
            </section>
        </main>
    )
}


