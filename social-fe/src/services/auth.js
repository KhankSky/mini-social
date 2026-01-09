import httpClient from "../config/HttpClient";

const AuthService = {
  login: async ({ username, password }) => {
    // backend returns plain jwt string
    const res = await httpClient.post('/auth/login', { username, password });
    return res.data;
  },

  register: async ({ email, password, username, avatarUrl, bio }) => {
    const payload = { email, password, username, avatarUrl, bio };
    const res = await httpClient.post('/users', payload);
    return res.data;
  },

  logout: () => {
    localStorage.removeItem('social_app_token');
    localStorage.removeItem('user');
    window.location.href = '/login';
  },

  fetchProfile: async () => {
    const res = await httpClient.get('/auth/me');
    return res.data;
  }
};

export const getCurrentUser = () => {
  const userStr = localStorage.getItem("user");
  if (userStr) return JSON.parse(userStr);
  return null;
};

export default AuthService;
