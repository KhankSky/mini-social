import httpClient from '../config/HttpClient';

export const createPost = async (content, images = []) => {
  const formData = new FormData();
  if (content && content.trim().length > 0) {
    formData.append('content', content.trim());
  }

  if (images && images.length > 0) {
    images.forEach((file) => {
      if (file) {
        formData.append('images', file);
      }
    });
  }

  return httpClient.post('/posts', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
};

export const fetchPosts = async (page = 1, size = 5) => {
  // Backend sử dụng Pageable (page index 0-based)
  return httpClient.get('/posts', {
    params: {
      page: page - 1,
      size,
    },
  });
};

export const likePost = async (postId) => {
  return httpClient.post(`/likes/post/${postId}`);
};

export const unlikePost = async (postId) => {
  return httpClient.delete(`/likes/post/${postId}`);
};

export const isPostLikedByCurrentUser = async (postId) => {
  return httpClient.get(`/likes/post/${postId}/is-liked`);
};

export const getPostLikeCount = async (postId) => {
  return httpClient.get(`/likes/post/${postId}/count`);
};


