import React, { useState } from 'react';
import { Navigate } from 'react-router-dom';
import AuthService from '../../services/auth';
import AdminDashboard from './AdminDashboard';
import UserManagement from './UserManagement';
import ContentManagement from './ContentManagement';

const AdminPage = () => {
    const [activeTab, setActiveTab] = useState('dashboard');
    const user = AuthService.getCurrentUser();

    // Check if user has admin or moderator role
    if (!user || (user.role !== 'ADMIN' && user.role !== 'MODERATOR')) {
        return <Navigate to="/feed" replace />;
    }

    return (
        <div className="min-h-screen bg-gray-50">
            <div className="bg-white shadow">
                <div className="max-w-7xl mx-auto px-4">
                    <div className="flex items-center justify-between py-4">
                        <h1 className="text-2xl font-bold">Admin Panel</h1>
                        <div className="flex items-center gap-2">
                            <span className={`px-3 py-1 rounded-full text-sm font-medium ${user.role === 'ADMIN' ? 'bg-red-100 text-red-800' : 'bg-blue-100 text-blue-800'
                                }`}>
                                {user.role}
                            </span>
                            <span className="text-gray-600">{user.username}</span>
                        </div>
                    </div>
                    <div className="flex gap-6 border-t">
                        <TabButton
                            active={activeTab === 'dashboard'}
                            onClick={() => setActiveTab('dashboard')}
                        >
                            📊 Dashboard
                        </TabButton>
                        <TabButton
                            active={activeTab === 'users'}
                            onClick={() => setActiveTab('users')}
                        >
                            👥 Users
                        </TabButton>
                        <TabButton
                            active={activeTab === 'content'}
                            onClick={() => setActiveTab('content')}
                        >
                            📝 Content
                        </TabButton>
                    </div>
                </div>
            </div>

            <div className="max-w-7xl mx-auto">
                {activeTab === 'dashboard' && <AdminDashboard />}
                {activeTab === 'users' && <UserManagement />}
                {activeTab === 'content' && <ContentManagement />}
            </div>
        </div>
    );
};

const TabButton = ({ active, onClick, children }) => (
    <button
        onClick={onClick}
        className={`px-4 py-3 font-medium border-b-2 transition-colors ${active
                ? 'border-black text-black'
                : 'border-transparent text-gray-500 hover:text-gray-700'
            }`}
    >
        {children}
    </button>
);

export default AdminPage;
