
import {GamePage} from "./pages/GamePage.jsx";
import {JoinGamePage} from "./pages/JoinGamePage.jsx";
import {Routes, Route} from "react-router";

const App = () => {
    return (
        <Routes>
            <Route path="/" element={<GamePage />} />
            <Route path="/games/:inviteCode/join" element={<JoinGamePage />} />
        </Routes>
    )
}

// const handleCreateGame = async () => {
//     ({gameId, hostToken} = await createGame());
//     console.log(gameId);
//     console.log(hostToken);
//
//     setGameId(gameId);
// }
//
// const handleGetShips = async () => {
//     console.log(await getShips(gameId, hostToken));
// }
//
// const drawHostShips = async () => {
//     setHostShips(await getShips(gameId, hostToken));
// }
//
// const drawOpponentShips = async () => {
//     setOpponentShips(await getShips(gameId, opponentToken));
// }
//
// const handleJoinGame = async () => {
//     opponentToken = await joinGame(gameId);
//     console.log(`Opponent token - ${opponentToken}`);
//     console.log(await getShips(gameId, opponentToken));
//
//     setOpponentJoined(true);
// }
//
// const joinSse = () => {
//     console.log("Setting up SSE");
//     const url = new URL(`http://localhost:5173/api/games/${gameId}/events`);
//     url.searchParams.set("token", hostToken);
//
//     source = new EventSource(url);
//
//     source.addEventListener("opponent-joined", event => {
//         console.log(`Opponent joined. Received: ${event.data}`);
//     })
//
//     source.addEventListener("ship-moved", event => {
//         console.log(`Ship's moved. Received: ${event.data}`);
//     })
//
//     console.log("SSE setup finished");
// }

export default App;