import httpClient from "../config/HttpClient";

export const sendMessage = async (data) => {
    const formData = new FormData();
    formData.append("receiverId", data.receiverId);
    formData.append("content", data.content);

    if (data.files && data.files.length > 0) {
        data.files.forEach((file) => {
            formData.append("files", file);
        });
    }

    return httpClient.post("/messages", formData, {
        headers: {
            "Content-Type": "multipart/form-data",
        },
    });
};

export const getConversations = async () => {
    return httpClient.get("/messages/conversations");
};

export const getConversationMessages = async (userId) => {
    return httpClient.get(`/messages/conversations/${userId}`);
};

export const markAsRead = async (messageId) => {
    return httpClient.put(`/messages/${messageId}/read`);
};

export const markAllAsRead = async (userId) => {
    return httpClient.put(`/messages/conversations/${userId}/read`);
};

export const getUnreadCount = async () => {
    return httpClient.get("/messages/unread-count");
};
