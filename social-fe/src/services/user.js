import httpClient from "../config/HttpClient";

const UserService = {
  list: async (params) => {
    const res = await httpClient.get('/users', { params });
    return res.data;
  },

  getById: async (id) => {
    const res = await httpClient.get(`/users/${id}`);
    return res.data;
  },

  create: async (payload) => {
    const res = await httpClient.post('/users', payload);
    return res.data;
  },

  update: async (payload) => {
    const res = await httpClient.put('/users', payload);
    return res.data;
  },

  remove: async (id) => {
    const res = await httpClient.delete(`/users/${id}`);
    return res.data;
  }
};

export default UserService;
