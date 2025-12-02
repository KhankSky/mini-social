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


