import React, { useRef, useEffect } from "react";
import { formatDistanceToNow } from "date-fns";

const NotificationDropdown = ({ notifications, onMarkRead, onClose }) => {
    const dropdownRef = useRef(null);

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
                onClose();
            }
        };
        document.addEventListener("mousedown", handleClickOutside);
        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
        };
    }, [onClose]);

    if (!notifications) return null;

    return (
        <div
            ref={dropdownRef}
            className="absolute left-0 mt-2 w-80 bg-white rounded-xl shadow-lg border border-gray-100 py-2 z-[100] animate-fade-in-up"
        >
            <div className="flex items-center justify-between px-4 py-2 border-b border-gray-50">
                <h3 className="font-semibold text-gray-800">Notifications</h3>
                {notifications.length > 0 && (
                    <button
                        onClick={() => onMarkRead("all")}
                        className="text-xs text-indigo-600 hover:text-indigo-700 font-medium"
                    >
                        Mark all as read
                    </button>
                )}
            </div>

            <div className="max-h-96 overflow-y-auto">
                {notifications.length === 0 ? (
                    <div className="px-4 py-8 text-center text-gray-500 text-sm">
                        No notifications yet
                    </div>
                ) : (
                    notifications.map((notification) => (
                        <div
                            key={notification.id}
                            onClick={() => onMarkRead(notification.id)}
                            className={`px-4 py-3 hover:bg-gray-50 cursor-pointer transition-colors border-b border-gray-50 last:border-0 ${!notification.isRead ? "bg-indigo-50/50" : ""
                                }`}
                        >
                            <div className="flex gap-3">
                                <img
                                    src={
                                        notification.actorAvatar ||
                                        "https://ui-avatars.com/api/?name=" + notification.actorName
                                    }
                                    alt={notification.actorName}
                                    className="w-10 h-10 rounded-full object-cover"
                                />
                                <div className="flex-1">
                                    <p className="text-sm text-gray-800">
                                        <span className="font-semibold">
                                            {notification.actorName}
                                        </span>{" "}
                                        {getNotificationText(notification)}
                                    </p>
                                    <p className="text-xs text-gray-400 mt-1">
                                        {formatDistanceToNow(new Date(notification.createdAt), {
                                            addSuffix: true,
                                        })}
                                    </p>
                                </div>
                                {!notification.isRead && (
                                    <span className="w-2 h-2 bg-indigo-600 rounded-full mt-2"></span>
                                )}
                            </div>
                        </div>
                    ))
                )}
            </div>
        </div>
    );
};

const getNotificationText = (notification) => {
    switch (notification.type) {
        case "COMMENT":
            return "commented on your post";
        case "FRIEND_REQUEST":
            return "sent you a friend request";
        case "FRIEND_ACCEPT":
            return "accepted your friend request";
        default:
            return notification.content || "sent a notification";
    }
};

export default NotificationDropdown;
