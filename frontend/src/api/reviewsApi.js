import {request} from "./httpClient.js";

export const getComments = (game = "battleships") => request(`/comments/${game}`)

export const getMyComments = (game = "battleships") => {
    return request(`/comments/${game}/me`)
}

export const addComment =  (game = "battleships", comment) => {
    return request(`/comments/${game}`, {
        method: 'POST',
        body: {comment}
    });
}
export const getRatingSummary = (game = "battleships") => request(`/rating/${game}/summary`)

export const getMyRating = (game = "battleships") => request(`/rating/${game}/me`)

export const setRating = (game = "battleships", rating) => {
    return request(`/rating/${game}`, {
        method: 'POST',
        body: rating
    });
}