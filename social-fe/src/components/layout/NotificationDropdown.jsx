import React, { useRef, useEffect } from "react";
import dayjs from "dayjs";
import relativeTime from "dayjs/plugin/relativeTime";
import { useNavigate } from "react-router-dom";

dayjs.extend(relativeTime);

const NotificationDropdown = ({ notifications, onMarkRead, onClose }) => {
    const dropdownRef = useRef(null);
    const navigate = useNavigate();

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

    const handleNotificationClick = (notification) => {
        if (!notification.isRead) {
            onMarkRead(notification.id);
        }

        switch (notification.type) {
            case "MESSAGE":
                navigate("/messages");
                break;
            case "FRIEND_REQUEST":
            case "FRIEND_ACCEPT":
                navigate("/friends");
                break;
            case "COMMENT":
            case "LIKE":
                navigate("/feed");
                break;
            default:
                break;
        }

        onClose();
    };

    if (!notifications) return null;

    return (
        <div
            ref={dropdownRef}
            className="w-96 bg-white/95 backdrop-blur-xl rounded-2xl shadow-2xl border border-white/20 overflow-hidden z-50 animate-in fade-in zoom-in duration-200"
            style={{
                boxShadow: "0 20px 50px rgba(0,0,0,0.1), 0 0 1px rgba(0,0,0,0.1)"
            }}
        >
            <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100/50 bg-white/50">
                <h3 className="text-lg font-bold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent">
                    Notifications
                </h3>
                {notifications.length > 0 && (
                    <button
                        onClick={() => onMarkRead("all")}
                        className="text-xs font-semibold text-indigo-600 hover:text-indigo-800 transition-colors py-1 px-3 rounded-full hover:bg-indigo-50"
                    >
                        Mark all as read
                    </button>
                )}
            </div>

            <div className="max-h-[450px] overflow-y-auto custom-scrollbar">
                {notifications.length === 0 ? (
                    <div className="px-6 py-12 text-center">
                        <div className="text-4xl mb-3">🔔</div>
                        <p className="text-gray-500 font-medium text-sm">No notifications yet</p>
                        <p className="text-gray-400 text-xs mt-1">We'll notify you when something happens</p>
                    </div>
                ) : (
                    notifications.map((notification, index) => (
                        <div
                            key={notification.id}
                            onClick={() => handleNotificationClick(notification)}
                            className={`px-6 py-4 hover:bg-indigo-50/50 cursor-pointer transition-all border-b border-gray-50/50 last:border-0 relative group ${!notification.isRead ? "bg-indigo-50/30" : ""
                                }`}
                            style={{ animationDelay: `${index * 50}ms` }}
                        >
                            <div className="flex gap-4">
                                <div className="relative flex-shrink-0">
                                    <img
                                        src={
                                            notification.actorAvatar ||
                                            "https://ui-avatars.com/api/?name=" + notification.actorName
                                        }
                                        alt={notification.actorName}
                                        className="w-12 h-12 rounded-2xl object-cover shadow-sm group-hover:scale-105 transition-transform duration-200"
                                    />
                                    {!notification.isRead && (
                                        <span className="absolute -top-1 -right-1 w-3.5 h-3.5 bg-indigo-600 border-2 border-white rounded-full"></span>
                                    )}
                                </div>
                                <div className="flex-1 min-w-0">
                                    <p className="text-sm text-gray-800 leading-relaxed">
                                        <span className="font-bold text-gray-900">
                                            {notification.actorName}
                                        </span>{" "}
                                        <span className="text-gray-600">
                                            {getNotificationText(notification)}
                                        </span>
                                    </p>
                                    <p className="text-[11px] font-medium text-indigo-400 mt-1.5 flex items-center gap-1">
                                        <span className="w-1 h-1 rounded-full bg-indigo-300"></span>
                                        {dayjs(notification.createdAt).fromNow()}
                                    </p>
                                </div>
                            </div>
                        </div>
                    ))
                )}
            </div>

            <style>{`
                .custom-scrollbar::-webkit-scrollbar {
                    width: 6px;
                }
                .custom-scrollbar::-webkit-scrollbar-track {
                    background: transparent;
                }
                .custom-scrollbar::-webkit-scrollbar-thumb {
                    background: #e2e8f0;
                    border-radius: 10px;
                }
                .custom-scrollbar::-webkit-scrollbar-thumb:hover {
                    background: #cbd5e1;
                }
            `}</style>
        </div>
    );
};

const getNotificationText = (notification) => {
    switch (notification.type) {
        case "COMMENT":
            return "commented on your post";
        case "LIKE":
            return "liked your post";
        case "FRIEND_REQUEST":
            return "sent you a friend request";
        case "FRIEND_ACCEPT":
            return "accepted your friend request";
        case "MESSAGE":
            return "sent you a message";
        default:
            return notification.content || "sent a notification";
    }
};

export default NotificationDropdown;


