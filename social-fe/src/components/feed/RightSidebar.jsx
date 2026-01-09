import React, { useEffect, useState } from 'react';
import { sendFriendRequest } from '../../services/friend';
import httpClient from '../../config/HttpClient';

const RightSidebar = () => {
  const [suggestions, setSuggestions] = useState([]);
  const [currentUser, setCurrentUser] = useState(null);
  const [sentRequests, setSentRequests] = useState(new Set());

  useEffect(() => {
    loadCurrentUser();
  }, []);

  useEffect(() => {
    if (currentUser) {
      loadSuggestions();
    }
  }, [currentUser]);

  const loadCurrentUser = async () => {
    try {
      const res = await httpClient.get('/auth/me');
      setCurrentUser(res.data);
    } catch (err) {
      console.error('Failed to load current user', err);
    }
  };

  const loadSuggestions = async () => {
    try {
      const res = await httpClient.get('/users?page=1&size=5');
      const allUsers = res.data?.result || [];
      // Filter out current user and limit to 5 suggestions
      const filteredUsers = allUsers
        .filter(u => u.id !== currentUser?.id)
        .slice(0, 5);
      setSuggestions(filteredUsers);
    } catch (err) {
      console.error('Failed to load suggestions', err);
    }
  };

  const handleAddFriend = async (userId) => {
    try {
      await sendFriendRequest(userId);
      setSentRequests(prev => new Set([...prev, userId]));
    } catch (err) {
      console.error('Failed to send friend request', err);
      alert(err.response?.data?.message || 'Failed to send friend request');
    }
  };

  return (
    <>
      <div className="mb-8">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-2xl font-bold">Suggestions</h2>
          <a href="/friends" className="text-sm text-gray-500 hover:text-black">See all</a>
        </div>
        <div className="space-y-3">
          {suggestions.length === 0 ? (
            <p className="text-gray-500 text-sm">No suggestions available</p>
          ) : (
            suggestions.map((user) => {
              const avatarUrl = user.avatarUrl || `https://ui-avatars.com/api/?name=${encodeURIComponent(user.username || user.email)}&background=random`;
              const isSent = sentRequests.has(user.id);

              return (
                <div key={user.id} className="flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <img
                      src={avatarUrl}
                      alt={user.username || user.email}
                      className="w-10 h-10 rounded-full object-cover"
                    />
                    <div className="flex flex-col">
                      <span className="font-semibold text-sm">{user.username || user.email}</span>
                      {user.username && (
                        <span className="text-xs text-gray-500">{user.email}</span>
                      )}
                    </div>
                  </div>
                  <button
                    onClick={() => handleAddFriend(user.id)}
                    disabled={isSent}
                    className={`px-4 py-1 rounded-full text-sm transition-colors ${isSent
                        ? 'bg-gray-200 text-gray-500 cursor-not-allowed'
                        : 'bg-black text-white hover:bg-gray-800'
                      }`}
                  >
                    {isSent ? 'Sent' : 'Add Friend'}
                  </button>
                </div>
              );
            })
          )}
        </div>
      </div>
    </>
  );
};

export default RightSidebar;
