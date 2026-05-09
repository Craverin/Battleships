export const subscribeToSse = (gameId, playerToken) => {
    const url = new URL(`${window.location.origin}/api/games/${gameId}/events`);
    url.searchParams.set("token", playerToken);

    return new EventSource(url);
}