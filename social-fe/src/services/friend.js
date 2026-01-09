import httpClient from '../config/HttpClient';

// Send friend request
export const sendFriendRequest = async (receiverId) => {
  return await httpClient.post('/friends/requests', { receiverId });
};

// Accept friend request
export const acceptFriendRequest = async (requestId) => {
  return await httpClient.put(`/friends/requests/${requestId}/accept`);
};

// Reject friend request
export const rejectFriendRequest = async (requestId) => {
  return await httpClient.put(`/friends/requests/${requestId}/reject`);
};

// Get pending friend requests (received)
export const getPendingRequests = async () => {
  return await httpClient.get('/friends/requests/pending');
};

// Get sent friend requests
export const getSentRequests = async () => {
  return await httpClient.get('/friends/requests/sent');
};

// Get friends list
export const getFriends = async () => {
  return await httpClient.get('/friends');
};

// Unfriend a user
export const unfriend = async (friendId) => {
  return await httpClient.delete(`/friends/${friendId}`);
};
