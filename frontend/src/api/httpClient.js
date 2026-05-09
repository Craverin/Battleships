export const request = async(path, options = {}) => {
    const {
        method = 'GET',
        token = null,
        body = null,
    } = options;

    const headers = {};

    if (body !== null) headers['Content-Type'] = 'application/json';
    if (token) headers['Player-Token'] = token;

    console.log(`Sending ${method} to \`/api${path}\``);
    const response = await fetch(`/api${path}`, {
        method: method,
        headers: headers,
        body: body !== null ? JSON.stringify(body) : undefined,
    });

    const contentType = response.headers.get('content-type') ?? '';
    console.log(response.status);
    if (!response.ok)
    {
        const error = new Error();
        error.status = response.status;

        throw error;
    }


    return contentType.includes('application/json') ? await response.json() : await response.text();
}