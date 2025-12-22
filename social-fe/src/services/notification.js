import httpClient from "../config/HttpClient";

export const getMyNotifications = async () => {
    const response = await httpClient.get('/notifications');
    return response.data;
};

export const markAsRead = async (id) => {
    await httpClient.put(`/notifications/${id}/read`);
};

export const markAllAsRead = async () => {
    await httpClient.put('/notifications/read-all');
};
