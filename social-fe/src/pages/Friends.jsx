import React, { useEffect, useState } from 'react';
import { getFriends, getPendingRequests, acceptFriendRequest, rejectFriendRequest, sendFriendRequest, unfriend } from '../services/friend';
import FriendCard from '../components/friends/FriendCard';
import FriendRequestCard from '../components/friends/FriendRequestCard';
import UserSearchCard from '../components/friends/UserSearchCard';
import Sidebar from '../components/feed/Sidebar.jsx';
import httpClient from '../config/HttpClient';

const Friends = () => {
    const [activeTab, setActiveTab] = useState('friends');
    const [friends, setFriends] = useState([]);
    const [pendingRequests, setPendingRequests] = useState([]);
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(false);
    const [currentUser, setCurrentUser] = useState(null);

    useEffect(() => {
        loadCurrentUser();
    }, []);

    useEffect(() => {
        if (activeTab === 'friends') {
            loadFriends();
        } else if (activeTab === 'requests') {
            loadPendingRequests();
        } else if (activeTab === 'find') {
            loadUsers();
        }
    }, [activeTab]);

    const loadCurrentUser = async () => {
        try {
            const res = await httpClient.get('/auth/me');
            setCurrentUser(res.data);
        } catch (err) {
            console.error('Failed to load current user', err);
        }
    };

    const loadFriends = async () => {
        setLoading(true);
        try {
            const res = await getFriends();
            setFriends(res.data || []);
        } catch (err) {
            console.error('Failed to load friends', err);
            setFriends([]);
        } finally {
            setLoading(false);
        }
    };

    const loadPendingRequests = async () => {
        setLoading(true);
        try {
            const res = await getPendingRequests();
            setPendingRequests(res.data || []);
        } catch (err) {
            console.error('Failed to load pending requests', err);
            setPendingRequests([]);
        } finally {
            setLoading(false);
        }
    };

    const loadUsers = async () => {
        setLoading(true);
        try {
            const res = await httpClient.get('/users?page=1&size=20');
            const allUsers = res.data?.result || [];
            // Filter out current user
            const filteredUsers = allUsers.filter(u => u.id !== currentUser?.id);
            setUsers(filteredUsers);
        } catch (err) {
            console.error('Failed to load users', err);
            setUsers([]);
        } finally {
            setLoading(false);
        }
    };

    const handleAcceptRequest = async (requestId) => {
        try {
            await acceptFriendRequest(requestId);
            loadPendingRequests();
            // Also refresh friends list if already loaded
            if (friends.length > 0) {
                loadFriends();
            }
        } catch (err) {
            console.error('Failed to accept request', err);
            alert(err.response?.data?.message || 'Failed to accept friend request');
        }
    };

    const handleRejectRequest = async (requestId) => {
        try {
            await rejectFriendRequest(requestId);
            loadPendingRequests();
        } catch (err) {
            console.error('Failed to reject request', err);
            alert(err.response?.data?.message || 'Failed to reject friend request');
        }
    };

    const handleSendRequest = async (userId) => {
        try {
            await sendFriendRequest(userId);
        } catch (err) {
            console.error('Failed to send request', err);
            alert(err.response?.data?.message || 'Failed to send friend request');
        }
    };

    const handleUnfriend = async (friendId) => {
        try {
            await unfriend(friendId);
            loadFriends();
        } catch (err) {
            console.error('Failed to unfriend', err);
            alert(err.response?.data?.message || 'Failed to unfriend user');
        }
    };

    const tabs = [
        { id: 'friends', label: 'My Friends', icon: '👥' },
        { id: 'requests', label: 'Requests', icon: '📬', badge: pendingRequests.length },
        { id: 'find', label: 'Find Friends', icon: '🔍' },
    ];

    return (
        <div className="flex h-screen bg-gray-100">
            {/* Sidebar */}
            <div className="w-64 bg-white p-6 flex flex-col flex-shrink-0">
                <Sidebar user={currentUser} />
            </div>

            {/* Main Content */}
            <div className="flex-1 overflow-y-auto p-8">
                <div className="max-w-4xl mx-auto">
                    {/* Header */}
                    <div className="mb-8">
                        <h1 className="text-4xl font-bold bg-gradient-to-r from-purple-600 to-blue-600 bg-clip-text text-transparent mb-2">
                            Friends
                        </h1>
                        <p className="text-gray-600">Manage your connections and find new friends</p>
                    </div>

                    {/* Tabs */}
                    <div className="bg-white rounded-2xl shadow-lg p-2 mb-6">
                        <div className="flex gap-2">
                            {tabs.map((tab) => (
                                <button
                                    key={tab.id}
                                    onClick={() => setActiveTab(tab.id)}
                                    className={`flex-1 px-6 py-3 rounded-xl font-medium transition-all duration-300 relative ${activeTab === tab.id
                                        ? 'bg-gradient-to-r from-purple-500 to-blue-500 text-white shadow-md'
                                        : 'text-gray-600 hover:bg-gray-50'
                                        }`}
                                >
                                    <span className="mr-2">{tab.icon}</span>
                                    {tab.label}
                                    {tab.badge > 0 && (
                                        <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full w-6 h-6 flex items-center justify-center font-bold">
                                            {tab.badge}
                                        </span>
                                    )}
                                </button>
                            ))}
                        </div>
                    </div>

                    {/* Content */}
                    <div className="bg-white/80 backdrop-blur-sm rounded-2xl shadow-lg p-6">
                        {loading ? (
                            <div className="flex items-center justify-center py-12">
                                <div className="animate-spin rounded-full h-12 w-12 border-4 border-purple-500 border-t-transparent"></div>
                            </div>
                        ) : (
                            <>
                                {/* Friends Tab */}
                                {activeTab === 'friends' && (
                                    <div className="space-y-4">
                                        {friends.length === 0 ? (
                                            <div className="text-center py-12">
                                                <div className="text-6xl mb-4">😊</div>
                                                <h3 className="text-xl font-semibold text-gray-700 mb-2">No friends yet</h3>
                                                <p className="text-gray-500">Start by finding and adding friends!</p>
                                                <button
                                                    onClick={() => setActiveTab('find')}
                                                    className="mt-4 px-6 py-2 bg-gradient-to-r from-purple-500 to-blue-500 text-white rounded-lg hover:shadow-lg transition-all"
                                                >
                                                    Find Friends
                                                </button>
                                            </div>
                                        ) : (
                                            <>
                                                <h2 className="text-lg font-semibold text-gray-800 mb-4">
                                                    {friends.length} Friend{friends.length !== 1 ? 's' : ''}
                                                </h2>
                                                {friends.map((friend) => (
                                                    <FriendCard key={friend.id} friend={friend} onUnfriend={handleUnfriend} />
                                                ))}
                                            </>
                                        )}
                                    </div>
                                )}

                                {/* Requests Tab */}
                                {activeTab === 'requests' && (
                                    <div className="space-y-4">
                                        {pendingRequests.length === 0 ? (
                                            <div className="text-center py-12">
                                                <div className="text-6xl mb-4">📭</div>
                                                <h3 className="text-xl font-semibold text-gray-700 mb-2">No pending requests</h3>
                                                <p className="text-gray-500">You're all caught up!</p>
                                            </div>
                                        ) : (
                                            <>
                                                <h2 className="text-lg font-semibold text-gray-800 mb-4">
                                                    {pendingRequests.length} Pending Request{pendingRequests.length !== 1 ? 's' : ''}
                                                </h2>
                                                {pendingRequests.map((request) => (
                                                    <FriendRequestCard
                                                        key={request.id}
                                                        request={request}
                                                        onAccept={handleAcceptRequest}
                                                        onReject={handleRejectRequest}
                                                    />
                                                ))}
                                            </>
                                        )}
                                    </div>
                                )}

                                {/* Find Friends Tab */}
                                {activeTab === 'find' && (
                                    <div className="space-y-4">
                                        <h2 className="text-lg font-semibold text-gray-800 mb-4">Suggested People</h2>
                                        {users.length === 0 ? (
                                            <div className="text-center py-12">
                                                <div className="text-6xl mb-4">🔍</div>
                                                <h3 className="text-xl font-semibold text-gray-700 mb-2">No users found</h3>
                                                <p className="text-gray-500">Check back later for suggestions</p>
                                            </div>
                                        ) : (
                                            users.map((user) => (
                                                <UserSearchCard
                                                    key={user.id}
                                                    user={user}
                                                    onSendRequest={handleSendRequest}
                                                    requestSent={false}
                                                />
                                            ))
                                        )}
                                    </div>
                                )}
                            </>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Friends;
