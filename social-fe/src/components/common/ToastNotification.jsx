import React, { useEffect, useState } from 'react';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';

dayjs.extend(relativeTime);

const ToastNotification = ({ notification, onClose, onClick }) => {
    const [isVisible, setIsVisible] = useState(false);
    const [isExiting, setIsExiting] = useState(false);

    useEffect(() => {
        // Animate in
        setTimeout(() => setIsVisible(true), 10);

        // Auto-dismiss after 5 seconds
        const timer = setTimeout(() => {
            handleClose();
        }, 5000);

        return () => clearTimeout(timer);
    }, []);

    const handleClose = () => {
        setIsExiting(true);
        setTimeout(() => {
            onClose();
        }, 300);
    };

    const handleClick = () => {
        if (onClick) {
            onClick(notification);
        }
        handleClose();
    };

    const getNotificationText = (notif) => {
        switch (notif.type) {
            case 'COMMENT':
                return 'commented on your post';
            case 'FRIEND_REQUEST':
                return 'sent you a friend request';
            case 'FRIEND_ACCEPT':
                return 'accepted your friend request';
            case 'MESSAGE':
                return 'sent you a message';
            default:
                return notif.content || 'sent a notification';
        }
    };

    return (
        <div
            onClick={handleClick}
            className={`
                fixed top-4 right-4 z-[9999] w-96 bg-white rounded-xl shadow-2xl border border-gray-100
                cursor-pointer transform transition-all duration-300 ease-out
                ${isVisible && !isExiting ? 'translate-x-0 opacity-100' : 'translate-x-full opacity-0'}
                hover:shadow-3xl hover:scale-105
            `}
        >
            <div className="p-4 flex items-start gap-3">
                {/* Avatar */}
                <img
                    src={
                        notification.actorAvatar ||
                        `https://ui-avatars.com/api/?name=${notification.actorName}`
                    }
                    alt={notification.actorName}
                    className="w-12 h-12 rounded-full object-cover border-2 border-indigo-100"
                />

                {/* Content */}
                <div className="flex-1 min-w-0">
                    <div className="flex items-start justify-between gap-2">
                        <div className="flex-1">
                            <p className="text-sm font-semibold text-gray-900">
                                {notification.actorName}
                            </p>
                            <p className="text-sm text-gray-600 mt-0.5">
                                {getNotificationText(notification)}
                            </p>
                        </div>
                        <button
                            onClick={(e) => {
                                e.stopPropagation();
                                handleClose();
                            }}
                            className="text-gray-400 hover:text-gray-600 transition-colors"
                        >
                            <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                                <path fillRule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clipRule="evenodd" />
                            </svg>
                        </button>
                    </div>
                    <p className="text-xs text-gray-400 mt-1">
                        {dayjs(notification.createdAt).fromNow()}
                    </p>
                </div>
            </div>

            {/* Progress bar */}
            <div className="h-1 bg-gray-100 rounded-b-xl overflow-hidden">
                <div
                    className="h-full bg-gradient-to-r from-indigo-500 to-purple-500 animate-progress"
                    style={{ animation: 'progress 5s linear forwards' }}
                />
            </div>

            <style jsx>{`
                @keyframes progress {
                    from { width: 100%; }
                    to { width: 0%; }
                }
                .animate-progress {
                    animation: progress 5s linear forwards;
                }
            `}</style>
        </div>
    );
};

export default ToastNotification;
