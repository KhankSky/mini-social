// Service để gọi API gợi ý địa điểm
// Gọi qua backend proxy để tránh CORS và bảo mật

import httpClient from '../config/HttpClient';

/**
 * Tìm kiếm địa điểm dựa trên query
 * @param {string} query - Từ khóa tìm kiếm
 * @param {number} limit - Số lượng kết quả tối đa (mặc định 5)
 * @returns {Promise<Array>} Danh sách địa điểm gợi ý
 */
export const searchLocations = async (query, limit = 5) => {
  if (!query || query.trim().length < 2) {
    return [];
  }

  try {
    // Gọi backend proxy endpoint
    const response = await httpClient.get('/locations/search', {
      params: {
        q: query.trim(),
        limit: limit,
      },
    });

    return response.data || [];
  } catch (error) {
    console.error('Error searching locations:', error);
    return [];
  }
};

/**
 * Lấy địa điểm hiện tại của user (nếu cho phép)
 * @returns {Promise<Object|null>} Thông tin địa điểm hiện tại
 */
export const getCurrentLocation = () => {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      reject(new Error('Geolocation is not supported'));
      return;
    }

    navigator.geolocation.getCurrentPosition(
      async (position) => {
        try {
          const { latitude, longitude } = position.coords;
          
          // Gọi backend proxy endpoint để reverse geocode
          const response = await httpClient.get('/locations/reverse', {
            params: {
              lat: latitude,
              lon: longitude,
            },
          });

          if (response.data) {
            resolve({
              displayName: response.data.displayName,
              name: response.data.shortName || response.data.name,
              lat: response.data.lat,
              lon: response.data.lon,
            });
          } else {
            reject(new Error('No location data returned'));
          }
        } catch (error) {
          console.error('Error reverse geocoding:', error);
          reject(error);
        }
      },
      (error) => {
        reject(error);
      },
      {
        enableHighAccuracy: true,
        timeout: 5000,
        maximumAge: 0,
      }
    );
  });
};

