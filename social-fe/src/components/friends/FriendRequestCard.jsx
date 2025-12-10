import React, { useState } from 'react';

const FriendRequestCard = ({ request, onAccept, onReject }) => {
    const [loading, setLoading] = useState(false);
    const avatarUrl = request.senderAvatarUrl || `https://ui-avatars.com/api/?name=${encodeURIComponent(request.senderUsername || request.senderEmail)}&background=random`;

    const handleAccept = async () => {
        setLoading(true);
        try {
            await onAccept(request.id);
        } finally {
            setLoading(false);
        }
    };

    const handleReject = async () => {
        setLoading(true);
        try {
            await onReject(request.id);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="bg-white rounded-xl p-4 hover:shadow-lg transition-all duration-300 border border-gray-100">
            <div className="flex items-center gap-4">
                <img
                    src={avatarUrl}
                    alt={request.senderUsername || request.senderEmail}
                    className="w-14 h-14 rounded-full object-cover ring-2 ring-blue-100"
                />
                <div className="flex-1">
                    <h3 className="font-semibold text-gray-900">
                        {request.senderUsername || request.senderEmail}
                    </h3>
                    <p className="text-sm text-gray-500">
                        {request.senderEmail}
                    </p>
                    <p className="text-xs text-gray-400 mt-1">
                        {new Date(request.createdAt).toLocaleDateString()}
                    </p>
                </div>
                <div className="flex gap-2">
                    <button
                        onClick={handleAccept}
                        disabled={loading}
                        className="px-4 py-2 text-sm font-medium text-white bg-gradient-to-r from-green-500 to-emerald-500 hover:from-green-600 hover:to-emerald-600 rounded-lg transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed shadow-md hover:shadow-lg"
                    >
                        {loading ? 'Loading...' : 'Accept'}
                    </button>
                    <button
                        onClick={handleReject}
                        disabled={loading}
                        className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                        Reject
                    </button>
                </div>
            </div>
        </div>
    );
};

export default FriendRequestCard;
