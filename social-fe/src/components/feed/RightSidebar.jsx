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
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-xl font-bold text-gray-900">Suggestions</h2>
          <a href="/friends" className="text-sm font-medium text-indigo-600 hover:text-indigo-700">See all</a>
        </div>
        <div className="space-y-4">
          {suggestions.length === 0 ? (
            <div className="text-center py-8 bg-white rounded-2xl shadow-sm border border-gray-100">
              <p className="text-gray-500 text-sm">No suggestions available</p>
            </div>
          ) : (
            suggestions.map((user, index) => {
              const avatarUrl = user.avatarUrl || `https://ui-avatars.com/api/?name=${encodeURIComponent(user.username || user.email)}&background=random`;
              const isSent = sentRequests.has(user.id);

              return (
                <div
                  key={user.id}
                  className="flex items-center justify-between p-4 bg-white rounded-2xl shadow-sm border border-gray-100 hover:shadow-md transition-all duration-200"
                  style={{ animationDelay: `${index * 100}ms` }}
                >
                  <div className="flex items-center gap-4">
                    <img
                      src={avatarUrl}
                      alt={user.username || user.email}
                      className="w-12 h-12 rounded-full object-cover ring-2 ring-gray-50"
                    />
                    <div className="flex flex-col">
                      <span className="font-bold text-gray-900 text-sm truncate max-w-[120px]" title={user.username || user.email}>
                        {user.username || user.email}
                      </span>
                      {user.username && (
                        <span className="text-xs text-gray-500 truncate max-w-[120px]">Suggested for you</span>
                      )}
                    </div>
                  </div>
                  <button
                    onClick={() => handleAddFriend(user.id)}
                    disabled={isSent}
                    className={`w-10 h-10 flex items-center justify-center rounded-xl transition-all duration-200 ${isSent
                      ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
                      : 'bg-white border-2 border-gray-100 text-gray-700 hover:border-black hover:text-black hover:bg-gray-50'
                      }`}
                    title={isSent ? "Request Sent" : "Add Friend"}
                  >
                    {isSent ? (
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                        <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                      </svg>
                    ) : (
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                        <path d="M8 9a3 3 0 100-6 3 3 0 000 6zM8 11a6 6 0 016 6H2a6 6 0 016-6zM16 7a1 1 0 10-2 0v1h-1a1 1 0 100 2h1v1a1 1 0 102 0v-1h1a1 1 0 100-2h-1V7z" />
                      </svg>
                    )}
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
