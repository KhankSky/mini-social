import React, { useState, useEffect } from 'react';
import httpClient from '../../config/HttpClient';
import { useNavigate } from 'react-router-dom';

const SearchModal = ({ onClose }) => {
    const [query, setQuery] = useState('');
    const [results, setResults] = useState([]);
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        const delayDebounceFn = setTimeout(() => {
            if (query.trim()) {
                handleSearch();
            } else {
                setResults([]);
            }
        }, 500);

        return () => clearTimeout(delayDebounceFn);
    }, [query]);

    const handleSearch = async () => {
        setLoading(true);
        try {
            // Using spring-filter syntax: fullName~~'%query%' or username~~'%query%'
            const filter = `fullName~~'%${query}%' or username~~'%${query}%'`;
            const res = await httpClient.get(`/users?filter=${encodeURIComponent(filter)}&size=10`);
            setResults(res.data.result || []);
        } catch (error) {
            console.error("Search failed", error);
        } finally {
            setLoading(false);
        }
    };

    const handleUserClick = (userId) => {
        navigate(`/profile/${userId}`);
        onClose();
    };

    const handleAddFriend = async (e, userId) => {
        e.stopPropagation();
        try {
            await httpClient.post('/friends/request', { receiverId: userId });
            alert('Friend request sent!');
        } catch (error) {
            console.error(error);
            alert('Failed to send request');
        }
    };

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center p-4" onClick={onClose}>
            <div className="bg-white rounded-2xl w-full max-w-lg overflow-hidden flex flex-col max-h-[80vh]" onClick={e => e.stopPropagation()}>
                <div className="p-4 border-b">
                    <div className="relative">
                        <span className="absolute left-3 top-3 text-gray-400">🔍</span>
                        <input
                            type="text"
                            placeholder="Search users..."
                            className="w-full bg-gray-100 rounded-full py-2 pl-10 pr-4 outline-none focus:ring-2 focus:ring-black"
                            value={query}
                            onChange={(e) => setQuery(e.target.value)}
                            autoFocus
                        />
                    </div>
                </div>

                <div className="overflow-y-auto flex-1 p-2">
                    {loading && <div className="text-center p-4">Loading...</div>}
                    {!loading && results.length === 0 && query && (
                        <div className="text-center p-4 text-gray-500">No results found.</div>
                    )}

                    {results.map(user => (
                        <div
                            key={user.id}
                            onClick={() => handleUserClick(user.id)}
                            className="flex items-center gap-3 p-3 hover:bg-gray-50 rounded-lg cursor-pointer transition-colors"
                        >
                            <img
                                src={user.avatarUrl || 'https://api.dicebear.com/7.x/avataaars/svg?seed=' + user.username}
                                alt={user.username}
                                className="w-12 h-12 rounded-full object-cover"
                            />
                            <div className="flex-1">
                                <div className="font-semibold">{user.fullName || user.username}</div>
                                <div className="text-sm text-gray-500">@{user.username}</div>
                            </div>
                            {user.relationStatus === 'NONE' && (
                                <button
                                    onClick={(e) => handleAddFriend(e, user.id)}
                                    className="px-3 py-1 bg-black text-white text-sm rounded-full hover:bg-gray-800"
                                >
                                    Add
                                </button>
                            )}
                        </div>
                    ))}
                </div>

                <div className="p-3 border-t text-center">
                    <button onClick={onClose} className="text-red-500 hover:underline">Close</button>
                </div>
            </div>
        </div>
    );
};

export default SearchModal;
