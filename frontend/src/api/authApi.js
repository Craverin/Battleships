import {request} from "./httpClient.js";

export const register = (username, password) => request('/auth/register', {
    method: 'POST',
    body: {username, password}
})

export const login = (username, password) => request('/auth/login', {
    method: 'POST',
    body: {username, password}
})

export const getCurrentUser = () => request('/auth/me')

export const logout = () => request('/auth/logout', {method: 'POST'})