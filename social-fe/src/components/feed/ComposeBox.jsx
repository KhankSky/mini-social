import React from 'react';
import { FiImage, FiFileText, FiVideo } from 'react-icons/fi';

const ComposeBox = ({ onPost }) => {
  return (
    <div className="bg-white rounded-lg shadow-sm p-4">
      <div className="flex items-start gap-3">
        <img src="/uploads/default-avatar.png" alt="you" className="w-10 h-10 rounded-full object-cover" />
        <textarea
          rows={3}
          className="flex-1 resize-none p-3 rounded-md bg-gray-50 border border-transparent focus:border-blue-300"
          placeholder="Start a post"
        />
      </div>

      <div className="mt-3 flex items-center justify-between">
        <div className="flex items-center gap-4 text-gray-600">
          <button className="flex items-center gap-2 hover:text-blue-600">
            <FiImage />
            <span className="text-sm hidden sm:inline">Photo</span>
          </button>
          <button className="flex items-center gap-2 hover:text-blue-600">
            <FiVideo />
            <span className="text-sm hidden sm:inline">Video</span>
          </button>
          <button className="flex items-center gap-2 hover:text-blue-600">
            <FiFileText />
            <span className="text-sm hidden sm:inline">Write article</span>
          </button>
        </div>

        <div>
          <button className="bg-blue-600 text-white px-4 py-1 rounded-md">Post</button>
        </div>
      </div>
    </div>
  );
};

export default ComposeBox;
