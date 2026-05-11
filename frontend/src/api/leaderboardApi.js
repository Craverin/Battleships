import {request} from "./httpClient.js";

export const getMyStats = (game = "battleships") => {
    return request(`/stats/${game}/me`);
}

export const getTopPlayers = ({game = "battleships", sortBy = "bestScore", sortType = "DESC"}) => {
    return request(`/stats/${game}?sortBy=${sortBy}&sortType=${sortType}`);
}