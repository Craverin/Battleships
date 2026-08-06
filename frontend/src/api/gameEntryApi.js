import {request} from "./httpClient.js";

export const createGame = () => {
    return request("/games", {method: "POST"});
}

export const joinGame = (inviteCode) => {
    return request(`/games/${inviteCode}/join`, {method: "POST"})
}