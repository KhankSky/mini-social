import React from 'react';
import { Link } from 'react-router-dom';

const Sidebar = ({ user }) => {
  const avatar = user && (user.avatar || user.avatarUrl || user.photo) ? (user.avatar || user.avatarUrl || user.photo) : '/default-avatar.png';
  const name = user && (user.fullName || user.name || user.username) ? (user.fullName || user.name || user.username) : 'Nguyen Van A';
  const title = user && (user.title || user.job || user.role) ? (user.title || user.job || user.role) : 'Software Engineer';
  const bio = user && (user.bio || user.description) ? (user.bio || user.description) : 'This is a short bio about the user. Add something interesting here.';

  return (
    <aside>
      <div className="bg-white rounded-lg shadow-sm p-4 mb-4">
        <div className="flex items-center gap-3">
          <img src={avatar} alt="avatar" className="w-12 h-12 rounded-full object-cover" />
          <div>
            <div className="font-medium">{name}</div>
            <div className="text-xs text-gray-500">{title}</div>
          </div>
        </div>
        <div className="mt-3 text-sm">
          <Link to="/profile" className="text-blue-600 hover:underline">View profile</Link>
        </div>
      </div>

      <div className="bg-white rounded-lg shadow-sm p-4">
        <h4 className="font-medium">My Items</h4>
        <ul className="mt-3 text-sm text-gray-600 space-y-2">
          <li><Link to="/saved" className="hover:text-blue-600">Saved</Link></li>
          <li><Link to="/groups" className="hover:text-blue-600">Groups</Link></li>
          <li><Link to="/events" className="hover:text-blue-600">Events</Link></li>
        </ul>
      </div>
    </aside>
  );
};

export default Sidebar;
