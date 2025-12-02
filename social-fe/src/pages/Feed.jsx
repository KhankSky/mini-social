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
    <div className="max-w-6xl mx-auto px-4">
      <div className="grid grid-cols-12 gap-6">
        <div className="col-span-12 md:col-span-3">
          <Sidebar user={user} />
        </div>

        <div className="col-span-12 md:col-span-6">
          <div className="space-y-4">
            <ComposeBox user={user} onPostCreated={handlePostCreated} />
            {posts.map((p) => (
              <PostCard key={p.id} post={p} />
            ))}
          </div>
        </div>

        <div className="col-span-12 md:col-span-3 hidden lg:block">
          <RightSidebar />
        </div>
      </div>
    </div>
  );
};

export default Feed;