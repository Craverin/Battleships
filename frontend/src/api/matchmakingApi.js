import {request} from "./httpClient.js";

export const findGame = () => {
    return request('/games/find', {method: 'POST'});
}

export const cancelSearch = (gameId, playerToken) => {
    return request(`/games/${gameId}/cancel`, {
        method: 'POST',
        token: playerToken
    });
}