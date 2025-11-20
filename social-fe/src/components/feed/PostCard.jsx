import React from 'react';
import { FiThumbsUp, FiMessageCircle, FiShare2 } from 'react-icons/fi';

const PostCard = ({ post }) => {
  return (
    <article className="bg-white rounded-lg shadow-sm p-4">
      <header className="flex items-start gap-3">
        <img src={post.avatar} alt="avatar" className="w-10 h-10 rounded-full object-cover" />
        <div>
          <div className="flex items-center gap-2">
            <h3 className="font-medium text-gray-800">{post.name}</h3>
            <span className="text-xs text-gray-500">· {post.time}</span>
          </div>
          <div className="text-sm text-gray-600">{post.position}</div>
        </div>
      </header>

      <div className="mt-3 text-gray-800 text-sm">{post.content}</div>

      {post.image && (
        <div className="mt-3 rounded overflow-hidden">
          <img src={post.image} alt="post" className="w-full object-cover" />
        </div>
      )}

      <footer className="mt-3">
        <div className="flex items-center justify-between text-gray-600 text-sm">
          <div className="flex items-center gap-6">
            <div className="flex items-center gap-2">
              <FiThumbsUp />
              <span>{post.likes}</span>
            </div>
            <div className="flex items-center gap-2">
              <FiMessageCircle />
              <span>{post.comments} comments</span>
            </div>
          </div>
          <div className="flex items-center gap-4">
            <button className="flex items-center gap-2 text-gray-600 hover:text-blue-600">
              <FiThumbsUp />
              <span className="hidden sm:inline">Like</span>
            </button>
            <button className="flex items-center gap-2 text-gray-600 hover:text-blue-600">
              <FiMessageCircle />
              <span className="hidden sm:inline">Comment</span>
            </button>
            <button className="flex items-center gap-2 text-gray-600 hover:text-blue-600">
              <FiShare2 />
              <span className="hidden sm:inline">Share</span>
            </button>
          </div>
        </div>
      </footer>
    </article>
  );
};

export default PostCard;
