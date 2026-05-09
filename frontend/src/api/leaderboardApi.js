import {request} from "./httpClient.js";

export const getPlayerStats = (player, game = "battleships") => {
    if (!player) return;
    return request(`/stats/${player}/${game}`);
}

export const getTopPlayers = ({game = "battleships", sortBy = "bestScore", sortType = "DESC"}) => {
    return request(`/stats/${game}?sortBy=${sortBy}&sortType=${sortType}`);
}