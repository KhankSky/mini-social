import React from 'react';
import { Link } from 'react-router-dom';

const RightSidebar = () => {
  return (
    <aside>
      <div className="bg-white rounded-lg shadow-sm p-4 mb-4">
        <h4 className="font-medium">People you may know</h4>
        <ul className="mt-3 space-y-3">
          <li className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <img src="/uploads/default-avatar.png" alt="s" className="w-8 h-8 rounded-full" />
              <div className="text-sm">Suggested User</div>
            </div>
            <button className="text-sm text-blue-600">Connect</button>
          </li>
        </ul>
      </div>

      <div className="bg-white rounded-lg shadow-sm p-4">
        <h4 className="font-medium">Trending</h4>
        <ul className="mt-3 text-sm text-gray-600 space-y-2">
          <li>#webdev</li>
          <li>#react</li>
          <li>#opensource</li>
        </ul>
      </div>
    </aside>
  );
};

export default RightSidebar;
