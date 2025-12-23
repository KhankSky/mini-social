import React, { useState, useEffect } from 'react';
import { getAdminStats } from '../../services/admin';

const AdminDashboard = () => {
    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadStats();
    }, []);

    const loadStats = async () => {
        try {
            const data = await getAdminStats();
            setStats(data);
        } catch (error) {
            console.error('Failed to load stats', error);
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return <div className="p-8 text-center">Loading statistics...</div>;
    }

    if (!stats) {
        return <div className="p-8 text-center text-red-600">Failed to load statistics</div>;
    }

    return (
        <div className="p-6">
            <h2 className="text-2xl font-bold mb-6">Dashboard Statistics</h2>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
                <StatCard
                    title="Total Users"
                    value={stats.totalUsers}
                    icon="👥"
                    color="bg-blue-500"
                />
                <StatCard
                    title="Total Posts"
                    value={stats.totalPosts}
                    icon="📝"
                    color="bg-green-500"
                />
                <StatCard
                    title="Total Comments"
                    value={stats.totalComments}
                    icon="💬"
                    color="bg-purple-500"
                />
                <StatCard
                    title="Active Today"
                    value={stats.activeToday}
                    icon="✨"
                    color="bg-orange-500"
                />
            </div>

            <div className="bg-white rounded-lg shadow p-6">
                <h3 className="text-lg font-semibold mb-4">Recent Activity</h3>
                <div className="text-gray-600">
                    <p className="mb-2">New users this week: <span className="font-bold text-black">{stats.newUsersThisWeek}</span></p>
                    <p>Users active today: <span className="font-bold text-black">{stats.activeToday}</span></p>
                </div>
            </div>
        </div>
    );
};

const StatCard = ({ title, value, icon, color }) => (
    <div className="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow">
        <div className="flex items-center justify-between">
            <div>
                <p className="text-gray-500 text-sm mb-1">{title}</p>
                <p className="text-3xl font-bold">{value}</p>
            </div>
            <div className={`${color} text-white text-3xl rounded-full w-16 h-16 flex items-center justify-center`}>
                {icon}
            </div>
        </div>
    </div>
);

export default AdminDashboard;
