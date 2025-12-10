import React from 'react';
import { Link } from 'react-router-dom';
import { API_ORIGIN } from '../../config/HttpClient';

const resolveImageUrl = (url) => {
  if (!url) return 'https://api.dicebear.com/7.x/avataaars/svg?seed=User';
  if (url.startsWith("http://") || url.startsWith("https://")) return url;
  if (url.startsWith("/")) return `${API_ORIGIN}${url}`;
  return `${API_ORIGIN}/${url}`;
};

const Sidebar = ({ user }) => {
  const avatar = user ? resolveImageUrl(user.avatar || user.avatarUrl || user.photo) : 'https://api.dicebear.com/7.x/avataaars/svg?seed=User';
  const name = user && (user.fullName || user.name || user.username) ? (user.fullName || user.name || user.username) : 'Bogdan Nikitin';
  const username = user && user.username ? `@${user.username}` : '@nikitinteam';

  return (
    <>
      <div className="flex items-center gap-4 mb-8">
        <img
          src={avatar}
          alt={name}
          className="w-16 h-16 rounded-full object-cover"
        />
        <div className="flex-1">
          <div className="font-bold">{name}</div>
          <div className="text-sm text-gray-500">{username}</div>
        </div>
      </div>

      <nav className="space-y-2 flex-1">
        <Link to="/feed" className="w-full flex items-center gap-3 px-4 py-3 bg-black text-white rounded-full">
          <div className="w-6 h-6 bg-white rounded-full flex items-center justify-center text-black text-xs">📰</div>
          <span>News Feed</span>
        </Link>
        <button className="w-full flex items-center gap-3 px-4 py-3 hover:bg-gray-100 rounded-full relative">
          <span className="text-xl">✉️</span>
          <span>Messages</span>
          <span className="ml-auto bg-black text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">6</span>
        </button>
        <button className="w-full flex items-center gap-3 px-4 py-3 hover:bg-gray-100 rounded-full">
          <span className="text-xl">🔔</span>
          <span>Notifications</span>
        </button>
        <Link to="/friends" className="w-full flex items-center gap-3 px-4 py-3 hover:bg-gray-100 rounded-full relative">
          <span className="text-xl">👥</span>
          <span>Friends</span>
          <span className="ml-auto bg-black text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">3</span>
        </Link>
        <button className="w-full flex items-center gap-3 px-4 py-3 hover:bg-gray-100 rounded-full">
          <span className="text-xl">🔍</span>
          <span>Search</span>
        </button>
        <button className="w-full flex items-center gap-3 px-4 py-3 hover:bg-gray-100 rounded-full">
          <span className="text-xl">⚙️</span>
          <span>Settings</span>
        </button>
      </nav>
    </>
  );
};

export default Sidebar; 