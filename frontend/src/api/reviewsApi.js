import {request} from "./httpClient.js";

export const getComments = (game = "battleships") => request(`/comments/${game}`)

export const getPlayerComments = (player, game = "battleships") => {
    return request(`/comments/${game}/players/${player}`)
}

export const addComment = (player, game = "battleships", comment, commentedOn) => {
    return request('/comments', {
        method: 'POST',
        body: {player, game, comment, commentedOn}
    });
}
export const getRatingSummary = (game = "battleships") => request(`/rating/${game}/summary`)

export const getRating = (player, game = "battleships") => request(`/rating/${game}/players/${player}`)
export const setRating = (player, game = "battleships", rating, ratedOn) => {
    return request('/rating', {
        method: 'POST',
        body: {player, game, rating, ratedOn}
    });
}