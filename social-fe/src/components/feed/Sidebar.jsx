import React from 'react';
import { Link } from 'react-router-dom';

const Sidebar = () => {
  return (
    <aside>
      <div className="bg-white rounded-lg shadow-sm p-4 mb-4">
        <div className="flex items-center gap-3">
          <img src="/uploads/default-avatar.png" alt="you" className="w-12 h-12 rounded-full object-cover" />
          <div>
            <div className="font-medium">Your Name</div>
            <div className="text-xs text-gray-500">@username</div>
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
