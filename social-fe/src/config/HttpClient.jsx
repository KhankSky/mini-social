import axios from "axios";

const API_BASE_URL ="http://localhost:9090/api";

const httpClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

// Request Interceptor: Thêm token vào header Authorization
httpClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("social_app_token");
    if (token) {
      // Các endpoints không cần gửi token
      const noAuthEndpoints = [
        "/auth/token",
        "/auth/refresh",
        "/auth/introspect", // Introspect có thể cần token, tùy vào logic backend của bạn
        "/users/register",
        "/users/forgot-password",
        "/users/reset-password",
      ];

      // Kiểm tra xem URL của request có nằm trong danh sách noAuthEndpoints không
      // Lưu ý: config.url có thể là URL đầy đủ hoặc chỉ là path.
      // Cần đảm bảo logic kiểm tra này chính xác.
      const requestPath = config.url.replace(config.baseURL, "");
      const isAuthEndpoint = noAuthEndpoints.some(endpoint => requestPath.startsWith(endpoint));

      if (!isAuthEndpoint) {
        config.headers["Authorization"] = `Bearer ${token}`;
      }
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response Interceptor (Xử lý lỗi 401 cơ bản)
httpClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // Nếu lỗi là 401 và không phải là request refresh token ban đầu
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true; // Đánh dấu đã thử lại
      console.warn("Token expired or invalid (401). Attempting to handle...");
      // Xử lý đơn giản: logout
      localStorage.removeItem("social_app_token");q
      // Sử dụng window.location để đảm bảo reload hoàn toàn và state được reset
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
      return Promise.reject(error); // Quan trọng: reject error sau khi xử lý
    }
    return Promise.reject(error);
  }
);

export default httpClient;