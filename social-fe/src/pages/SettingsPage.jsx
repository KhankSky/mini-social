import React, { useState, useEffect } from 'react';
import httpClient from '../config/HttpClient';
import AuthService from '../services/auth';

const SettingsPage = () => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [activeTab, setActiveTab] = useState('profile');

    // Profile Form
    const [profileForm, setProfileForm] = useState({
        username: ''
    });

    // Password Form
    const [passwordForm, setPasswordForm] = useState({
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
    });

    const [message, setMessage] = useState({ type: '', text: '' });

    useEffect(() => {
        const fetchUser = async () => {
            try {
                const currentUser = await AuthService.fetchProfile();
                setUser(currentUser);
                setProfileForm({
                    username: currentUser.username || ''
                });
            } catch (error) {
                console.error("Failed to load profile", error);
            } finally {
                setLoading(false);
            }
        };
        fetchUser();
    }, []);

    const handleProfileUpdate = async (e) => {
        e.preventDefault();
        setMessage({ type: '', text: '' });
        try {
            await httpClient.put('/users', {
                id: user.id,
                ...profileForm
            });
            setMessage({ type: 'success', text: 'Profile updated successfully!' });
        } catch (error) {
            console.error(error);
            setMessage({ type: 'error', text: 'Failed to update profile.' });
        }
    };

    const handlePasswordChange = async (e) => {
        e.preventDefault();
        setMessage({ type: '', text: '' });

        if (passwordForm.newPassword !== passwordForm.confirmPassword) {
            setMessage({ type: 'error', text: 'New passwords do not match.' });
            return;
        }

        try {
            await httpClient.patch('/users/password', passwordForm);
            setMessage({ type: 'success', text: 'Password changed successfully!' });
            setPasswordForm({ currentPassword: '', newPassword: '', confirmPassword: '' });
        } catch (error) {
            console.error(error);
            setMessage({ type: 'error', text: error.response?.data?.message || 'Failed to change password.' });
        }
    };

    if (loading) return <div className="p-8 text-center">Loading settings...</div>;

    return (
        <div className="max-w-2xl mx-auto py-8 px-4">
            <h1 className="text-3xl font-bold mb-8">Settings</h1>

            {/* Tabs */}
            <div className="flex border-b mb-8">
                <button
                    className={`px-6 py-3 font-medium ${activeTab === 'profile' ? 'border-b-2 border-black' : 'text-gray-500'}`}
                    onClick={() => setActiveTab('profile')}
                >
                    Edit Profile
                </button>
                <button
                    className={`px-6 py-3 font-medium ${activeTab === 'security' ? 'border-b-2 border-black' : 'text-gray-500'}`}
                    onClick={() => setActiveTab('security')}
                >
                    Security
                </button>
            </div>

            {message.text && (
                <div className={`p-4 mb-6 rounded-lg ${message.type === 'success' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
                    {message.text}
                </div>
            )}

            {/* Profile Tab */}
            {activeTab === 'profile' && (
                <form onSubmit={handleProfileUpdate} className="space-y-6">
                    <div>
                        <label className="block text-sm font-medium mb-1">Username</label>
                        <input
                            type="text"
                            className="w-full p-2 border rounded-lg focus:ring-2 focus:ring-black outline-none"
                            value={profileForm.username}
                            onChange={(e) => setProfileForm({ ...profileForm, username: e.target.value })}
                        />
                    </div>
                    <button type="submit" className="bg-black text-white px-6 py-2 rounded-full hover:bg-gray-800">
                        Save Changes
                    </button>
                </form>
            )}

            {/* Security Tab */}
            {activeTab === 'security' && (
                <form onSubmit={handlePasswordChange} className="space-y-6">
                    <div>
                        <label className="block text-sm font-medium mb-1">Current Password</label>
                        <input
                            type="password"
                            required
                            className="w-full p-2 border rounded-lg focus:ring-2 focus:ring-black outline-none"
                            value={passwordForm.currentPassword}
                            onChange={(e) => setPasswordForm({ ...passwordForm, currentPassword: e.target.value })}
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium mb-1">New Password</label>
                        <input
                            type="password"
                            required
                            className="w-full p-2 border rounded-lg focus:ring-2 focus:ring-black outline-none"
                            value={passwordForm.newPassword}
                            onChange={(e) => setPasswordForm({ ...passwordForm, newPassword: e.target.value })}
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium mb-1">Confirm New Password</label>
                        <input
                            type="password"
                            required
                            className="w-full p-2 border rounded-lg focus:ring-2 focus:ring-black outline-none"
                            value={passwordForm.confirmPassword}
                            onChange={(e) => setPasswordForm({ ...passwordForm, confirmPassword: e.target.value })}
                        />
                    </div>
                    <button type="submit" className="bg-red-600 text-white px-6 py-2 rounded-full hover:bg-red-700">
                        Change Password
                    </button>
                </form>
            )}
        </div>
    );
};

export default SettingsPage;
