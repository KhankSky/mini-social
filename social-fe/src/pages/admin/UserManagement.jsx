import React, { useState, useEffect } from 'react';
import { getAllUsers, updateUserRole, deleteUser } from '../../services/admin';

const UserManagement = () => {
    const [users, setUsers] = useState([]);
    const [search, setSearch] = useState('');
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadUsers();
    }, [page, search]);

    const loadUsers = async () => {
        setLoading(true);
        try {
            const data = await getAllUsers(page, 20, search);
            setUsers(data.content);
            setTotalPages(data.totalPages);
        } catch (error) {
            console.error('Failed to load users', error);
        } finally {
            setLoading(false);
        }
    };

    const handleRoleChange = async (userId, newRole) => {
        if (window.confirm(`Change user role to ${newRole}?`)) {
            try {
                await updateUserRole(userId, newRole);
                loadUsers();
            } catch (error) {
                alert('Failed to update role: ' + (error.response?.data?.message || error.message));
            }
        }
    };

    const handleDelete = async (userId, username) => {
        if (window.confirm(`Are you sure you want to delete user "${username}"? This action cannot be undone.`)) {
            try {
                await deleteUser(userId);
                loadUsers();
            } catch (error) {
                alert('Failed to delete user: ' + (error.response?.data?.message || error.message));
            }
        }
    };

    const handleSearch = (e) => {
        setSearch(e.target.value);
        setPage(0);
    };

    if (loading && users.length === 0) {
        return <div className="p-8 text-center">Loading users...</div>;
    }

    return (
        <div className="p-6">
            <div className="flex justify-between items-center mb-6">
                <h2 className="text-2xl font-bold">User Management</h2>
                <input
                    type="text"
                    placeholder="Search users by name or email..."
                    className="px-4 py-2 border rounded-lg w-96 focus:ring-2 focus:ring-indigo-500 outline-none"
                    value={search}
                    onChange={handleSearch}
                />
            </div>

            <div className="bg-white rounded-lg shadow overflow-hidden">
                <table className="w-full">
                    <thead className="bg-gray-50 border-b">
                        <tr>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Avatar</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Username</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Email</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Role</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Created At</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Actions</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y">
                        {users.map((user) => (
                            <tr key={user.id} className="hover:bg-gray-50">
                                <td className="px-6 py-4">
                                    <img
                                        src={user.avatarUrl || `https://ui-avatars.com/api/?name=${user.username}`}
                                        alt={user.username}
                                        className="w-10 h-10 rounded-full"
                                    />
                                </td>
                                <td className="px-6 py-4 font-medium">{user.username}</td>
                                <td className="px-6 py-4 text-gray-600">{user.email}</td>
                                <td className="px-6 py-4">
                                    <select
                                        value={user.role}
                                        onChange={(e) => handleRoleChange(user.id, e.target.value)}
                                        className={`px-3 py-1 rounded-full text-sm font-medium ${getRoleBadgeColor(user.role)}`}
                                    >
                                        <option value="USER">User</option>
                                        <option value="MODERATOR">Moderator</option>
                                        <option value="ADMIN">Admin</option>
                                    </select>
                                </td>
                                <td className="px-6 py-4 text-gray-600 text-sm">
                                    {new Date(user.createdAt).toLocaleDateString()}
                                </td>
                                <td className="px-6 py-4">
                                    <button
                                        onClick={() => handleDelete(user.id, user.username)}
                                        className="text-red-600 hover:text-red-800 font-medium text-sm"
                                    >
                                        Delete
                                    </button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            {/* Pagination */}
            {totalPages > 1 && (
                <div className="flex justify-center gap-2 mt-6">
                    <button
                        onClick={() => setPage(p => Math.max(0, p - 1))}
                        disabled={page === 0}
                        className="px-4 py-2 bg-gray-200 rounded hover:bg-gray-300 disabled:opacity-50"
                    >
                        Previous
                    </button>
                    <span className="px-4 py-2">
                        Page {page + 1} of {totalPages}
                    </span>
                    <button
                        onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                        disabled={page >= totalPages - 1}
                        className="px-4 py-2 bg-gray-200 rounded hover:bg-gray-300 disabled:opacity-50"
                    >
                        Next
                    </button>
                </div>
            )}
        </div>
    );
};

const getRoleBadgeColor = (role) => {
    switch (role) {
        case 'ADMIN':
            return 'bg-red-100 text-red-800';
        case 'MODERATOR':
            return 'bg-blue-100 text-blue-800';
        default:
            return 'bg-gray-100 text-gray-800';
    }
};

export default UserManagement;
