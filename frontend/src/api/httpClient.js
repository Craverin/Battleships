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
        credentials: "include",
        body: body !== null ? JSON.stringify(body) : undefined
    });

    const contentType = response.headers.get('content-type') ?? '';
    console.log(response.status);

    if (!response.ok)
    {
        const error = new Error();
        error.status = response.status;

        if (contentType.includes("application/json"))
        {
            const data = await response.json();

            error.message = data.message || data.detail || data.error || "Request failed";
            error.data = data;
        }

        else
        {
            const text = await response.text();

            error.message = text || "Request failed";
        }

        throw error;
    }

    return contentType.includes('application/json') ? await response.json() : await response.text();
}