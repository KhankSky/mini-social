import httpClient from "../config/HttpClient";

export const getOnlineUsers = async () => {
    const res = await httpClient.get('/presence/online');
    return res.data;
};

export const getOnlineUserCount = async () => {
    const res = await httpClient.get('/presence/count');
    return res.data;
};
