import React, { useEffect, useState } from 'react';
import Sidebar from '../components/feed/Sidebar.jsx';
import RightSidebar from '../components/feed/RightSidebar.jsx';
import ComposeBox from '../components/feed/ComposeBox.jsx';
import PostCard from '../components/feed/PostCard.jsx';
import httpClient from '../config/HttpClient';
import { fetchPosts } from '../services/post';

const PAGE_SIZE = 10;

const Feed = () => {
  const [user, setUser] = useState(null);
  const [posts, setPosts] = useState([]);
  const [activeTab, setActiveTab] = useState('Recents');

  useEffect(() => {
    const loadMe = async () => {
      try {
        const res = await httpClient.get('/auth/me');
        setUser(res.data);
      } catch (err) {
        console.warn('Could not load current user', err);
      }
    };
    loadMe();
  }, []);

  useEffect(() => {
    const loadPosts = async () => {
      try {
        const res = await fetchPosts(1, PAGE_SIZE);
        const { result } = res.data || {};
        setPosts(result || []);
      } catch (err) {
        console.error('Failed to load posts', err);
      }
    };

    loadPosts();
  }, []);

  const handlePostCreated = (newPost) => {
    setPosts((prev) => [newPost, ...prev]);
  };

    return (
    <div className="flex h-screen bg-gray-100">
      {/* Sidebar */}
      <div className="w-64 bg-white p-6 flex flex-col flex-shrink-0">
        <Sidebar user={user} />
      </div>

      {/* Main Content */}
      <div className="flex-1 overflow-y-auto p-8">
        {/* Thêm mx-auto để căn giữa */}
        <div className="max-w-3xl mx-auto">
          <div className="flex items-center justify-between mb-6">
            <h1 className="text-3xl font-bold">Feeds</h1>
            <div className="flex gap-6">
              <button 
                onClick={() => setActiveTab('Recents')}
                className={activeTab === 'Recents' ? 'text-black font-semibold border-b-2 border-black pb-1' : 'text-gray-400 hover:text-gray-600'}
              >
                Recents
              </button>
              <button 
                onClick={() => setActiveTab('Friends')}
                className={activeTab === 'Friends' ? 'text-black font-semibold border-b-2 border-black pb-1' : 'text-gray-400 hover:text-gray-600'}
              >
                Friends
              </button>
            </div>
          </div>

          <ComposeBox user={user} onPostCreated={handlePostCreated} />

          {/* Posts */}
          <div className="space-y-6">
            {posts.map((p) => (
              <PostCard key={p.id} post={p} />
            ))}
          </div>
        </div>
      </div>

      {/* Right Sidebar */}
      <div className="w-80 bg-white p-6 overflow-y-auto flex-shrink-0">
        <RightSidebar />
      </div>
    </div>
  );
};

export default Feed;