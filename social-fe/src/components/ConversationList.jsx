import React, { useState } from 'react';
import { FiSearch, FiEdit } from "react-icons/fi";
import dayjs from "dayjs";
import relativeTime from "dayjs/plugin/relativeTime";

dayjs.extend(relativeTime);

const ConversationList = ({ conversations, selectedUserId, onSelectUser, onNewMessage, onlineUsers = [] }) => {
    const [activeTab, setActiveTab] = useState('All');
    const [searchQuery, setSearchQuery] = useState('');

    const getInitials = (name) => {
        return name ? name.substring(0, 2).toUpperCase() : "U";
    };

    const isUserOnline = (userId) => {
        return onlineUsers.some(u => u.id === userId);
    };

    return (
        <div className="flex flex-col h-full bg-white">
            {/* Header Section */}
            <div className="p-6 pb-2">
                <div className="flex items-center justify-between mb-1">
                    <h1 className="text-2xl font-bold text-gray-800">Message</h1>
                    <button
                        onClick={onNewMessage}
                        className="p-2.5 bg-gradient-to-tr from-blue-600 to-purple-600 text-white rounded-xl hover:shadow-lg transform hover:scale-105 transition-all"
                        title="New Message"
                    >
                        <FiEdit size={18} />
                    </button>
                </div>
                <p className="text-sm text-gray-400 mb-6">Handle all messages and alerts in one place.</p>

                {/* Search */}
                <div className="relative mb-6">
                    <FiSearch className="absolute left-4 top-1/2 transform -translate-y-1/2 text-gray-400 text-lg" />
                    <input
                        type="text"
                        placeholder="Search here..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        className="w-full pl-11 pr-4 py-3 bg-gray-50 border-none rounded-2xl text-gray-600 focus:outline-none focus:ring-2 focus:ring-blue-100 transition-all placeholder-gray-400"
                    />
                    <span className="absolute right-4 top-1/2 transform -translate-y-1/2 text-gray-400 text-xs border border-gray-200 rounded px-1.5 py-0.5">⌘/</span>
                </div>

                {/* Online Now Section */}
                {onlineUsers.length > 0 && (
                    <div className="mb-6">
                        <div className="flex justify-between items-center mb-3">
                            <h3 className="text-sm font-semibold text-gray-800">Online Now</h3>
                            <span className="text-xs text-blue-500 font-medium">{onlineUsers.length} online</span>
                        </div>
                        <div className="flex gap-4 overflow-x-auto no-scrollbar pb-2">
                            {onlineUsers.slice(0, 10).map((user) => (
                                <div
                                    key={user.id}
                                    className="relative flex-shrink-0 cursor-pointer group"
                                    onClick={() => {
                                        // Find or create conversation with this user
                                        const existingConv = conversations.find(c => c.userId === user.id);
                                        if (existingConv) {
                                            onSelectUser(existingConv);
                                        } else {
                                            onSelectUser({
                                                userId: user.id,
                                                username: user.username,
                                                avatarUrl: user.avatarUrl,
                                                lastMessage: '',
                                                lastMessageTime: null,
                                                unreadCount: 0
                                            });
                                        }
                                    }}
                                >
                                    <div className="w-12 h-12 rounded-full border-2 border-white shadow-sm overflow-hidden group-hover:border-blue-300 transition-colors">
                                        <img
                                            src={user.avatarUrl || `https://api.dicebear.com/7.x/avataaars/svg?seed=${user.username}`}
                                            alt={user.username}
                                            className="w-full h-full object-cover"
                                        />
                                    </div>
                                    <span className="absolute bottom-0 right-0 w-3.5 h-3.5 bg-green-500 border-2 border-white rounded-full"></span>
                                    <div className="absolute -bottom-5 left-1/2 transform -translate-x-1/2 opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap">
                                        <span className="text-[10px] text-gray-600 font-medium">{user.username}</span>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                )}

                {/* Filter Tabs */}
                <div className="flex gap-2 mb-2">
                    {['All', 'Team', 'Personal'].map((tab) => (
                        <button
                            key={tab}
                            onClick={() => setActiveTab(tab)}
                            className={`flex-1 py-1.5 text-sm font-medium rounded-lg transition-colors ${activeTab === tab
                                ? 'bg-gray-100 text-gray-800'
                                : 'text-gray-400 hover:text-gray-600'
                                }`}
                        >
                            {tab}
                        </button>
                    ))}
                </div>
            </div>

            {/* List */}
            <div className="flex-1 overflow-y-auto px-4 pb-4 space-y-1 custom-scrollbar">
                {(() => {
                    const filteredConversations = conversations.filter(conv => {
                        if (!searchQuery) return true;
                        const query = searchQuery.toLowerCase();
                        return (
                            conv.username?.toLowerCase().includes(query) ||
                            conv.lastMessage?.toLowerCase().includes(query)
                        );
                    });

                    if (filteredConversations.length === 0) {
                        return (
                            <div className="flex flex-col items-center justify-center py-12 text-center">
                                <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mb-4">
                                    <FiSearch size={24} className="text-gray-400" />
                                </div>
                                <p className="text-gray-500 text-sm mb-2">
                                    {searchQuery ? 'No results found' : 'No conversations yet'}
                                </p>
                                <p className="text-gray-400 text-xs">
                                    {searchQuery ? 'Try a different search term' : 'Click the + button to start chatting'}
                                </p>
                            </div>
                        );
                    }

                    return filteredConversations.map((item) => (
                        <div
                            key={item.userId}
                            onClick={() => onSelectUser(item)}
                            className={`group p-3 rounded-2xl cursor-pointer transition-all duration-200 flex items-start gap-3 ${selectedUserId === item.userId
                                ? 'bg-blue-50/50'
                                : 'hover:bg-gray-50'
                                }`}
                        >
                            {/* Avatar */}
                            <div className="relative flex-shrink-0">
                                <img
                                    src={item.avatarUrl || `https://api.dicebear.com/7.x/avataaars/svg?seed=${item.username}`}
                                    alt={item.username}
                                    className="w-12 h-12 rounded-full object-cover shadow-sm bg-white"
                                />
                                {isUserOnline(item.userId) && (
                                    <span className="absolute bottom-0 right-0 w-3 h-3 bg-green-500 border-2 border-white rounded-full"></span>
                                )}
                            </div>

                            {/* Content */}
                            <div className="flex-1 min-w-0 pt-0.5">
                                <div className="flex justify-between items-baseline mb-1">
                                    <h4 className={`text-sm font-bold truncate ${selectedUserId === item.userId ? 'text-gray-900' : 'text-gray-800'}`}>
                                        {item.username}
                                    </h4>
                                    <span className={`text-xs ${item.unreadCount > 0 ? 'text-blue-500 font-semibold' : 'text-gray-400'}`}>
                                        {item.lastMessageTime ? dayjs(item.lastMessageTime).format("hh:mm A") : ""}
                                    </span>
                                </div>
                                <div className="flex justify-between items-center gap-2">
                                    <p className={`text-sm truncate ${item.unreadCount > 0 ? 'text-gray-800 font-medium' : 'text-gray-500'}`}>
                                        {item.lastMessage || <span className="italic text-gray-400">Start a conversation</span>}
                                    </p>
                                    {item.unreadCount > 0 && (
                                        <span className="flex-shrink-0 w-5 h-5 flex items-center justify-center bg-red-500 text-white text-[10px] font-bold rounded-full shadow-sm">
                                            {item.unreadCount}
                                        </span>
                                    )}
                                    {item.unreadCount === 0 && selectedUserId === item.userId && (
                                        <span className="text-blue-500 text-xs">✓✓</span>
                                    )}
                                </div>
                            </div>
                        </div>
                    ));
                })()}
            </div>
        </div>
    );
};

export default ConversationList;
