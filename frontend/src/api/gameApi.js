import {request} from "./httpClient.js";
// Add API BASE /games, maybe change it

export const createGame = () => {
    return request("/games", {method: "POST"});
}

export const joinGame = (inviteCode) => {
    return request(`/games/${inviteCode}/join`, {method: "POST"})
}

export const shoot = (gameId, playerToken, cell) => {
    return request(`/games/${gameId}/shoot`, {
        method: "PUT",
        token: playerToken,
        body: cell
    });
}

export const moveShip = (gameId, shipId, playerToken, position) => {
    return request(`/games/${gameId}/ships/move/${shipId}`, {
        method: "PUT",
        token: playerToken,
        body: position
    });
}

export const getShips = (gameId, playerToken) => {
    return request(`/games/${gameId}/ships`, {token: playerToken})
}

export const setReady = (gameId, playerToken) => {
    return request(`/games/${gameId}/ready`, {token: playerToken,
        method: "POST"})
}
