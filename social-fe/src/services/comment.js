import httpClient from '../config/HttpClient';

export const fetchComments = async (postId) => {
  return httpClient.get(`/posts/${postId}/comments`);
};

export const createComment = async (postId, payload) => {
  return httpClient.post(`/posts/${postId}/comments`, payload);
};


