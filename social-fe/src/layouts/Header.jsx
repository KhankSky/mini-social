import React from 'react';
import { Link } from 'react-router-dom';
import { FiSearch } from 'react-icons/fi';
import { AiFillHome, AiOutlineBell } from 'react-icons/ai';
import { RiUserFollowLine, RiMessage2Line, RiGridFill } from 'react-icons/ri';
import { BsBriefcase } from 'react-icons/bs';

const Header = () => {
  return (
    <header className="bg-white border-b">
      <div className="max-w-6xl mx-auto px-4">
        <div className="flex items-center justify-between h-16">
          {/* Left: logo + search (small screens) */}
          <div className="flex items-center gap-4">
            <Link to="/" className="flex items-center gap-2">
              <div className="w-8 h-8 bg-blue-600 text-white rounded flex items-center justify-center font-bold">S</div>
              <span className="hidden md:inline font-semibold text-gray-800">MiniSocial</span>
            </Link>

            <div className="hidden sm:block">
              <div className="relative">
                <FiSearch className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-500" />
                <input
                  aria-label="Search"
                  className="pl-10 pr-4 py-2 w-72 rounded-full bg-gray-100 border border-transparent focus:border-blue-300 focus:bg-white"
                  placeholder="Search"
                />
              </div>
            </div>
          </div>

          {/* Center: nav like LinkedIn */}
          <nav className="flex-1 flex justify-center">
            <ul className="flex items-center gap-8">
              <li>
                <Link to="/" className="flex flex-col items-center text-sm text-gray-600 hover:text-blue-600">
                  <AiFillHome size={22} />
                  <span className="text-xs mt-1 hidden md:block">Home</span>
                </Link>
              </li>

              <li>
                <Link to="/network" className="flex flex-col items-center text-sm text-gray-600 hover:text-blue-600">
                  <RiUserFollowLine size={22} />
                  <span className="text-xs mt-1 hidden md:block">My Network</span>
                </Link>
              </li>

              <li>
                <Link to="/jobs" className="flex flex-col items-center text-sm text-gray-600 hover:text-blue-600">
                  <BsBriefcase size={20} />
                  <span className="text-xs mt-1 hidden md:block">Jobs</span>
                </Link>
              </li>

              <li>
                <Link to="/messaging" className="flex flex-col items-center text-sm text-gray-600 hover:text-blue-600">
                  <RiMessage2Line size={22} />
                  <span className="text-xs mt-1 hidden md:block">Messaging</span>
                </Link>
              </li>

              <li>
                <Link to="/notifications" className="flex flex-col items-center text-sm text-gray-600 hover:text-blue-600">
                  <AiOutlineBell size={22} />
                  <span className="text-xs mt-1 hidden md:block">Notifications</span>
                </Link>
              </li>

              <li>
                <Link to="/me" className="flex items-center gap-2 px-2 text-sm text-gray-700 hover:text-blue-600">
                  <img src="/uploads/default-avatar.png" alt="avatar" className="w-8 h-8 rounded-full object-cover" />
                  <span className="hidden lg:inline">You</span>
                </Link>
              </li>
            </ul>
          </nav>

          {/* Right: apps grid or small actions */}
          <div className="flex items-center gap-3">
            <button className="p-2 rounded hover:bg-gray-100" aria-label="Work">
              <RiGridFill size={18} />
            </button>
          </div>
        </div>
      </div>
    </header>
  );
};

export default Header;