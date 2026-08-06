import {request} from "./httpClient.js";

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
