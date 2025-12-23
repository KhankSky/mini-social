import httpClient from '../config/HttpClient';

// Admin API calls
export const getAdminStats = async () => {
    const response = await httpClient.get('/admin/stats');
    return response.data;
};

export const getAllUsers = async (page = 0, size = 20, search = '') => {
    const params = { page, size };
    if (search) params.search = search;
    const response = await httpClient.get('/admin/users', { params });
    return response.data;
};

export const updateUserRole = async (userId, role) => {
    await httpClient.put(`/admin/users/${userId}/role`, { role });
};

export const deleteUser = async (userId) => {
    await httpClient.delete(`/admin/users/${userId}`);
};

export const getAllPosts = async (page = 0, size = 20) => {
    const response = await httpClient.get('/admin/posts', { params: { page, size } });
    return response.data;
};

export const deletePost = async (postId) => {
    await httpClient.delete(`/admin/posts/${postId}`);
};

export const deleteComment = async (commentId) => {
    await httpClient.delete(`/admin/comments/${commentId}`);
};
