import React, { useState } from 'react';

const UserSearchCard = ({ user, onSendRequest, requestSent }) => {
    const [loading, setLoading] = useState(false);
    const [sent, setSent] = useState(requestSent);
    const avatarUrl = user.avatarUrl || `https://ui-avatars.com/api/?name=${encodeURIComponent(user.username || user.email)}&background=random`;

    const handleSendRequest = async () => {
        setLoading(true);
        try {
            await onSendRequest(user.id);
            setSent(true);
        } catch (error) {
            console.error('Failed to send friend request', error);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="bg-white rounded-xl p-4 hover:shadow-lg transition-all duration-300 border border-gray-100">
            <div className="flex items-center gap-4">
                <img
                    src={avatarUrl}
                    alt={user.username || user.email}
                    className="w-14 h-14 rounded-full object-cover ring-2 ring-indigo-100"
                />
                <div className="flex-1">
                    <h3 className="font-semibold text-gray-900">
                        {user.username || user.email}
                    </h3>
                    <p className="text-sm text-gray-500">
                        {user.email}
                    </p>
                </div>
                <button
                    onClick={handleSendRequest}
                    disabled={loading || sent}
                    className={`px-4 py-2 text-sm font-medium rounded-lg transition-all duration-200 ${sent
                            ? 'bg-gray-100 text-gray-500 cursor-not-allowed'
                            : 'text-white bg-gradient-to-r from-purple-500 to-indigo-500 hover:from-purple-600 hover:to-indigo-600 shadow-md hover:shadow-lg'
                        } disabled:opacity-50`}
                >
                    {loading ? 'Sending...' : sent ? 'Sent' : 'Add Friend'}
                </button>
            </div>
        </div>
    );
};

export default UserSearchCard;
