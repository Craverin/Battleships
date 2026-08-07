import {request} from "./httpClient.js";

export const changeUsername = (username) => request('/me/username', {
    method: 'POST',
    body: {username}
})

export const changePassword = (currentPassword, newPassword) => request('/me/password', {
    method: 'POST',
    body: {currentPassword, newPassword}
})
