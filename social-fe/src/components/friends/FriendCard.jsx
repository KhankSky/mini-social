import React, { useState } from 'react';

const FriendCard = ({ friend, onUnfriend }) => {
    const [loading, setLoading] = useState(false);
    const avatarUrl = friend.avatarUrl || `https://ui-avatars.com/api/?name=${encodeURIComponent(friend.username || friend.email)}&background=random`;

    const handleUnfriend = async () => {
        if (!window.confirm(`Are you sure you want to unfriend ${friend.username || friend.email}?`)) {
            return;
        }

        setLoading(true);
        try {
            await onUnfriend(friend.id);
        } catch (error) {
            console.error('Failed to unfriend', error);
            alert(error.response?.data?.message || 'Failed to unfriend user');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="bg-white rounded-xl p-4 hover:shadow-lg transition-all duration-300 border border-gray-100">
            <div className="flex items-center gap-4">
                <img
                    src={avatarUrl}
                    alt={friend.username || friend.email}
                    className="w-14 h-14 rounded-full object-cover ring-2 ring-purple-100"
                />
                <div className="flex-1">
                    <h3 className="font-semibold text-gray-900">
                        {friend.username || friend.email}
                    </h3>
                    {friend.bio && (
                        <p className="text-sm text-gray-500 line-clamp-1">{friend.bio}</p>
                    )}
                    {friend.friendsSince && (
                        <p className="text-xs text-gray-400 mt-1">
                            Friends since {new Date(friend.friendsSince).toLocaleDateString()}
                        </p>
                    )}
                </div>
                <div className="flex gap-2">
                    <button
                        onClick={handleUnfriend}
                        disabled={loading}
                        className="px-4 py-2 text-sm font-medium text-red-600 hover:bg-red-50 rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                        {loading ? 'Removing...' : 'Unfriend'}
                    </button>
                    <button
                        className="px-4 py-2 text-sm font-medium text-purple-600 hover:bg-purple-50 rounded-lg transition-colors"
                    >
                        Message
                    </button>
                </div>
            </div>
        </div>
    );
};

export default FriendCard;
